package cc.shiyi.dashboard.service;

import cc.shiyi.oss.utils.DownloadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * 附件内容解析服务，使用 Apache Tika 提取文档文本
 */
@Slf4j
@Service
public class AttachmentParserService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 单文件最大 20MB
    private static final int MAX_TEXT_LENGTH = 8000; // 每个文件最多提取 8000 字符
    private static final int REQUEST_TIMEOUT = 30_000; // 下载超时 30 秒

    private final Tika tika = new Tika();

    /**
     * 解析附件列表，返回拼装好的文本
     */
    public String parseAttachments(List<Map<String, Object>> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("用户上传了以下附件及其内容：\n\n");

        for (int i = 0; i < attachments.size(); i++) {
            Map<String, Object> file = attachments.get(i);
            String name = (String) file.get("name");
            String url = (String) file.get("url");

            sb.append("--- 附件 ").append(i + 1).append("：").append(name).append(" ---\n");

            String text = extractText(name, url);
            if (text != null && !text.isEmpty()) {
                sb.append(text).append("\n");
            } else {
                sb.append("(无法解析此文件内容)\n");
            }
        }

        sb.append("--- 附件内容结束 ---\n\n用户问题：");
        return sb.toString();
    }

    /**
     * 下载文件并提取文本
     */
    private String extractText(String fileName, String fileUrl) {
        File tempFile = null;
        try {
            // 检查是否为纯文本类型，直接用流解析避免写磁盘
            String mimeType = tika.detect(fileName);
            if (isPlainText(mimeType) || isCodeFile(fileName)) {
                return extractTextFromUrl(fileUrl);
            }

            // 其他格式通过 DownloadUtil 下载后解析
            tempFile = DownloadUtil.download(fileUrl);
            if (tempFile == null || !tempFile.exists() || tempFile.length() == 0) {
                log.warn("文件下载为空: {}", fileName);
                return null;
            }

            if (tempFile.length() > MAX_FILE_SIZE) {
                log.warn("文件过大，跳过解析: {} ({} bytes)", fileName, tempFile.length());
                return "[文件过大，未提取内容]";
            }

            String text = tika.parseToString(tempFile);
            return truncate(text, MAX_TEXT_LENGTH);

        } catch (Exception e) {
            log.error("解析附件失败: {}", fileName, e);
            return null;
        } finally {
            if (tempFile != null) {
                DownloadUtil.deleteFile(tempFile);
            }
        }
    }

    /**
     * 通过 URL 直接读取文本内容（适用于 .txt .md .json .xml .csv 等）
     */
    private String extractTextFromUrl(String fileUrl) {
        try {
            URLConnection conn = new URL(fileUrl).openConnection();
            conn.setConnectTimeout(REQUEST_TIMEOUT);
            conn.setReadTimeout(REQUEST_TIMEOUT);
            try (InputStream is = conn.getInputStream()) {
                String text = new String(is.readAllBytes(), "UTF-8");
                return truncate(text, MAX_TEXT_LENGTH);
            }
        } catch (Exception e) {
            log.warn("直接读取文本失败: {}", fileUrl, e);
            return null;
        }
    }

    private boolean isPlainText(String mimeType) {
        return mimeType != null && (
            mimeType.startsWith("text/") ||
            mimeType.equals("application/json") ||
            mimeType.equals("application/xml") ||
            mimeType.equals("application/javascript")
        );
    }

    private boolean isCodeFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".csv") ||
               lower.endsWith(".log") || lower.endsWith(".yaml") ||
               lower.endsWith(".yml") || lower.endsWith(".toml") ||
               lower.endsWith(".properties") || lower.endsWith(".env");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return null;
        text = text.trim();
        return text.length() > maxLen ? text.substring(0, maxLen) + "\n...(内容已截断)" : text;
    }
}