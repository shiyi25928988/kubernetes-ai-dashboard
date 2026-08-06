package cc.shiyi.oss.common;

import cc.shiyi.oss.exceptions.InvalidFileNameException;
import cc.shiyi.oss.http.ContentHeader;
import cc.shiyi.oss.http.ContentType;
import cc.shiyi.oss.services.MinioFileUploadService;
import com.google.common.base.Strings;
import io.minio.errors.*;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: minio-client
 * @description: UploadObject
 * @author: shiyi
 * @create: 2020-12-08 14:06
 */
@Slf4j
@Service
public class UploadService {

    private String filePath;

    private File file;

    private InputStream inputStream;

    private String fileName;

    private String bucket;

    private String subFolder;

    private MinioFileUploadService fileUploadService;

    private List<ContentHeader> contentHeaders = new ArrayList<>();

    public UploadService(@Autowired MinioFileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * @param file
     * @return file download url
     * @throws FileNotFoundException
     */
    public String uploadFile(@NonNull File file) throws Exception {
        this.file = file;
        this.fileName = file.getName();
        this.inputStream = new BufferedInputStream(new FileInputStream(file));
        return this.upload();
    }

    /**
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    public String uploadFile(@NonNull String filePath) throws Exception {
        this.filePath = filePath;
        this.file = new File(filePath);
        this.fileName = this.file.getName();
        @Cleanup BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        this.inputStream = bufferedInputStream;
        return this.upload();
    }

    /**
     * @param multipartFile
     * @return
     * @throws Exception
     */
    public String uploadFile(MultipartFile multipartFile) throws Exception {
        this.fileName = multipartFile.getOriginalFilename();
        @Cleanup InputStream inputStream = multipartFile.getInputStream();
        this.inputStream = inputStream;
        this.addContentHeader(ContentType.getContentTypeByFileName(multipartFile.getOriginalFilename()));
        return this.upload();
    }

    /**
     * @param inputStream
     * @return
     * @throws Exception
     */
    public String uploadFile(InputStream inputStream) throws Exception {
        @Cleanup BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        this.inputStream = bufferedInputStream;
        return this.upload();
    }

    /**
     * set file name
     *
     * @param fileName
     * @return
     */
    public UploadService setFileName(@NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * @param inputStream
     * @return
     */
    public UploadService inputStream(@NonNull InputStream inputStream) {
        this.inputStream = new BufferedInputStream(inputStream);
        return this;
    }

    /**
     * set bucket
     *
     * @param bucket
     * @return
     */
    public UploadService bucket(@NonNull String bucket) {
        this.bucket = bucket;
        return this;
    }

    /**
     * @param subFolder
     * @return
     */
    public UploadService setFolder(@NonNull String subFolder) {
        if (!subFolder.endsWith("/")) {
            this.subFolder = subFolder + "/";
        } else {
            this.subFolder = subFolder;
        }
        return this;
    }

    /**
     * @param fileUploadService
     * @return
     */
    public UploadService setService(@NonNull MinioFileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
        return this;
    }

    /**
     *
     * @param contentHeader
     * @return
     */
    public UploadService addContentHeader(ContentHeader contentHeader) {
        this.contentHeaders.add(contentHeader);
        return this;
    }

    /**
     * @return
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws InsufficientDataException
     * @throws NoSuchAlgorithmException
     * @throws ServerException
     * @throws InternalException
     * @throws XmlParserException
     * @throws ErrorResponseException
     * @throws IOException
     */
    private String upload() throws Exception {

        if (Strings.isNullOrEmpty(this.fileName)) {
            throw new InvalidFileNameException("unspecified file name!");
        }

        if (Strings.isNullOrEmpty(bucket)) {
            if (Strings.isNullOrEmpty(subFolder)) {
                return this.fileUploadService.upload(inputStream, fileName, contentHeaders);
            } else {
                return this.fileUploadService.upload(inputStream, subFolder.concat(fileName), contentHeaders);
            }
        } else {
            if (Strings.isNullOrEmpty(subFolder)) {
                return this.fileUploadService.upload(inputStream, fileName, bucket, contentHeaders);
            } else {
                return this.fileUploadService.upload(inputStream, subFolder.concat(fileName), bucket, contentHeaders);
            }
        }
    }

}
