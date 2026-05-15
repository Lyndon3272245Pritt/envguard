package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ViolationGrouperTest {

    private ViolationGrouper grouper;

    @BeforeEach
    void setUp() {
        grouper = new ViolationGrouper();
    }

    @Test
    void groupByFile_emptyList_returnsEmptyMap() {
        Map<String, List<ScanViolation>> result = grouper.groupByFile(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void groupByFile_nullList_returnsEmptyMap() {
        Map<String, List<ScanViolation>> result = grouper.groupByFile(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void groupByFile_multipleViolationsSameFile_groupedTogether() {
        ScanViolation v1 = new ScanViolation(".env", 1, "AWS_KEY=abc", "HIGH");
        ScanViolation v2 = new ScanViolation(".env", 3, "DB_PASS=secret", "HIGH");
        ScanViolation v3 = new ScanViolation("config.yml", 5, "api_token=xyz", "MEDIUM");

        Map<String, List<ScanViolation>> result = grouper.groupByFile(Arrays.asList(v1, v2, v3));

        assertEquals(2, result.size());
        assertEquals(2, result.get(".env").size());
        assertEquals(1, result.get("config.yml").size());
    }

    @Test
    void groupBySeverity_groupsCorrectly() {
        ScanViolation v1 = new ScanViolation("a.env", 1, "AWS_SECRET=x", "HIGH");
        ScanViolation v2 = new ScanViolation("b.env", 2, "DB_URL=y", "MEDIUM");
        ScanViolation v3 = new ScanViolation("c.env", 3, "LOG_LEVEL=z", "LOW");
        ScanViolation v4 = new ScanViolation("d.env", 4, "TOKEN=t", "HIGH");

        Map<String, List<ScanViolation>> result = grouper.groupBySeverity(Arrays.asList(v1, v2, v3, v4));

        assertEquals(3, result.size());
        assertEquals(2, result.get("HIGH").size());
        assertEquals(1, result.get("MEDIUM").size());
        assertEquals(1, result.get("LOW").size());
    }

    @Test
    void groupBySeverity_nullSeverity_groupedUnderUnknown() {
        ScanViolation v = new ScanViolation("file.env", 1, "SECRET=abc", null);
        Map<String, List<ScanViolation>> result = grouper.groupBySeverity(Collections.singletonList(v));
        assertTrue(result.containsKey("UNKNOWN"));
    }

    @Test
    void countAffectedFiles_returnsDistinctFileCount() {
        ScanViolation v1 = new ScanViolation(".env", 1, "A=1", "HIGH");
        ScanViolation v2 = new ScanViolation(".env", 2, "B=2", "HIGH");
        ScanViolation v3 = new ScanViolation("secrets.yml", 1, "C=3", "LOW");

        int count = grouper.countAffectedFiles(Arrays.asList(v1, v2, v3));
        assertEquals(2, count);
    }

    @Test
    void countAffectedFiles_emptyList_returnsZero() {
        assertEquals(0, grouper.countAffectedFiles(Collections.emptyList()));
    }
}
