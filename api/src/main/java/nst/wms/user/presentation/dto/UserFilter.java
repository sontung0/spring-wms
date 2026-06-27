package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Search and filter parameters for users")
public class UserFilter {

    @Schema(description = "Filter by name (case-insensitive, partial match)", example = "John")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public nst.wms.user.domain.UserFilter toDomain() {
        var domain = new nst.wms.user.domain.UserFilter();
        domain.name = this.name;
        return domain;
    }
}
