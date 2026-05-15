package com.envguard.reporter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classifies scan violations into severity levels based on matched pattern names.
 */
public class SeverityClassifier {

    private static final Map<Pattern, ScanViolation.Severity> SEVERITY_RULES = new HashMap<>();

    static {
        // CRITICAL: private keys, tokens with full access
        SEVERITY_RULES.put(Pattern.compile("(?i)(private.?key|rsa|dsa|ecdsa|openssh)"), ScanViolation.Severity.CRITICAL);
        SEVERITY_RULES.put(Pattern.compile("(?i)(aws.?secret|github.?token|stripe.?secret)"), ScanViolation.Severity.CRITICAL);

        // HIGH: API keys, passwords, credentials
        SEVERITY_RULES.put(Pattern.compile("(?i)(api.?key|password|passwd|secret|credential)"), ScanViolation.Severity.HIGH);
        SEVERITY_RULES.put(Pattern.compile("(?i)(auth.?token|access.?token|bearer)"), ScanViolation.Severity.HIGH);

        // MEDIUM: connection strings, endpoints with embedded info
        SEVERITY_RULES.put(Pattern.compile("(?i)(database.?url|connection.?string|jdbc)"), ScanViolation.Severity.MEDIUM);
        SEVERITY_RULES.put(Pattern.compile("(?i)(smtp|ftp.?pass|mongo.?uri)"), ScanViolation.Severity.MEDIUM);

        // LOW: generic env vars, usernames, non-sensitive keys
        SEVERITY_RULES.put(Pattern.compile("(?i)(username|user.?name|app.?key|client.?id)"), ScanViolation.Severity.LOW);
    }

    /**
     * Classifies the severity of a violation based on its matched pattern name.
     *
     * @param patternName the name or label of the matched pattern
     * @return the classified severity level
     */
    public ScanViolation.Severity classify(String patternName) {
        if (patternName == null || patternName.isBlank()) {
            return ScanViolation.Severity.LOW;
        }
        for (Map.Entry<Pattern, ScanViolation.Severity> entry : SEVERITY_RULES.entrySet()) {
            if (entry.getKey().matcher(patternName).find()) {
                return entry.getValue();
            }
        }
        return ScanViolation.Severity.LOW;
    }

    /**
     * Annotates a violation with the appropriate severity level.
     *
     * @param violation the violation to annotate
     * @return the same violation with severity set
     */
    public ScanViolation annotate(ScanViolation violation) {
        if (violation == null) {
            throw new IllegalArgumentException("Violation must not be null");
        }
        ScanViolation.Severity severity = classify(violation.getPatternName());
        violation.setSeverity(severity);
        return violation;
    }
}
