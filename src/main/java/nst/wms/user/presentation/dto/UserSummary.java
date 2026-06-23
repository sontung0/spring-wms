package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight user summary for list responses")
public class UserSummary {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    public UserSummary() {}

    public UserSummary(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
