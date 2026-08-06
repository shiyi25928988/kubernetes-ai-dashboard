package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI专家/Agent实体类
 */
@Data
@TableName("ai_expert")
public class AiExpert {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（NULL表示系统默认）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 专家名称
     */
    @TableField("name")
    private String name;

    /**
     * 专家描述
     */
    @TableField("description")
    private String description;

    /**
     * 图标（emoji或图标名称）
     */
    @TableField("icon")
    private String icon;

    /**
     * 系统提示词，定义专家行为
     */
    @TableField("system_prompt")
    private String systemPrompt;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /**
     * 是否为系统预设（用户不可编辑）
     */
    @TableField("is_system")
    private Boolean isSystem;

    /**
     * 逻辑删除标记
     */
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Date updatedAt;
}