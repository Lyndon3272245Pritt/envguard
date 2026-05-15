package com.envguard.reporter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ExitCodeResolverTest {

    @Test
    void returnsCleanWhenNoViolations() {
        ExitCodeResolver resolver = new ExitCodeResolver("LOW");
        ScanResult result = new ScanResult(Collections.emptyList());
        assertEquals(ExitCodeResolver.EXIT_CLEAN, resolver.resolve(result));
    }

    @Test
    void returnsViolationsWhenHighViolationAndThresholdLow() {
        ExitCodeResolver resolver = new ExitCodeResolver("LOW");
        ScanViolation v = new ScanViolation("file.env", 3, "SECRET=abc", "HIGH");
        ScanResult result = new ScanResult(Collections.singletonList(v));
        assertEquals(ExitCodeResolver.EXIT_VIOLATIONS, resolver.resolve(result));
    }

    @Test
    void returnsCleanWhenLowViolationAndThresholdHigh() {
        ExitCodeResolver resolver = new ExitCodeResolver("HIGH");
        ScanViolation v = new ScanViolation("file.env", 1, "DEBUG=true", "LOW");
        ScanResult result = new ScanResult(Collections.singletonList(v));
        assertEquals(ExitCodeResolver.EXIT_CLEAN, resolver.resolve(result));
    }

    @Test
    void returnsCriticalViolationAboveHighThreshold() {
        ExitCodeResolver resolver = new ExitCodeResolver("HIGH");
        ScanViolation v = new ScanViolation("secrets.txt", 5, "API_KEY=xyz", "CRITICAL");
        ScanResult result = new ScanResult(Collections.singletonList(v));
        assertEquals(ExitCodeResolver.EXIT_VIOLATIONS, resolver.resolve(result));
    }

    @Test
    void returnsViolationsWhenExactSeverityMatch() {
        ExitCodeResolver resolver = new ExitCodeResolver("MEDIUM");
        ScanViolation v = new ScanViolation(".env", 2, "DB_PASS=secret", "MEDIUM");
        ScanResult result = new ScanResult(Collections.singletonList(v));
        assertEquals(ExitCodeResolver.EXIT_VIOLATIONS, resolver.resolve(result));
    }

    @Test
    void returnsErrorWhenResultIsNull() {
        ExitCodeResolver resolver = new ExitCodeResolver("LOW");
        assertEquals(ExitCodeResolver.EXIT_ERROR, resolver.resolve(null));
    }

    @Test
    void throwsOnNullMinimumSeverity() {
        assertThrows(IllegalArgumentException.class, () -> new ExitCodeResolver(null));
    }

    @Test
    void throwsOnBlankMinimumSeverity() {
        assertThrows(IllegalArgumentException.class, () -> new ExitCodeResolver("  "));
    }

    @Test
    void mixedSeveritiesOnlyTriggersIfAnyMeetsThreshold() {
        ExitCodeResolver resolver = new ExitCodeResolver("HIGH");
        ScanViolation low = new ScanViolation("a.txt", 1, "X=1", "LOW");
        ScanViolation high = new ScanViolation("b.txt", 2, "TOKEN=abc", "HIGH");
        ScanResult result = new ScanResult(Arrays.asList(low, high));
        assertEquals(ExitCodeResolver.EXIT_VIOLATIONS, resolver.resolve(result));
    }

    @Test
    void getMinimumFailSeverityReturnsUpperCase() {
        ExitCodeResolver resolver = new ExitCodeResolver("medium");
        assertEquals("MEDIUM", resolver.getMinimumFailSeverity());
    }
}
