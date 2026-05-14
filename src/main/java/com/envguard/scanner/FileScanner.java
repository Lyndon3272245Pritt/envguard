package com.envguard.scanner;

import com.envguard.reporter.ScanResult;
import com.envguard.reporter.ScanViolation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scans individual files for secret/env variable pattern matches.
 */
public class FileScanner {

    private final List<Pattern> patterns;

    public FileScanner(List<Pattern> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            throw new IllegalArgumentException("Pattern list must not be null or empty");
        }
        this.patterns = patterns;
    }

    /**
     * Scans the given file and returns a ScanResult with any violations found.
     *
     * @param filePath path to the file to scan
     * @return ScanResult containing all violations detected
     * @throws IOException if the file cannot be read
     */
    public ScanResult scan(Path filePath) throws IOException {
        ScanResult result = new ScanResult(filePath.toString());

        List<String> lines = Files.readAllLines(filePath);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    ScanViolation violation = new ScanViolation(
                            filePath.toString(),
                            i + 1,
                            pattern.pattern(),
                            redact(line.trim())
                    );
                    result.addViolation(violation);
                    break; // one violation per line is sufficient
                }
            }
        }
        return result;
    }

    /**
     * Redacts the value portion of a matched line for safe reporting.
     */
    private String redact(String line) {
        return line.replaceAll("=.*", "=***REDACTED***");
    }
}
