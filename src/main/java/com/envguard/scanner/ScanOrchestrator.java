package com.envguard.scanner;

import com.envguard.reporter.ScanResult;
import com.envguard.reporter.ScanViolation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Orchestrates a full scan by coordinating PatternLoader, DirectoryScanner,
 * and FileScanner, then aggregating results into a single ScanResult.
 */
public class ScanOrchestrator {

    private static final Logger LOGGER = Logger.getLogger(ScanOrchestrator.class.getName());

    private final PatternLoader patternLoader;
    private final DirectoryScanner directoryScanner;
    private final FileScanner fileScanner;

    public ScanOrchestrator(PatternLoader patternLoader,
                            DirectoryScanner directoryScanner,
                            FileScanner fileScanner) {
        this.patternLoader = patternLoader;
        this.directoryScanner = directoryScanner;
        this.fileScanner = fileScanner;
    }

    /**
     * Runs a full scan of the given root directory using patterns from the given config path.
     *
     * @param rootDir     the directory to scan recursively
     * @param patternFile path to the patterns config file
     * @return aggregated ScanResult containing all violations found
     * @throws IOException if pattern loading or directory traversal fails
     */
    public ScanResult scan(Path rootDir, Path patternFile) throws IOException {
        LOGGER.info("Loading patterns from: " + patternFile);
        List<String> patterns = patternLoader.load(patternFile);

        if (patterns.isEmpty()) {
            LOGGER.warning("No patterns loaded — scan will produce no violations.");
        }

        LOGGER.info("Scanning directory: " + rootDir);
        List<Path> files = directoryScanner.collectFiles(rootDir);

        List<ScanViolation> allViolations = new ArrayList<>();
        for (Path file : files) {
            List<ScanViolation> violations = fileScanner.scan(file, patterns);
            allViolations.addAll(violations);
        }

        LOGGER.info("Scan complete. Files scanned: " + files.size()
                + ", violations found: " + allViolations.size());

        return new ScanResult(allViolations, files.size());
    }

    /** Convenience factory using default config path. */
    public static ScanOrchestrator createDefault() {
        return new ScanOrchestrator(
                new PatternLoader(),
                new DirectoryScanner(),
                new FileScanner()
        );
    }
}
