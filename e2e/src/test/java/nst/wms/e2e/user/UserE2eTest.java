package nst.wms.e2e.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import nst.wms.e2e.AbstractE2eTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

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
        var createRequest = new HttpEntity<>(new CreateUserRequest("Alice"));
        var createResponse = restTemplate.exchange(
                USERS_PATH, HttpMethod.POST, createRequest, UserResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse user = createResponse.getBody();
        assertThat(user).isNotNull();
        assertThat(user.id()).isNotNull();
        assertThat(user.name()).isEqualTo("Alice");
        assertThat(user.createdAt()).isNotNull();

        // Get by ID
        var getResponse = restTemplate.exchange(
                USERS_PATH + "/" + user.id(), HttpMethod.GET, null, UserResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse fetched = getResponse.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.name()).isEqualTo("Alice");

        // List — should contain Alice
        var listResponse = restTemplate.exchange(
                USERS_PATH + "?page=0&size=10",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<PageResponse<UserSummary>>() {});

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PageResponse<UserSummary> list = listResponse.getBody();
        assertThat(list).isNotNull();
        assertThat(list.data()).extracting(UserSummary::name).contains("Alice");

        // Update
        var updateRequest = new HttpEntity<>(new UpdateUserRequest("Alice Updated"));
        var updateResponse = restTemplate.exchange(
                USERS_PATH + "/" + user.id(), HttpMethod.PUT, updateRequest, UserResponse.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse updated = updateResponse.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.name()).isEqualTo("Alice Updated");

        // Delete
        var deleteResponse = restTemplate.exchange(
                USERS_PATH + "/" + user.id(), HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify gone
        var goneResponse = restTemplate.exchange(
                USERS_PATH + "/" + user.id(), HttpMethod.GET, null, UserResponse.class);

        assertThat(goneResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        var request = new HttpEntity<>(new CreateUserRequest(""));
        var response = restTemplate.exchange(
                USERS_PATH, HttpMethod.POST, request, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn404ForNonExistentUser() {
        var response = restTemplate.exchange(
                USERS_PATH + "/99999", HttpMethod.GET, null, UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
