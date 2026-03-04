package com.neil.automation.petstore.base;

import com.neil.automation.ai.AITestGenerator;
import com.neil.automation.config.ConfigLoader;
import com.neil.automation.petstore.client.PetStoreClient;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BaseTest — TestNG foundation for all PetStore test classes.
 *
 * <p>Provides:
 * - Suite-level setup and teardown
 * - Per-test setup and teardown
 * - Shared client instance
 * - AI generator access
 * - Allure reporting helpers
 * - Structured logging per test
 *
 * <p>All test classes MUST extend this class:
 * <pre>
 *   public class PetCRUDTest extends BaseTest { ... }
 *   public class PetNegativeTest extends BaseTest { ... }
 *   public class PetSchemaTest extends BaseTest { ... }
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public abstract class BaseTest {

    protected static final Logger LOG =
        LogManager.getLogger(BaseTest.class);

    // ── Shared across all tests ──
    protected PetStoreClient  client;
    protected ConfigLoader    config;
    protected AITestGenerator aiGenerator;

    // ── Test timing ──
    private long testStartTime;

    // ─────────────────────────────────────────
    // SUITE SETUP — Runs ONCE before all tests
    // ─────────────────────────────────────────

    /**
     * Suite-level setup — runs once before entire test suite.
     * Validates config, logs environment info.
     */
    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        LOG.info("╔══════════════════════════════════════════╗");
        LOG.info("║   Neil's AI API Automation Suite         ║");
        LOG.info("║   PetStore Module — Starting             ║");
        LOG.info("╠══════════════════════════════════════════╣");

        // Initialize config
        config = ConfigLoader.getInstance();

        LOG.info("║  Environment : [{}]",
            config.getEnvironment());
        LOG.info("║  Base URL    : [{}]",
            config.get("petstore.base.url"));
        LOG.info("║  AI Enabled  : [{}]",
            config.isAiEnabled());
        LOG.info("║  Started     : [{}]",
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        LOG.info("╚══════════════════════════════════════════╝");

        // Validate critical config exists
        validateConfig();

        LOG.info("✅ Suite setup complete");
    }

    // ─────────────────────────────────────────
    // TEST SETUP — Runs before EACH test
    // ─────────────────────────────────────────

    /**
     * Per-test setup — runs before each @Test method.
     * Creates fresh client and logs test start.
     *
     * @param method current test method injected by TestNG
     */
    @BeforeMethod(alwaysRun = true)
    public void testSetup(Method method) {
        testStartTime = System.currentTimeMillis();

        String testName  = method.getName();
        String className =
            method.getDeclaringClass().getSimpleName();

        LOG.info("┌──────────────────────────────────────────");
        LOG.info("│ ▶ START: {}.{}", className, testName);
        LOG.info("│   Time : {}",
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")
            ));
        LOG.info("└──────────────────────────────────────────");

        // Fresh client per test — no state leakage
        client      = new PetStoreClient();
        config      = ConfigLoader.getInstance();
        aiGenerator = AITestGenerator.getInstance();

        // Add test metadata to Allure report
        Allure.label("author",      "Neil J. Marshall");
        Allure.label("environment", config.getEnvironment());
        Allure.label("framework",   "AI-Powered API Automation");
    }

    // ─────────────────────────────────────────
    // TEST TEARDOWN — Runs after EACH test
    // ─────────────────────────────────────────

    /**
     * Per-test teardown — runs after each @Test method.
     * Logs test result and execution time.
     *
     * @param result TestNG result object injected by TestNG
     */
    @AfterMethod(alwaysRun = true)
    public void testTeardown(ITestResult result) {
        long duration =
            System.currentTimeMillis() - testStartTime;
        String testName =
            result.getMethod().getMethodName();
        String status =
            getStatusLabel(result.getStatus());

        LOG.info("┌──────────────────────────────────────────");
        LOG.info("│ {} FINISH: {}", status, testName);
        LOG.info("│   Duration : [{}ms]", duration);

        // Log failure details
        if (result.getStatus() == ITestResult.FAILURE) {
            Throwable cause = result.getThrowable();
            if (cause != null) {
                LOG.error("│   Failure  : {}",
                    cause.getMessage());
                Allure.addAttachment(
                    "Failure Details",
                    cause.getMessage()
                );
            }
        }

        // Performance warning threshold
        if (duration > 5000) {
            LOG.warn(
                "│   ⚠️  SLOW TEST: [{}ms] > 5000ms threshold",
                duration);
        }

        LOG.info("└──────────────────────────────────────────");
    }

    // ─────────────────────────────────────────
    // SUITE TEARDOWN — Runs ONCE after all tests
    // ─────────────────────────────────────────

    /**
     * Suite-level teardown — runs once after all tests.
     */
    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        LOG.info("╔══════════════════════════════════════════╗");
        LOG.info("║   PetStore Module — Complete             ║");
        LOG.info("║   Finished : [{}]",
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        LOG.info("╚══════════════════════════════════════════╝");
    }

    // ─────────────────────────────────────────
    // PROTECTED HELPERS
    // ─────────────────────────────────────────

    /**
     * Logs an Allure step description manually.
     * Useful for custom reporting in complex tests.
     *
     * @param stepDescription step text to log
     */
    protected void logStep(String stepDescription) {
        LOG.info("  ▸ STEP: {}", stepDescription);
        Allure.step(stepDescription);
    }

    /**
     * Attaches plain text to Allure report.
     *
     * @param title   attachment title
     * @param content text content
     */
    protected void attachToReport(String title, String content) {
        Allure.addAttachment(title, content);
    }

    /**
     * Adds a description to the current Allure test report.
     *
     * @param description test description markdown
     */
    protected void setTestDescription(String description) {
        Allure.description(description);
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────

    /**
     * Validates all required config keys exist.
     * Fails fast if critical config is missing.
     */
    private void validateConfig() {
        LOG.info("🔍 Validating required configuration...");

        String[] requiredKeys = {
            "petstore.base.url",
            "test.retry.count"
        };

        for (String key : requiredKeys) {
            try {
                String value = config.get(key);
                LOG.info("  ✅ [{}] = [{}]", key, value);
            } catch (ConfigLoader.ConfigurationException e) {
                LOG.error("  ❌ Missing required config: [{}]", key);
                throw e;
            }
        }

        LOG.info("✅ All required config keys validated");
    }

    /**
     * Converts TestNG status int to readable label.
     *
     * @param status TestNG ITestResult status code
     * @return human-readable status string
     */
    private String getStatusLabel(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "✅";
            case ITestResult.FAILURE -> "❌";
            case ITestResult.SKIP    -> "⏭️ ";
            default                  -> "❓";
        };
    }
}
