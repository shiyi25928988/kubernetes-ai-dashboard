package cc.shiyi.oss.services.func;

import cc.shiyi.oss.items.FileItem;
import com.google.common.base.Strings;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @program: object-saved-tool
 * @description:
 * @author: shiyi
 * @create: 2023-03-09 17:10
 */
@Builder
public final class MinioFileListFunc {

    private MinioClient minioClient;
    private String host;
    private String port;
    private String bucket;
    private Boolean isHttps;

    /*
    * 便利当前配置下bucket中的所有文件
    * */
    public List<FileItem> listFile() throws Exception {
        List<FileItem> fileList = new ArrayList<>();
        Iterable<Result<Item>> list = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucket).build());
        for(Result<Item> result : list) {
            FileItem fileItem = convertItem(result.get(), null);
            if(fileItem.isFolder()){
                fileList.addAll(folderRecursive(fileItem));
            }else{
                fileList.add(fileItem);
            }
        }
        return fileList;
    }

    /*
    * 递归遍历当前folder/dir 下所有文件
    * */
    private List<FileItem> folderRecursive(FileItem item) throws Exception {
        if(Objects.isNull(item)) {
            return Collections.EMPTY_LIST;
        }
        List<FileItem> list = new CopyOnWriteArrayList<>();
        if(item.isFolder()){
            listFolderFile(item.getName()).forEach(i ->{
                try {
                    list.addAll(folderRecursive(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            list.add(item);
        }
        return list;
    }


    /*
    * 遍历指定文件夹下的全部文件
    * */
    private List<FileItem> listFolderFile(String folder) throws Exception {
        String subFolder = folder;
        if(!subFolder.endsWith("/")){
            subFolder = subFolder.concat("/");
        }
        List<FileItem> fileList = new ArrayList<>();
        Iterable<Result<Item>> list = minioClient
                .listObjects(ListObjectsArgs
                        .builder()
                        .bucket(bucket)
                        .prefix(subFolder)
                        .maxKeys(100)
                        .build());
        for(Result<Item> result : list) {
            fileList.add(convertItem(result.get(), folder));
        }
        return fileList;
    }

    /**
     * 列出指定文件夹下的所有文件（递归遍历子文件夹）
     * @param folder 文件夹路径
     * @return 文件项列表，包含所有子文件夹中的文件
     * @throws Exception 当文件操作或递归遍历时发生异常
     */
    public List<FileItem> listFile(String folder) throws Exception {
        List<FileItem> list = new CopyOnWriteArrayList<>();
        listFolderFile(folder).forEach(i ->{
            if(i.isFolder()){
                try {
                    list.addAll(folderRecursive(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                list.add(i);
            }
        });
        return list;
    }

    /*
    * 将mioio client 中的Item 对象zhuan成自定义的FileItem
    * */
    private FileItem convertItem(Item item, String folder){
        FileItem fileItem = new FileItem();
        String protocal = "http://";
        if(isHttps){
            protocal = "https://";
        }
        fileItem.setDownloadUrl(protocal.concat(host).concat(":").concat(port).concat("/").concat(bucket).concat("/").concat(item.objectName()));
        fileItem.setName(item.objectName());
        fileItem.setSize(item.size());
        fileItem.setFolder(item.isDir());
        fileItem.setBucket(bucket);
        if(!Strings.isNullOrEmpty(folder)){
            fileItem.setFolderName(folder);
        }
        return fileItem;
    }
}
