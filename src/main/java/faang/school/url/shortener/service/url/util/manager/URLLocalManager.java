package faang.school.url.shortener.service.url.util.manager;

import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.service.url.util.service.LocalURLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class URLLocalManager implements ApplicationListener<ContextRefreshedEvent> {
    private final LocalURLService localURLService;
    private final URLCacheManager cacheManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        localURLService.tryToFillFreeURLsInLocal(cacheManager);
    }

    public FreeURL getFreeShortURL() {
        return localURLService.getAndRemoveFreeShortURLFromLocal()
                .orElseGet(() -> {
                    log.info("LOCAL: Get free URL from Redis.");
                    localURLService.tryToFillFreeURLsInLocal(cacheManager);
                    return cacheManager.getFreeURLFromCachePool();
                });
    }


}
