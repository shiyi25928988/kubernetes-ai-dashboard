package cc.shiyi.coleditor.common.annotation.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DistributedLock {
    long expire() default 1l;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
