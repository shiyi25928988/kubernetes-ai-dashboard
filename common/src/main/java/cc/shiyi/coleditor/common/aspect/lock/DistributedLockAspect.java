package cc.shiyi.coleditor.common.aspect.lock;

import cc.shiyi.coleditor.common.annotation.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@Slf4j
public class DistributedLockAspect {

    @Autowired(required = false)
    RedisTemplate redisTemplate;
    @Around("@annotation(com.gitee.common.annotation.lock.DistributedLock)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        if (Objects.nonNull(redisTemplate)) {
            Signature signature = pjp.getSignature();
            if (signature instanceof MethodSignature) {
                DistributedLock distributedLock = ((MethodSignature) signature).getMethod().getAnnotation(DistributedLock.class);
                String lockKey = signature.getDeclaringTypeName() + "#" + signature.getName();
                long expire = distributedLock.expire();
                boolean requestLock = false;
                try {
                    while (!requestLock) {
                        if(redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", expire, distributedLock.timeUnit())){
                            requestLock = true;
                        }else {
                            Thread.sleep(50);
                        }
                    }
                    return pjp.proceed();
                }finally {
                    if(requestLock){
                        redisTemplate.delete(lockKey);
                    }
                }
            }
        }
        return pjp.proceed();
    }
}
