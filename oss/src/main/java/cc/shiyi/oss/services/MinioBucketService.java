package cc.shiyi.oss.services;

import io.minio.ListBucketsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MinIO Bucket管理服务
 */
@Slf4j
@Service
public class MinioBucketService {

    @Autowired
    private MinioClient minioClient;

    /**
     * 获取所有Bucket列表
     */
    public List<String> listBuckets() throws Exception {
        List<Bucket> buckets = minioClient.listBuckets();
        return buckets.stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
    }

    /**
     * 创建新的Bucket
     */
    public void createBucket(String bucketName) throws Exception {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }
}
