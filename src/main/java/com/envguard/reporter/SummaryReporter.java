package com.envguard.reporter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a human-readable summary of a scan result,
 * grouping violations by severity and providing counts.
 */
public class SummaryReporter {

    /**
     * Produces a multi-line summary string from the given ScanResult.
     *
     * @param result the completed scan result
     * @return formatted summary text
     */
    public String summarize(ScanResult result) {
        if (result == null) {
            throw new IllegalArgumentException("ScanResult must not be null");
        }

        List<ScanViolation> violations = result.getViolations();
        StringBuilder sb = new StringBuilder();

        sb.append("=== EnvGuard Scan Summary ===").append(System.lineSeparator());
        sb.append(String.format("Files scanned : %d%n", result.getFilesScanned()));
        sb.append(String.format("Total violations: %d%n", violations.size()));

        if (violations.isEmpty()) {
            sb.append("No secrets or environment variables detected. ✓").append(System.lineSeparator());
            return sb.toString();
        }

        // Group by severity
        Map<String, List<ScanViolation>> bySeverity = violations.stream()
                .collect(Collectors.groupingBy(ScanViolation::getSeverity));

        sb.append(System.lineSeparator()).append("Breakdown by severity:").append(System.lineSeparator());
        for (Map.Entry<String, List<ScanViolation>> entry : bySeverity.entrySet()) {
            sb.append(String.format("  %-10s : %d%n", entry.getKey(), entry.getValue().size()));
        }

        sb.append(System.lineSeparator()).append("Top violations:").append(System.lineSeparator());
        violations.stream()
                .limit(5)
                .forEach(v -> sb.append(String.format(
                        "  [%s] %s (line %d) — pattern: %s%n",
                        v.getSeverity(), v.getFilePath(), v.getLineNumber(), v.getMatchedPattern()
                )));

        if (violations.size() > 5) {
            sb.append(String.format("  ... and %d more.%n", violations.size() - 5));
        }

        return sb.toString();
    }
}
