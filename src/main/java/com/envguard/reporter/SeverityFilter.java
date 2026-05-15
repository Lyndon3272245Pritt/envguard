package com.envguard.reporter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filters scan violations based on minimum severity level.
 */
public class SeverityFilter {

    private final ScanViolation.Severity minimumSeverity;

    public SeverityFilter(ScanViolation.Severity minimumSeverity) {
        if (minimumSeverity == null) {
            throw new IllegalArgumentException("Minimum severity must not be null");
        }
        this.minimumSeverity = minimumSeverity;
    }

    /**
     * Filters a list of violations, returning only those at or above the minimum severity.
     *
     * @param violations the list of violations to filter
     * @return filtered list of violations meeting the severity threshold
     */
    public List<ScanViolation> filter(List<ScanViolation> violations) {
        if (violations == null) {
            return List.of();
        }
        return violations.stream()
                .filter(v -> v.getSeverity().ordinal() >= minimumSeverity.ordinal())
                .collect(Collectors.toList());
    }

    /**
     * Filters violations from a ScanResult, returning a new ScanResult with only matching violations.
     *
     * @param result the original scan result
     * @return a new ScanResult containing only violations at or above minimum severity
     */
    public ScanResult filterResult(ScanResult result) {
        if (result == null) {
            throw new IllegalArgumentException("ScanResult must not be null");
        }
        List<ScanViolation> filtered = filter(result.getViolations());
        ScanResult filteredResult = new ScanResult();
        filtered.forEach(filteredResult::addViolation);
        return filteredResult;
    }

    public ScanViolation.Severity getMinimumSeverity() {
        return minimumSeverity;
    }
}
