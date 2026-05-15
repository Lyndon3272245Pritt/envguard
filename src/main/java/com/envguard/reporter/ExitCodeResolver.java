package com.envguard.reporter;

import java.util.List;

/**
 * Resolves the appropriate process exit code based on scan results.
 * Exit code 0 = clean, 1 = violations found, 2 = internal error.
 */
public class ExitCodeResolver {

    public static final int EXIT_CLEAN = 0;
    public static final int EXIT_VIOLATIONS = 1;
    public static final int EXIT_ERROR = 2;

    private final String minimumFailSeverity;

    public ExitCodeResolver(String minimumFailSeverity) {
        if (minimumFailSeverity == null || minimumFailSeverity.isBlank()) {
            throw new IllegalArgumentException("minimumFailSeverity must not be null or blank");
        }
        this.minimumFailSeverity = minimumFailSeverity.toUpperCase();
    }

    /**
     * Resolves exit code from a ScanResult.
     * Returns EXIT_VIOLATIONS if any violation meets or exceeds the minimum severity.
     */
    public int resolve(ScanResult result) {
        if (result == null) {
            return EXIT_ERROR;
        }
        List<ScanViolation> violations = result.getViolations();
        if (violations == null || violations.isEmpty()) {
            return EXIT_CLEAN;
        }
        for (ScanViolation v : violations) {
            if (severityRank(v.getSeverity()) >= severityRank(minimumFailSeverity)) {
                return EXIT_VIOLATIONS;
            }
        }
        return EXIT_CLEAN;
    }

    private int severityRank(String severity) {
        if (severity == null) return 0;
        switch (severity.toUpperCase()) {
            case "LOW":      return 1;
            case "MEDIUM":   return 2;
            case "HIGH":     return 3;
            case "CRITICAL": return 4;
            default:         return 0;
        }
    }

    public String getMinimumFailSeverity() {
        return minimumFailSeverity;
    }
}
