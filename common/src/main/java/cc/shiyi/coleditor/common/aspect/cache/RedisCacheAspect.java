package cc.shiyi.coleditor.common.aspect.cache;

import cc.shiyi.coleditor.common.annotation.cache.RedisCache;
import cc.shiyi.coleditor.common.utils.MD5Util;
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

@Aspect
@Component
@Slf4j
public class RedisCacheAspect {

    @Autowired(required = false)
    RedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    String applicationName;

    @Around("@annotation(com.gitee.common.annotation.cache.RedisCache)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try{
            if(Objects.nonNull(redisTemplate)) {
                Signature signature = pjp.getSignature();
                if (signature instanceof MethodSignature) {
                    RedisCache remoteCache = ((MethodSignature) signature).getMethod().getAnnotation(RedisCache.class);
                    String key = applicationName.concat(remoteCache.name()).concat(MD5Util.caculateMD5(pjp.getArgs()));
                    if (redisTemplate.hasKey(key)) {
                        return redisTemplate.opsForValue().get(key);
                    } else {
                        Object obj = pjp.proceed();
                        redisTemplate.opsForValue().set(key, obj, remoteCache.expire(), remoteCache.timeUnit());
                        return obj;
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return pjp.proceed();
    }
}
