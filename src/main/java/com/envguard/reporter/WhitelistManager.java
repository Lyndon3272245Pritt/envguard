package com.envguard.reporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Manages a whitelist of known-safe patterns or file paths that should be
 * excluded from secret scan violations.
 */
public class WhitelistManager {

    private static final Logger LOGGER = Logger.getLogger(WhitelistManager.class.getName());
    private static final String DEFAULT_WHITELIST_PATH = ".envguard-whitelist";

    private final Set<String> whitelistedEntries;

    public WhitelistManager() {
        this.whitelistedEntries = new HashSet<>();
    }

    public WhitelistManager(String whitelistFilePath) throws IOException {
        this.whitelistedEntries = loadFromFile(whitelistFilePath);
    }

    public static WhitelistManager loadDefault() {
        Path defaultPath = Paths.get(DEFAULT_WHITELIST_PATH);
        if (Files.exists(defaultPath)) {
            try {
                return new WhitelistManager(DEFAULT_WHITELIST_PATH);
            } catch (IOException e) {
                LOGGER.warning("Could not load default whitelist: " + e.getMessage());
            }
        }
        return new WhitelistManager();
    }

    private Set<String> loadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new HashSet<>();
        }
        List<String> lines = Files.readAllLines(path);
        Set<String> entries = new HashSet<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    public boolean isWhitelisted(ScanViolation violation) {
        String fileEntry = violation.getFilePath();
        String lineEntry = violation.getFilePath() + ":" + violation.getLineNumber();
        String patternEntry = "pattern:" + violation.getMatchedPattern();
        return whitelistedEntries.contains(fileEntry)
                || whitelistedEntries.contains(lineEntry)
                || whitelistedEntries.contains(patternEntry);
    }

    public void addEntry(String entry) {
        whitelistedEntries.add(entry);
    }

    public Set<String> getEntries() {
        return Collections.unmodifiableSet(whitelistedEntries);
    }

    public int size() {
        return whitelistedEntries.size();
    }
}
