package com.envguard.reporter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Deduplicates scan violations by grouping identical findings
 * across files, keeping only unique violations based on pattern
 * and matched value fingerprint.
 */
public class ViolationDeduplicator {

    /**
     * Removes duplicate violations from the list.
     * Two violations are considered duplicates if they share the same
     * pattern name and matched value (case-sensitive).
     *
     * @param violations the raw list of violations
     * @return a deduplicated list preserving first-occurrence order
     */
    public List<ScanViolation> deduplicate(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, ScanViolation> seen = new LinkedHashMap<>();
        for (ScanViolation v : violations) {
            String key = buildKey(v);
            seen.putIfAbsent(key, v);
        }
        return new ArrayList<>(seen.values());
    }

    /**
     * Groups violations by their pattern name.
     *
     * @param violations the list of violations to group
     * @return a map from pattern name to list of violations
     */
    public Map<String, List<ScanViolation>> groupByPattern(List<ScanViolation> violations) {
        Map<String, List<ScanViolation>> grouped = new LinkedHashMap<>();
        if (violations == null) {
            return grouped;
        }
        for (ScanViolation v : violations) {
            String pattern = v.getPatternName() != null ? v.getPatternName() : "UNKNOWN";
            grouped.computeIfAbsent(pattern, k -> new ArrayList<>()).add(v);
        }
        return grouped;
    }

    private String buildKey(ScanViolation v) {
        return Objects.toString(v.getPatternName(), "") + "::"
                + Objects.toString(v.getMatchedValue(), "");
    }
}
