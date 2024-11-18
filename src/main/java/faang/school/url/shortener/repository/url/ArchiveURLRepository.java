package faang.school.url.shortener.repository.url;

import faang.school.url.shortener.entity.url.ArchiveURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArchiveURLRepository extends JpaRepository<ArchiveURL, Long> {

    @Query(nativeQuery = true,
            value = "UPDATE archive_urls a SET reuse_short_url_status = false WHERE a.archived_at < :now and a.reuse_short_url_status = true returning *")
    List<ArchiveURL> getShortURLsThatShouldBeReused(@Param("now") Timestamp now);
}
