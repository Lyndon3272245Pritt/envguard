package com.envguard.reporter;

import java.util.List;
import java.util.Map;

/**
 * Holds the result of a deduplication pass, including the cleaned
 * violation list and metadata about how many duplicates were removed.
 */
public class DeduplicationReport {

    private final List<ScanViolation> uniqueViolations;
    private final int originalCount;
    private final int duplicatesRemoved;
    private final Map<String, List<ScanViolation>> groupedByPattern;

    public DeduplicationReport(
            List<ScanViolation> uniqueViolations,
            int originalCount,
            Map<String, List<ScanViolation>> groupedByPattern) {
        this.uniqueViolations = uniqueViolations;
        this.originalCount = originalCount;
        this.duplicatesRemoved = originalCount - uniqueViolations.size();
        this.groupedByPattern = groupedByPattern;
    }

    public List<ScanViolation> getUniqueViolations() {
        return uniqueViolations;
    }

    public int getOriginalCount() {
        return originalCount;
    }

    public int getDuplicatesRemoved() {
        return duplicatesRemoved;
    }

    public Map<String, List<ScanViolation>> getGroupedByPattern() {
        return groupedByPattern;
    }

    public boolean hasDuplicates() {
        return duplicatesRemoved > 0;
    }

    /**
     * Builds a DeduplicationReport from a raw violation list using the
     * provided deduplicator.
     *
     * @param raw          the original unfiltered violations
     * @param deduplicator the deduplicator instance to use
     * @return a fully populated DeduplicationReport
     */
    public static DeduplicationReport from(
            List<ScanViolation> raw, ViolationDeduplicator deduplicator) {
        List<ScanViolation> unique = deduplicator.deduplicate(raw);
        Map<String, List<ScanViolation>> grouped = deduplicator.groupByPattern(unique);
        return new DeduplicationReport(unique, raw == null ? 0 : raw.size(), grouped);
    }

    @Override
    public String toString() {
        return String.format(
                "DeduplicationReport{original=%d, unique=%d, duplicatesRemoved=%d, patterns=%d}",
                originalCount,
                uniqueViolations.size(),
                duplicatesRemoved,
                groupedByPattern.size());
    }
}
