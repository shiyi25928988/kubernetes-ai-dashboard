package cc.shiyi.oss.utils;

import cc.shiyi.oss.items.FileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @program: demo
 * @description:
 * @author: shiyi
 * @create: 2022-06-30 10:46
 */
public final class LocalStorageUtil {

    public static final String tempDir = System.getProperty("user.dir").concat(File.separator).concat("temp");

    /**
     * 将File对象转换为FileItem对象
     *
     * @param file 要转换的File对象
     * @return 转换后的FileItem对象，包含文件名和文件绝对路径信息
     */
    public static FileItem convertFile2FileItem(File file){
        FileItem fileItem = new FileItem();
        fileItem.setName(file.getName());
        fileItem.setFilePath(file.getAbsolutePath());
        return fileItem;
    }

    /**
     * 将文件列表转换为文件项列表
     * @param files 文件列表
     * @return 文件项列表，如果输入列表为空则返回空列表
     */
    public static List<FileItem> convertFiles2FileItems(List<File> files){
        List<FileItem> fileItems = new ArrayList<>();
        if(files.isEmpty()){
            return fileItems;
        }
        files.forEach(file -> {
            if(file.isFile()){
                fileItems.add(convertFile2FileItem(file));
            }
        });
        return fileItems;
    }

    /**
     * 列出指定路径下的所有文件
     * @param localPath 本地文件路径
     * @return 文件列表，如果路径为空或不是目录则返回空列表
     */
    public static List<File> listFiles(String localPath){
        if(StringUtils.isEmpty(localPath)){
            return Collections.emptyList();
        }
        if(new File(localPath).isDirectory()){
            Collection<File> files = FileUtils.listFiles(new File(localPath), null, true);
            if(!files.isEmpty()){
                return new ArrayList<>(files);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 创建一个临时文件夹
     *
     * 该方法在系统临时目录下创建一个随机命名的文件夹，并返回该文件夹对象。
     * 文件夹路径由临时目录路径和20位随机字符串组成。
     *
     * @return File 返回新创建的临时文件夹对象
     */
    public static File tempFolder(){
        String folderPath = tempDir.concat(File.separator).concat(org.apache.commons.lang3.RandomStringUtils.random(20,true,true).concat(File.separator));
        File folder = new File(folderPath);
        folder.mkdirs();
        return folder;
    }

    /**
     * 创建临时文件
     * @param fileName 文件名，不能为空
     * @return 创建的临时文件对象
     * @throws IOException 当文件名为空或创建文件失败时抛出异常
     */
    public static File tempFile(String fileName) throws IOException {
        if(StringUtils.isEmpty(fileName)){
            throw new IOException("fileName is empty!");
        }
        File folder = tempFolder();
        String filePath = folder.getAbsolutePath().concat(File.separator).concat(fileName);
        File _file = new File(filePath);
        _file.createNewFile();
        return _file;
    }

    /**
     * 将输入流转换为临时文件
     *
     * @param fileName 临时文件的名称
     * @param inputStream 输入流数据源
     * @return 创建的临时文件对象
     * @throws IOException 当文件操作失败时抛出
     */
    public static File inputStreamToTempFile(String fileName, InputStream inputStream) throws IOException {
        File tempFile = tempFile(fileName);
        FileUtils.copyInputStreamToFile(inputStream, tempFile);
        return tempFile;
    }

}
