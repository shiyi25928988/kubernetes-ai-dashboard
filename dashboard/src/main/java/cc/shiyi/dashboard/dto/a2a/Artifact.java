package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Artifact
 * A2A 任务产生的产物，可包含文本、数据、文件等片段。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Artifact {

    /**
     * 产物名称
     */
    private String name;

    /**
     * 产物描述
     */
    private String description;

    /**
     * 产物内容片段列表
     */
    private List<Part> parts;

    /**
     * 产物索引（同一任务内多个产物的序号）
     */
    private Integer index;

    /**
     * 产物追加标识，true 表示增量追加
     */
    private Boolean append;
}
