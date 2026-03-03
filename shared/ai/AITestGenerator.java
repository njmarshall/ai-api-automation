package com.neil.automation.ai;

import com.neil.automation.config.ConfigLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AITestGenerator — OpenAI-powered test case generator.
 *
 * <p>FAANG-grade AI integration that automatically:
 * - Generates test cases from API endpoint descriptions
 * - Identifies edge cases and boundary conditions
 * - Creates negative test scenarios
 * - Suggests invalid data combinations
 * - Recommends security test scenarios
 *
 * <p>Usage:
 * <pre>
 *   AITestGenerator ai = AITestGenerator.getInstance();
 *
 *   // Generate test cases for an endpoint
 *   List&lt;TestCase&gt; cases = ai.generateTestCases(
 *       "POST /pet",
 *       "Creates a new pet with name, status and photoUrls"
 *   );
 *
 *   // Generate negative tests only
 *   List&lt;TestCase&gt; negative = ai.generateNegativeTests(
 *       "GET /pet/{petId}",
 *       "Retrieves a pet by ID"
 *   );
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public class AITestGenerator {

    private static final Logger LOG =
        LogManager.getLogger(AITestGenerator.class);

    // ── Singleton instance ──
    private static AITestGenerator instance;

    // ── Dependencies ──
    private final ConfigLoader   config;
    private final HttpClient     httpClient;
    private final ObjectMapper   objectMapper;
    private final PromptBuilder  promptBuilder;

    // ── OpenAI API constants ──
    private static final String OPENAI_API_URL =
        "https://api.openai.com/v1/chat/completions";
    private static final String CONTENT_TYPE   =
        "application/json";
    private static final int    TIMEOUT_SECONDS = 30;

    // ─────────────────────────────────────────
    // INNER CLASS: TestCase
    // ─────────────────────────────────────────

    /**
     * Represents a single AI-generated test case.
     */
    public static class TestCase {

        private final String name;
        private final String description;
        private final String type;          // positive/negative/edge/security
        private final String endpoint;
        private final String method;
        private final String requestBody;
        private final int    expectedStatus;
        private final String expectedBehavior;
        private final String priority;      // HIGH/MEDIUM/LOW

        public TestCase(
                String name,
                String description,
                String type,
                String endpoint,
                String method,
                String requestBody,
                int    expectedStatus,
                String expectedBehavior,
                String priority) {
            this.name             = name;
            this.description      = description;
            this.type             = type;
            this.endpoint         = endpoint;
            this.method           = method;
            this.requestBody      = requestBody;
            this.expectedStatus   = expectedStatus;
            this.expectedBehavior = expectedBehavior;
            this.priority         = priority;
        }

        // ── Getters ──
        public String getName()             { return name;             }
        public String getDescription()      { return description;      }
        public String getType()             { return type;             }
        public String getEndpoint()         { return endpoint;         }
        public String getMethod()           { return method;           }
        public String getRequestBody()      { return requestBody;      }
        public int    getExpectedStatus()   { return expectedStatus;   }
        public String getExpectedBehavior() { return expectedBehavior; }
        public String getPriority()         { return priority;         }

        @Override
        public String toString() {
            return String.format(
                "[%s] %s | %s %s | Status: %d | Priority: %s",
                type.toUpperCase(),
                name,
                method,
                endpoint,
                expectedStatus,
                priority
            );
        }
    }

    // ─────────────────────────────────────────
    // INNER CLASS: PromptBuilder
    // ─────────────────────────────────────────

    /**
     * PromptBuilder — Builds optimized prompts for OpenAI.
     *
     * <p>Prompt engineering is critical for quality output.
     * These prompts are tuned specifically for API test generation.
     */
    public static class PromptBuilder {

        // ── System prompt — defines AI behavior ──
        private static final String SYSTEM_PROMPT = """
            You are an expert FAANG-level QA automation engineer
            with 15+ years of experience in API test automation.
            
            Your role is to generate comprehensive test cases for
            REST API endpoints following FAANG testing standards.
            
            For each test case, always return a JSON array with
            this exact structure:
            [
              {
                "name": "test case name",
                "description": "what this test validates",
                "type": "positive|negative|edge|security",
                "endpoint": "/endpoint/path",
                "method": "GET|POST|PUT|DELETE|PATCH",
                "requestBody": "JSON string or null",
                "expectedStatus": 200,
                "expectedBehavior": "what should happen",
                "priority": "HIGH|MEDIUM|LOW"
              }
            ]
            
            Always include:
            - Happy path (positive) tests
            - Negative tests (invalid data, missing fields)
            - Edge cases (boundary values, empty strings)
            - Security tests (SQL injection, XSS attempts)
            - Priority based on business impact
            
            Return ONLY valid JSON array. No explanation text.
            """;

        /**
         * Builds prompt for full test suite generation.
         *
         * @param httpMethod  HTTP method (GET/POST/PUT/DELETE)
         * @param endpoint    API endpoint path
         * @param description endpoint description
         * @param schema      request/response schema (optional)
         * @return formatted prompt string
         */
        public String buildFullSuitePrompt(
                String httpMethod,
                String endpoint,
                String description,
                String schema) {

            return String.format("""
                Generate a comprehensive test suite for this API endpoint:
                
                HTTP Method: %s
                Endpoint: %s
                Description: %s
                Schema: %s
                
                Generate at least:
                - 3 positive test cases
                - 3 negative test cases  
                - 2 edge case tests
                - 1 security test
                
                Focus on real-world scenarios a FAANG engineer would test.
                """,
                httpMethod,
                endpoint,
                description,
                schema != null ? schema : "Not provided"
            );
        }

        /**
         * Builds prompt for negative tests only.
         *
         * @param httpMethod  HTTP method
         * @param endpoint    API endpoint
         * @param description endpoint description
         * @return formatted prompt string
         */
        public String buildNegativeTestPrompt(
                String httpMethod,
                String endpoint,
                String description) {

            return String.format("""
                Generate ONLY negative test cases for this API endpoint:
                
                HTTP Method: %s
                Endpoint: %s
                Description: %s
                
                Focus on:
                - Missing required fields
                - Invalid data types
                - Out-of-range values
                - Malformed JSON
                - Invalid authentication
                - Non-existent resource IDs
                
                Generate at least 5 negative test cases.
                """,
                httpMethod,
                endpoint,
                description
            );
        }

        /**
         * Builds prompt for edge case tests only.
         *
         * @param httpMethod  HTTP method
         * @param endpoint    API endpoint
         * @param description endpoint description
         * @return formatted prompt string
         */
        public String buildEdgeCasePrompt(
                String httpMethod,
                String endpoint,
                String description) {

            return String.format("""
                Generate ONLY edge case test scenarios for:
                
                HTTP Method: %s
                Endpoint: %s
                Description: %s
                
                Focus on:
                - Boundary values (min/max lengths)
                - Empty strings vs null values
                - Special characters and Unicode
                - Very large payloads
                - Concurrent request scenarios
                - Whitespace-only fields
                - Maximum integer values
                
                Generate at least 4 edge case tests.
                """,
                httpMethod,
                endpoint,
                description
            );
        }

        /**
         * Builds prompt for security test cases.
         *
         * @param httpMethod  HTTP method
         * @param endpoint    API endpoint
         * @param description endpoint description
         * @return formatted prompt string
         */
        public String buildSecurityTestPrompt(
                String httpMethod,
                String endpoint,
                String description) {

            return String.format("""
                Generate security-focused test cases for:
                
                HTTP Method: %s
                Endpoint: %s
                Description: %s
                
                Focus on:
                - SQL injection attempts
                - XSS payload injection
                - Authentication bypass
                - Authorization escalation
                - Rate limiting validation
                - Sensitive data exposure
                - IDOR (Insecure Direct Object Reference)
                
                Generate at least 4 security test cases.
                """,
                httpMethod,
                endpoint,
                description
            );
        }

        /**
         * Returns the system prompt for OpenAI.
         *
         * @return system prompt string
         */
        public String getSystemPrompt() {
            return SYSTEM_PROMPT;
        }
    }

    // ─────────────────────────────────────────
    // PRIVATE CONSTRUCTOR (Singleton)
    // ─────────────────────────────────────────

    private AITestGenerator() {
        this.config        = ConfigLoader.getInstance();
        this.objectMapper  = new ObjectMapper();
        this.promptBuilder = new PromptBuilder();
        this.httpClient    = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

        LOG.info("✅ AITestGenerator initialized");
        LOG.info("✅ AI enabled: [{}]", config.isAiEnabled());
    }

    // ─────────────────────────────────────────
    // SINGLETON ACCESSOR
    // ─────────────────────────────────────────

    /**
     * Returns singleton instance of AITestGenerator.
     *
     * @return AITestGenerator instance
     */
    public static synchronized AITestGenerator getInstance() {
        if (instance == null) {
            instance = new AITestGenerator();
        }
        return instance;
    }

    // ─────────────────────────────────────────
    // PUBLIC API — TEST GENERATION METHODS
    // ─────────────────────────────────────────

    /**
     * Generates a full test suite for an API endpoint.
     * Includes positive, negative, edge, and security tests.
     *
     * <p>Returns empty list if AI is disabled in config.
     *
     * @param httpMethod  HTTP method (GET/POST/PUT/DELETE)
     * @param endpoint    API endpoint path
     * @param description human-readable endpoint description
     * @return list of generated TestCase objects
     */
    public List<TestCase> generateTestCases(
            String httpMethod,
            String endpoint,
            String description) {

        return generateTestCases(
            httpMethod, endpoint, description, null
        );
    }

    /**
     * Generates a full test suite with schema context.
     *
     * @param httpMethod  HTTP method
     * @param endpoint    API endpoint path
     * @param description endpoint description
     * @param schema      request/response schema for context
     * @return list of generated TestCase objects
     */
    public List<TestCase> generateTestCases(
            String httpMethod,
            String endpoint,
            String description,
            String schema) {

        if (!config.isAiEnabled()) {
            LOG.info("⏭️  AI disabled — skipping test generation for [{}]",
                endpoint);
            return new ArrayList<>();
        }

        LOG.info("🤖 Generating full test suite for: [{} {}]",
            httpMethod, endpoint);

        String prompt = promptBuilder.buildFullSuitePrompt(
            httpMethod, endpoint, description, schema
        );

        return callOpenAI(prompt);
    }

    /**
     * Generates negative test cases only.
     *
     * @param httpMethod  HTTP method
     * @param endpoint    API endpoint path
     * @param description endpoint description
     * @return list of negative TestCase objects
     */
    public List<TestCase> generateNegativeTests(
            String httpMethod,
            String endpoint,
            String description) {

        if (!config.isAiEnabled()) {
            LOG.info("⏭️  AI disabled — skipping negative test generation");
            return new ArrayList<>();
        }

        LOG.info("🤖 Generating negative tests for: [{} {}]",
            httpMethod, endpoint);

        String prompt = promptBuilder.buildNegativeTestPrompt(
            httpMethod, endpoint, description
        );

        return callOpenAI(prompt);
    }

    /**
     * Generates edge case tests only.
     *
     * @param httpMethod  HTTP method
     * @param endpoint    API endpoint path
     * @param description endpoint description
     * @return list of edge case TestCase objects
     */
    public List<TestCase> generateEdgeCaseTests(
            String httpMethod,
            String endpoint,
            String description) {

        if (!config.isAiEnabled()) {
            LOG.info("⏭️  AI disabled — skipping edge case generation");
            return new ArrayList<>();
        }

        LOG.info("🤖 Generating edge case tests for: [{} {}]",
            httpMethod, endpoint);

        String prompt = promptBuilder.buildEdgeCasePrompt(
            httpMethod, endpoint, description
        );

        return callOpenAI(prompt);
    }

    /**
     * Generates security test cases only.
     *
     * @param httpMethod  HTTP method
     * @param endpoint    API endpoint path
     * @param description endpoint description
     * @return list of security TestCase objects
     */
    public List<TestCase> generateSecurityTests(
            String httpMethod,
            String endpoint,
            String description) {

        if (!config.isAiEnabled()) {
            LOG.info("⏭️  AI disabled — skipping security test generation");
            return new ArrayList<>();
        }

        LOG.info("🤖 Generating security tests for: [{} {}]",
            httpMethod, endpoint);

        String prompt = promptBuilder.buildSecurityTestPrompt(
            httpMethod, endpoint, description
        );

        return callOpenAI(prompt);
    }

    /**
     * Logs all generated test cases in a readable format.
     * Useful for debugging and review before execution.
     *
     * @param testCases list of generated test cases
     * @param endpoint  endpoint they were generated for
     */
    public void logGeneratedTestCases(
            List<TestCase> testCases,
            String endpoint) {

        if (testCases.isEmpty()) {
            LOG.info("⚠️  No test cases generated for: [{}]", endpoint);
            return;
        }

        LOG.info("═══════════════════════════════════════");
        LOG.info("🤖 AI Generated Test Cases for: [{}]", endpoint);
        LOG.info("   Total: [{}] test cases", testCases.size());
        LOG.info("═══════════════════════════════════════");

        // Group by type
        testCases.stream()
            .collect(java.util.stream.Collectors
                .groupingBy(TestCase::getType))
            .forEach((type, cases) -> {
                LOG.info("── {} ({}) ──", type.toUpperCase(), cases.size());
                cases.forEach(tc ->
                    LOG.info("  ▸ [{}] {} → Status: {}",
                        tc.getPriority(),
                        tc.getName(),
                        tc.getExpectedStatus()
                    )
                );
            });

        LOG.info("═══════════════════════════════════════");
    }

    // ─────────────────────────────────────────
    // PRIVATE — OPENAI API CALL
    // ─────────────────────────────────────────

    /**
     * Makes HTTP call to OpenAI Chat Completions API.
     * Parses JSON response into TestCase objects.
     *
     * @param userPrompt the user prompt to send
     * @return parsed list of TestCase objects
     */
    private List<TestCase> callOpenAI(String userPrompt) {
        List<TestCase> testCases = new ArrayList<>();

        try {
            // ── Build request payload ──
            String apiKey = config.get("openai.api.key");
            String model  = config.get("openai.model", "gpt-4");
            int maxTokens = config.getInt("openai.max.tokens", 2000);

            String requestBody = objectMapper
                .writeValueAsString(new java.util.HashMap<>() {{
                    put("model", model);
                    put("max_tokens", maxTokens);
                    put("temperature", 0.3);
                    put("messages", new java.util.ArrayList<>() {{
                        add(new java.util.HashMap<>() {{
                            put("role", "system");
                            put("content",
                                promptBuilder.getSystemPrompt());
                        }});
                        add(new java.util.HashMap<>() {{
                            put("role", "user");
                            put("content", userPrompt);
                        }});
                    }});
                }});

            // ── Build HTTP request ──
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .header("Content-Type", CONTENT_TYPE)
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            LOG.info("🌐 Calling OpenAI API: model=[{}]", model);

            // ── Execute request ──
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            LOG.info("✅ OpenAI response status: [{}]",
                response.statusCode());

            if (response.statusCode() != 200) {
                LOG.error("❌ OpenAI API error: [{}] — {}",
                    response.statusCode(), response.body());
                return testCases;
            }

            // ── Parse response ──
            testCases = parseOpenAIResponse(response.body());

            LOG.info("✅ AI generated [{}] test cases", testCases.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("❌ OpenAI call interrupted", e);
        } catch (Exception e) {
            LOG.error("❌ Failed to generate test cases via AI", e);
        }

        return testCases;
    }

    // ─────────────────────────────────────────
    // PRIVATE — RESPONSE PARSER
    // ─────────────────────────────────────────

    /**
     * Parses OpenAI API response JSON into TestCase objects.
     *
     * @param responseBody raw OpenAI response JSON string
     * @return list of parsed TestCase objects
     */
    private List<TestCase> parseOpenAIResponse(String responseBody) {
        List<TestCase> testCases = new ArrayList<>();

        try {
            // ── Extract content from OpenAI response ──
            JsonNode root    = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");

            if (choices.isEmpty()) {
                LOG.warn("⚠️  No choices in OpenAI response");
                return testCases;
            }

            String content = choices
                .get(0)
                .path("message")
                .path("content")
                .asText();

            LOG.debug("📄 Raw AI content: [{}]", content);

            // ── Parse the JSON array in content ──
            // Strip any markdown code blocks if present
            content = content
                .replaceAll("son", "")
                .replaceAll(" ", "")
                .trim();

            // ── Parse JSON array ──
            ArrayNode testCaseArray =
                (ArrayNode) objectMapper.readTree(content);

            LOG.info("📋 Parsing [{}] AI-generated test cases",
                testCaseArray.size());

            // ── Map each JSON node to TestCase object ──
            for (JsonNode node : testCaseArray) {

                // Handle requestBody — can be null or JSON object
                String requestBody = null;
                JsonNode requestBodyNode = node.path("requestBody");
                if (!requestBodyNode.isNull()
                        && !requestBodyNode.isMissingNode()) {
                    requestBody = requestBodyNode.isTextual()
                        ? requestBodyNode.asText()
                        : requestBodyNode.toString();
                }

                TestCase tc = new TestCase(
                    node.path("name")
                        .asText("Unnamed Test"),
                    node.path("description")
                        .asText("No description"),
                    node.path("type")
                        .asText("positive"),
                    node.path("endpoint")
                        .asText(""),
                    node.path("method")
                        .asText("GET"),
                    requestBody,
                    node.path("expectedStatus")
                        .asInt(200),
                    node.path("expectedBehavior")
                        .asText(""),
                    node.path("priority")
                        .asText("MEDIUM")
                );

                testCases.add(tc);
                LOG.debug("  ✔ Parsed: {}", tc);
            }

        } catch (Exception e) {
            LOG.error("❌ Failed to parse OpenAI response: {}",
                e.getMessage(), e);
        }

        return testCases;
    }

    // ═════════════════════════════════════════
    // PACKAGE-PRIVATE — TEST SUPPORT
    // ═════════════════════════════════════════

    /**
     * Resets singleton instance.
     * Used in unit tests ONLY — never call in production!
     */
    static synchronized void reset() {
        instance = null;
    }
}


            ArrayNode testCaseArray =
                (ArrayNode) objectMapper.readTree(content);

            // ── Map each JSON object to TestCase ──
            for (JsonNode node : testCaseArray) {
                TestCase tc = new TestCase(
                    node.path("name").asText("Unnamed Test"),
                    node.path("description").asText(""),
                    node.path("type").asText("positive"),
                    node.path("endpoint").asText(""),
                    node.path("method").asText("GET"),
                    node.path("requestBody").isNull()
                        ? null
                        : node.path("requestBody").asText(),
                    node.path("expectedStatus").asInt(200),
                    node.path("expectedBehavior").asText(""),
                    node.path("priority").asText("MEDIUM")
                );

                testCases.add(tc);
                LOG.debug("  ▸ Parsed: {}", tc);
            }

        } catch (Exception e) {
            LOG.error("❌ Failed to parse OpenAI response", e);
        }

        return testCases;
    }
}
