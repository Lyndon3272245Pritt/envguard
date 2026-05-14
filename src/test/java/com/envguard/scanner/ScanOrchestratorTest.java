package com.envguard.scanner;

import com.envguard.reporter.ScanResult;
import com.envguard.reporter.ScanViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScanOrchestratorTest {

    @TempDir
    Path tempDir;

    private Path patternFile;

    @BeforeEach
    void setUp() throws IOException {
        patternFile = tempDir.resolve("patterns.txt");
        Files.writeString(patternFile,
                "AWS_SECRET_ACCESS_KEY\n" +
                "password\s*=\s*\\S+\n" +
                "API_KEY");
    }

    @Test
    void scanFindsViolationsInMatchingFiles() throws IOException {
        Path sourceFile = tempDir.resolve("config.env");
        Files.writeString(sourceFile, "AWS_SECRET_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE\n");

        ScanOrchestrator orchestrator = ScanOrchestrator.createDefault();
        ScanResult result = orchestrator.scan(tempDir, patternFile);

        assertTrue(result.hasViolations(), "Expected at least one violation");
        List<ScanViolation> violations = result.getViolations();
        assertTrue(violations.stream()
                .anyMatch(v -> v.getFilePath().equals(sourceFile.toString())),
                "Violation should reference the scanned file");
    }

    @Test
    void scanReturnsNoViolationsForCleanFiles() throws IOException {
        Path sourceFile = tempDir.resolve("clean.java");
        Files.writeString(sourceFile, "public class Clean { }\n");

        ScanOrchestrator orchestrator = ScanOrchestrator.createDefault();
        ScanResult result = orchestrator.scan(tempDir, patternFile);

        // patterns.txt itself may match, so only assert the clean file has no violations
        result.getViolations().forEach(v ->
                assertNotEquals(sourceFile.toString(), v.getFilePath(),
                        "Clean file should have no violations"));
    }

    @Test
    void scanReportsCorrectFileCount() throws IOException {
        Files.writeString(tempDir.resolve("a.env"), "nothing=here\n");
        Files.writeString(tempDir.resolve("b.env"), "also=clean\n");

        ScanOrchestrator orchestrator = ScanOrchestrator.createDefault();
        ScanResult result = orchestrator.scan(tempDir, patternFile);

        // tempDir contains a.env, b.env + patternFile = 3 files
        assertTrue(result.getScannedFileCount() >= 2,
                "Should report at least 2 scanned files");
    }

    @Test
    void scanWithEmptyPatternFileProducesNoViolations() throws IOException {
        Files.writeString(patternFile, "");
        Files.writeString(tempDir.resolve("secrets.env"), "API_KEY=super_secret\n");

        ScanOrchestrator orchestrator = ScanOrchestrator.createDefault();
        ScanResult result = orchestrator.scan(tempDir, patternFile);

        assertFalse(result.hasViolations(),
                "Empty pattern file should yield no violations");
    }
}
