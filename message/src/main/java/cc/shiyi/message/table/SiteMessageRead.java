package cc.shiyi.message.table;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "site_message_read")
public class SiteMessageRead {
    @TableField(value = "id", fill = FieldFill.INSERT)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "message_id")
    private Long messageId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "read_time")
    private LocalDateTime readTime;
}
