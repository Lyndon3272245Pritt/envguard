package com.envguard.cli;

import com.envguard.config.ConfigLoader;
import com.envguard.reporter.*;
import com.envguard.scanner.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanRunnerTest {

    @Mock ConfigLoader configLoader;
    @Mock PatternLoader patternLoader;
    @Mock ScanOrchestrator orchestrator;
    @Mock SeverityClassifier classifier;
    @Mock SeverityFilter severityFilter;
    @Mock WhitelistFilter whitelistFilter;
    @Mock SummaryReporter summaryReporter;
    @Mock ReportWriter reportWriter;
    @Mock ExitCodeResolver exitCodeResolver;
    @Mock CommitBlocker commitBlocker;

    private ScanRunner scanRunner;
    private final File targetDir = new File(".");

    @BeforeEach
    void setUp() {
        scanRunner = new ScanRunner(
                configLoader, patternLoader, orchestrator,
                classifier, severityFilter, whitelistFilter,
                summaryReporter, reportWriter, exitCodeResolver, commitBlocker);
    }

    @Test
    void run_noViolations_returnsZero() {
        Properties props = new Properties();
        props.setProperty("envguard.min.severity", "LOW");
        when(configLoader.load()).thenReturn(props);
        when(patternLoader.loadPatterns()).thenReturn(Collections.emptyList());
        ScanResult emptyResult = new ScanResult(Collections.emptyList());
        when(orchestrator.scan(any(), anyList())).thenReturn(emptyResult);
        when(classifier.classify(anyList())).thenReturn(Collections.emptyList());
        when(severityFilter.filter(anyList(), anyString())).thenReturn(Collections.emptyList());
        when(whitelistFilter.filter(anyList())).thenReturn(Collections.emptyList());
        when(exitCodeResolver.resolve(any())).thenReturn(0);

        int code = scanRunner.run(targetDir);

        assertEquals(0, code);
        verify(commitBlocker).applyExitCode(0);
        verify(summaryReporter).printSummary(any());
        verify(reportWriter).write(any(), eq(props));
    }

    @Test
    void run_withViolations_returnsOne() {
        Properties props = new Properties();
        when(configLoader.load()).thenReturn(props);
        when(patternLoader.loadPatterns()).thenReturn(List.of("AWS_SECRET"));
        ScanViolation v = new ScanViolation("file.env", 3, "AWS_SECRET_KEY=abc", "AWS_SECRET");
        ScanResult result = new ScanResult(List.of(v));
        when(orchestrator.scan(any(), anyList())).thenReturn(result);
        when(classifier.classify(anyList())).thenReturn(List.of(v));
        when(severityFilter.filter(anyList(), any())).thenReturn(List.of(v));
        when(whitelistFilter.filter(anyList())).thenReturn(List.of(v));
        when(exitCodeResolver.resolve(any())).thenReturn(1);

        int code = scanRunner.run(targetDir);

        assertEquals(1, code);
        verify(commitBlocker).applyExitCode(1);
    }
}
