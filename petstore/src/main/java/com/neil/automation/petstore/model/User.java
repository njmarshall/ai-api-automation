package com.neil.automation.petstore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User — Represents a PetStore user account.
 *
 * <p>Maps to PetStore v3 User schema:
 * <pre>
 * {
 *   "id":         1,
 *   "username":   "neil_marshall",
 *   "firstName":  "Neil",
 *   "lastName":   "Marshall",
 *   "email":      "neil@example.com",
 *   "password":   "secret",
 *   "phone":      "415-555-0100",
 *   "userStatus": 1
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
public class User {

    // ─────────────────────────────────────────
    // FIELDS
    // ─────────────────────────────────────────

    /**
     * Unique user identifier.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Unique username for login.
     */
    @JsonProperty("username")
    private String username;

    /**
     * User first name.
     */
    @JsonProperty("firstName")
    private String firstName;

    /**
     * User last name.
     */
    @JsonProperty("lastName")
    private String lastName;

    /**
     * User email address.
     */
    @JsonProperty("email")
    private String email;

    /**
     * User password — never log or expose!
     */
    @JsonProperty("password")
    private String password;

    /**
     * User phone number.
     */
    @JsonProperty("phone")
    private String phone;

    /**
     * User account status.
     * 0 = inactive, 1 = active
     */
    @JsonProperty("userStatus")
    private Integer userStatus;

    // ─────────────────────────────────────────
    // CONVENIENCE METHODS
    // ─────────────────────────────────────────

    /**
     * Checks if user account is active.
     *
     * @return true if userStatus is 1
     */
    public boolean isActive() {
        return userStatus != null && userStatus == 1;
    }

    /**
     * Returns full display name.
     *
     * @return "FirstName LastName"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
