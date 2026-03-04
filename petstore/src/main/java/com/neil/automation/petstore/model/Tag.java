package com.neil.automation.petstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag — Represents a descriptive tag for a pet.
 *
 * <p>Maps to PetStore v3 Tag schema:
 * <pre>
 * {
 *   "id":   1,
 *   "name": "friendly"
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
public class Tag {

    // ── Common tag name constants ──
    public static final String FRIENDLY    = "friendly";
    public static final String VACCINATED  = "vaccinated";
    public static final String TRAINED     = "trained";
    public static final String NEUTERED    = "neutered";
    public static final String HYPOALLERGENIC = "hypoallergenic";

    // ─────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────

    /**
     * Unique tag identifier.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Tag label.
     * Examples: friendly, vaccinated, trained
     */
    @JsonProperty("name")
    private String name;

    // ─────────────────────────────────────────
    // STATIC FACTORIES
    // ─────────────────────────────────────────

    /**
     * Creates a "friendly" tag.
     *
     * @return Tag with name "friendly"
     */
    public static Tag friendly() {
        return Tag.builder()
            .id(1L)
            .name(FRIENDLY)
            .build();
    }

    /**
     * Creates a "vaccinated" tag.
     *
     * @return Tag with name "vaccinated"
     */
    public static Tag vaccinated() {
        return Tag.builder()
            .id(2L)
            .name(VACCINATED)
            .build();
    }

    /**
     * Creates a "trained" tag.
     *
     * @return Tag with name "trained"
     */
    public static Tag trained() {
        return Tag.builder()
            .id(3L)
            .name(TRAINED)
            .build();
    }

    /**
     * Creates a custom tag.
     *
     * @param id   tag ID
     * @param name tag label
     * @return custom Tag
     */
    public static Tag of(Long id, String name) {
        return Tag.builder()
            .id(id)
            .name(name)
            .build();
    }
}
