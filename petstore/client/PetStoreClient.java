package com.neil.automation.petstore.client;

import com.neil.automation.client.BaseApiClient;
import com.neil.automation.config.ConfigLoader;
import com.neil.automation.petstore.model.Order;
import com.neil.automation.petstore.model.Pet;
import com.neil.automation.petstore.model.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * PetStoreClient — PetStore v3 API operations client.
 *
 * <p>Extends BaseApiClient to provide all PetStore
 * API operations with full Allure step annotations.
 *
 * <p>Covers all three PetStore resource groups:
 * - Pet   : CRUD + findByStatus + findByTags
 * - Store : inventory + order management
 * - User  : registration + authentication + CRUD
 *
 * <p>Usage:
 * <pre>
 *   PetStoreClient client = new PetStoreClient();
 *
 *   // Create a pet
 *   Response response = client.createPet(pet);
 *
 *   // Get by ID
 *   Response response = client.getPetById(123L);
 *
 *   // Find available pets
 *   Response response = client.getPetsByStatus("available");
 * </pre>
 *
 * @author Neil J. Marshall
 * @version 1.0.0
 */
public class PetStoreClient extends BaseApiClient {

    private static final Logger LOG =
        LogManager.getLogger(PetStoreClient.class);

    // ── API Endpoint constants ──
    private static final String PET_BASE        = "/pet";
    private static final String PET_BY_ID       = "/pet/{petId}";
    private static final String PET_BY_STATUS   = "/pet/findByStatus";
    private static final String PET_BY_TAGS     = "/pet/findByTags";
    private static final String STORE_INVENTORY = "/store/inventory";
    private static final String STORE_ORDER     = "/store/order";
    private static final String STORE_ORDER_ID  = "/store/order/{orderId}";
    private static final String USER_BASE       = "/user";
    private static final String USER_BY_NAME    = "/user/{username}";
    private static final String USER_LOGIN      = "/user/login";
    private static final String USER_LOGOUT     = "/user/logout";

    // ─────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────

    /**
     * Initializes PetStoreClient with base URL from config.
     */
    public PetStoreClient() {
        super(ConfigLoader.getInstance().get("petstore.base.url"));
        LOG.info("✅ PetStoreClient initialized: url=[{}]",
            ConfigLoader.getInstance().get("petstore.base.url"));
    }

    // ═════════════════════════════════════════
    // PET OPERATIONS
    // ═════════════════════════════════════════

    /**
     * Creates a new pet.
     * POST /pet
     *
     * @param pet Pet object to create
     * @return API Response
     */
    @Step("POST /pet — Create pet: [{pet.name}]")
    public Response createPet(Pet pet) {
        LOG.info("→ POST /pet | name=[{}] status=[{}]",
            pet.getName(), pet.getStatus());
        return post(PET_BASE, pet);
    }

    /**
     * Retrieves a pet by its unique ID.
     * GET /pet/{petId}
     *
     * @param petId pet identifier
     * @return API Response
     */
    @Step("GET /pet/{petId} — Get pet by ID: [{petId}]")
    public Response getPetById(Long petId) {
        LOG.info("→ GET /pet/{}", petId);
        Map<String, Object> params = new HashMap<>();
        params.put("petId", petId);
        return get(PET_BY_ID, params);
    }

    /**
     * Updates an existing pet.
     * PUT /pet
     *
     * @param pet Pet object with updated fields
     * @return API Response
     */
    @Step("PUT /pet — Update pet: [{pet.name}]")
    public Response updatePet(Pet pet) {
        LOG.info("→ PUT /pet | id=[{}] name=[{}]",
            pet.getId(), pet.getName());
        return put(PET_BASE, pet);
    }

    /**
     * Deletes a pet by its ID.
     * DELETE /pet/{petId}
     *
     * @param petId pet identifier to delete
     * @return API Response
     */
    @Step("DELETE /pet/{petId} — Delete pet: [{petId}]")
    public Response deletePet(Long petId) {
        LOG.info("→ DELETE /pet/{}", petId);
        Map<String, Object> params = new HashMap<>();
        params.put("petId", petId);
        return delete(PET_BY_ID, params);
    }

    /**
     * Finds pets by availability status.
     * GET /pet/findByStatus?status={status}
     *
     * @param status available | pending | sold
     * @return API Response containing list of pets
     */
    @Step("GET /pet/findByStatus — status: [{status}]")
    public Response getPetsByStatus(String status) {
        LOG.info("→ GET /pet/findByStatus | status=[{}]", status);
        Map<String, Object> query = new HashMap<>();
        query.put("status", status);
        return getWithQuery(PET_BY_STATUS, query);
    }

    /**
     * Finds pets by tags.
     * GET /pet/findByTags?tags={tags}
     *
     * @param tags comma-separated tag names
     * @return API Response containing list of pets
     */
    @Step("GET /pet/findByTags — tags: [{tags}]")
    public Response getPetsByTags(String tags) {
        LOG.info("→ GET /pet/findByTags | tags=[{}]", tags);
        Map<String, Object> query = new HashMap<>();
        query.put("tags", tags);
        return getWithQuery(PET_BY_TAGS, query);
    }

    /**
     * Updates pet using form data (multipart).
     * POST /pet/{petId}
     *
     * @param petId  pet identifier
     * @param name   new pet name
     * @param status new pet status
     * @return API Response
     */
    @Step("POST /pet/{petId} — Update pet form data")
    public Response updatePetWithForm(
            Long petId,
            String name,
            String status) {
        LOG.info("→ POST /pet/{} (form) | name=[{}] status=[{}]",
            petId, name, status);
        Map<String, Object> params = new HashMap<>();
        params.put("petId", petId);
        return given()
            .spec(getRequestSpec())
            .pathParams(params)
            .formParam("name",   name)
            .formParam("status", status)
            .contentType("application/x-www-form-urlencoded")
            .when()
            .post(PET_BY_ID)
            .then()
            .extract()
            .response();
    }

    // ═════════════════════════════════════════
    // STORE OPERATIONS
    // ═════════════════════════════════════════

    /**
     * Retrieves pet store inventory by status.
     * GET /store/inventory
     *
     * @return API Response with inventory map
     */
    @Step("GET /store/inventory — Get inventory")
    public Response getInventory() {
        LOG.info("→ GET /store/inventory");
        return get(STORE_INVENTORY);
    }

    /**
     * Places a new order for a pet.
     * POST /store/order
     *
     * @param order Order object to place
     * @return API Response
     */
    @Step("POST /store/order — Place order for petId: [{order.petId}]")
    public Response placeOrder(Order order) {
        LOG.info("→ POST /store/order | petId=[{}] qty=[{}]",
            order.getPetId(), order.getQuantity());
        return post(STORE_ORDER, order);
    }

    /**
     * Retrieves an order by its ID.
     * GET /store/order/{orderId}
     *
     * @param orderId order identifier
     * @return API Response
     */
    @Step("GET /store/order/{orderId} — Get order: [{orderId}]")
    public Response getOrderById(Long orderId) {
        LOG.info("→ GET /store/order/{}", orderId);
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        return get(STORE_ORDER_ID, params);
    }

    /**
     * Deletes an order by its ID.
     * DELETE /store/order/{orderId}
     *
     * @param orderId order identifier to delete
     * @return API Response
     */
    @Step("DELETE /store/order/{orderId} — Delete order: [{orderId}]")
    public Response deleteOrder(Long orderId) {
        LOG.info("→ DELETE /store/order/{}", orderId);
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        return delete(STORE_ORDER_ID, params);
    }

    // ═════════════════════════════════════════
    // USER OPERATIONS
    // ═════════════════════════════════════════

    /**
     * Creates a new user account.
     * POST /user
     *
     * @param user User object to create
     * @return API Response
     */
    @Step("POST /user — Create user: [{user.username}]")
    public Response createUser(User user) {
        LOG.info("→ POST /user | username=[{}]", user.getUsername());
        return post(USER_BASE, user);
    }

    /**
     * Retrieves a user by username.
     * GET /user/{username}
     *
     * @param username unique username
     * @return API Response
     */
    @Step("GET /user/{username} — Get user: [{username}]")
    public Response getUserByUsername(String username) {
        LOG.info("→ GET /user/{}", username);
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        return get(USER_BY_NAME, params);
    }

    /**
     * Updates an existing user.
     * PUT /user/{username}
     *
     * @param username existing username
     * @param user     updated User object
     * @return API Response
     */
    @Step("PUT /user/{username} — Update user: [{username}]")
    public Response updateUser(String username, User user) {
        LOG.info("→ PUT /user/{}", username);
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        return given()
            .spec(getRequestSpec())
            .pathParams(params)
            .body(user)
            .when()
            .put(USER_BY_NAME)
            .then()
            .extract()
            .response();
    }

    /**
     * Deletes a user by username.
     * DELETE /user/{username}
     *
     * @param username username to delete
     * @return API Response
     */
    @Step("DELETE /user/{username} — Delete user: [{username}]")
    public Response deleteUser(String username) {
        LOG.info("→ DELETE /user/{}", username);
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        return delete(USER_BY_NAME, params);
    }

    /**
     * Logs in a user and returns session token.
     * GET /user/login?username={}&password={}
     *
     * @param username user login name
     * @param password user password
     * @return API Response with session token
     */
    @Step("GET /user/login — Login user: [{username}]")
    public Response loginUser(String username, String password) {
        LOG.info("→ GET /user/login | username=[{}]", username);
        Map<String, Object> query = new HashMap<>();
        query.put("username", username);
        query.put("password", password);
        return getWithQuery(USER_LOGIN, query);
    }

    /**
     * Logs out the currently logged-in user.
     * GET /user/logout
     *
     * @return API Response
     */
    @Step("GET /user/logout — Logout current user")
    public Response logoutUser() {
        LOG.info("→ GET /user/logout");
        return get(USER_LOGOUT);
    }

    /**
     * Creates multiple users from a list.
     * POST /user/createWithList
     *
     * @param users list of User objects
     * @return API Response
     */
    @Step("POST /user/createWithList — Create [{users.size()}] users")
    public Response createUsersWithList(
            java.util.List<User> users) {
        LOG.info("→ POST /user/createWithList | count=[{}]",
            users.size());
        return post("/user/createWithList", users);
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPER
    // ─────────────────────────────────────────

    /**
     * Provides RestAssured given() with base spec.
     * Used for complex requests (form data, custom paths).
     *
     * @return RequestSpecification with base config
     */
    private io.restassured.specification.RequestSpecification given() {
        return io.restassured.RestAssured.given()
            .spec(getRequestSpec());
    }
}
