package com.envguard.reporter;

import java.util.List;

/**
 * Formats scan results into human-readable console output.
 */
public class ReportFormatter {

    private static final String RESET  = "\033[0m";
    private static final String RED    = "\033[0;31m";
    private static final String YELLOW = "\033[0;33m";
    private static final String GREEN  = "\033[0;32m";
    private static final String BOLD   = "\033[1m";

    public String format(List<ScanResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append(BOLD).append("\n=== EnvGuard Scan Report ===").append(RESET).append("\n");

        int totalViolations = 0;
        for (ScanResult result : results) {
            totalViolations += result.getViolationCount();
            sb.append(formatFileResult(result));
        }

        sb.append("\n").append(BOLD).append("--- Summary ---").append(RESET).append("\n");
        sb.append(String.format("Files scanned : %d%n", results.size()));
        sb.append(String.format("Total violations: %d%n", totalViolations));

        if (totalViolations > 0) {
            sb.append(RED).append(BOLD)
              .append("\u274C Commit blocked. Secrets detected.")
              .append(RESET).append("\n");
        } else {
            sb.append(GREEN).append(BOLD)
              .append("\u2705 No secrets detected. Safe to commit.")
              .append(RESET).append("\n");
        }
        return sb.toString();
    }

    private String formatFileResult(ScanResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.hasViolations()) {
            sb.append(RED).append("\nFILE: ").append(result.getFilePath()).append(RESET).append("\n");
            for (ScanViolation v : result.getViolations()) {
                String color = v.getSeverity() == ScanViolation.Severity.CRITICAL ? RED : YELLOW;
                sb.append(color)
                  .append(String.format("  Line %-4d [%-8s] %s: %s%n",
                          v.getLineNumber(), v.getSeverity(),
                          v.getPatternName(), v.getRedactedLine()))
                  .append(RESET);
            }
        }
        return sb.toString();
    }
}
