package com.envguard.cli;

import com.envguard.config.ConfigLoader;
import com.envguard.reporter.*;
import com.envguard.scanner.*;

import java.io.File;

/**
 * Application entry point. Bootstraps all components and delegates
 * to {@link ScanRunner} for the actual scan logic.
 */
public class EnvGuardMain {

    public static void main(String[] args) {
        File targetDir = resolveTargetDir(args);

        ConfigLoader configLoader = new ConfigLoader();
        PatternLoader patternLoader = new PatternLoader(configLoader);
        FileScanner fileScanner = new FileScanner(patternLoader);
        DirectoryScanner directoryScanner = new DirectoryScanner(fileScanner, configLoader);
        ScanOrchestrator orchestrator = new ScanOrchestrator(directoryScanner, fileScanner);

        SeverityClassifier classifier = new SeverityClassifier(configLoader);
        SeverityFilter severityFilter = new SeverityFilter();
        WhitelistManager whitelistManager = new WhitelistManager(configLoader);
        WhitelistFilter whitelistFilter = new WhitelistFilter(whitelistManager);

        ReportFormatter formatter = new ReportFormatter();
        SummaryReporter summaryReporter = new SummaryReporter(formatter);
        ReportWriter reportWriter = new ReportWriter(formatter);

        ExitCodeResolver exitCodeResolver = new ExitCodeResolver();
        CommitBlocker commitBlocker = new CommitBlocker();

        ScanRunner runner = new ScanRunner(
                configLoader, patternLoader, orchestrator,
                classifier, severityFilter, whitelistFilter,
                summaryReporter, reportWriter, exitCodeResolver, commitBlocker);

        int exitCode = runner.run(targetDir);
        System.exit(exitCode);
    }

    private static File resolveTargetDir(String[] args) {
        if (args.length > 0) {
            File dir = new File(args[0]);
            if (!dir.isDirectory()) {
                System.err.println("[envguard] ERROR: '" + args[0] + "' is not a valid directory.");
                System.exit(2);
            }
            return dir;
        }
        return new File(System.getProperty("user.dir"));
    }
}
