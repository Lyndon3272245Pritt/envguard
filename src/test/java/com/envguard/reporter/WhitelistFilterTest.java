package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhitelistFilterTest {

    private WhitelistManager whitelistManager;
    private WhitelistFilter whitelistFilter;

    @BeforeEach
    void setUp() {
        whitelistManager = new WhitelistManager();
        whitelistFilter = new WhitelistFilter(whitelistManager);
    }

    private ScanViolation makeViolation(String filePath, int line, String pattern) {
        return new ScanViolation(filePath, line, "matched-text", pattern, "HIGH");
    }

    @Test
    void filterReturnsAllViolationsWhenWhitelistIsEmpty() {
        List<ScanViolation> violations = List.of(
                makeViolation("src/main/resources/app.properties", 5, "API_KEY"),
                makeViolation(".env", 3, "SECRET")
        );
        List<ScanViolation> result = whitelistFilter.filter(violations);
        assertEquals(2, result.size());
    }

    @Test
    void filterRemovesViolationMatchingWhitelistedFile() {
        whitelistManager.addEntry(".env");
        List<ScanViolation> violations = List.of(
                makeViolation("src/main/resources/app.properties", 5, "API_KEY"),
                makeViolation(".env", 3, "SECRET")
        );
        List<ScanViolation> result = whitelistFilter.filter(violations);
        assertEquals(1, result.size());
        assertEquals("src/main/resources/app.properties", result.get(0).getFilePath());
    }

    @Test
    void filterRemovesViolationMatchingWhitelistedLineEntry() {
        whitelistManager.addEntry("config/settings.yml:12");
        List<ScanViolation> violations = List.of(
                makeViolation("config/settings.yml", 12, "DB_PASSWORD"),
                makeViolation("config/settings.yml", 20, "DB_PASSWORD")
        );
        List<ScanViolation> result = whitelistFilter.filter(violations);
        assertEquals(1, result.size());
        assertEquals(20, result.get(0).getLineNumber());
    }

    @Test
    void filterRemovesViolationMatchingWhitelistedPattern() {
        whitelistManager.addEntry("pattern:EXAMPLE_PATTERN");
        List<ScanViolation> violations = List.of(
                makeViolation("src/test/resources/test.env", 1, "EXAMPLE_PATTERN"),
                makeViolation("src/main/resources/real.env", 2, "REAL_SECRET")
        );
        List<ScanViolation> result = whitelistFilter.filter(violations);
        assertEquals(1, result.size());
        assertEquals("REAL_SECRET", result.get(0).getMatchedPattern());
    }

    @Test
    void countSuppressedReturnsCorrectCount() {
        whitelistManager.addEntry(".env");
        whitelistManager.addEntry("pattern:EXAMPLE_PATTERN");
        List<ScanViolation> violations = List.of(
                makeViolation(".env", 1, "SECRET"),
                makeViolation("src/test/resources/test.env", 1, "EXAMPLE_PATTERN"),
                makeViolation("src/main/resources/real.env", 2, "REAL_SECRET")
        );
        assertEquals(2L, whitelistFilter.countSuppressed(violations));
    }

    @Test
    void filterReturnsEmptyListForNullInput() {
        List<ScanViolation> result = whitelistFilter.filter(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void constructorThrowsOnNullWhitelistManager() {
        assertThrows(NullPointerException.class, () -> new WhitelistFilter(null));
    }
}
