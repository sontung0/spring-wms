package nst.wms.e2e.user;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import nst.wms.e2e.AbstractE2eTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class UserE2eTest extends AbstractE2eTest {

    private static final String USERS_PATH = "/api/users";

    record CreateUserRequest(String name) {}
    record UpdateUserRequest(String name) {}
    record UserResponse(Long id, String name, String createdAt, String updatedAt) {}
    record UserSummary(Long id, String name) {}
    record PageResponse<T>(List<T> data, int page, int size, long count, int pages) {
        static <T> TypeRef<PageResponse<T>> typeRef() {
            return new TypeRef<>() {};
        }
    }

    @Test
    void shouldCreateGetUpdateDeleteUser() {
        // Create
        UserResponse user = given()
                .contentType(ContentType.JSON)
                .body(new CreateUserRequest("Alice"))
            .when()
                .post(USERS_PATH)
            .then()
                .statusCode(201)
                .extract().as(UserResponse.class);

        assertThat(user.id()).isNotNull();
        assertThat(user.name()).isEqualTo("Alice");
        assertThat(user.createdAt()).isNotNull();

        // Get by ID
        UserResponse fetched = given()
            .when()
                .get("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(200)
                .extract().as(UserResponse.class);

        assertThat(fetched.name()).isEqualTo("Alice");

        // List — should contain Alice
        PageResponse<UserSummary> list = given()
                .param("page", 0)
                .param("size", 10)
            .when()
                .get(USERS_PATH)
            .then()
                .statusCode(200)
                .extract().as(PageResponse.typeRef());

        assertThat(list.data()).extracting(UserSummary::name).contains("Alice");

        // Update
        UserResponse updated = given()
                .contentType(ContentType.JSON)
                .body(new UpdateUserRequest("Alice Updated"))
            .when()
                .put("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(200)
                .extract().as(UserResponse.class);

        assertThat(updated.name()).isEqualTo("Alice Updated");

        // Delete
        given()
            .when()
                .delete("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(204);

        // Verify gone
        given()
            .when()
                .get("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        given()
                .contentType(ContentType.JSON)
                .body(new CreateUserRequest(""))
            .when()
                .post(USERS_PATH)
            .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn404ForNonExistentUser() {
        given()
            .when()
                .get("{}/{}", USERS_PATH, 99999L)
            .then()
                .statusCode(404);
    }
}
