package com.envguard.scanner;

import com.envguard.reporter.ScanResult;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Walks a directory tree and delegates per-file scanning to FileScanner.
 */
public class DirectoryScanner {

    private static final Set<String> SKIP_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico",
            ".class", ".jar", ".zip", ".tar", ".gz"
    );

    private final FileScanner fileScanner;

    public DirectoryScanner(List<Pattern> patterns) {
        this.fileScanner = new FileScanner(patterns);
    }

    /**
     * Scans all eligible files under the given root directory.
     *
     * @param root directory to scan recursively
     * @return list of ScanResult, one per scanned file that has violations
     * @throws IOException if directory traversal fails
     */
    public List<ScanResult> scanDirectory(Path root) throws IOException {
        List<ScanResult> results = new ArrayList<>();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (shouldSkip(file)) {
                    return FileVisitResult.CONTINUE;
                }
                ScanResult result = fileScanner.scan(file);
                if (result.hasViolations()) {
                    results.add(result);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.err.println("[WARN] Could not read file: " + file + " — " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        return results;
    }

    private boolean shouldSkip(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        return SKIP_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
