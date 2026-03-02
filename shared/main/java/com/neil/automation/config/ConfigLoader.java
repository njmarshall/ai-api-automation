package com.neil.automation.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigLoader — Singleton configuration manager.
 *
 * <p>Loads environment-specific properties from config files.
 * Supports multi-environment: dev, staging, prod.
 *
 * <p>Usage:
 * <pre>
 *   String baseUrl = ConfigLoader.getInstance().get("petstore.base.url");
 *   String apiKey  = ConfigLoader.getInstance().get("openai.api.key");
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public class ConfigLoader {

    private static final Logger LOG = LogManager.getLogger(ConfigLoader.class);

    // ── Singleton instance ──
    private static ConfigLoader instance;

    // ── Loaded properties ──
    private final Properties properties;

    // ── Environment — default to dev ──
    private final String env;

    // ─────────────────────────────────────────
    // PRIVATE CONSTRUCTOR (Singleton pattern)
    // ─────────────────────────────────────────
    private ConfigLoader() {
        this.env        = System.getProperty("env", "dev");
        this.properties = new Properties();
        loadProperties();
    }

    // ─────────────────────────────────────────
    // SINGLETON ACCESSOR — Thread Safe
    // ─────────────────────────────────────────

    /**
     * Returns the singleton instance of ConfigLoader.
     * Thread-safe using double-checked locking.
     *
     * @return ConfigLoader singleton
     */
    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    // ─────────────────────────────────────────
    // LOAD PROPERTIES
    // ─────────────────────────────────────────

    /**
     * Loads properties in priority order:
     * 1. Base config (config.properties)
     * 2. Environment override (config-dev.properties)
     * 3. System properties (highest priority)
     */
    private void loadProperties() {
        // Step 1: Load base config
        loadFile("config/config.properties");

        // Step 2: Load environment override
        loadFile("config/config-" + env + ".properties");

        // Step 3: System properties override everything
        properties.putAll(System.getProperties());

        LOG.info("✅ ConfigLoader initialized for environment: [{}]", env);
        LOG.info("✅ Total properties loaded: [{}]", properties.size());
    }

    /**
     * Loads a single properties file from classpath.
     * Silently skips if file not found (env override may not exist).
     *
     * @param filePath path relative to resources
     */
    private void loadFile(String filePath) {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream(filePath)) {

            if (input == null) {
                LOG.warn("⚠️  Config file not found: [{}] — skipping", filePath);
                return;
            }

            properties.load(input);
            LOG.info("✅ Loaded config file: [{}]", filePath);

        } catch (FileNotFoundException e) {
            LOG.warn("⚠️  Config file missing: [{}]", filePath);
        } catch (IOException e) {
            LOG.error("❌ Failed to load config file: [{}]", filePath, e);
            throw new ConfigurationException(
                "Failed to load configuration: " + filePath, e
            );
        }
    }

    // ─────────────────────────────────────────
    // PUBLIC ACCESSORS
    // ─────────────────────────────────────────

    /**
     * Gets a required property value.
     * Throws exception if key is missing.
     *
     * @param key property key
     * @return property value
     * @throws ConfigurationException if key not found
     */
    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(
                "❌ Required config key not found: [" + key + "] " +
                "for environment: [" + env + "]"
            );
        }
        return value.trim();
    }

    /**
     * Gets an optional property value with a default fallback.
     *
     * @param key          property key
     * @param defaultValue fallback value if key missing
     * @return property value or default
     */
    public String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            LOG.debug("Config key [{}] not found — using default: [{}]",
                key, defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Gets a property as integer.
     *
     * @param key          property key
     * @param defaultValue fallback integer
     * @return integer value
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            LOG.warn("⚠️  Config key [{}] is not a valid integer — " +
                "using default: [{}]", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Gets a property as boolean.
     *
     * @param key          property key
     * @param defaultValue fallback boolean
     * @return boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets current active environment.
     *
     * @return environment name (dev/staging/prod)
     */
    public String getEnvironment() {
        return env;
    }

    /**
     * Checks if AI test generation is enabled.
     *
     * @return true if AI enabled
     */
    public boolean isAiEnabled() {
        return getBoolean("ai.enabled", false);
    }

    // ─────────────────────────────────────────
    // RESET — For Testing Only
    // ─────────────────────────────────────────

    /**
     * Resets singleton — used in unit tests only.
     * Never call in production code!
     */
    static synchronized void reset() {
        instance = null;
    }

    // ─────────────────────────────────────────
    // INNER EXCEPTION CLASS
    // ─────────────────────────────────────────

    /**
     * Custom exception for configuration errors.
     */
    public static class ConfigurationException extends RuntimeException {

        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
