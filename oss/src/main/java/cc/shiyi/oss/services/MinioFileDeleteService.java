package cc.shiyi.oss.services;

import cc.shiyi.oss.items.FileItem;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author shiyi
 */
@Slf4j
@Service
public class MinioFileDeleteService {

    @Value("${minio.bucket}")
    String bucket;

    @Autowired
    MinioClient minioClient;

    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(fileName).build());
    }

    public void deleteFile(String fileName, String folder) throws Exception {
        if (folder.endsWith("/")) {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(folder.concat(fileName)).build());
        } else {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(folder.concat("/").concat(fileName)).build());
        }
    }

    public void deleteFile(FileItem fileItem) throws Exception {
        if (fileItem.isFolder()) {
            return;
        }
        this.deleteFile(fileItem.getName());
    }

}
