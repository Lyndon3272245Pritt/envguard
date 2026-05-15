package com.envguard.cli;

import com.envguard.reporter.ExitCodeResolver;
import com.envguard.reporter.ScanResult;
import com.envguard.reporter.SummaryReporter;

/**
 * Handles the final step of a pre-commit hook: printing a summary and
 * determining whether the commit should be blocked.
 */
public class CommitBlocker {

    private final ExitCodeResolver exitCodeResolver;
    private final SummaryReporter summaryReporter;

    public CommitBlocker(ExitCodeResolver exitCodeResolver, SummaryReporter summaryReporter) {
        if (exitCodeResolver == null) throw new IllegalArgumentException("exitCodeResolver must not be null");
        if (summaryReporter == null) throw new IllegalArgumentException("summaryReporter must not be null");
        this.exitCodeResolver = exitCodeResolver;
        this.summaryReporter = summaryReporter;
    }

    /**
     * Evaluates the scan result, prints a summary to stdout, and returns
     * the exit code that the pre-commit hook should propagate.
     *
     * @param result the completed scan result
     * @return process exit code (0 = allow commit, 1 = block, 2 = error)
     */
    public int evaluate(ScanResult result) {
        if (result == null) {
            System.err.println("[EnvGuard] ERROR: Scan result is null — aborting commit as a precaution.");
            return ExitCodeResolver.EXIT_ERROR;
        }

        String summary = summaryReporter.generateSummary(result);
        System.out.println(summary);

        int code = exitCodeResolver.resolve(result);

        if (code == ExitCodeResolver.EXIT_VIOLATIONS) {
            System.err.println("[EnvGuard] Commit BLOCKED: secrets or env variables detected.");
        } else if (code == ExitCodeResolver.EXIT_CLEAN) {
            System.out.println("[EnvGuard] Commit ALLOWED: no violations above threshold.");
        }

        return code;
    }
}
