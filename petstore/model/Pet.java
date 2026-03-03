package com.neil.automation.petstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pet — Domain model representing a Pet resource.
 *
 * <p>Maps directly to PetStore v3 API Pet schema:
 * <pre>
 * {
 *   "id":        123,
 *   "name":      "doggie",
 *   "status":    "available",
 *   "category":  { "id": 1, "name": "Dogs" },
 *   "tags":      [{ "id": 1, "name": "friendly" }],
 *   "photoUrls": ["https://example.com/photo.jpg"]
 * }
 * </pre>
 *
 * <p>Lombok annotations eliminate boilerplate:
 * - @Data         → getters, setters, equals, hashCode, toString
 * - @Builder      → fluent builder pattern
 * - @NoArgsConstructor → required for Jackson deserialization
 * - @AllArgsConstructor → required for @Builder
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
public class Pet {

    // ── Pet Status Enum ──
    public enum Status {
        available,
        pending,
        sold
    }

    // ─────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────

    /**
     * Unique pet identifier.
     * Optional on creation — server assigns ID.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Pet name — REQUIRED field.
     * Example: "doggie", "kitty"
     */
    @JsonProperty("name")
    private String name;

    /**
     * Pet availability status.
     * Values: available | pending | sold
     */
    @JsonProperty("status")
    private String status;

    /**
     * Pet category (e.g., Dogs, Cats, Birds).
     */
    @JsonProperty("category")
    private Category category;

    /**
     * List of pet photo URLs — REQUIRED field.
     * Minimum one URL required by API spec.
     */
    @JsonProperty("photoUrls")
    private List<String> photoUrls;

    /**
     * List of descriptive tags for the pet.
     * Example: ["friendly", "vaccinated"]
     */
    @JsonProperty("tags")
    private List<Tag> tags;

    // ─────────────────────────────────────────
    // CONVENIENCE METHODS
    // ─────────────────────────────────────────

    /**
     * Checks if pet is available for adoption.
     *
     * @return true if status is "available"
     */
    public boolean isAvailable() {
        return Status.available.name().equals(status);
    }

    /**
     * Checks if pet is pending adoption.
     *
     * @return true if status is "pending"
     */
    public boolean isPending() {
        return Status.pending.name().equals(status);
    }

    /**
     * Checks if pet has been sold/adopted.
     *
     * @return true if status is "sold"
     */
    public boolean isSold() {
        return Status.sold.name().equals(status);
    }

    /**
     * Checks if pet has at least one photo.
     *
     * @return true if photoUrls is not null and not empty
     */
    public boolean hasPhotos() {
        return photoUrls != null && !photoUrls.isEmpty();
    }

    /**
     * Checks if pet has tags assigned.
     *
     * @return true if tags is not null and not empty
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    /**
     * Checks if pet belongs to a category.
     *
     * @return true if category is not null
     */
    public boolean hasCategory() {
        return category != null;
    }
}
