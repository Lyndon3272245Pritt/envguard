package com.envguard.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Loads secret detection patterns from a configuration file.
 * Supports both classpath resources and filesystem paths.
 */
public class PatternLoader {

    private static final Logger logger = Logger.getLogger(PatternLoader.class.getName());
    private static final String DEFAULT_PATTERNS_RESOURCE = "/config/patterns.txt";

    /**
     * Loads patterns from the default classpath resource (config/patterns.txt).
     */
    public List<Pattern> loadDefaultPatterns() throws IOException {
        InputStream is = getClass().getResourceAsStream(DEFAULT_PATTERNS_RESOURCE);
        if (is == null) {
            throw new IOException("Default patterns file not found on classpath: " + DEFAULT_PATTERNS_RESOURCE);
        }
        return parsePatterns(new BufferedReader(new InputStreamReader(is)));
    }

    /**
     * Loads patterns from a filesystem path.
     */
    public List<Pattern> loadFromPath(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("Patterns file not found: " + filePath);
        }
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            return parsePatterns(reader);
        }
    }

    private List<Pattern> parsePatterns(BufferedReader reader) throws IOException {
        List<Pattern> patterns = new ArrayList<>();
        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                patterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException e) {
                logger.warning("Skipping invalid pattern at line " + lineNumber + ": " + line + " — " + e.getMessage());
            }
        }

        logger.info("Loaded " + patterns.size() + " patterns.");
        return Collections.unmodifiableList(patterns);
    }
}
