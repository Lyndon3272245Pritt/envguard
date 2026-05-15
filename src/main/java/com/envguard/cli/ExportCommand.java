package com.envguard.cli;

import com.envguard.reporter.ScanViolation;
import com.envguard.reporter.ViolationExporter;
import com.envguard.reporter.ViolationExporter.ExportFormat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * CLI command that triggers export of scan violations to a specified file and format.
 */
public class ExportCommand {

    private static final Logger LOGGER = Logger.getLogger(ExportCommand.class.getName());

    private final ViolationExporter exporter;

    public ExportCommand(ViolationExporter exporter) {
        this.exporter = exporter;
    }

    /**
     * Executes the export command.
     *
     * @param violations list of violations to export
     * @param outputPath destination file path
     * @param formatArg  format string: "json", "csv", or "text"
     * @return true if export succeeded, false otherwise
     */
    public boolean execute(List<ScanViolation> violations, String outputPath, String formatArg) {
        ExportFormat format = resolveFormat(formatArg);
        if (format == null) {
            LOGGER.warning("Unknown export format: " + formatArg + ". Use json, csv, or text.");
            return false;
        }
        try {
            exporter.export(violations, Path.of(outputPath), format);
            LOGGER.info("Exported " + violations.size() + " violation(s) to " + outputPath + " as " + format);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to export violations: " + e.getMessage());
            return false;
        }
    }

    private ExportFormat resolveFormat(String formatArg) {
        if (formatArg == null) return ExportFormat.TEXT;
        return switch (formatArg.toLowerCase().trim()) {
            case "json" -> ExportFormat.JSON;
            case "csv"  -> ExportFormat.CSV;
            case "text", "txt" -> ExportFormat.TEXT;
            default -> null;
        };
    }
}
