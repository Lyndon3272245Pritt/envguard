package com.envguard.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeverityClassifierTest {

    private SeverityClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new SeverityClassifier();
    }

    @Test
    void testClassifiesCriticalForAwsSecretKey() {
        SeverityClassifier.Severity result = classifier.classify(
                "AWS_SECRET_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE", "aws-secret");
        assertEquals(SeverityClassifier.Severity.CRITICAL, result);
    }

    @Test
    void testClassifiesCriticalForGithubToken() {
        SeverityClassifier.Severity result = classifier.classify(
                "GITHUB_TOKEN=ghp_abc123", "github-token");
        assertEquals(SeverityClassifier.Severity.CRITICAL, result);
    }

    @Test
    void testClassifiesHighForPassword() {
        SeverityClassifier.Severity result = classifier.classify(
                "DB_PASSWORD=supersecret", "generic-password");
        assertEquals(SeverityClassifier.Severity.HIGH, result);
    }

    @Test
    void testClassifiesHighForApiKey() {
        SeverityClassifier.Severity result = classifier.classify(
                "API_KEY=12345abcde", "api-key");
        assertEquals(SeverityClassifier.Severity.HIGH, result);
    }

    @Test
    void testClassifiesMediumForDatabaseUrl() {
        SeverityClassifier.Severity result = classifier.classify(
                "DATABASE_URL=jdbc:postgresql://localhost/mydb", "db-url");
        assertEquals(SeverityClassifier.Severity.MEDIUM, result);
    }

    @Test
    void testClassifiesLowForAccessToken() {
        SeverityClassifier.Severity result = classifier.classify(
                "ACCESS_TOKEN=sometoken", "access-token");
        assertEquals(SeverityClassifier.Severity.LOW, result);
    }

    @Test
    void testClassifiesLowForUnrecognizedPattern() {
        SeverityClassifier.Severity result = classifier.classify(
                "SOME_RANDOM_VAR=value", "unknown");
        assertEquals(SeverityClassifier.Severity.LOW, result);
    }

    @Test
    void testClassifiesLowForNullLine() {
        SeverityClassifier.Severity result = classifier.classify(null, "any-pattern");
        assertEquals(SeverityClassifier.Severity.LOW, result);
    }

    @Test
    void testGetColoredLabelContainsSeverityName() {
        String label = classifier.getColoredLabel(SeverityClassifier.Severity.CRITICAL);
        assertTrue(label.contains("CRITICAL"));

        label = classifier.getColoredLabel(SeverityClassifier.Severity.HIGH);
        assertTrue(label.contains("HIGH"));

        label = classifier.getColoredLabel(SeverityClassifier.Severity.MEDIUM);
        assertTrue(label.contains("MEDIUM"));

        label = classifier.getColoredLabel(SeverityClassifier.Severity.LOW);
        assertTrue(label.contains("LOW"));
    }

    @Test
    void testCaseInsensitiveMatching() {
        SeverityClassifier.Severity upper = classifier.classify("PRIVATE_KEY=abc", "key");
        SeverityClassifier.Severity lower = classifier.classify("private_key=abc", "key");
        assertEquals(upper, lower);
        assertEquals(SeverityClassifier.Severity.CRITICAL, upper);
    }
}
