package cc.shiyi.coleditor.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;

@Configuration
public class RedisConfig {

    @Bean
    public RedisSerializer getRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean
    public RedisTemplate redisTemplate(RedisTemplate redisTemplate){
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public DefaultRedisList defaultRedisList(RedisTemplate redisTemplate){
        return new DefaultRedisList("_QUEUE", redisTemplate);
    }
}
