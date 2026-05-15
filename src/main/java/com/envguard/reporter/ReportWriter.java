package com.envguard.reporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes scan reports to the console and optionally to a file.
 */
public class ReportWriter {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final ReportFormatter formatter;
    private final boolean writeToFile;
    private final String outputDir;

    public ReportWriter(ReportFormatter formatter, boolean writeToFile, String outputDir) {
        this.formatter = formatter;
        this.writeToFile = writeToFile;
        this.outputDir = outputDir;
    }

    public ReportWriter(ReportFormatter formatter) {
        this(formatter, false, null);
    }

    /**
     * Outputs the scan result to stdout and optionally to a timestamped report file.
     *
     * @param result the scan result to report
     * @return the path of the written file, or null if file writing is disabled
     * @throws IOException if file writing fails
     */
    public Path write(ScanResult result) throws IOException {
        String report = formatter.format(result);
        System.out.println(report);

        if (!writeToFile || outputDir == null) {
            return null;
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String filename = "envguard-report-" + timestamp + ".txt";
        Path outputPath = Paths.get(outputDir, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            writer.println(report);
        }

        System.out.println("[EnvGuard] Report written to: " + outputPath.toAbsolutePath());
        return outputPath;
    }
}
