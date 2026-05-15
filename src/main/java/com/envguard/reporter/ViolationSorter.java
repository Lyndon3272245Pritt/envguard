package com.envguard.reporter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sorts scan violations by configurable criteria such as severity, file path, or line number.
 */
public class ViolationSorter {

    public enum SortOrder {
        SEVERITY_DESC,
        SEVERITY_ASC,
        FILE_PATH,
        LINE_NUMBER
    }

    private final SortOrder primaryOrder;

    public ViolationSorter(SortOrder primaryOrder) {
        if (primaryOrder == null) {
            throw new IllegalArgumentException("SortOrder must not be null");
        }
        this.primaryOrder = primaryOrder;
    }

    public List<ScanViolation> sort(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }
        return violations.stream()
                .sorted(buildComparator())
                .collect(Collectors.toList());
    }

    private Comparator<ScanViolation> buildComparator() {
        switch (primaryOrder) {
            case SEVERITY_DESC:
                return Comparator
                        .comparingInt((ScanViolation v) -> severityRank(v.getSeverity()))
                        .reversed()
                        .thenComparing(ScanViolation::getFilePath)
                        .thenComparingInt(ScanViolation::getLineNumber);
            case SEVERITY_ASC:
                return Comparator
                        .comparingInt((ScanViolation v) -> severityRank(v.getSeverity()))
                        .thenComparing(ScanViolation::getFilePath)
                        .thenComparingInt(ScanViolation::getLineNumber);
            case FILE_PATH:
                return Comparator
                        .comparing(ScanViolation::getFilePath)
                        .thenComparingInt(ScanViolation::getLineNumber);
            case LINE_NUMBER:
                return Comparator
                        .comparingInt(ScanViolation::getLineNumber)
                        .thenComparing(ScanViolation::getFilePath);
            default:
                return Comparator.comparing(ScanViolation::getFilePath);
        }
    }

    private int severityRank(String severity) {
        if (severity == null) return 0;
        switch (severity.toUpperCase()) {
            case "CRITICAL": return 4;
            case "HIGH":     return 3;
            case "MEDIUM":   return 2;
            case "LOW":      return 1;
            default:         return 0;
        }
    }
}
