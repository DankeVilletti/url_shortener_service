package faang.school.url.shortener.config.url;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class URLConfig {

    @Value("${url-shortener-service.config.short-urls.free.db.max-size:10000}")
    private int dbMaxSize;

    @Value("${url-shortener-service.config.short-urls.free.db.limit:2000}")
    private int dbLimit;

    @Value("${url-shortener-service.config.short-urls.free.redis.max-size:1000}")
    private int redisMaxSize;

    @Value("${url-shortener-service.config.short-urls.free.redis.limit:200}")
    private int redisLimit;

    @Value("${url-shortener-service.config.short-urls.free.redis.pool-key:free-short-urls}")
    private String redisPoolKey;

    @Value("${url-shortener-service.config.short-urls.free.redis.locker-key:lock-add-short-urls}")
    private String redisLockerKey;

    @Value("${url-shortener-service.config.short-urls.registered.redis.pool-key:registered-short-urls}")
    private String redisRegisteredPoolKey;

    @Value("${url-shortener-service.config.short-urls.registered.redis.minutes-of-life:90}")
    private long redisRegisteredURLsMinutesOfLife;

    @Value("${url-shortener-service.config.short-urls.free.redis.second-timeout:30}")
    private long redisSecondTimeout;

    @Value("${url-shortener-service.config.short-urls.free.local.max-size:100}")
    private int localMaxSize;

    @Value("${url-shortener-service.config.short-urls.free.local.limit:20}")
    private int localLimit;

}
