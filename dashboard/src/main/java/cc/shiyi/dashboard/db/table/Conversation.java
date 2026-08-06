package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 对话元数据实体类
 * 存储对话的基本信息，用于用户对话列表展示
 */
@Data
@TableName("conversation")
public class Conversation {


    @TableId(type = IdType.INPUT)
    private String id;


    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 对话标题，通常是第一条消息的摘要
     */
    @TableField("title")
    private String title;

    /**
     * 最后一条消息的内容摘要
     */
    @TableField("last_message")
    private String lastMessage;

    /**
     * 对话创建时间
     */
    @TableField("created_at")
    private Date createdAt;

    /**
     * 最后更新时间
     */
    @TableField("updated_at")
    private Date updatedAt;

    /**
     * 是否删除，逻辑删除
     */
    @TableField("is_deleted")
    private Boolean isDeleted;
}
