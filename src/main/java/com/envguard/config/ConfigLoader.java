package com.envguard.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads EnvGuard configuration from a .envguard.properties file,
 * falling back to built-in defaults when no config file is present.
 */
public class ConfigLoader {

    private static final String DEFAULT_CONFIG_RESOURCE = "/envguard-defaults.properties";
    private static final String USER_CONFIG_FILENAME = ".envguard.properties";

    private final Properties properties;

    public ConfigLoader() throws IOException {
        this(Paths.get(System.getProperty("user.dir"), USER_CONFIG_FILENAME));
    }

    public ConfigLoader(Path configPath) throws IOException {
        properties = new Properties();
        loadDefaults();
        if (Files.exists(configPath)) {
            loadUserConfig(configPath);
        }
    }

    private void loadDefaults() throws IOException {
        try (InputStream in = getClass().getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (in != null) {
                properties.load(in);
            }
        }
    }

    private void loadUserConfig(Path configPath) throws IOException {
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
        }
    }

    public String getPatternsFile() {
        return properties.getProperty("envguard.patterns.file", "config/patterns.txt");
    }

    public boolean isFailOnViolation() {
        return Boolean.parseBoolean(properties.getProperty("envguard.fail.on.violation", "true"));
    }

    public int getMaxFileSizeKb() {
        return Integer.parseInt(properties.getProperty("envguard.max.file.size.kb", "512"));
    }

    public String getExcludedPaths() {
        return properties.getProperty("envguard.excluded.paths", ".git,target,node_modules");
    }

    public String getReportFormat() {
        return properties.getProperty("envguard.report.format", "text");
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
