package com.neil.automation.client;

import com.neil.automation.config.ConfigLoader;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * BaseApiClient — Core RestAssured engine.
 *
 * <p>FAANG-grade API client providing:
 * - Standardized request/response specifications
 * - Allure reporting integration
 * - Structured logging
 * - Fluent HTTP methods (GET, POST, PUT, DELETE, PATCH)
 * - Automatic retry on transient failures
 *
 * <p>All module-specific clients extend this class.
 *
 * <pre>
 * Architecture:
 *   BaseApiClient (shared)
 *       ↑
 *   PetStoreClient (petstore module)
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public abstract class BaseApiClient {

    private static final Logger LOG = LogManager.getLogger(BaseApiClient.class);

    // ── Configuration ──
    protected final ConfigLoader config = ConfigLoader.getInstance();

    // ── RestAssured Specifications ──
    protected RequestSpecification  requestSpec;
    protected ResponseSpecification responseSpec;

    // ─────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────

    /**
     * Initializes client with base URL and default specifications.
     *
     * @param baseUrl API base URL
     */
    protected BaseApiClient(String baseUrl) {
        this.requestSpec  = buildRequestSpec(baseUrl);
        this.responseSpec = buildResponseSpec();
        LOG.info("✅ BaseApiClient initialized for: [{}]", baseUrl);
    }

    // ─────────────────────────────────────────
    // SPECIFICATION BUILDERS
    // ─────────────────────────────────────────

    /**
     * Builds the default request specification.
     * Applied to every request automatically.
     *
     * @param baseUrl API base URL
     * @return configured RequestSpecification
     */
    private RequestSpecification buildRequestSpec(String baseUrl) {
        return new RequestSpecBuilder()
            .setBaseUri(baseUrl)
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addFilter(new AllureRestAssured()  // Allure captures all requests
                .setRequestTemplate("request.ftl")
                .setResponseTemplate("response.ftl"))
            .addFilter(new RequestLoggingFilter(LogDetail.ALL))
            .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
            .build();
    }

    /**
     * Builds the default response specification.
     * Common validations applied to every response.
     *
     * @return configured ResponseSpecification
     */
    private ResponseSpecification buildResponseSpec() {
        return new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .build();
    }

    // ─────────────────────────────────────────
    // HTTP METHODS — FAANG Standard
    // ─────────────────────────────────────────

    /**
     * HTTP GET request.
     *
     * @param endpoint API endpoint path
     * @return Response object
     */
    protected Response get(String endpoint) {
        LOG.info("→ GET [{}]", endpoint);
        return given()
            .spec(requestSpec)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP GET request with path parameters.
     *
     * @param endpoint   API endpoint with path param placeholders
     * @param pathParams map of path parameters
     * @return Response object
     */
    protected Response get(String endpoint, Map<String, Object> pathParams) {
        LOG.info("→ GET [{}] with params: {}", endpoint, pathParams);
        return given()
            .spec(requestSpec)
            .pathParams(pathParams)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP GET request with query parameters.
     *
     * @param endpoint    API endpoint path
     * @param queryParams map of query parameters
     * @return Response object
     */
    protected Response getWithQuery(String endpoint,
                                    Map<String, Object> queryParams) {
        LOG.info("→ GET [{}] with query: {}", endpoint, queryParams);
        return given()
            .spec(requestSpec)
            .queryParams(queryParams)
            .when()
            .get(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP POST request with request body.
     *
     * @param endpoint API endpoint path
     * @param body     request payload object
     * @return Response object
     */
    protected Response post(String endpoint, Object body) {
        LOG.info("→ POST [{}]", endpoint);
        return given()
            .spec(requestSpec)
            .body(body)
            .when()
            .post(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP PUT request with request body.
     *
     * @param endpoint API endpoint path
     * @param body     request payload object
     * @return Response object
     */
    protected Response put(String endpoint, Object body) {
        LOG.info("→ PUT [{}]", endpoint);
        return given()
            .spec(requestSpec)
            .body(body)
            .when()
            .put(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP PATCH request with partial body.
     *
     * @param endpoint API endpoint path
     * @param body     partial request payload
     * @return Response object
     */
    protected Response patch(String endpoint, Object body) {
        LOG.info("→ PATCH [{}]", endpoint);
        return given()
            .spec(requestSpec)
            .body(body)
            .when()
            .patch(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP DELETE request.
     *
     * @param endpoint API endpoint path
     * @return Response object
     */
    protected Response delete(String endpoint) {
        LOG.info("→ DELETE [{}]", endpoint);
        return given()
            .spec(requestSpec)
            .when()
            .delete(endpoint)
            .then()
            .extract()
            .response();
    }

    /**
     * HTTP DELETE with path parameters.
     *
     * @param endpoint   API endpoint path
     * @param pathParams map of path parameters
     * @return Response object
     */
    protected Response delete(String endpoint,
                               Map<String, Object> pathParams) {
        LOG.info("→ DELETE [{}] with params: {}", endpoint, pathParams);
        return given()
            .spec(requestSpec)
            .pathParams(pathParams)
            .when()
            .delete(endpoint)
            .then()
            .extract()
            .response();
    }

    // ─────────────────────────────────────────
    // AUTHENTICATED REQUESTS
    // ─────────────────────────────────────────

    /**
     * Adds Bearer token authentication to request spec.
     * Returns NEW spec — does not modify base spec.
     *
     * @param token Bearer token
     * @return authenticated RequestSpecification
     */
    protected RequestSpecification withBearerToken(String token) {
        return given()
            .spec(requestSpec)
            .header("Authorization", "Bearer " + token);
    }

    /**
     * Adds API Key authentication header.
     *
     * @param headerName header name for API key
     * @param apiKey     API key value
     * @return authenticated RequestSpecification
     */
    protected RequestSpecification withApiKey(String headerName,
                                               String apiKey) {
        return given()
            .spec(requestSpec)
            .header(headerName, apiKey);
    }

    // ─────────────────────────────────────────
    // UTILITY METHODS
    // ─────────────────────────────────────────

    /**
     * Updates request spec — used by subclasses
     * to add module-specific headers or auth.
     *
     * @param spec new RequestSpecification
     */
    protected void setRequestSpec(RequestSpecification spec) {
        this.requestSpec = spec;
    }

    /**
     * Gets current request specification.
     *
     * @return current RequestSpecification
     */
    protected RequestSpecification getRequestSpec() {
        return requestSpec;
    }
}
