package cc.shiyi.oss.services;

import cc.shiyi.oss.services.func.MinioFileListFunc;
import cc.shiyi.oss.items.FileItem;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shiyi
 *
 */
@Slf4j
@Service
public class MinioFileListService {

    @Value("${minio.bucket}")
    String bucket;

	@Value("${minio.host}")
	String host;

	@Value("${minio.isHttps}")
	boolean isHttps;

	@Value("${minio.port}")
	String port;

    @Autowired
    MinioClient minioClient;

	public List<FileItem> listFile() throws Exception {
		MinioFileListFunc func = MinioFileListFunc.builder()
				.host(host)
				.bucket(bucket)
				.isHttps(isHttps)
				.minioClient(minioClient)
				.port(port)
				.build();
		return func.listFile();
	}

	public List<FileItem> listFile(String folder) throws Exception {
		MinioFileListFunc func = MinioFileListFunc.builder()
				.host(host)
				.bucket(bucket)
				.isHttps(isHttps)
				.minioClient(minioClient)
				.port(port)
				.build();
		return func.listFile(folder);
	}

	/**
	 * 列出指定bucket下的文件（非递归）
	 */
	public List<FileItem> listFilesByBucket(String bucketName) throws Exception {
		return listFilesByBucketAndPrefix(bucketName, null);
	}

	/**
	 * 列出指定bucket下、指定前缀（文件夹）中的文件（非递归）
	 */
	public List<FileItem> listFilesByBucketAndPrefix(String bucketName, String prefix) throws Exception {
		List<FileItem> fileList = new ArrayList<>();
		String effectivePrefix = (prefix != null && !prefix.isEmpty()) ? prefix : "";
		if (!effectivePrefix.isEmpty() && !effectivePrefix.endsWith("/")) {
			effectivePrefix = effectivePrefix + "/";
		}
		Iterable<Result<Item>> list = minioClient.listObjects(
				ListObjectsArgs.builder()
						.bucket(bucketName)
						.prefix(effectivePrefix)
						.delimiter("/")
						.build()
		);
		for (Result<Item> result : list) {
			Item item = result.get();
			// 跳过表示当前文件夹自身的空对象（如 prefix="test/" 时跳过 "test/" 自身）
			if (item.objectName().equals(effectivePrefix)) {
				continue;
			}
			FileItem fileItem = new FileItem();
			String protocal = "http://";
			if (isHttps) {
				protocal = "https://";
			}
			fileItem.setDownloadUrl(protocal.concat(host).concat(":").concat(port).concat("/").concat(bucketName).concat("/").concat(item.objectName()));
			fileItem.setName(item.objectName());
			fileItem.setSize(item.size());
			fileItem.setFolder(item.isDir());
			fileItem.setBucket(bucketName);
			if (!effectivePrefix.isEmpty() && item.objectName().startsWith(effectivePrefix)) {
				String relativePath = item.objectName().substring(effectivePrefix.length());
				fileItem.setName(relativePath);
			}
			fileList.add(fileItem);
		}
		return fileList;
	}

	/**
	 * 在指定bucket中创建文件夹（上传一个空对象，以/结尾）
	 */
	public void createFolder(String bucketName, String prefix, String folderName) throws Exception {
		String folderPath = (prefix != null && !prefix.isEmpty()) ? prefix : "";
		if (!folderPath.isEmpty() && !folderPath.endsWith("/")) {
			folderPath = folderPath + "/";
		}
		folderPath = folderPath + folderName;
		if (!folderPath.endsWith("/")) {
			folderPath = folderPath + "/";
		}
		minioClient.putObject(
				PutObjectArgs.builder()
						.bucket(bucketName)
						.object(folderPath)
						.stream(new ByteArrayInputStream(new byte[0]), 0, -1)
						.build()
		);
	}

	/**
	 * 删除指定bucket中的对象（文件或文件夹）
	 */
	public void deleteObject(String bucketName, String prefix, String objectName) throws Exception {
		String fullPath = (prefix != null && !prefix.isEmpty()) ? prefix : "";
		if (!fullPath.isEmpty() && !fullPath.endsWith("/")) {
			fullPath = fullPath + "/";
		}
		fullPath = fullPath + objectName;
		minioClient.removeObject(
				RemoveObjectArgs.builder()
						.bucket(bucketName)
						.object(fullPath)
						.build()
		);
	}

	/**
	 * 读取指定对象的内容，返回UTF-8字符串（用于文本文件预览）
	 */
	public String getObjectContent(String bucketName, String prefix, String objectName) throws Exception {
		String fullPath = (prefix != null && !prefix.isEmpty()) ? prefix : "";
		if (!fullPath.isEmpty() && !fullPath.endsWith("/")) {
			fullPath = fullPath + "/";
		}
		fullPath = fullPath + objectName;
		try (InputStream is = minioClient.getObject(
				GetObjectArgs.builder()
						.bucket(bucketName)
						.object(fullPath)
						.build())) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}
