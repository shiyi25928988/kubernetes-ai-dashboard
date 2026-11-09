package cc.shiyi.dashboard.service.mcp;

import cc.shiyi.oss.common.UploadService;
import cc.shiyi.oss.items.FileItem;
import cc.shiyi.oss.services.MinioFileListService;
import com.google.common.base.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
@Setter(onMethod_ = @Autowired)
@Slf4j
public class OssMcpService {
    UploadService uploadService;
    MinioFileListService minioFileListService;

    @Tool(description = "上传服务器上已存在的文件到对象存储/文件存储服务，并返回下载路径")
    public String uploadFile(@ToolParam(description = "文件夹名称") String folder,
                             @ToolParam(description = "服务器上待上传文件的绝对路径，例如 /opt/data/report.pdf 或 D:/data/report.pdf") String filePath) throws Exception {
        log.info("MCP调用-上传文件: folder={}, path={}", folder, filePath);
        // MCP 传输是 JSON，参数改为路径字符串；File 类型无法从 JSON 反序列化
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }
        return uploadService.setFolder(folder).uploadFile(file);
    }

    @Tool(description = "列出指定文件夹下的文件列表，如果文件夹为空字符串，则列出根目录下的文件列表")
    public List<FileItem> listFile(@ToolParam(description = "文件夹名称，可以为空字符串")String folder) throws Exception {
        if(Strings.isNullOrEmpty(folder)){
            return minioFileListService.listFile();
        }else{
            return minioFileListService.listFile(folder);
        }

    }

}
