package cc.shiyi.oss.services;

import cc.shiyi.oss.http.ContentHeader;
import cc.shiyi.oss.services.func.MinioFileUploadFunc;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * @program: minio-client
 * @description: upload service
 * @author: shiyi
 * @create: 2020-12-08 10:22
 */
@Slf4j
@Service
public class MinioFileUploadService {

    @Autowired
    MinioClient minioClient;
    @Value("${minio.bucket}")
    private String defaultBucket;
    @Value("${minio.auto.create.bucket:true}")
    private boolean autoCreateBucket;

    /**
     *
     */
    public String upload(InputStream inputStream, String fileName, List<ContentHeader> contentHeaders) throws Exception {
        return upload(inputStream, fileName, defaultBucket, contentHeaders);
    }

    /**
     *
     */
    public String upload(InputStream inputStream, String fileName, String bucket, List<ContentHeader> contentHeaders) throws Exception {
        MinioFileUploadFunc minioFileUploadFunc = MinioFileUploadFunc.builder()
                .minioClient(minioClient)
                .bucket(bucket)
                .autoCreateBucket(autoCreateBucket)
                .inputStream(inputStream)
                .fileName(fileName)
                .build();
        return minioFileUploadFunc.upload();
    }


}
