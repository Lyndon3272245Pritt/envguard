package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeverityFilterTest {

    private SeverityFilter filterHigh;
    private SeverityFilter filterCritical;

    private ScanViolation makeViolation(ScanViolation.Severity severity) {
        ScanViolation v = new ScanViolation("file.env", 1, "MATCH", "pattern");
        v.setSeverity(severity);
        return v;
    }

    @BeforeEach
    void setUp() {
        filterHigh = new SeverityFilter(ScanViolation.Severity.HIGH);
        filterCritical = new SeverityFilter(ScanViolation.Severity.CRITICAL);
    }

    @Test
    void shouldThrowOnNullSeverity() {
        assertThrows(IllegalArgumentException.class, () -> new SeverityFilter(null));
    }

    @Test
    void shouldReturnEmptyListForNullInput() {
        List<ScanViolation> result = filterHigh.filter(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterOutBelowMinimumSeverity() {
        List<ScanViolation> violations = List.of(
                makeViolation(ScanViolation.Severity.LOW),
                makeViolation(ScanViolation.Severity.MEDIUM),
                makeViolation(ScanViolation.Severity.HIGH),
                makeViolation(ScanViolation.Severity.CRITICAL)
        );
        List<ScanViolation> result = filterHigh.filter(violations);
        assertEquals(2, result.size());
        result.forEach(v -> assertTrue(v.getSeverity().ordinal() >= ScanViolation.Severity.HIGH.ordinal()));
    }

    @Test
    void shouldReturnOnlyCriticalWhenFilteredByCritical() {
        List<ScanViolation> violations = List.of(
                makeViolation(ScanViolation.Severity.LOW),
                makeViolation(ScanViolation.Severity.HIGH),
                makeViolation(ScanViolation.Severity.CRITICAL)
        );
        List<ScanViolation> result = filterCritical.filter(violations);
        assertEquals(1, result.size());
        assertEquals(ScanViolation.Severity.CRITICAL, result.get(0).getSeverity());
    }

    @Test
    void shouldFilterScanResultCorrectly() {
        ScanResult original = new ScanResult();
        original.addViolation(makeViolation(ScanViolation.Severity.LOW));
        original.addViolation(makeViolation(ScanViolation.Severity.HIGH));
        original.addViolation(makeViolation(ScanViolation.Severity.CRITICAL));

        ScanResult filtered = filterHigh.filterResult(original);
        assertEquals(2, filtered.getViolations().size());
    }

    @Test
    void shouldThrowOnNullScanResult() {
        assertThrows(IllegalArgumentException.class, () -> filterHigh.filterResult(null));
    }

    @Test
    void shouldExposeMinimumSeverity() {
        assertEquals(ScanViolation.Severity.HIGH, filterHigh.getMinimumSeverity());
    }
}
