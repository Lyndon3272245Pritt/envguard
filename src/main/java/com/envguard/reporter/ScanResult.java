package com.envguard.reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the result of a secret scan on a single file.
 */
public class ScanResult {

    private final String filePath;
    private final List<ScanViolation> violations;
    private final long scanDurationMs;

    public ScanResult(String filePath, List<ScanViolation> violations, long scanDurationMs) {
        this.filePath = filePath;
        this.violations = Collections.unmodifiableList(new ArrayList<>(violations));
        this.scanDurationMs = scanDurationMs;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<ScanViolation> getViolations() {
        return violations;
    }

    public boolean hasViolations() {
        return !violations.isEmpty();
    }

    public long getScanDurationMs() {
        return scanDurationMs;
    }

    public int getViolationCount() {
        return violations.size();
    }

    @Override
    public String toString() {
        return String.format("ScanResult{file='%s', violations=%d, durationMs=%d}",
                filePath, violations.size(), scanDurationMs);
    }
}
