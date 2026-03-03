package com.neil.automation.petstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Category — Represents a pet category.
 *
 * <p>Maps to PetStore v3 Category schema:
 * <pre>
 * {
 *   "id":   1,
 *   "name": "Dogs"
 * }
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Category {

    // ── Predefined category constants ──
    public static final String DOGS   = "Dogs";
    public static final String CATS   = "Cats";
    public static final String BIRDS  = "Birds";
    public static final String FISH   = "Fish";
    public static final String RABBIT = "Rabbits";

    // ─────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────

    /**
     * Unique category identifier.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Category name.
     * Examples: Dogs, Cats, Birds, Fish
     */
    @JsonProperty("name")
    private String name;

    // ─────────────────────────────────────────
    // STATIC FACTORIES — Convenience builders
    // ─────────────────────────────────────────

    /**
     * Creates a Dogs category.
     *
     * @return Category for Dogs
     */
    public static Category dogs() {
        return Category.builder()
            .id(1L)
            .name(DOGS)
            .build();
    }

    /**
     * Creates a Cats category.
     *
     * @return Category for Cats
     */
    public static Category cats() {
        return Category.builder()
            .id(2L)
            .name(CATS)
            .build();
    }

    /**
     * Creates a Birds category.
     *
     * @return Category for Birds
     */
    public static Category birds() {
        return Category.builder()
            .id(3L)
            .name(BIRDS)
            .build();
    }

    /**
     * Creates a custom category.
     *
     * @param id   category ID
     * @param name category name
     * @return custom Category
     */
    public static Category of(Long id, String name) {
        return Category.builder()
            .id(id)
            .name(name)
            .build();
    }
}
