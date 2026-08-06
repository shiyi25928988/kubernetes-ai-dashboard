package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 预设指令实体类
 */
@Data
@TableName("preset_prompt")
public class PresetPrompt {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预设名称
     */
    @TableField("name")
    private String name;

    /**
     * 预设描述
     */
    @TableField("description")
    private String description;

    /**
     * 图标（可选，emoji或图标名称）
     */
    @TableField("icon")
    private String icon;

    /**
     * 提示词内容
     */
    @TableField("prompt_content")
    private String promptContent;

    /**
     * 分类
     */
    @TableField("category")
    private String category;

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
     * 是否为系统预设（系统预设用户不能删除）
     */
    @TableField("is_system")
    private Boolean isSystem;

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
