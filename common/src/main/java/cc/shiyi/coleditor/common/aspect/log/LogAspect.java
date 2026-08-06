package cc.shiyi.coleditor.common.aspect.log;

import cc.shiyi.coleditor.common.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("@annotation(com.gitee.common.annotation.log.Log)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Stream.of(pjp.getArgs()).forEach(arg -> {
                try {
                    log.info(JsonUtil.toJson(arg));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            Object result = pjp.proceed();
            log.info(JsonUtil.toJson(result));
            return result;
        }catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }
}
