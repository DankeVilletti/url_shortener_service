package faang.school.url.shortener.entity.url;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "unique_incrementer_urls")
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class UniqueIncrementerURL {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long counter;

    @Column(name = "generation_state")
    private boolean generationState;

    @Column(name = "version")
    @Version
    private Long version;

    @Column(name = "thread_id")
    private String threadID;

    public boolean permissionToStartGeneration(String threadID) {
        boolean permissionStatus = !this.generationState && this.threadID.equals(threadID);
        if (permissionStatus) {
            log.info("DB: Permission to start generation granted for Thread ID: {}", threadID);
        } else {
            log.warn("DB: Permission to start generation denied for Thread ID: {}. Another thread is already generating.", threadID);
        }
        return permissionStatus;
    }
}
