package com.envguard.cli;

import com.envguard.reporter.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitBlockerTest {

    private ExitCodeResolver mockResolver;
    private SummaryReporter mockSummary;
    private CommitBlocker blocker;

    @BeforeEach
    void setUp() {
        mockResolver = mock(ExitCodeResolver.class);
        mockSummary = mock(SummaryReporter.class);
        blocker = new CommitBlocker(mockResolver, mockSummary);
    }

    @Test
    void returnsCleanCodeWhenNoViolations() {
        ScanResult result = new ScanResult(Collections.emptyList());
        when(mockSummary.generateSummary(result)).thenReturn("No issues.");
        when(mockResolver.resolve(result)).thenReturn(ExitCodeResolver.EXIT_CLEAN);

        int code = blocker.evaluate(result);

        assertEquals(ExitCodeResolver.EXIT_CLEAN, code);
        verify(mockSummary).generateSummary(result);
        verify(mockResolver).resolve(result);
    }

    @Test
    void returnsViolationCodeWhenSecretsFound() {
        ScanViolation v = new ScanViolation(".env", 1, "SECRET=abc", "HIGH");
        ScanResult result = new ScanResult(List.of(v));
        when(mockSummary.generateSummary(result)).thenReturn("1 violation found.");
        when(mockResolver.resolve(result)).thenReturn(ExitCodeResolver.EXIT_VIOLATIONS);

        int code = blocker.evaluate(result);

        assertEquals(ExitCodeResolver.EXIT_VIOLATIONS, code);
    }

    @Test
    void returnsErrorCodeWhenResultIsNull() {
        int code = blocker.evaluate(null);
        assertEquals(ExitCodeResolver.EXIT_ERROR, code);
        verifyNoInteractions(mockResolver, mockSummary);
    }

    @Test
    void throwsWhenResolverIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommitBlocker(null, mockSummary));
    }

    @Test
    void throwsWhenSummaryReporterIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommitBlocker(mockResolver, null));
    }

    @Test
    void summaryIsAlwaysPrintedBeforeExitCode() {
        ScanResult result = new ScanResult(Collections.emptyList());
        when(mockSummary.generateSummary(result)).thenReturn("Summary text");
        when(mockResolver.resolve(result)).thenReturn(ExitCodeResolver.EXIT_CLEAN);

        blocker.evaluate(result);

        // Verify ordering via inOrder
        var inOrder = inOrder(mockSummary, mockResolver);
        inOrder.verify(mockSummary).generateSummary(result);
        inOrder.verify(mockResolver).resolve(result);
    }
}
