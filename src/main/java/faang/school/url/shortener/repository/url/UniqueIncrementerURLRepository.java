package faang.school.url.shortener.repository.url;

import faang.school.url.shortener.entity.url.UniqueIncrementerURL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UniqueIncrementerURLRepository extends JpaRepository<UniqueIncrementerURL, Long> {

    @Query(nativeQuery = true,
            value = """
                    UPDATE unique_incrementer_urls
                    SET counter = counter + 1 
                    RETURNING counter""")
    Long getAndIncrement();

    @Modifying
    @Query("UPDATE UniqueIncrementerURL a SET a.generationState = true")
    void setActiveGenerationState();

    @Modifying
    @Query("UPDATE UniqueIncrementerURL a SET a.generationState = false, a.threadID = null ")
    void setNonActiveGenerationState();

    @Query(nativeQuery = true,
            value = """
                    UPDATE unique_incrementer_urls
                    SET counter = counter + :batchSize 
                    RETURNING counter""")
    Long getAndIncrease(long batchSize);



    @Modifying
    @Query("""
                UPDATE UniqueIncrementerURL u
                SET u.threadID = :threadID                
                WHERE u.generationState = false and u.threadID is null
            """)
    Integer tryToLockForGeneration(String threadID);
}
