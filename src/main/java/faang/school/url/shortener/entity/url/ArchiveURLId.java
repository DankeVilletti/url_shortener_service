package faang.school.url.shortener.entity.url;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ArchiveURLId implements Serializable {
    private String shortURL;
    private Timestamp archivedAt;
}
