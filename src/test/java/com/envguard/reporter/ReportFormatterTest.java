package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportFormatterTest {

    private ReportFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new ReportFormatter();
    }

    @Test
    void formatWithNoViolationsShowsSafeMessage() {
        ScanResult clean = new ScanResult("src/main/App.java", Collections.emptyList(), 12L);
        String report = formatter.format(List.of(clean));
        assertTrue(report.contains("No secrets detected"));
        assertTrue(report.contains("Files scanned : 1"));
        assertTrue(report.contains("Total violations: 0"));
    }

    @Test
    void formatWithViolationsShowsBlockedMessage() {
        ScanViolation v = new ScanViolation(
                42, "AWS_SECRET_ACCESS_KEY=.*", "AWS Secret Key",
                ScanViolation.Severity.CRITICAL, "AWS_SECRET_ACCESS_KEY=***REDACTED***");
        ScanResult result = new ScanResult(".env", List.of(v), 8L);
        String report = formatter.format(List.of(result));
        assertTrue(report.contains("Commit blocked"));
        assertTrue(report.contains("Total violations: 1"));
        assertTrue(report.contains(".env"));
        assertTrue(report.contains("Line 42"));
    }

    @Test
    void formatMultipleFilesAggregatesViolationCount() {
        ScanViolation v1 = new ScanViolation(1, ".*", "API Key",
                ScanViolation.Severity.HIGH, "API_KEY=***REDACTED***");
        ScanViolation v2 = new ScanViolation(5, ".*", "DB Password",
                ScanViolation.Severity.MEDIUM, "DB_PASS=***REDACTED***");
        ScanResult r1 = new ScanResult("config.env", List.of(v1), 5L);
        ScanResult r2 = new ScanResult("secrets.yml", List.of(v2), 3L);
        String report = formatter.format(Arrays.asList(r1, r2));
        assertTrue(report.contains("Files scanned : 2"));
        assertTrue(report.contains("Total violations: 2"));
    }

    @Test
    void scanResultHasViolationsReturnsFalseWhenEmpty() {
        ScanResult result = new ScanResult("clean.java", Collections.emptyList(), 1L);
        assertFalse(result.hasViolations());
        assertEquals(0, result.getViolationCount());
    }

    @Test
    void scanViolationToStringContainsExpectedFields() {
        ScanViolation v = new ScanViolation(10, "PATTERN", "Test Pattern",
                ScanViolation.Severity.LOW, "some=***");
        String str = v.toString();
        assertTrue(str.contains("LOW"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("Test Pattern"));
    }
}
