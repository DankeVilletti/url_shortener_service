package faang.school.url.shortener.scheduler;

import faang.school.url.shortener.service.url.util.manager.URLCacheManager;
import faang.school.url.shortener.service.url.util.manager.URLDBManager;
import faang.school.url.shortener.service.url.util.service.FreeShortURLService;
import faang.school.url.shortener.service.url.util.service.LocalURLService;
import faang.school.url.shortener.service.url.util.service.RedisURLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortURLSchedulerManager {
    private final LocalURLService localURLService;
    private final RedisURLService redisURLService;
    private final FreeShortURLService freeShortURLService;
    private final URLCacheManager cacheManager;
    private final URLDBManager dbManager;


    @Scheduled(cron = "${url-shortener-service.config.short-urls.free.db.scheduler-cron:0 0 0,6,12,18 * * ?}")
    public void scheduleDbTask() {
        log.info("SchedulerManager: Checking DB pool for free short URLs ...");
        freeShortURLService.tryStartGenerationAndSaveToDBShortURLsAsync();
    }

    @Scheduled(cron = "${url-shortener-service.config.short-urls.free.redis.scheduler-cron:0 */10 * * * ?}")
    public void scheduleRedisTask() {
        log.info("SchedulerManager: Executing Redis task pool for free short URLs...");
        redisURLService.tryToFillFreeURLsInRedis(dbManager);
    }

    @Scheduled(cron = "${url-shortener-service.config.short-urls.free.local.scheduler-cron:0 * * * * ?}")
    public void scheduleLocalTask() {
        log.info("SchedulerManager: Executing Local task pool for free short URLs...");
        localURLService.tryToFillFreeURLsInLocal(cacheManager);
    }

    @Scheduled(cron = "${url-shortener-service.config.short-urls.registered.db.archive.scheduler-cron:0 0 0 * * ?}")
    public void scheduleArchiveReuseTask() {
        log.info("SchedulerManager: Executing Archive task check short URLs to be reused...");
        dbManager.checkShortURLsTobeReused();
    }

    @Scheduled(cron = "${url-shortener-service.config.short-urls.registered.db.archive.scheduler-cron:0 0 0 * * ?}")
    public void scheduleArchiveFromRegisteredTask() {
        log.info("SchedulerManager: Executing Registered URLs task check short URLs to archive...");
        dbManager.checkToArchiveShortURLs();
    }


}
