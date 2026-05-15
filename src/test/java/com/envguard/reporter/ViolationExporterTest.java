package com.envguard.reporter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViolationExporterTest {

    private final ViolationExporter exporter = new ViolationExporter();

    @TempDir
    Path tempDir;

    private ScanViolation violation(String file, int line, String pattern, String severity) {
        ScanViolation v = new ScanViolation(file, line, pattern, "SECRET=abc123");
        v.setSeverity(severity);
        return v;
    }

    @Test
    void exportTextFormat_writesExpectedLines() throws IOException {
        Path out = tempDir.resolve("report.txt");
        List<ScanViolation> violations = List.of(
                violation(".env", 3, "API_KEY", "HIGH"),
                violation("config.yml", 7, "DB_PASSWORD", "CRITICAL")
        );
        exporter.export(violations, out, ViolationExporter.ExportFormat.TEXT);
        String content = Files.readString(out);
        assertTrue(content.contains("[HIGH] .env:3"));
        assertTrue(content.contains("[CRITICAL] config.yml:7"));
    }

    @Test
    void exportCsvFormat_containsHeaderAndRows() throws IOException {
        Path out = tempDir.resolve("report.csv");
        List<ScanViolation> violations = List.of(
                violation(".env", 1, "SECRET_KEY", "HIGH")
        );
        exporter.export(violations, out, ViolationExporter.ExportFormat.CSV);
        String content = Files.readString(out);
        assertTrue(content.startsWith("file,line,pattern,severity"));
        assertTrue(content.contains(".env"));
        assertTrue(content.contains("SECRET_KEY"));
    }

    @Test
    void exportJsonFormat_isValidJsonArray() throws IOException {
        Path out = tempDir.resolve("report.json");
        List<ScanViolation> violations = List.of(
                violation("app.properties", 10, "AWS_SECRET", "CRITICAL")
        );
        exporter.export(violations, out, ViolationExporter.ExportFormat.JSON);
        String content = Files.readString(out);
        assertTrue(content.trim().startsWith("["));
        assertTrue(content.trim().endsWith("]"));
        assertTrue(content.contains("\"file\""));
        assertTrue(content.contains("\"AWS_SECRET\""));
    }

    @Test
    void exportTextFormat_emptyViolations_writesNoViolationsMessage() throws IOException {
        Path out = tempDir.resolve("empty.txt");
        exporter.export(List.of(), out, ViolationExporter.ExportFormat.TEXT);
        String content = Files.readString(out);
        assertTrue(content.contains("No violations found."));
    }

    @Test
    void exportJsonFormat_emptyViolations_writesEmptyArray() throws IOException {
        Path out = tempDir.resolve("empty.json");
        exporter.export(List.of(), out, ViolationExporter.ExportFormat.JSON);
        String content = Files.readString(out).trim();
        assertEquals("[]", content);
    }

    @Test
    void exportCsvFormat_emptyViolations_writesHeaderOnly() throws IOException {
        Path out = tempDir.resolve("empty.csv");
        exporter.export(List.of(), out, ViolationExporter.ExportFormat.CSV);
        String content = Files.readString(out).trim();
        // CSV export with no violations should still contain the header row
        assertTrue(content.startsWith("file,line,pattern,severity"));
        // Ensure no data rows are present beyond the header
        assertEquals(1, content.lines().count());
    }
}
