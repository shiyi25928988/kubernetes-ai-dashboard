package cc.shiyi.oss.services.func;

import cc.shiyi.oss.http.ContentType;
import io.minio.*;
import io.minio.http.Method;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @program: object-saved-tool
 * @description:
 * @author: shiyi
 * @create: 2023-03-13 11:27
 */
@Builder
@Slf4j
public class MinioFileUploadFunc {

    private MinioClient minioClient;

    private String bucket;

    private Boolean autoCreateBucket;

    private static final long UPLOAD_MAX_PART_SIZE = 10 * 1024 * 1024L;

    private InputStream inputStream;

    private String fileName;

    /**
     * 上传文件到MinIO存储服务
     * <p>
     * 该方法将输入流中的文件数据上传到指定的存储桶中，并返回文件的访问URL。
     * 上传完成后会关闭输入流，并生成一个不带过期参数的预签名URL。
     *
     * @return 上传文件的访问URL地址
     * @throws Exception 上传过程中可能抛出的异常，如网络错误、权限不足等
     */
    public String upload() throws Exception {
        PutObjectArgs args = PutObjectArgs
                .builder()
                .stream(inputStream, -1, UPLOAD_MAX_PART_SIZE)
                .contentType(ContentType.getTypeByFileName(fileName))
                .bucket(bucket)
                .object(fileName)
                .build();
        minioClient.putObject(args);
        inputStream.close();
        String objUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs
                .builder()
                .bucket(bucket)
                .object(fileName)
                .method(Method.GET)
                .build());
                //.expiry(7, TimeUnit.DAYS).method(Method.GET).build());
        if(objUrl.contains("?")){
            objUrl = objUrl.substring(0, objUrl.indexOf("?"));
        }
        return objUrl;
    }

}
