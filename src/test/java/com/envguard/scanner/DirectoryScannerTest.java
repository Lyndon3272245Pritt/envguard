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

class DirectoryScannerTest {

    private DirectoryScanner directoryScanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        List<Pattern> patterns = List.of(
                Pattern.compile("(?i)(api_key|secret|password)\\s*=\\s*.+")
        );
        directoryScanner = new DirectoryScanner(patterns);
    }

    @Test
    void findsViolationsInNestedFiles() throws IOException {
        Path subDir = tempDir.resolve("config");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("app.env"), "API_KEY=leaked\n");
        Files.writeString(tempDir.resolve("readme.txt"), "No secrets here.\n");

        List<ScanResult> results = directoryScanner.scanDirectory(tempDir);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getFilePath().contains("app.env"));
    }

    @Test
    void returnsEmptyListForCleanDirectory() throws IOException {
        Files.writeString(tempDir.resolve("notes.txt"), "All good here.\n");

        List<ScanResult> results = directoryScanner.scanDirectory(tempDir);

        assertTrue(results.isEmpty());
    }

    @Test
    void skipsBinaryFileExtensions() throws IOException {
        Path imageFile = tempDir.resolve("logo.png");
        Files.write(imageFile, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        List<ScanResult> results = directoryScanner.scanDirectory(tempDir);

        assertTrue(results.isEmpty(), "Binary files should be skipped");
    }

    @Test
    void aggregatesResultsFromMultipleFiles() throws IOException {
        Files.writeString(tempDir.resolve("a.env"), "SECRET=abc\n");
        Files.writeString(tempDir.resolve("b.env"), "PASSWORD=xyz\n");
        Files.writeString(tempDir.resolve("c.txt"), "clean file\n");

        List<ScanResult> results = directoryScanner.scanDirectory(tempDir);

        assertEquals(2, results.size());
    }
}
