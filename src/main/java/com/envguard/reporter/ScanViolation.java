package com.envguard.reporter;

/**
 * Represents a single detected secret or environment variable violation.
 */
public class ScanViolation {

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private final int lineNumber;
    private final String matchedPattern;
    private final String patternName;
    private final Severity severity;
    private final String redactedLine;

    public ScanViolation(int lineNumber, String matchedPattern, String patternName,
                         Severity severity, String redactedLine) {
        this.lineNumber = lineNumber;
        this.matchedPattern = matchedPattern;
        this.patternName = patternName;
        this.severity = severity;
        this.redactedLine = redactedLine;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMatchedPattern() {
        return matchedPattern;
    }

    public String getPatternName() {
        return patternName;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getRedactedLine() {
        return redactedLine;
    }

    @Override
    public String toString() {
        return String.format("[%s] Line %d — Pattern: %s | %s",
                severity, lineNumber, patternName, redactedLine);
    }
}
