package faang.school.url.shortener.entity.url;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "registered_urls")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RegisteredURL {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "creator_id")
    @NotNull(message = "Creator ID cannot be null")
    private Long creatorId;

    @Id
    @Column(name = "short_url")
    @NotBlank(message = "Short URL cannot be blank")
    private String shortUrl;

    @Column(name = "full_url")
    @NotBlank(message = "Full URL cannot be blank")
    private String fullUrl;

    @Column(name = "counter")
    private Long counter;

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;


}
