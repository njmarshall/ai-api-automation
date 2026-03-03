package com.neil.automation.petstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order — Represents a pet store purchase order.
 *
 * <p>Maps to PetStore v3 Order schema:
 * <pre>
 * {
 *   "id":       1,
 *   "petId":    123,
 *   "quantity": 1,
 *   "shipDate": "2026-01-01T00:00:00.000Z",
 *   "status":   "placed",
 *   "complete": false
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
public class Order {

    // ── Order Status Enum ──
    public enum Status {
        placed,
        approved,
        delivered
    }

    // ─────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────

    /**
     * Unique order identifier.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * ID of the pet being ordered.
     */
    @JsonProperty("petId")
    private Long petId;

    /**
     * Quantity of pets ordered.
     */
    @JsonProperty("quantity")
    private Integer quantity;

    /**
     * Expected ship date in ISO 8601 format.
     */
    @JsonProperty("shipDate")
    private String shipDate;

    /**
     * Order fulfillment status.
     * Values: placed | approved | delivered
     */
    @JsonProperty("status")
    private String status;

    /**
     * Whether the order is complete.
     */
    @JsonProperty("complete")
    private Boolean complete;

    // ─────────────────────────────────────────
    // CONVENIENCE METHODS
    // ─────────────────────────────────────────

    /**
     * Checks if order has been placed.
     *
     * @return true if status is "placed"
     */
    public boolean isPlaced() {
        return Status.placed.name().equals(status);
    }

    /**
     * Checks if order has been approved.
     *
     * @return true if status is "approved"
     */
    public boolean isApproved() {
        return Status.approved.name().equals(status);
    }

    /**
     * Checks if order has been delivered.
     *
     * @return true if status is "delivered"
     */
    public boolean isDelivered() {
        return Status.delivered.name().equals(status);
    }
}
