package faang.school.url.shortener.config.redis;

import faang.school.url.shortener.entity.url.FreeURL;
import faang.school.url.shortener.entity.url.RegisteredURL;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    private <T> RedisTemplate<String, T> createRedisTemplate(Class<T> clazz) {
        RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        Jackson2JsonRedisSerializer<T> jsonSerializer = new Jackson2JsonRedisSerializer<>(clazz);
        redisTemplate.setValueSerializer(jsonSerializer);
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Object> customRedisTemplateObject() {
        return createRedisTemplate(Object.class);
    }

    @Bean
    public RedisTemplate<String, FreeURL> customRedisTemplateFreeURL() {
        return createRedisTemplate(FreeURL.class);
    }

    @Bean
    public RedisTemplate<String, RegisteredURL> customRedisTemplateRegisteredURL() {
        return createRedisTemplate(RegisteredURL.class);
    }

}