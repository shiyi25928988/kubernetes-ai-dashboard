package cc.shiyi.dashboard.db.mapper;

import cc.shiyi.dashboard.db.table.KnowledgeDoc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档Mapper接口
 */
@Mapper
public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDoc> {
}
