package cc.shiyi.oss.config;

import com.google.common.base.Strings;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @program: minio-client
 * @description: spring configuration
 * @author: shiyi
 * @create: 2020-12-08 14:06
 */
@Slf4j
@Configuration
public class MinioConfiguration {

    @Value("${minio.bucket}")
    String bucket;

    @Value("${minio.host}")
    private String host;

    @Value("${minio.port}")
    private int port;

    @Value("${minio.username}")
    private String username;

    @Value("${minio.password}")
    private String password;

    @Value("${minio.isHttps}")
    private boolean isHttps;

    private static final String DEFAULT_BUCKET_POLICY =
            """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": "*",
                  "Action": "s3:GetObject",
                  "Resource": "arn:aws:s3:::%s/*"
                }
              ]
            }
            """;

    /**
     * 创建并配置Minio客户端Bean
     *
     * @return 配置好的MinioClient实例
     * @throws ServerException 服务器异常
     * @throws InsufficientDataException 数据不足异常
     * @throws ErrorResponseException 错误响应异常
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 无此算法异常
     * @throws InvalidKeyException 无效密钥异常
     * @throws InvalidResponseException 无效响应异常
     * @throws XmlParserException XML解析异常
     * @throws InternalException 内部异常
     */
    @Bean
    MinioClient minioClient() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, BucketPolicyTooLargeException {
        OkHttpClient okHttpClient = new OkHttpClient();
        MinioClient client = MinioClient
                .builder()
                .httpClient(okHttpClient)
                .endpoint(host, port, isHttps)
                .region(null)
                .credentials(username, password)
                .build();
        if (client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            log.info("Bucket already exists.");
        } else {
            log.info("Bucket doesn't exist. Creating new bucket : " + bucket);
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(String.format(DEFAULT_BUCKET_POLICY, bucket)).build());
        }
        String originBucketPolicy = client.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucket).build());
        log.info("bucketPolicy: {}", originBucketPolicy);
        return client;
    }

}
