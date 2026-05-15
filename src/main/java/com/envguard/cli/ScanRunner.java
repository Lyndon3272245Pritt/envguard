package com.envguard.cli;

import com.envguard.config.ConfigLoader;
import com.envguard.reporter.*;
import com.envguard.scanner.*;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Main entry point that wires together scanning, filtering, reporting,
 * and exit-code resolution for a single envguard run.
 */
public class ScanRunner {

    private final ConfigLoader configLoader;
    private final PatternLoader patternLoader;
    private final ScanOrchestrator orchestrator;
    private final SeverityClassifier classifier;
    private final SeverityFilter severityFilter;
    private final WhitelistFilter whitelistFilter;
    private final SummaryReporter summaryReporter;
    private final ReportWriter reportWriter;
    private final ExitCodeResolver exitCodeResolver;
    private final CommitBlocker commitBlocker;

    public ScanRunner(
            ConfigLoader configLoader,
            PatternLoader patternLoader,
            ScanOrchestrator orchestrator,
            SeverityClassifier classifier,
            SeverityFilter severityFilter,
            WhitelistFilter whitelistFilter,
            SummaryReporter summaryReporter,
            ReportWriter reportWriter,
            ExitCodeResolver exitCodeResolver,
            CommitBlocker commitBlocker) {
        this.configLoader = configLoader;
        this.patternLoader = patternLoader;
        this.orchestrator = orchestrator;
        this.classifier = classifier;
        this.severityFilter = severityFilter;
        this.whitelistFilter = whitelistFilter;
        this.summaryReporter = summaryReporter;
        this.reportWriter = reportWriter;
        this.exitCodeResolver = exitCodeResolver;
        this.commitBlocker = commitBlocker;
    }

    /**
     * Executes a full scan of the given target directory.
     *
     * @param targetDir directory to scan
     * @return exit code (0 = clean, 1 = violations found)
     */
    public int run(File targetDir) {
        Properties config = configLoader.load();

        List<String> patterns = patternLoader.loadPatterns();
        ScanResult rawResult = orchestrator.scan(targetDir, patterns);

        List<ScanViolation> classified = classifier.classify(rawResult.getViolations());
        String minSeverity = config.getProperty("envguard.min.severity", "LOW");
        List<ScanViolation> filtered = severityFilter.filter(classified, minSeverity);
        List<ScanViolation> whitelisted = whitelistFilter.filter(filtered);

        ScanResult finalResult = new ScanResult(whitelisted);

        summaryReporter.printSummary(finalResult);
        reportWriter.write(finalResult, config);

        int exitCode = exitCodeResolver.resolve(finalResult);
        commitBlocker.applyExitCode(exitCode);
        return exitCode;
    }
}
