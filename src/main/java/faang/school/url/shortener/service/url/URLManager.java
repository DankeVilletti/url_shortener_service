package faang.school.url.shortener.service.url;

import faang.school.url.shortener.builder.URLBuilder;
import faang.school.url.shortener.dto.URLToRegisterDTO;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.service.url.util.manager.URLCacheManager;
import faang.school.url.shortener.service.url.util.manager.URLDBManager;
import faang.school.url.shortener.service.url.util.manager.URLLocalManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class URLManager {
    private final URLLocalManager localManager;
    private final URLCacheManager cacheManager;
    private final URLDBManager dbManager;
    private final URLBuilder builder;

    public RegisteredURL assignHashToFullURLAndRegisterIt(URLToRegisterDTO toRegisterDTO) {
        RegisteredURL toRegisterURL = builder.registeredURL(localManager.getFreeShortURL(), toRegisterDTO);
        cacheManager.putRegisteredURLToCachePool(toRegisterURL);
        return dbManager.assignHashToFullURLAndRegisterIt(toRegisterURL);
    }

    public String redirectToFullURL(String shortURL) {
        return cacheManager.redirectToFullURL(shortURL)
                .map(RegisteredURL::getFullUrl)
                .orElseGet(() -> dbManager.redirectToFullURL(shortURL));
    }

}
