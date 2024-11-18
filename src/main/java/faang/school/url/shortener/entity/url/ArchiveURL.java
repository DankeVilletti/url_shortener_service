package faang.school.url.shortener.entity.url;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "archive_urls")
@IdClass(ArchiveURLId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveURL {
    @Id
    @Column(name = "short_url")
    private String shortURL;

    @Id
    @Column(name = "archived_at")
    private Timestamp archivedAt;

    @Column(name = "full_url")
    private String fullURL;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "counter")
    private Long counter;

    @Column(name = "reuse_short_url_status")
    private Boolean reuseShortUrlStatus;

    public ArchiveURL(RegisteredURL registeredURL) {
        this.shortURL = registeredURL.getShortUrl();
        this.fullURL = registeredURL.getFullUrl();
        this.creatorId = registeredURL.getCreatorId();
        this.projectId = registeredURL.getProjectId();
        this.counter = registeredURL.getCounter();
        this.reuseShortUrlStatus = true;
    }

    @PrePersist
    public void prePersist() {
        if (this.archivedAt == null) {
            this.archivedAt = new Timestamp(System.currentTimeMillis());
        }
        this.reuseShortUrlStatus = true;

    }
}
