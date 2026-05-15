package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ViolationDeduplicatorTest {

    private ViolationDeduplicator deduplicator;

    @BeforeEach
    void setUp() {
        deduplicator = new ViolationDeduplicator();
    }

    @Test
    void deduplicate_nullInput_returnsEmptyList() {
        List<ScanViolation> result = deduplicator.deduplicate(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deduplicate_emptyInput_returnsEmptyList() {
        List<ScanViolation> result = deduplicator.deduplicate(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void deduplicate_noDuplicates_returnsAllViolations() {
        ScanViolation v1 = new ScanViolation("file1.env", 1, "AWS_KEY", "AKIA1234");
        ScanViolation v2 = new ScanViolation("file2.env", 2, "DB_PASS", "secret99");
        List<ScanViolation> result = deduplicator.deduplicate(Arrays.asList(v1, v2));
        assertEquals(2, result.size());
    }

    @Test
    void deduplicate_withDuplicates_removesSecondOccurrence() {
        ScanViolation v1 = new ScanViolation("file1.env", 1, "AWS_KEY", "AKIA1234");
        ScanViolation v2 = new ScanViolation("file2.env", 5, "AWS_KEY", "AKIA1234");
        ScanViolation v3 = new ScanViolation("file3.env", 3, "DB_PASS", "secret99");
        List<ScanViolation> result = deduplicator.deduplicate(Arrays.asList(v1, v2, v3));
        assertEquals(2, result.size());
        assertEquals("file1.env", result.get(0).getFilePath());
    }

    @Test
    void deduplicate_samePatternDifferentValues_keepsAll() {
        ScanViolation v1 = new ScanViolation("file1.env", 1, "AWS_KEY", "AKIA1111");
        ScanViolation v2 = new ScanViolation("file1.env", 2, "AWS_KEY", "AKIA2222");
        List<ScanViolation> result = deduplicator.deduplicate(Arrays.asList(v1, v2));
        assertEquals(2, result.size());
    }

    @Test
    void groupByPattern_groupsCorrectly() {
        ScanViolation v1 = new ScanViolation("a.env", 1, "AWS_KEY", "val1");
        ScanViolation v2 = new ScanViolation("b.env", 2, "AWS_KEY", "val2");
        ScanViolation v3 = new ScanViolation("c.env", 3, "DB_PASS", "val3");
        Map<String, List<ScanViolation>> grouped =
                deduplicator.groupByPattern(Arrays.asList(v1, v2, v3));
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get("AWS_KEY").size());
        assertEquals(1, grouped.get("DB_PASS").size());
    }

    @Test
    void groupByPattern_nullPatternName_groupedUnderUnknown() {
        ScanViolation v = new ScanViolation("x.env", 1, null, "somevalue");
        Map<String, List<ScanViolation>> grouped =
                deduplicator.groupByPattern(Collections.singletonList(v));
        assertTrue(grouped.containsKey("UNKNOWN"));
    }
}
