package com.envguard.reporter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classifies scan violations by severity level based on the matched pattern.
 */
public class SeverityClassifier {

    public enum Severity {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    private static final Map<Pattern, Severity> SEVERITY_RULES = new HashMap<>();

    static {
        // Critical: private keys, tokens with high entropy patterns
        SEVERITY_RULES.put(Pattern.compile("(?i)(private[_-]?key|rsa[_-]?private|pem)"), Severity.CRITICAL);
        SEVERITY_RULES.put(Pattern.compile("(?i)(aws[_-]?secret|aws[_-]?access[_-]?key)"), Severity.CRITICAL);
        SEVERITY_RULES.put(Pattern.compile("(?i)(github[_-]?token|gh[_-]?token)"), Severity.CRITICAL);

        // High: passwords, API keys
        SEVERITY_RULES.put(Pattern.compile("(?i)(password|passwd|api[_-]?key|api[_-]?secret)"), Severity.HIGH);
        SEVERITY_RULES.put(Pattern.compile("(?i)(secret[_-]?key|auth[_-]?token|bearer)"), Severity.HIGH);

        // Medium: credentials, connection strings
        SEVERITY_RULES.put(Pattern.compile("(?i)(database[_-]?url|db[_-]?password|jdbc:)"), Severity.MEDIUM);
        SEVERITY_RULES.put(Pattern.compile("(?i)(smtp[_-]?password|mail[_-]?password)"), Severity.MEDIUM);

        // Low: general env vars that may contain sensitive info
        SEVERITY_RULES.put(Pattern.compile("(?i)(access[_-]?token|client[_-]?secret)"), Severity.LOW);
    }

    /**
     * Classifies the severity of a violation based on the matched line content.
     *
     * @param matchedLine the line that triggered the violation
     * @param patternName the name of the pattern that matched
     * @return the severity level
     */
    public Severity classify(String matchedLine, String patternName) {
        if (matchedLine == null || matchedLine.isEmpty()) {
            return Severity.LOW;
        }
        String combined = (patternName + " " + matchedLine).toLowerCase();
        for (Map.Entry<Pattern, Severity> entry : SEVERITY_RULES.entrySet()) {
            if (entry.getKey().matcher(combined).find()) {
                return entry.getValue();
            }
        }
        return Severity.LOW;
    }

    /**
     * Returns a display label with ANSI color for terminal output.
     */
    public String getColoredLabel(Severity severity) {
        return switch (severity) {
            case CRITICAL -> "\u001B[31m[CRITICAL]\u001B[0m";
            case HIGH     -> "\u001B[33m[HIGH]\u001B[0m";
            case MEDIUM   -> "\u001B[34m[MEDIUM]\u001B[0m";
            case LOW      -> "\u001B[37m[LOW]\u001B[0m";
        };
    }
}
