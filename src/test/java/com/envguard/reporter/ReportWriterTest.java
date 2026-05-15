package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportWriterTest {

    @TempDir
    Path tempDir;

    private ScanResult resultWithViolations;
    private ScanResult cleanResult;
    private ReportFormatter formatter;

    @BeforeEach
    void setUp() {
        ScanViolation violation = new ScanViolation(
                "src/main/resources/application.properties",
                3,
                "AWS_SECRET_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE",
                "AWS secret key"
        );
        resultWithViolations = new ScanResult(List.of(violation));
        cleanResult = new ScanResult(List.of());
        formatter = new ReportFormatter();
    }

    @Test
    void writeToConsoleOnly_doesNotCreateFile() throws IOException {
        ReportWriter writer = new ReportWriter(formatter);
        Path result = writer.write(cleanResult);
        assertNull(result, "No file should be written when file output is disabled");
    }

    @Test
    void writeToFile_createsReportFile() throws IOException {
        ReportWriter writer = new ReportWriter(formatter, true, tempDir.toString());
        Path reportPath = writer.write(resultWithViolations);

        assertNotNull(reportPath);
        assertTrue(Files.exists(reportPath), "Report file should exist");
        assertTrue(reportPath.getFileName().toString().startsWith("envguard-report-"));
        assertTrue(reportPath.getFileName().toString().endsWith(".txt"));
    }

    @Test
    void writeToFile_reportContainsViolationDetails() throws IOException {
        ReportWriter writer = new ReportWriter(formatter, true, tempDir.toString());
        Path reportPath = writer.write(resultWithViolations);

        assertNotNull(reportPath);
        String content = Files.readString(reportPath);
        assertTrue(content.contains("AWS secret key"), "Report should contain violation pattern name");
        assertTrue(content.contains("application.properties"), "Report should contain the file name");
    }

    @Test
    void writeToFile_cleanScanReportContainsNoViolations() throws IOException {
        ReportWriter writer = new ReportWriter(formatter, true, tempDir.toString());
        Path reportPath = writer.write(cleanResult);

        assertNotNull(reportPath);
        String content = Files.readString(reportPath);
        assertTrue(content.contains("No secrets detected") || content.contains("PASSED"),
                "Clean report should indicate no violations found");
    }
}
