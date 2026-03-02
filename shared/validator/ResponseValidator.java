package com.neil.automation.validator;

import io.qameta.allure.Step;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ResponseValidator — FAANG-grade response validation engine.
 *
 * <p>Provides comprehensive validation for API responses:
 * - Status code validation
 * - Response time validation (SLA enforcement)
 * - JSON Schema validation
 * - Header validation
 * - Body field validation
 * - Soft assertions (collect ALL failures)
 *
 * <p>All methods annotated with @Step for Allure reporting.
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public class ResponseValidator {

    private static final Logger LOG =
        LogManager.getLogger(ResponseValidator.class);

    // ── SLA thresholds (milliseconds) ──
    private static final long SLA_FAST   = 500L;   // < 500ms  = fast
    private static final long SLA_OK     = 2000L;  // < 2000ms = acceptable
    private static final long SLA_SLOW   = 5000L;  // > 5000ms = fail

    // ── Response under validation ──
    private final Response response;

    // ── Soft assertions collector ──
    private final SoftAssertions softly;

    // ─────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────

    /**
     * Creates validator for a given response.
     *
     * @param response RestAssured Response to validate
     */
    public ResponseValidator(Response response) {
        this.response = response;
        this.softly   = new SoftAssertions();

        LOG.info("🔍 ResponseValidator created for status: [{}]",
            response.getStatusCode());
    }

    // ─────────────────────────────────────────
    // FACTORY METHOD
    // ─────────────────────────────────────────

    /**
     * Static factory — fluent entry point.
     *
     * <pre>
     *   ResponseValidator.validate(response)
     *       .statusCode(200)
     *       .responseTimeLessThan(2000)
     *       .bodyFieldEquals("name", "doggie")
     *       .assertAll();
     * </pre>
     *
     * @param response response to validate
     * @return ResponseValidator instance
     */
    public static ResponseValidator validate(Response response) {
        return new ResponseValidator(response);
    }

    // ─────────────────────────────────────────
    // STATUS CODE VALIDATION
    // ─────────────────────────────────────────

    /**
     * Validates HTTP status code.
     *
     * @param expectedCode expected HTTP status code
     * @return this (fluent chain)
     */
    @Step("Validate status code is [{expectedCode}]")
    public ResponseValidator statusCode(int expectedCode) {
        int actualCode = response.getStatusCode();
        LOG.info("✔ Validating status code: expected=[{}] actual=[{}]",
            expectedCode, actualCode);

        softly.assertThat(actualCode)
            .as("HTTP Status Code")
            .isEqualTo(expectedCode);

        return this;
    }

    /**
     * Validates status code is in 2xx success range.
     *
     * @return this (fluent chain)
     */
    @Step("Validate status code is 2xx success")
    public ResponseValidator statusIsSuccess() {
        int code = response.getStatusCode();
        LOG.info("✔ Validating 2xx status: actual=[{}]", code);

        softly.assertThat(code)
            .as("HTTP Status should be 2xx")
            .isBetween(200, 299);

        return this;
    }

    /**
     * Validates status code is 4xx client error.
     *
     * @return this (fluent chain)
     */
    @Step("Validate status code is 4xx client error")
    public ResponseValidator statusIsClientError() {
        int code = response.getStatusCode();

        softly.assertThat(code)
            .as("HTTP Status should be 4xx")
            .isBetween(400, 499);

        return this;
    }

    // ─────────────────────────────────────────
    // RESPONSE TIME VALIDATION
    // ─────────────────────────────────────────

    /**
     * Validates response time is below threshold (SLA enforcement).
     *
     * @param maxMilliseconds maximum acceptable response time
     * @return this (fluent chain)
     */
    @Step("Validate response time < [{maxMilliseconds}ms]")
    public ResponseValidator responseTimeLessThan(long maxMilliseconds) {
        long actualTime = response.getTimeIn(TimeUnit.MILLISECONDS);
        LOG.info("✔ Response time: [{}ms] (max: [{}ms])",
            actualTime, maxMilliseconds);

        // Log performance tier
        if (actualTime < SLA_FAST) {
            LOG.info("🟢 Performance: FAST (< 500ms)");
        } else if (actualTime < SLA_OK) {
            LOG.info("🟡 Performance: ACCEPTABLE (< 2000ms)");
        } else {
            LOG.warn("🔴 Performance: SLOW (> 2000ms)");
        }

        softly.assertThat(actualTime)
            .as("Response time should be under [%dms]", maxMilliseconds)
            .isLessThanOrEqualTo(maxMilliseconds);

        return this;
    }

    /**
     * Validates response meets default SLA of 2 seconds.
     *
     * @return this (fluent chain)
     */
    @Step("Validate response meets 2s SLA")
    public ResponseValidator meetsDefaultSLA() {
        return responseTimeLessThan(SLA_OK);
    }

    // ─────────────────────────────────────────
    // BODY VALIDATION
    // ─────────────────────────────────────────

    /**
     * Validates a specific JSON field equals expected value.
     *
     * @param jsonPath      JsonPath expression
     * @param expectedValue expected value
     * @return this (fluent chain)
     */
    @Step("Validate body field [{jsonPath}] equals [{expectedValue}]")
    public ResponseValidator bodyFieldEquals(String jsonPath,
                                              Object expectedValue) {
        Object actualValue = response.jsonPath().get(jsonPath);
        LOG.info("✔ Field [{}]: expected=[{}] actual=[{}]",
            jsonPath, expectedValue, actualValue);

        softly.assertThat(actualValue)
            .as("Body field [%s]", jsonPath)
            .isEqualTo(expectedValue);

        return this;
    }

    /**
     * Validates a JSON field is not null and not empty.
     *
     * @param jsonPath JsonPath expression
     * @return this (fluent chain)
     */
    @Step("Validate body field [{jsonPath}] is not null or empty")
    public ResponseValidator bodyFieldNotNull(String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        LOG.info("✔ Field [{}] not null check: value=[{}]",
            jsonPath, value);

        softly.assertThat(value)
            .as("Body field [%s] should not be null", jsonPath)
            .isNotNull();

        return this;
    }

    /**
     * Validates a JSON array field has expected size.
     *
     * @param jsonPath     JsonPath to array
     * @param expectedSize expected array size
     * @return this (fluent chain)
     */
    @Step("Validate array [{jsonPath}] has size [{expectedSize}]")
    public ResponseValidator arrayHasSize(String jsonPath, int expectedSize) {
        List<?> list = response.jsonPath().getList(jsonPath);

        softly.assertThat(list)
            .as("Array [%s] size", jsonPath)
            .hasSize(expectedSize);

        return this;
    }

    /**
     * Validates a JSON array field is not empty.
     *
     * @param jsonPath JsonPath to array
     * @return this (fluent chain)
     */
    @Step("Validate array [{jsonPath}] is not empty")
    public ResponseValidator arrayNotEmpty(String jsonPath) {
        List<?> list = response.jsonPath().getList(jsonPath);

        softly.assertThat(list)
            .as("Array [%s] should not be empty", jsonPath)
            .isNotEmpty();

        return this;
    }

    /**
     * Validates response body contains expected string.
     *
     * @param expectedString string to search for
     * @return this (fluent chain)
     */
    @Step("Validate body contains [{expectedString}]")
    public ResponseValidator bodyContains(String expectedString) {
        String body = response.getBody().asString();

        softly.assertThat(body)
            .as("Response body")
            .contains(expectedString);

        return this;
    }

    // ─────────────────────────────────────────
    // HEADER VALIDATION
    // ─────────────────────────────────────────

    /**
     * Validates a response header equals expected value.
     *
     * @param headerName    header name
     * @param expectedValue expected header value
     * @return this (fluent chain)
     */
    @Step("Validate header [{headerName}] equals [{expectedValue}]")
    public ResponseValidator headerEquals(String headerName,
                                           String expectedValue) {
        String actual = response.getHeader(headerName);
        LOG.info("✔ Header [{}]: expected=[{}] actual=[{}]",
            headerName, expectedValue, actual);

        softly.assertThat(actual)
            .as("Header [%s]", headerName)
            .isEqualTo(expectedValue);

        return this;
    }

    /**
     * Validates Content-Type header.
     *
     * @param expectedContentType expected content type
     * @return this (fluent chain)
     */
    @Step("Validate Content-Type is [{expectedContentType}]")
    public ResponseValidator contentType(String expectedContentType) {
        String actual = response.getContentType();

        softly.assertThat(actual)
            .as("Content-Type header")
            .contains(expectedContentType);

        return this;
    }

    // ─────────────────────────────────────────
    // JSON SCHEMA VALIDATION
    // ─────────────────────────────────────────

    /**
     * Validates response against a JSON Schema file.
     * Schema files stored in resources/schemas/
     *
     * @param schemaFileName schema file name (e.g., "pet_schema.json")
     * @return this (fluent chain)
     */
    @Step("Validate JSON Schema: [{schemaFileName}]")
    public ResponseValidator matchesSchema(String schemaFileName) {
        LOG.info("✔ Validating against schema: [{}]", schemaFileName);

        try {
            InputStream schema = getClass()
                .getClassLoader()
                .getResourceAsStream("schemas/" + schemaFileName);

            assertThat(schema)
                .as("Schema file [%s] must exist in resources/schemas/",
                    schemaFileName)
                .isNotNull();

            response.then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(schema));

            LOG.info("✅ Schema validation PASSED: [{}]", schemaFileName);

        } catch (Exception e) {
            LOG.error("❌ Schema validation FAILED: [{}]", schemaFileName, e);
            softly.fail("Schema validation failed: " + e.getMessage());
        }

        return this;
    }

    // ─────────────────────────────────────────
    // ASSERT — COLLECT ALL FAILURES
    // ─────────────────────────────────────────

    /**
     * Executes all soft assertions.
     * Call this at the END of every validation chain.
     *
     * <p>Collects ALL failures before throwing —
     * no stopping at first failure like hard assertions!
     */
    @Step("Assert all validations")
    public void assertAll() {
        LOG.info("🔍 Executing all soft assertions...");
        softly.assertAll();
        LOG.info("✅ All assertions PASSED!");
    }

    // ─────────────────────────────────────────
    // RAW RESPONSE ACCESS
    // ─────────────────────────────────────────

    /**
     * Returns raw response for custom
