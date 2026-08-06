package cc.shiyi.dashboard.service.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作MCP服务
 * 提供文件的查看、创建、修改、删除功能
 * 所有操作都限制在配置的工作目录内，防止路径遍历攻击
 */
@Slf4j
@Service
public class FileMcpService {

    /**
     * 文件操作根目录，可通过配置file.mcp.root-path修改
     */
    @Value("${file.mcp.root-path:./data/mcp-files}")
    private String rootPath;

    /**
     * 读取文件内容
     * @param filePath 文件路径，相对于根目录
     * @return 文件内容
     */
    @Tool(description = "读取指定文件的内容，文件路径相对于工作根目录")
    public String readFile(@ToolParam(description = "要读取的文件路径，相对于工作根目录，例如: config/app.properties") String filePath) throws IOException {
        log.info("MCP调用-读取文件: {}", filePath);
        Path path = validatePath(filePath);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("不是有效的文件: " + filePath);
        }

        // 限制最大读取10MB，防止大文件内存溢出
        if (Files.size(path) > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("文件过大，最大支持10MB: " + filePath);
        }

        return Files.readString(path);
    }

    /**
     * 创建文件
     * @param filePath 文件路径，相对于根目录
     * @param content 文件内容
     * @param overwrite 如果文件已存在是否覆盖
     * @return 创建结果
     */
    @Tool(description = "创建新文件，文件路径相对于工作根目录，如果文件已存在可选择是否覆盖")
    public String createFile(
            @ToolParam(description = "要创建的文件路径，相对于工作根目录，例如: config/app.properties") String filePath,
            @ToolParam(description = "文件内容") String content,
            @ToolParam(description = "如果文件已存在是否覆盖，默认false") Boolean overwrite) throws IOException {
        log.info("MCP调用-创建文件: {}, 覆盖: {}", filePath, overwrite);
        Path path = validatePath(filePath);

        if (Files.exists(path)) {
            if (!Boolean.TRUE.equals(overwrite)) {
                throw new IllegalArgumentException("文件已存在，如需覆盖请设置overwrite=true: " + filePath);
            }
            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException("路径已存在且不是文件: " + filePath);
            }
        } else {
            // 创建父目录
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        }

        Files.writeString(path, content != null ? content : "", StandardOpenOption.CREATE,
                overwrite ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.CREATE_NEW);

        return "文件创建成功: " + filePath;
    }

    /**
     * 修改文件内容
     * @param filePath 文件路径，相对于根目录
     * @param content 新的文件内容
     * @return 修改结果
     */
    @Tool(description = "修改已存在文件的内容，文件路径相对于工作根目录")
    public String updateFile(
            @ToolParam(description = "要修改的文件路径，相对于工作根目录，例如: config/app.properties") String filePath,
            @ToolParam(description = "新的文件内容") String content) throws IOException {
        log.info("MCP调用-修改文件: {}", filePath);
        Path path = validatePath(filePath);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件不存在: " + filePath);
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("不是有效的文件: " + filePath);
        }

        Files.writeString(path, content != null ? content : "", StandardOpenOption.TRUNCATE_EXISTING);

        return "文件修改成功: " + filePath;
    }

    /**
     * 删除文件
     * @param filePath 文件路径，相对于根目录
     * @param recursive 如果是目录是否递归删除
     * @return 删除结果
     */
    @Tool(description = "删除文件或目录，文件路径相对于工作根目录，如果是目录可选择是否递归删除")
    public String deleteFile(
            @ToolParam(description = "要删除的文件或目录路径，相对于工作根目录，例如: config/app.properties") String filePath,
            @ToolParam(description = "如果是目录是否递归删除，默认false") Boolean recursive) throws IOException {
        log.info("MCP调用-删除文件: {}, 递归: {}", filePath, recursive);
        Path path = validatePath(filePath);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("文件或目录不存在: " + filePath);
        }

        if (Files.isDirectory(path)) {
            if (!Boolean.TRUE.equals(recursive)) {
                throw new IllegalArgumentException("路径是目录，如需删除请设置recursive=true: " + filePath);
            }
            // 递归删除目录
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.delete(path);
        }

        return "删除成功: " + filePath;
    }

    /**
     * 列出目录内容
     * @param dirPath 目录路径，相对于根目录
     * @return 目录内容列表
     */
    @Tool(description = "列出指定目录下的文件和子目录，路径相对于工作根目录")
    public List<String> listDirectory(
            @ToolParam(description = "要列出的目录路径，相对于工作根目录，默认根目录") String dirPath) throws IOException {
        log.info("MCP调用-列出目录: {}", dirPath);
        String pathStr = dirPath != null ? dirPath : ".";
        Path path = validatePath(pathStr);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("目录不存在: " + pathStr);
        }
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("不是有效的目录: " + pathStr);
        }

        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                String type = Files.isDirectory(entry) ? "D" : "F";
                String name = entry.getFileName().toString();
                result.add(String.format("[%s] %s", type, name));
            }
        }

        return result;
    }

    /**
     * 验证文件路径，防止路径遍历攻击
     * @param filePath 输入的文件路径
     * @return 标准化后的绝对路径
     */
    private Path validatePath(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 标准化根路径
        Path root = Paths.get(rootPath).toAbsolutePath().normalize();
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        // 标准化用户输入路径
        Path userPath = root.resolve(filePath).normalize();

        // 检查是否在根目录内
        if (!userPath.startsWith(root)) {
            throw new SecurityException("非法路径访问，不允许超出工作根目录范围: " + filePath);
        }

        return userPath;
    }
}
