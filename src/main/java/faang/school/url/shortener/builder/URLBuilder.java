package faang.school.url.shortener.builder;

import faang.school.url.shortener.dto.URLToRegisterDTO;
import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.validator.URLValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class URLBuilder {
    private final URLValidator validator;

    public RegisteredURL registeredURL(FreeURL shortUrl, URLToRegisterDTO toRegisterURL) {
        RegisteredURL registerURL = RegisteredURL.builder()
                .fullUrl(toRegisterURL.getFullUrl())
                .shortUrl(shortUrl.getShortUrl())
                .expiresAt(toRegisterURL.getExpiresAt())
                .creatorId(toRegisterURL.getCreatorId())
                .projectId(toRegisterURL.getProjectId())
                .build();
        validator.validate(registerURL);
        return registerURL;
    }

}
