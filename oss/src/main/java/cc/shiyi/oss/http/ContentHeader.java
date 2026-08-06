package cc.shiyi.oss.http;

/**
 * @program: minio-client
 * @description:
 * @author: shiyi
 * @create: 2021-02-18 16:52
 */
public interface ContentHeader<T,V> {
    T getType();
    V getValue();
}
