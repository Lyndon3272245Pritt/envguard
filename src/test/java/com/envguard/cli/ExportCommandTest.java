package com.envguard.cli;

import com.envguard.reporter.ScanViolation;
import com.envguard.reporter.ViolationExporter;
import com.envguard.reporter.ViolationExporter.ExportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportCommandTest {

    private ViolationExporter mockExporter;
    private ExportCommand command;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockExporter = mock(ViolationExporter.class);
        command = new ExportCommand(mockExporter);
    }

    private ScanViolation violation() {
        ScanViolation v = new ScanViolation(".env", 1, "API_KEY", "API_KEY=secret");
        v.setSeverity("HIGH");
        return v;
    }

    @Test
    void execute_withJsonFormat_callsExporterWithJsonEnum() throws IOException {
        String outPath = tempDir.resolve("out.json").toString();
        boolean result = command.execute(List.of(violation()), outPath, "json");
        assertTrue(result);
        verify(mockExporter).export(anyList(), eq(Path.of(outPath)), eq(ExportFormat.JSON));
    }

    @Test
    void execute_withCsvFormat_callsExporterWithCsvEnum() throws IOException {
        String outPath = tempDir.resolve("out.csv").toString();
        boolean result = command.execute(List.of(violation()), outPath, "csv");
        assertTrue(result);
        verify(mockExporter).export(anyList(), eq(Path.of(outPath)), eq(ExportFormat.CSV));
    }

    @Test
    void execute_withTextFormat_callsExporterWithTextEnum() throws IOException {
        String outPath = tempDir.resolve("out.txt").toString();
        boolean result = command.execute(List.of(violation()), outPath, "text");
        assertTrue(result);
        verify(mockExporter).export(anyList(), eq(Path.of(outPath)), eq(ExportFormat.TEXT));
    }

    @Test
    void execute_withUnknownFormat_returnsFalse() throws IOException {
        boolean result = command.execute(List.of(), tempDir.resolve("out").toString(), "xml");
        assertFalse(result);
        verifyNoInteractions(mockExporter);
    }

    @Test
    void execute_whenExporterThrows_returnsFalse() throws IOException {
        String outPath = tempDir.resolve("out.txt").toString();
        doThrow(new IOException("disk full")).when(mockExporter).export(anyList(), any(), any());
        boolean result = command.execute(List.of(violation()), outPath, "text");
        assertFalse(result);
    }

    @Test
    void execute_nullFormat_defaultsToText() throws IOException {
        String outPath = tempDir.resolve("out.txt").toString();
        boolean result = command.execute(List.of(), outPath, null);
        assertTrue(result);
        verify(mockExporter).export(anyList(), eq(Path.of(outPath)), eq(ExportFormat.TEXT));
    }
}
