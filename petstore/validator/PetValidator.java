package com.neil.automation.petstore.validator;

import com.neil.automation.petstore.model.Pet;
import com.neil.automation.validator.ResponseValidator;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

/**
 * PetValidator — Pet-specific response validation engine.
 *
 * <p>Extends the shared ResponseValidator with pet-domain
 * validations. Follows the same fluent chain pattern.
 *
 * <p>Usage:
 * <pre>
 *   PetValidator.validate(response)
 *       .statusCode(200)
 *       .meetsDefaultSLA()
 *       .petNameEquals("Buddy")
 *       .petStatusEquals("available")
 *       .petHasPhotos()
 *       .petHasId()
 *       .assertAll();
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public class PetValidator extends ResponseValidator {

    private static final Logger LOG =
        LogManager.getLogger(PetValidator.class);

    // ── Soft assertions for pet-specific checks ──
    private final SoftAssertions softly;

    // ── Raw response reference ──
    private final Response response;

    // ─────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────

    /**
     * Creates PetValidator wrapping a ResponseValidator.
     *
     * @param response RestAssured Response to validate
     */
    public PetValidator(Response response) {
        super(response);
        this.response = response;
        this.softly   = new SoftAssertions();
        LOG.info("🐾 PetValidator created");
    }

    // ─────────────────────────────────────────
    // FACTORY METHOD
    // ─────────────────────────────────────────

    /**
     * Static factory — fluent entry point.
     *
     * <pre>
     *   PetValidator.validate(response)
     *       .statusCode(200)
     *       .petNameEquals("Buddy")
     *       .assertAll();
     * </pre>
     *
     * @param response response to validate
     * @return PetValidator instance
     */
    public static PetValidator validate(Response response) {
        return new PetValidator(response);
    }

    // ─────────────────────────────────────────
    // PET-SPECIFIC VALIDATIONS
    // ─────────────────────────────────────────

    /**
     * Validates the pet name in response.
     *
     * @param expectedName expected pet name
     * @return this validator (fluent chain)
     */
    @Step("Validate pet name equals [{expectedName}]")
    public PetValidator petNameEquals(String expectedName) {
        String actualName = response.jsonPath().getString("name");
        LOG.info("✔ Pet name: expected=[{}] actual=[{}]",
            expectedName, actualName);

        softly.assertThat(actualName)
            .as("Pet name")
            .isEqualTo(expectedName);

        return this;
    }

    /**
     * Validates the pet status in response.
     *
     * @param expectedStatus available | pending | sold
     * @return this validator (fluent chain)
     */
    @Step("Validate pet status equals [{expectedStatus}]")
    public PetValidator petStatusEquals(String expectedStatus) {
        String actualStatus = response.jsonPath().getString("status");
        LOG.info("✔ Pet status: expected=[{}] actual=[{}]",
            expectedStatus, actualStatus);

        softly.assertThat(actualStatus)
            .as("Pet status")
            .isEqualTo(expectedStatus);

        return this;
    }

    /**
     * Validates pet has a non-null ID assigned by server.
     *
     * @return this validator (fluent chain)
     */
    @Step("Validate pet has ID assigned")
    public PetValidator petHasId() {
        Long id = response.jsonPath().getLong("id");
        LOG.info("✔ Pet ID: [{}]", id);

        softly.assertThat(id)
            .as("Pet ID should be assigned by server")
            .isNotNull()
            .isPositive();

        return this;
    }

    /**
     * Validates pet ID matches expected value.
     *
     * @param expectedId expected pet ID
     * @return this validator (fluent chain)
     */
    @Step("Validate pet ID equals [{expectedId}]")
    public PetValidator petIdEquals(Long expectedId) {
        Long actualId = response.jsonPath().getLong("id");
        LOG.info("✔ Pet ID: expected=[{}] actual=[{}]",
            expectedId, actualId);

        softly.assertThat(actualId)
            .as("Pet ID")
            .isEqualTo(expectedId);

        return this;
    }

    /**
     * Validates pet has at least one photo URL.
     *
     * @return this validator (fluent chain)
     */
    @Step("Validate pet has photo URLs")
    public PetValidator petHasPhotos() {
        java.util.List<?> photoUrls =
            response.jsonPath().getList("photoUrls");

        LOG.info("✔ Pet photoUrls count: [{}]",
            photoUrls != null ? photoUrls.size() : 0);

        softly.assertThat(photoUrls)
            .as("Pet photoUrls should not be empty")
            .isNotNull()
            .isNotEmpty();

        return this;
    }

    /**
     * Validates pet has a category assigned.
     *
     * @return this validator (fluent chain)
     */
    @Step("Validate pet has category")
    public PetValidator petHasCategory() {
        Object category = response.jsonPath().get("category");
        LOG.info("✔ Pet category: [{}]", category);

        softly.assertThat(category)
            .as("Pet category should not be null")
            .isNotNull();

        return this;
    }

    /**
     * Validates pet category name.
     *
     * @param expectedCategoryName expected category name
     * @return this validator (fluent chain)
     */
    @Step("Validate pet category name equals [{expectedCategoryName}]")
    public PetValidator petCategoryNameEquals(
            String expectedCategoryName) {
        String actualCategory =
            response.jsonPath().getString("category.name");

        LOG.info("✔ Pet category: expected=[{}] actual=[{}]",
            expectedCategoryName, actualCategory);

        softly.assertThat(actualCategory)
            .as("Pet category name")
            .isEqualTo(expectedCategoryName);

        return this;
    }

    /**
     * Validates pet has tags assigned.
     *
     * @return this validator (fluent chain)
     */
    @Step("Validate pet has tags")
    public PetValidator petHasTags() {
        java.util.List<?> tags =
            response.jsonPath().getList("tags");

        LOG.info("✔ Pet tags count: [{}]",
            tags != null ? tags.size() : 0);

        softly.assertThat(tags)
            .as("Pet tags should not be empty")
            .isNotNull()
            .isNotEmpty();

        return this;
    }

    /**
     * Validates full Pet object matches expected Pet model.
     * Comprehensive comparison of all fields.
     *
     * @param expectedPet expected Pet object
     * @return this validator (fluent chain)
     */
    @Step("Validate full pet object matches expected")
    public PetValidator petMatchesExpected(Pet expectedPet) {
        LOG.info("✔ Full pet validation against expected model");

        if (expectedPet.getName() != null) {
            petNameEquals(expectedPet.getName());
        }
        if (expectedPet.getStatus() != null) {
            petStatusEquals(expectedPet.getStatus());
        }
        if (expectedPet.getCategory() != null
                && expectedPet.getCategory().getName() != null) {
            petCategoryNameEquals(
                expectedPet.getCategory().getName()
            );
        }

        return this;
    }

    /**
     * Validates pet status is one of the valid enum values.
     *
     * @return this validator (fluent chain)
     */
    @Step("Validate pet status is a valid enum value")
    public PetValidator petStatusIsValid() {
        String status = response.jsonPath().getString("status");

        softly.assertThat(status)
            .as("Pet status must be valid enum")
            .isIn("available", "pending", "sold");

        return this;
    }

    /**
     * Validates pet name is not null or blank.
     *
     * @return this validator (fluent chain)
     */
    @Step("Validate pet name is not blank")
    public PetValidator petNameNotBlank() {
        String name = response.jsonPath().getString("name");

        softly.assertThat(name)
            .as("Pet name should not be blank")
            .isNotNull()
            .isNotBlank();

        return this;
    }

    // ─────────────────────────────────────────
    // OVERRIDE assertAll — collect ALL failures
    // ─────────────────────────────────────────

    /**
     * Executes all pet-specific AND base soft assertions.
     * Always call at the END of every validation chain!
     */
    @Override
    @Step("Assert all pet validations")
    public void assertAll() {
        LOG.info("🔍 Executing all PetValidator assertions...");
        // Execute pet-specific assertions
        softly.assertAll();
        // Execute base ResponseValidator assertions
        super.assertAll();
        LOG.info("✅ All pet assertions PASSED!");
    }
}
