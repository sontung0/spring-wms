package nst.wms.user.presentation;

import nst.wms.user.presentation.dto.CreateUserRequest;
import nst.wms.user.presentation.dto.UpdateUserRequest;
import nst.wms.user.presentation.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createUser_withBlankName_shouldReturn400() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationFailed"));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Jane");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        mockMvc.perform(get("/api/users/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    void getUserById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("UserNotFound"));
    }

    @Test
    void listUsers_shouldReturnPaginatedResponse() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("TestUser");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThan(0)));
    }

    @Test
    void listUsers_withNameFilter_shouldFilterResults() throws Exception {
        CreateUserRequest req1 = new CreateUserRequest();
        req1.setName("Alice");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        CreateUserRequest req2 = new CreateUserRequest();
        req2.setName("Bob");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.name =~ /.*Alice.*/)]").exists())
                .andExpect(jsonPath("$.data[?(@.name =~ /.*Bob.*/)]").doesNotExist());
    }

    @Test
    void updateUser_shouldReturn200() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("OldName");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("NewName");

        mockMvc.perform(put("/api/users/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("ToDelete");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        mockMvc.perform(delete("/api/users/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + created.getId()))
                .andExpect(status().isNotFound());
    }
}
