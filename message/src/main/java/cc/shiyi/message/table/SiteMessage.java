package cc.shiyi.message.table;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "site_message")
public class SiteMessage {
    @TableField(value = "id", fill = FieldFill.INSERT)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "title")
    private String title;

    @TableField(value = "content")
    private String content;

    @TableField(value = "type")
    private String type;

    @TableField(value = "priority")
    private Integer priority;

    @TableField(value = "is_active")
    private Boolean isActive;

    @TableField(value = "publish_time")
    private LocalDateTime publishTime;

    @TableField(value = "expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "creator_id")
    private Long creatorId;

    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
