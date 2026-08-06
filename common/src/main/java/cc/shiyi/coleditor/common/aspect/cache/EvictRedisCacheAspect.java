package cc.shiyi.coleditor.common.aspect.cache;

import cc.shiyi.coleditor.common.annotation.cache.EvictRedisCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
public class EvictRedisCacheAspect {

    @Autowired(required = false)
    RedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    String applicationName;

    @Around("@annotation(com.gitee.common.annotation.cache.EvictRedisCache)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try{
            if(Objects.nonNull(redisTemplate)) {
                Signature signature = pjp.getSignature();
                if (signature instanceof MethodSignature) {
                    EvictRedisCache evictRemoteCache = ((MethodSignature) signature).getMethod().getAnnotation(EvictRedisCache.class);
                    String[] keys = evictRemoteCache.name();
                    Stream.of(keys).forEach(key -> {
                        Set<String> keySet = redisTemplate.keys(applicationName.concat(key).concat("*"));
                        if(!keySet.isEmpty()){
                            keySet.forEach(k -> {
                                if(redisTemplate.hasKey(k)){
                                    log.info("deleting key: " + k);
                                    redisTemplate.delete(k);
                                }
                            });
                        }
                    });
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }

        return pjp.proceed();
    }
}
