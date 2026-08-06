package cc.shiyi.coleditor.common.annotation.cache;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisCache {
    String name();
    long expire() default 5l;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
