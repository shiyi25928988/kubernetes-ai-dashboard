package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.oss.common.UploadService;
import cc.shiyi.oss.items.FileItem;
import cc.shiyi.oss.services.MinioBucketService;
import cc.shiyi.oss.services.MinioFileListService;
import cc.shiyi.oss.utils.DownloadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/oss")
@Setter(onMethod_ = @Autowired)
@Tag(name = "对象存储管理-OssController")
public class OssController {

    private UploadService uploadService;
    private MinioBucketService minioBucketService;
    private MinioFileListService minioFileListService;

    @Operation(summary = "上传文件到指定Bucket和目录")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseWrapper<String> upload(
            @RequestPart MultipartFile file,
            @RequestParam(required = false) String bucket,
            @RequestParam(required = false) String prefix) throws Exception {
        UploadService svc = uploadService;
        if (bucket != null && !bucket.isEmpty()) {
            svc = svc.bucket(bucket);
        }
        if (prefix != null && !prefix.isEmpty()) {
            svc = svc.setFolder(prefix);
        }
        return new ResponseWrapper().success(svc.uploadFile(file));
    }

    @Operation(summary = "在指定Bucket中创建文件夹")
    @PostMapping(value = "/buckets/{bucketName}/folders")
    public ResponseWrapper<Void> createFolder(
            @PathVariable String bucketName,
            @RequestParam(required = false, defaultValue = "") String prefix,
            @RequestParam String folderName) throws Exception {
        minioFileListService.createFolder(bucketName, prefix, folderName);
        return new ResponseWrapper().success(null);
    }

    @Operation(summary = "删除指定Bucket中的对象")
    @DeleteMapping(value = "/buckets/{bucketName}/objects")
    public ResponseWrapper<Void> deleteObject(
            @PathVariable String bucketName,
            @RequestParam(required = false, defaultValue = "") String prefix,
            @RequestParam String objectName) throws Exception {
        minioFileListService.deleteObject(bucketName, prefix, objectName);
        return new ResponseWrapper().success(null);
    }

    @Operation(summary = "下载文件")
    @RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/octet-stream")
    public ResponseEntity<byte[]> download(@RequestParam("url") String url) throws IOException {
        File file = DownloadUtil.download(url);
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        String filename = file.getName();
        DownloadUtil.deleteFile(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @Operation(summary = "获取所有Bucket列表")
    @GetMapping(value = "/buckets")
    public ResponseWrapper<List<String>> listBuckets() throws Exception {
        return new ResponseWrapper().success(minioBucketService.listBuckets());
    }

    @Operation(summary = "创建Bucket")
    @PostMapping(value = "/buckets")
    public ResponseWrapper<Void> createBucket(@RequestParam String bucketName) throws Exception {
        minioBucketService.createBucket(bucketName);
        return new ResponseWrapper().success(null);
    }

    @Operation(summary = "获取指定Bucket下的文件列表，支持按前缀（文件夹）过滤")
    @GetMapping(value = "/buckets/{bucketName}/files")
    public ResponseWrapper<List<FileItem>> listFilesByBucket(
            @PathVariable String bucketName,
            @RequestParam(required = false) String prefix) throws Exception {
        return new ResponseWrapper().success(minioFileListService.listFilesByBucketAndPrefix(bucketName, prefix));
    }

    @Operation(summary = "预览文本文件内容")
    @GetMapping("/buckets/{bucketName}/preview")
    public ResponseWrapper<String> previewFile(
            @PathVariable String bucketName,
            @RequestParam(required = false, defaultValue = "") String prefix,
            @RequestParam String objectName) throws Exception {
        String content = minioFileListService.getObjectContent(bucketName, prefix, objectName);
        return new ResponseWrapper().success(content, "succeed!");
    }
}
