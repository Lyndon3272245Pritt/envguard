package com.envguard.reporter;

import java.util.List;
import java.util.Map;

/**
 * Formats a scan report grouped by file, providing a structured human-readable output.
 */
public class GroupedReportFormatter {

    private static final String SEPARATOR = "─".repeat(60);
    private final ViolationGrouper grouper;

    public GroupedReportFormatter() {
        this.grouper = new ViolationGrouper();
    }

    public GroupedReportFormatter(ViolationGrouper grouper) {
        this.grouper = grouper;
    }

    /**
     * Formats all violations grouped by file into a multi-line report string.
     *
     * @param violations the list of violations to format
     * @return formatted report as a string
     */
    public String format(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return "[EnvGuard] No violations found.\n";
        }

        Map<String, List<ScanViolation>> grouped = grouper.groupByFile(violations);
        StringBuilder sb = new StringBuilder();
        sb.append("[EnvGuard] Scan Report\n");
        sb.append(SEPARATOR).append("\n");

        for (Map.Entry<String, List<ScanViolation>> entry : grouped.entrySet()) {
            sb.append("File: ").append(entry.getKey()).append("\n");
            for (ScanViolation v : entry.getValue()) {
                sb.append(String.format("  Line %-4d [%-6s] %s%n",
                        v.getLineNumber(),
                        v.getSeverity() != null ? v.getSeverity() : "N/A",
                        v.getMatchedContent()));
            }
            sb.append("\n");
        }

        sb.append(SEPARATOR).append("\n");
        sb.append(String.format("Total violations: %d across %d file(s)%n",
                violations.size(), grouper.countAffectedFiles(violations)));
        return sb.toString();
    }
}
