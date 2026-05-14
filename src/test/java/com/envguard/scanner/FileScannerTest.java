package com.envguard.scanner;

import com.envguard.reporter.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class FileScannerTest {

    private FileScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        List<Pattern> patterns = List.of(
                Pattern.compile("(?i)(api_key|secret|password)\\s*=\\s*.+"),
                Pattern.compile("(?i)aws_access_key_id\\s*=\\s*.+")
        );
        scanner = new FileScanner(patterns);
    }

    @Test
    void detectsSecretInFile() throws IOException {
        Path file = tempDir.resolve("config.env");
        Files.writeString(file, "API_KEY=supersecret123\n");

        ScanResult result = scanner.scan(file);

        assertTrue(result.hasViolations());
        assertEquals(1, result.getViolations().size());
        assertEquals(1, result.getViolations().get(0).getLineNumber());
    }

    @Test
    void noViolationsForCleanFile() throws IOException {
        Path file = tempDir.resolve("clean.txt");
        Files.writeString(file, "Hello World\nThis is fine.\n");

        ScanResult result = scanner.scan(file);

        assertFalse(result.hasViolations());
    }

    @Test
    void redactsValueInViolation() throws IOException {
        Path file = tempDir.resolve("secrets.env");
        Files.writeString(file, "PASSWORD=hunter2\n");

        ScanResult result = scanner.scan(file);

        assertTrue(result.hasViolations());
        String matchedLine = result.getViolations().get(0).getMatchedLine();
        assertFalse(matchedLine.contains("hunter2"), "Value should be redacted");
        assertTrue(matchedLine.contains("REDACTED"));
    }

    @Test
    void throwsOnNullPatternList() {
        assertThrows(IllegalArgumentException.class, () -> new FileScanner(null));
    }

    @Test
    void detectsMultipleViolationsOnDifferentLines() throws IOException {
        Path file = tempDir.resolve("multi.env");
        Files.writeString(file, "API_KEY=abc123\nPASSWORD=secret\nhost=localhost\n");

        ScanResult result = scanner.scan(file);

        assertEquals(2, result.getViolations().size());
    }
}
