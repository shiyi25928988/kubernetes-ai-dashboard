package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

/**
 * Part
 * A2A 消息的组成部分，遵循 A2A 协议规范的 Part 模型。
 * 通过 kind 字段区分文本、数据、文件等类型。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Part {

    /**
     * Part 类型：text / data / file
     * <p>反序列化时同时接受 type 作为别名，兼容早期 A2A SDK 用 type 字段表示 Part 类型。</p>
     */
    @JsonAlias({"type"})
    private String kind;

    /**
     * 文本内容（kind=text 时使用）
     */
    private String text;

    /**
     * 结构化数据（kind=data 时使用）
     */
    private Map<String, Object> data;

    /**
     * 文件信息（kind=file 时使用）
     */
    private Map<String, Object> file;

    /**
     * 内容媒体类型（A2A 1.0：对所有 part 类型可用，如 "text/plain"、"application/json"）。
     * 1.0 中 part 类型由成员字段存在性判别（text/raw/url/data），不再依赖 kind。
     */
    private String mediaType;
}
