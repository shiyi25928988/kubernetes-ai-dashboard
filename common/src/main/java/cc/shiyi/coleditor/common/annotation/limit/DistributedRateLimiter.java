package cc.shiyi.coleditor.common.annotation.limit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedRateLimiter {

    int maxRequests();

    int timeWindowSeconds();
}
