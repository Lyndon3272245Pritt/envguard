package com.envguard.reporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Exports scan violations to various output formats (JSON, CSV, plain text).
 */
public class ViolationExporter {

    public enum ExportFormat {
        JSON, CSV, TEXT
    }

    public void export(List<ScanViolation> violations, Path outputPath, ExportFormat format) throws IOException {
        Files.createDirectories(outputPath.getParent() != null ? outputPath.getParent() : Path.of("."));
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
            switch (format) {
                case JSON -> writeJson(violations, writer);
                case CSV  -> writeCsv(violations, writer);
                case TEXT -> writeText(violations, writer);
            }
        }
    }

    private void writeJson(List<ScanViolation> violations, PrintWriter writer) {
        writer.println("[");
        for (int i = 0; i < violations.size(); i++) {
            ScanViolation v = violations.get(i);
            writer.printf("  {\"file\": \"%s\", \"line\": %d, \"pattern\": \"%s\", \"severity\": \"%s\"}%s%n",
                    escape(v.getFilePath()), v.getLineNumber(),
                    escape(v.getMatchedPattern()), v.getSeverity(),
                    i < violations.size() - 1 ? "," : "");
        }
        writer.println("]");
    }

    private void writeCsv(List<ScanViolation> violations, PrintWriter writer) {
        writer.println("file,line,pattern,severity");
        for (ScanViolation v : violations) {
            writer.printf("\"%s\",%d,\"%s\",\"%s\"%n",
                    escape(v.getFilePath()), v.getLineNumber(),
                    escape(v.getMatchedPattern()), v.getSeverity());
        }
    }

    private void writeText(List<ScanViolation> violations, PrintWriter writer) {
        if (violations.isEmpty()) {
            writer.println("No violations found.");
            return;
        }
        for (ScanViolation v : violations) {
            writer.printf("[%s] %s:%d — pattern: %s%n",
                    v.getSeverity(), v.getFilePath(), v.getLineNumber(), v.getMatchedPattern());
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
