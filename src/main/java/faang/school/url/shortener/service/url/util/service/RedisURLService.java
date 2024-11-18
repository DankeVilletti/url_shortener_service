package faang.school.url.shortener.service.url.util.service;

import faang.school.url.shortener.config.url.URLConfig;
import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import faang.school.url.shortener.service.url.util.manager.URLDBManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.between;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisURLService {
    private final RedisTemplate<String, FreeURL> customRedisTemplateFreeURL;
    private final RedisTemplate<String, RegisteredURL> customRedisTemplateRegisteredURL;
    private final RedisTemplate<String, String> redisTemplate;
    private final URLConfig URLConfig;


    @Async
    public void tryToFillFreeURLsInRedis(URLDBManager dbManager) {
        String lockerUniqueID = UUID.randomUUID().toString();
        long needAddToRedis = checkForStartPoolFilling(lockerUniqueID);
        if (needAddToRedis > 0) {
            List<FreeURL> shortURLs = dbManager.getFreeURLsFromDBPool(needAddToRedis);
            addFreeShortURLsToRedis(lockerUniqueID, shortURLs);
        }
    }

    @Async
    public void addFreeShortURLsToRedis(String lockerUniqueID, List<FreeURL> shortURLs) {
        customRedisTemplateFreeURL.opsForList().rightPushAll(URLConfig.getRedisPoolKey(), shortURLs);
        releaseLockForAddToRedis(lockerUniqueID);
    }

    public List<FreeURL> getAndRemoveURLsFromPool(int requiredShortURLs) {
        List<FreeURL> shortUrls = customRedisTemplateFreeURL.opsForList().leftPop(URLConfig.getRedisPoolKey(), requiredShortURLs);
        if (shortUrls == null || shortUrls.isEmpty()) {
            log.info("REDIS: short URLs pool is empty.");
            shortUrls = new ArrayList<>();
        }
        log.info("REDIS: Got and removed {} short URLs.", shortUrls.size());
        return shortUrls;
    }

    public Optional<FreeURL> getAndRemoveURLFromRedis() {
        FreeURL freeURL = customRedisTemplateFreeURL.opsForList().leftPop(URLConfig.getRedisPoolKey());
        if (freeURL == null) {
            log.info("REDIS: There is no short URL in the pool.");
            return Optional.empty();
        }
        log.info("REDIS: Got and removed short URL {}", freeURL);
        return Optional.of(freeURL);
    }

    public long getCountOfElementsInPool() {
        Long size = customRedisTemplateFreeURL.opsForList().size(URLConfig.getRedisPoolKey());
        if (size == null) {
            log.info("REDIS: Pool of short URLs is empty.");
            return 0L;
        }
        log.info("REDIS: There are {} short URLs in the pool.", size);
        return size;
    }

    public long checkForStartPoolFilling(String lockerUniqueID) {
        if (!isRequiredToAddShortURLsToRedis()) return 0;
        if (!tryLockForAddToRedis(lockerUniqueID)) return 0;
        return getBatchSizeForFilling();
    }

    public Optional<RegisteredURL> getRegisteredURLByShortURL(String shortURL) {
        Optional<RegisteredURL> registeredURLOpt = Optional.ofNullable(customRedisTemplateRegisteredURL.opsForValue().get(shortURL));
        registeredURLOpt.ifPresent(this::updateTimeToLiveForRegisteredURL);
        return registeredURLOpt;
    }

    @Async
    public void putRegisteredURLToRedis(RegisteredURL registeredURL) {
        updateTimeToLiveForRegisteredURL(registeredURL);
    }

    private void updateTimeToLiveForRegisteredURL(RegisteredURL registeredURL) {
        customRedisTemplateRegisteredURL.opsForValue()
                .set(registeredURL.getShortUrl(), registeredURL, choosettlToUse(registeredURL), TimeUnit.MINUTES);

    }

    private long choosettlToUse(RegisteredURL registeredURL) {
        long ttlFromConfig = URLConfig.getRedisRegisteredURLsMinutesOfLife();
        long ttlToUse = 0;
        if (registeredURL.getExpiresAt() != null) {
            long ttlFromExpiresAt = between(LocalDateTime.now(), registeredURL.getExpiresAt().toLocalDateTime()).toMinutes();
            if (ttlFromExpiresAt > 0) {
                ttlToUse = Math.min(ttlFromExpiresAt, ttlFromConfig);
            }
        }
        return ttlToUse;
    }


    private long getBatchSizeForFilling() {
        long amountToAdd = URLConfig.getRedisMaxSize() - getCountOfElementsInPool();
        log.info("REDIS: Need to add {} short URLs", amountToAdd);
        return amountToAdd;
    }

    private boolean isRequiredToAddShortURLsToRedis() {
        boolean isRequired = getCountOfElementsInPool() < URLConfig.getRedisMaxSize();
        if (isRequired) {
            log.info("REDIS: Need to add short URLs.");
        } else {
            log.info("REDIS: Not need to add short URLs.");
        }
        return isRequired;
    }

    private boolean tryLockForAddToRedis(String lockerUniqueID) {
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(URLConfig.getRedisLockerKey(), lockerUniqueID, URLConfig.getRedisSecondTimeout(), TimeUnit.SECONDS);
        boolean statusLock = lockAcquired != null && lockAcquired;
        if (statusLock) {
            log.info("REDIS: This thread is adding short URLs. Thread Locker ID {} valid {}{}", lockerUniqueID, URLConfig.getRedisSecondTimeout(), TimeUnit.SECONDS);
        } else {
            log.info("REDIS: Another thread is already adding short URLs.");
        }
        return statusLock;
    }


    private void releaseLockForAddToRedis(String lockerUniqueID) {
        log.debug("REDIS: Try release locker for adding short URLs. Thread Locker ID {}", lockerUniqueID);
        String currentLockValue = redisTemplate.opsForValue().get(URLConfig.getRedisLockerKey());
        if (lockerUniqueID != null && lockerUniqueID.equals(currentLockValue)) {
            log.info("REDIS: Lock was released. Locker ID {}", lockerUniqueID);
            redisTemplate.delete(URLConfig.getRedisLockerKey());
        } else {
            log.info("REDIS: Lock was released by another thread. Thread locker ID {}, current locker value {}", lockerUniqueID, currentLockValue);
        }
    }


}
