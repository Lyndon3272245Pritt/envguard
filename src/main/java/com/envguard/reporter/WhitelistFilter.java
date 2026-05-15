package com.envguard.reporter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Filters a list of ScanViolations by applying a WhitelistManager,
 * removing any violations that match whitelisted entries.
 */
public class WhitelistFilter {

    private final WhitelistManager whitelistManager;

    public WhitelistFilter(WhitelistManager whitelistManager) {
        Objects.requireNonNull(whitelistManager, "WhitelistManager must not be null");
        this.whitelistManager = whitelistManager;
    }

    /**
     * Returns a new list containing only violations not present in the whitelist.
     *
     * @param violations the original list of scan violations
     * @return filtered list with whitelisted violations removed
     */
    public List<ScanViolation> filter(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return List.of();
        }
        return violations.stream()
                .filter(v -> !whitelistManager.isWhitelisted(v))
                .collect(Collectors.toList());
    }

    /**
     * Returns the count of violations that would be suppressed by the whitelist.
     *
     * @param violations the original list of scan violations
     * @return number of suppressed violations
     */
    public long countSuppressed(List<ScanViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return 0L;
        }
        return violations.stream()
                .filter(whitelistManager::isWhitelisted)
                .count();
    }
}
