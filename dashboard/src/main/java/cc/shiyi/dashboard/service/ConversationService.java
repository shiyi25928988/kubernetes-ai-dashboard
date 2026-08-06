package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.table.Conversation;
import cc.shiyi.dashboard.db.mapper.ConversationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * 对话服务实现类
 */
@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation>  {

    public Conversation createConversation(Long userId, String conversationId, String title) {
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setCreatedAt(new Date());
        conversation.setUpdatedAt(new Date());
        conversation.setIsDeleted(false);
        save(conversation);
        return conversation;
    }

    public List<Conversation> getUserConversations(Long userId) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUserId, userId)
                .eq(Conversation::getIsDeleted, false)
                .orderByDesc(Conversation::getUpdatedAt);
        return list(queryWrapper);
    }

    public void updateLastMessage(String conversationId, String lastMessage) {
        Conversation conversation = getById(conversationId);
        if (conversation != null) {
            // 截断过长的消息作为摘要
            String summary = lastMessage.length() > 50 ? lastMessage.substring(0, 50) + "..." : lastMessage;
            conversation.setLastMessage(summary);
            conversation.setUpdatedAt(new Date());
            updateById(conversation);
        }
    }

    public boolean deleteConversation(String conversationId) {
        Conversation conversation = getById(conversationId);
        if (conversation != null) {
            conversation.setIsDeleted(true);
            conversation.setUpdatedAt(new Date());
            return updateById(conversation);
        }
        return false;
    }
}
