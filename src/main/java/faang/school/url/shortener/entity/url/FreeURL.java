package faang.school.url.shortener.entity.url;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "free_urls")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class FreeURL {
    @Id
    private String shortUrl;


}
