package nst.wms.user.internal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Search and filter parameters for users")
public class UserFilter {

    @Schema(description = "Filter by name (case-insensitive, partial match)", example = "John")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
