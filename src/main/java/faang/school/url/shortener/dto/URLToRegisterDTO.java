package faang.school.url.shortener.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class URLToRegisterDTO {
    @NotBlank
    private String fullUrl;
    private Timestamp expiresAt;
    private Long projectId;
    private Long creatorId;
}
