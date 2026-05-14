package com.envguard.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsDefaultsWhenNoUserConfigPresent() throws IOException {
        Path nonExistent = tempDir.resolve("missing.properties");
        ConfigLoader loader = new ConfigLoader(nonExistent);

        assertEquals("config/patterns.txt", loader.getPatternsFile());
        assertTrue(loader.isFailOnViolation());
        assertEquals(512, loader.getMaxFileSizeKb());
        assertEquals("text", loader.getReportFormat());
    }

    @Test
    void userConfigOverridesDefaults() throws IOException {
        Path userConfig = tempDir.resolve(".envguard.properties");
        Files.writeString(userConfig,
                "envguard.patterns.file=custom/my-patterns.txt\n" +
                "envguard.fail.on.violation=false\n" +
                "envguard.max.file.size.kb=1024\n" +
                "envguard.report.format=json\n");

        ConfigLoader loader = new ConfigLoader(userConfig);

        assertEquals("custom/my-patterns.txt", loader.getPatternsFile());
        assertFalse(loader.isFailOnViolation());
        assertEquals(1024, loader.getMaxFileSizeKb());
        assertEquals("json", loader.getReportFormat());
    }

    @Test
    void partialUserConfigKeepsDefaultsForMissingKeys() throws IOException {
        Path userConfig = tempDir.resolve(".envguard.properties");
        Files.writeString(userConfig, "envguard.report.format=json\n");

        ConfigLoader loader = new ConfigLoader(userConfig);

        assertEquals("json", loader.getReportFormat());
        // defaults preserved for keys not in user config
        assertEquals("config/patterns.txt", loader.getPatternsFile());
        assertTrue(loader.isFailOnViolation());
    }

    @Test
    void getExcludedPathsReturnsDefaultList() throws IOException {
        ConfigLoader loader = new ConfigLoader(tempDir.resolve("none.properties"));
        String excluded = loader.getExcludedPaths();
        assertTrue(excluded.contains(".git"));
        assertTrue(excluded.contains("target"));
    }

    @Test
    void getPropertyReturnsCustomFallback() throws IOException {
        ConfigLoader loader = new ConfigLoader(tempDir.resolve("none.properties"));
        assertEquals("fallback", loader.getProperty("envguard.nonexistent.key", "fallback"));
    }
}
