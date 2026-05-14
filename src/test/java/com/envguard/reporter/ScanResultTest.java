package com.envguard.reporter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScanResultTest {

    @Test
    void violationsListIsImmutable() {
        ScanViolation v = new ScanViolation(1, ".*", "Token",
                ScanViolation.Severity.HIGH, "TOKEN=***");
        ScanResult result = new ScanResult("file.env", List.of(v), 10L);
        assertThrows(UnsupportedOperationException.class,
                () -> result.getViolations().add(v));
    }

    @Test
    void getFilePathReturnsCorrectPath() {
        ScanResult result = new ScanResult("src/.env", List.of(), 5L);
        assertEquals("src/.env", result.getFilePath());
    }

    @Test
    void getScanDurationMsReturnsCorrectValue() {
        ScanResult result = new ScanResult("file", List.of(), 42L);
        assertEquals(42L, result.getScanDurationMs());
    }

    @Test
    void toStringContainsFilePathAndViolationCount() {
        ScanViolation v = new ScanViolation(3, ".*", "Secret",
                ScanViolation.Severity.CRITICAL, "SECRET=***");
        ScanResult result = new ScanResult("config.yml", List.of(v), 7L);
        String str = result.toString();
        assertTrue(str.contains("config.yml"));
        assertTrue(str.contains("violations=1"));
    }
}
