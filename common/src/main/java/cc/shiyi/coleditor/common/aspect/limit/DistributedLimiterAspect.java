package cc.shiyi.coleditor.common.aspect.limit;

import cc.shiyi.coleditor.common.annotation.limit.DistributedRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Aspect
@Component
@Slf4j
public class DistributedLimiterAspect {

    @Autowired(required = false)
    StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(com.gitee.common.annotation.limit.DistributedRateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            String key = "rate_limiter#" + signature.getName();
            DistributedRateLimiter distributedRateLimiter = ((MethodSignature) signature).getMethod().getAnnotation(DistributedRateLimiter.class);
            Integer maxRequests = distributedRateLimiter.maxRequests();
            Integer timeWindowSeconds = distributedRateLimiter.timeWindowSeconds();
            List<String> keys = Collections.singletonList(key);
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("rate_limit.lua")));
            script.setResultType(Long.class);
            Long result = stringRedisTemplate.execute(script, keys, maxRequests, timeWindowSeconds);
            if (result == null || result == 0L) {
                throw new RuntimeException("Too many requests");
            }
        }
        return joinPoint.proceed();
    }
}
