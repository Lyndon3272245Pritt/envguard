package com.envguard.reporter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filters a list of ScanViolations by a minimum severity threshold.
 * Useful for suppressing low-noise violations in CI environments.
 */
public class SeverityFilter {

    private final SeverityClassifier classifier;
    private final SeverityClassifier.Severity minimumSeverity;

    public SeverityFilter(SeverityClassifier classifier, SeverityClassifier.Severity minimumSeverity) {
        this.classifier = classifier;
        this.minimumSeverity = minimumSeverity;
    }

    /**
     * Filters violations, returning only those at or above the minimum severity.
     *
     * @param violations the full list of scan violations
     * @return filtered list meeting the severity threshold
     */
    public List<ScanViolation> filter(List<ScanViolation> violations) {
        if (violations == null) {
            return List.of();
        }
        return violations.stream()
                .filter(v -> meetsThreshold(
                        classifier.classify(v.getMatchedLine(), v.getPatternName())))
                .collect(Collectors.toList());
    }

    /**
     * Returns the count of violations that would be suppressed by the current filter.
     */
    public long countSuppressed(List<ScanViolation> violations) {
        if (violations == null) {
            return 0;
        }
        return violations.stream()
                .filter(v -> !meetsThreshold(
                        classifier.classify(v.getMatchedLine(), v.getPatternName())))
                .count();
    }

    private boolean meetsThreshold(SeverityClassifier.Severity severity) {
        return severity.ordinal() <= minimumSeverity.ordinal();
    }

    public SeverityClassifier.Severity getMinimumSeverity() {
        return minimumSeverity;
    }
}
