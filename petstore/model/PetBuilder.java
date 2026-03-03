package com.neil.automation.petstore.builder;

import com.github.javafaker.Faker;
import com.neil.automation.petstore.model.Category;
import com.neil.automation.petstore.model.Pet;
import com.neil.automation.petstore.model.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PetBuilder — FAANG-grade test data factory for Pet objects.
 *
 * <p>Provides fluent, readable pet creation for test scenarios.
 * Uses JavaFaker for realistic randomized test data.
 *
 * <p>Design Pattern: Builder + Factory Method
 *
 * <p>Usage:
 * <pre>
 *   // Quick valid pet
 *   Pet pet = PetBuilder.aValidPet().build();
 *
 *   // Custom pet
 *   Pet pet = PetBuilder.aPet()
 *       .withName("Buddy")
 *       .withStatus("available")
 *       .withCategory(Category.dogs())
 *       .withTags(Tag.friendly(), Tag.vaccinated())
 *       .build();
 *
 *   // Invalid pet for negative testing
 *   Pet pet = PetBuilder.anInvalidPet().build();
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public class PetBuilder {

    private static final Logger LOG =
        LogManager.getLogger(PetBuilder.class);

    // ── JavaFaker for realistic test data ──
    private static final Faker FAKER = new Faker();

    // ── Pet under construction ──
    private Long         id;
    private String       name;
    private String       status;
    private Category     category;
    private List<String> photoUrls;
    private List<Tag>    tags;

    // ─────────────────────────────────────────
    // PRIVATE CONSTRUCTOR
    // ─────────────────────────────────────────

    private PetBuilder() {
        // Use static factory methods
    }

    // ─────────────────────────────────────────
    // STATIC FACTORY METHODS
    // ─────────────────────────────────────────

    /**
     * Entry point for custom pet construction.
     *
     * @return fresh PetBuilder instance
     */
    public static PetBuilder aPet() {
        return new PetBuilder();
    }

    /**
     * Creates a fully valid pet with randomized data.
     * Ready to POST to PetStore API immediately.
     *
     * @return PetBuilder pre-populated with valid data
     */
    public static PetBuilder aValidPet() {
        String petName = FAKER.dog().name();
        LOG.debug("🐾 Building valid pet: [{}]", petName);

        return new PetBuilder()
            .withName(petName)
            .withStatus(Pet.Status.available.name())
            .withCategory(Category.dogs())
            .withPhotoUrls(generatePhotoUrl(petName))
            .withTags(Tag.friendly(), Tag.vaccinated());
    }

    /**
     * Creates a valid available pet.
     *
     * @return PetBuilder with status "available"
     */
    public static PetBuilder anAvailablePet() {
        return aValidPet()
            .withStatus(Pet.Status.available.name());
    }

    /**
     * Creates a valid pending pet.
     *
     * @return PetBuilder with status "pending"
     */
    public static PetBuilder aPendingPet() {
        return aValidPet()
            .withStatus(Pet.Status.pending.name());
    }

    /**
     * Creates a valid sold pet.
     *
     * @return PetBuilder with status "sold"
     */
    public static PetBuilder aSoldPet() {
        return aValidPet()
            .withStatus(Pet.Status.sold.name());
    }

    /**
     * Creates a pet with INVALID data for negative testing.
     * Missing required fields intentionally.
     *
     * @return PetBuilder with missing required fields
     */
    public static PetBuilder anInvalidPet() {
        LOG.debug("🐾 Building INVALID pet for negative testing");
        return new PetBuilder()
            .withName(null)          // name is required!
            .withPhotoUrls()         // photoUrls is required!
            .withStatus("INVALID_STATUS");
    }

    /**
     * Creates a pet with an existing ID for update tests.
     *
     * @param existingId ID of pet to update
     * @return PetBuilder with specified ID
     */
    public static PetBuilder anUpdatedPet(Long existingId) {
        return aValidPet()
            .withId(existingId)
            .withName("Updated_" + FAKER.dog().name())
            .withStatus(Pet.Status.sold.name());
    }

    /**
     * Creates a cat pet for category testing.
     *
     * @return PetBuilder with cat category
     */
    public static PetBuilder aCatPet() {
        String catName = FAKER.cat().name();
        return new PetBuilder()
            .withName(catName)
            .withStatus(Pet.Status.available.name())
            .withCategory(Category.cats())
            .withPhotoUrls(generatePhotoUrl(catName))
            .withTags(Tag.friendly());
    }

    // ─────────────────────────────────────────
    // FLUENT BUILDER METHODS
    // ─────────────────────────────────────────

    /**
     * Sets pet ID.
     *
     * @param id pet identifier
     * @return this builder
     */
    public PetBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets pet name.
     *
     * @param name pet name
     * @return this builder
     */
    public PetBuilder withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets pet status.
     *
     * @param status available | pending | sold
     * @return this builder
     */
    public PetBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Sets pet category.
     *
     * @param category Category object
     * @return this builder
     */
    public PetBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }

    /**
     * Sets pet photo URLs (varargs).
     *
     * @param urls one or more photo URL strings
     * @return this builder
     */
    public PetBuilder withPhotoUrls(String... urls) {
        this.photoUrls = urls.length > 0
            ? Arrays.asList(urls)
            : Collections.emptyList();
        return this;
    }

    /**
     * Sets pet photo URLs from list.
     *
     * @param urls list of photo URLs
     * @return this builder
     */
    public PetBuilder withPhotoUrls(List<String> urls) {
        this.photoUrls = urls;
        return this;
    }

    /**
     * Sets pet tags (varargs).
     *
     * @param tags one or more Tag objects
     * @return this builder
     */
    public PetBuilder withTags(Tag... tags) {
        this.tags = Arrays.asList(tags);
        return this;
    }

    // ─────────────────────────────────────────
    // BUILD
    // ─────────────────────────────────────────

    /**
     * Builds and returns the final Pet object.
     *
     * @return configured Pet instance
     */
    public Pet build() {
        Pet pet = Pet.builder()
            .id(id)
            .name(name)
            .status(status)
            .category(category)
            .photoUrls(photoUrls)
            .tags(tags)
            .build();

        LOG.debug("✅ Pet built: name=[{}] status=[{}]",
            pet.getName(), pet.getStatus());

        return pet;
    }

    // ─────────────────────────────────────────
    // PRIVATE UTILITIES
    // ─────────────────────────────────────────

    /**
     * Generates a realistic pet photo URL.
     *
     * @param petName pet name to include in URL
     * @return formatted photo URL string
     */
    private static String generatePhotoUrl(String petName) {
        String sanitized = petName
            .toLowerCase()
            .replaceAll("\\s+", "-");
        return String.format(
            "https://petphotos.example.com/%s/%s.jpg",
            sanitized,
            FAKER.number().digits(8)
        );
    }
}
