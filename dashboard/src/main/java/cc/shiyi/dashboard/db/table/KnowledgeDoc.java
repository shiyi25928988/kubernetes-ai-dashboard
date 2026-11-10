package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库文档实体类（内容为 Markdown，由 Lucene+IK 建全文索引）
 */
@Data
@TableName("knowledge_doc")
public class KnowledgeDoc {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文档标题
     */
    @TableField("title")
    private String title;

    /**
     * 分类
     */
    @TableField("category")
    private String category;

    /**
     * 文档内容（Markdown）
     */
    @TableField("content")
    private String content;

    /**
     * 创建者用户 ID
     */
    @TableField("creator_id")
    private Long creatorId;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    /**
     * 逻辑删除标志（0-未删除，1-已删除）
     */
    @TableField("is_deleted")
    private Boolean isDeleted;
}
