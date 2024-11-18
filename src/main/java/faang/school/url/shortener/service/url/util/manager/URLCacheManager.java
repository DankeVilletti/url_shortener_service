package faang.school.url.shortener.service.url.util.manager;

import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.service.url.util.service.RedisURLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class URLCacheManager implements ApplicationListener<ContextRefreshedEvent> {
    private final RedisURLService redisService;
    private final URLDBManager dbManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        redisService.tryToFillFreeURLsInRedis(dbManager);
    }

    public List<FreeURL> getFreeURLsFromCachePool(int requiredShortURLs) {
        List<FreeURL> shortURLs = redisService.getAndRemoveURLsFromPool(requiredShortURLs);
        shortURLs.addAll(generateMoreShortURLsIfNotEnough(requiredShortURLs, shortURLs));
        return shortURLs;
    }


    public void putRegisteredURLToCachePool(RegisteredURL toRegisterURL) {
        redisService.putRegisteredURLToRedis(toRegisterURL);
    }

    public FreeURL getFreeURLFromCachePool() {
        return redisService.getAndRemoveURLFromRedis()
                .orElseGet(() -> {
                    redisService.tryToFillFreeURLsInRedis(dbManager);
                    log.info("REDIS: Start query from the DB for 1 short URL.");
                    return dbManager.getFreeURLsFromDBPool(1).get(0);
                });
    }

    public Optional<RegisteredURL> redirectToFullURL(String shortURL) {
        Optional<RegisteredURL> registeredURLOpt = redisService.getRegisteredURLByShortURL(shortURL);
        registeredURLOpt.ifPresent(dbManager::upCounter);
        return registeredURLOpt;
    }


    private List<FreeURL> generateMoreShortURLsIfNotEnough(long requiredShortURLs, List<FreeURL> shortURLs) {
        if (requiredShortURLs > shortURLs.size()) {
            requiredShortURLs -= shortURLs.size();
            log.info("REDIS: Short URLs in the pool are not enough. Start query from the DB for {} short URLs.", requiredShortURLs);
            redisService.tryToFillFreeURLsInRedis(dbManager);
            return dbManager.getFreeURLsFromDBPool(requiredShortURLs);
        }
        return List.of();
    }


}
