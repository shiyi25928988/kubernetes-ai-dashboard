package cc.shiyi.dashboard.db.mapper;

import cc.shiyi.dashboard.db.table.Conversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话Mapper接口
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
