package faang.school.url.shortener.repository.url;

import faang.school.url.shortener.entity.url.FreeURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeURLRepository extends JpaRepository<FreeURL, String> {

    @Query(nativeQuery = true, value = """
        DELETE FROM free_urls 
        WHERE short_url IN (
            SELECT short_url 
            FROM free_urls 
            ORDER BY short_url ASC 
            LIMIT :countURLs
        )
        RETURNING *;
    """)
    List<FreeURL> getAndRemoveFreeURLs(long countURLs);


}
