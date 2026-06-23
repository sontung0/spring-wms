package nst.wms.e2e.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import nst.wms.e2e.AbstractE2eTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserE2eTest extends AbstractE2eTest {

    private static final String USERS_PATH = "/api/users";

    // Local DTOs — no imports from nst.wms.* (black-box guarantee)
    record CreateUserRequest(String name) {}
    record UpdateUserRequest(String name) {}

    public record UserResponse(
            Long id,
            String name,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt
    ) {}

    public record UserSummary(Long id, String name) {}

    public record PageResponse<T>(
            List<T> data,
            int page,
            int size,
            long count,
            int pages
    ) {}

    @Test
    void shouldCreateGetUpdateDeleteUser() {
        // Create
        UserResponse[] created = { null };
        client.post().uri(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateUserRequest("Alice"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .value(resp -> {
                    created[0] = resp;
                    assertThat(resp.id()).isNotNull();
                    assertThat(resp.name()).isEqualTo("Alice");
                    assertThat(resp.createdAt()).isNotNull();
                });

        assertThat(created[0]).isNotNull();
        Long userId = created[0].id();

        // Get by ID
        client.get().uri(USERS_PATH + "/" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(resp -> {
                    assertThat(resp.name()).isEqualTo("Alice");
                });

        // List — should contain Alice
        client.get().uri(uriBuilder -> uriBuilder
                        .path(USERS_PATH)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserSummary>>() {})
                .value(list -> {
                    assertThat(list.data()).extracting(UserSummary::name).contains("Alice");
                });

        // Update
        client.put().uri(USERS_PATH + "/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateUserRequest("Alice Updated"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(resp -> {
                    assertThat(resp.name()).isEqualTo("Alice Updated");
                });

        // Delete
        client.delete().uri(USERS_PATH + "/" + userId)
                .exchange()
                .expectStatus().value(status -> {
                    assertThat(status).isEqualTo(204);
                });

        // Verify gone
        client.get().uri(USERS_PATH + "/" + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        client.post().uri(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateUserRequest(""))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn404ForNonExistentUser() {
        client.get().uri(USERS_PATH + "/99999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
