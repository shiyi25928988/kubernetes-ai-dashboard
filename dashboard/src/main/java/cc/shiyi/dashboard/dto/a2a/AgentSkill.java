package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * AgentSkill
 * 描述 Agent 提供的一项具体技能，供上游 AI 应用发现与路由。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentSkill {

    /**
     * 技能唯一标识
     */
    private String id;

    /**
     * 技能名称（人类可读）
     */
    private String name;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 技能标签，用于分类与检索
     */
    private List<String> tags;

    /**
     * 调用示例
     */
    private List<String> examples;

    /**
     * 支持的输入模式列表（如 text, data, file）
     */
    private List<String> inputModes;

    /**
     * 支持的输出模式列表（如 text, data, file）
     */
    private List<String> outputModes;
}
