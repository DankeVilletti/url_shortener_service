package faang.school.url.shortener.repository.url;

import faang.school.url.shortener.entity.url.RegisteredURL;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredURLRepository extends JpaRepository<RegisteredURL, Long> {


    @Query("""
             SELECT r FROM RegisteredURL r
             WHERE r.fullUrl = :fullUrl
               AND r.creatorId = :creatorId
               AND r.projectId = :projectId
               AND r.expiresAt = :expiresAt
            """)
    Optional<RegisteredURL> checkForRegisteredFullURL(
            String fullUrl,
            Long creatorId,
            Long projectId,
            Timestamp expiresAt);

    @Modifying
    @Query("""
            update RegisteredURL r
            set r.counter = r.counter + 1""")
    void incrementCount(RegisteredURL registeredURL);

    Optional<RegisteredURL> findByShortUrl(String shortURL);

    @Query(nativeQuery = true, value = """
            DELETE FROM registered_urls r WHERE r.expires_at < :now RETURNING *""")
    List<RegisteredURL> findURLsThatShouldBeArchived(Timestamp now);
}
