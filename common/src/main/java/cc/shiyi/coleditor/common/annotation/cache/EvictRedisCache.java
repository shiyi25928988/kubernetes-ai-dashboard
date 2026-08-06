package cc.shiyi.coleditor.common.annotation.cache;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EvictRedisCache {
    String[] name();
}
