package com.envguard.reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups scan violations by file path for structured reporting.
 */
public class ViolationGrouper {

    /**
     * Groups a list of violations by their file path.
     *
     * @param violations the flat list of violations to group
     * @return a map from file path to list of violations in that file
     */
    public Map<String, List<ScanViolation>> groupByFile(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<ScanViolation>> grouped = new LinkedHashMap<>();
        for (ScanViolation violation : violations) {
            String filePath = violation.getFilePath();
            grouped.computeIfAbsent(filePath, k -> new ArrayList<>()).add(violation);
        }
        return grouped;
    }

    /**
     * Groups a list of violations by their severity level.
     *
     * @param violations the flat list of violations to group
     * @return a map from severity string to list of violations with that severity
     */
    public Map<String, List<ScanViolation>> groupBySeverity(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<ScanViolation>> grouped = new LinkedHashMap<>();
        for (ScanViolation violation : violations) {
            String severity = violation.getSeverity() != null ? violation.getSeverity() : "UNKNOWN";
            grouped.computeIfAbsent(severity, k -> new ArrayList<>()).add(violation);
        }
        return grouped;
    }

    /**
     * Returns the total count of files that contain at least one violation.
     *
     * @param violations the list of violations
     * @return number of distinct files affected
     */
    public int countAffectedFiles(List<ScanViolation> violations) {
        return groupByFile(violations).size();
    }
}
