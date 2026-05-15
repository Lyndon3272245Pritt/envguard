package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SummaryReporterTest {

    private SummaryReporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new SummaryReporter();
    }

    @Test
    void summarize_nullResult_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> reporter.summarize(null));
    }

    @Test
    void summarize_noViolations_returnsCleanMessage() {
        ScanResult result = new ScanResult(3, List.of());
        String summary = reporter.summarize(result);

        assertTrue(summary.contains("Files scanned : 3"));
        assertTrue(summary.contains("Total violations: 0"));
        assertTrue(summary.contains("No secrets or environment variables detected"));
    }

    @Test
    void summarize_withViolations_containsBreakdownAndTopList() {
        ScanViolation v1 = new ScanViolation(".env", 2, "AWS_SECRET_ACCESS_KEY=abc", "AWS_SECRET", "HIGH");
        ScanViolation v2 = new ScanViolation("config.yml", 10, "DB_PASSWORD=secret", "DB_PASSWORD", "HIGH");
        ScanViolation v3 = new ScanViolation("app.properties", 5, "API_KEY=xyz", "API_KEY", "MEDIUM");

        ScanResult result = new ScanResult(5, List.of(v1, v2, v3));
        String summary = reporter.summarize(result);

        assertTrue(summary.contains("Total violations: 3"));
        assertTrue(summary.contains("Breakdown by severity:"));
        assertTrue(summary.contains("HIGH"));
        assertTrue(summary.contains("MEDIUM"));
        assertTrue(summary.contains("Top violations:"));
        assertTrue(summary.contains(".env"));
        assertTrue(summary.contains("config.yml"));
    }

    @Test
    void summarize_moreThanFiveViolations_showsEllipsis() {
        List<ScanViolation> violations = List.of(
                new ScanViolation("f1", 1, "K=v", "K", "HIGH"),
                new ScanViolation("f2", 2, "K=v", "K", "HIGH"),
                new ScanViolation("f3", 3, "K=v", "K", "MEDIUM"),
                new ScanViolation("f4", 4, "K=v", "K", "LOW"),
                new ScanViolation("f5", 5, "K=v", "K", "LOW"),
                new ScanViolation("f6", 6, "K=v", "K", "LOW")
        );

        ScanResult result = new ScanResult(10, violations);
        String summary = reporter.summarize(result);

        assertTrue(summary.contains("... and 1 more."));
    }

    @Test
    void summarize_includesFilesScannedCount() {
        ScanResult result = new ScanResult(42, List.of());
        String summary = reporter.summarize(result);
        assertTrue(summary.contains("Files scanned : 42"));
    }
}
