package com.envguard.reporter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViolationSorterTest {

    private ScanViolation violation(String file, int line, String severity) {
        return new ScanViolation(file, line, "matched-content", "PATTERN", severity);
    }

    @Test
    void sortBySeverityDesc_ordersHighestFirst() {
        List<ScanViolation> input = Arrays.asList(
                violation("a.env", 1, "LOW"),
                violation("b.env", 2, "CRITICAL"),
                violation("c.env", 3, "MEDIUM")
        );
        ViolationSorter sorter = new ViolationSorter(ViolationSorter.SortOrder.SEVERITY_DESC);
        List<ScanViolation> result = sorter.sort(input);

        assertEquals("CRITICAL", result.get(0).getSeverity());
        assertEquals("MEDIUM",   result.get(1).getSeverity());
        assertEquals("LOW",      result.get(2).getSeverity());
    }

    @Test
    void sortBySeverityAsc_ordersLowestFirst() {
        List<ScanViolation> input = Arrays.asList(
                violation("a.env", 1, "HIGH"),
                violation("b.env", 2, "LOW"),
                violation("c.env", 3, "CRITICAL")
        );
        ViolationSorter sorter = new ViolationSorter(ViolationSorter.SortOrder.SEVERITY_ASC);
        List<ScanViolation> result = sorter.sort(input);

        assertEquals("LOW",      result.get(0).getSeverity());
        assertEquals("HIGH",     result.get(1).getSeverity());
        assertEquals("CRITICAL", result.get(2).getSeverity());
    }

    @Test
    void sortByFilePath_ordersAlphabetically() {
        List<ScanViolation> input = Arrays.asList(
                violation("z.env", 1, "HIGH"),
                violation("a.env", 2, "LOW"),
                violation("m.env", 3, "MEDIUM")
        );
        ViolationSorter sorter = new ViolationSorter(ViolationSorter.SortOrder.FILE_PATH);
        List<ScanViolation> result = sorter.sort(input);

        assertEquals("a.env", result.get(0).getFilePath());
        assertEquals("m.env", result.get(1).getFilePath());
        assertEquals("z.env", result.get(2).getFilePath());
    }

    @Test
    void sortByLineNumber_ordersAscending() {
        List<ScanViolation> input = Arrays.asList(
                violation("a.env", 30, "HIGH"),
                violation("a.env",  5, "LOW"),
                violation("a.env", 12, "MEDIUM")
        );
        ViolationSorter sorter = new ViolationSorter(ViolationSorter.SortOrder.LINE_NUMBER);
        List<ScanViolation> result = sorter.sort(input);

        assertEquals(5,  result.get(0).getLineNumber());
        assertEquals(12, result.get(1).getLineNumber());
        assertEquals(30, result.get(2).getLineNumber());
    }

    @Test
    void sort_emptyList_returnsEmpty() {
        ViolationSorter sorter = new ViolationSorter(ViolationSorter.SortOrder.SEVERITY_DESC);
        assertTrue(sorter.sort(List.of()).isEmpty());
    }

    @Test
    void constructor_nullSortOrder_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new ViolationSorter(null));
    }
}
