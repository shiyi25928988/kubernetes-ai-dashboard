package cc.shiyi.message.service;

import cc.shiyi.message.dto.SiteMessageCreateDTO;
import cc.shiyi.message.dto.SiteMessageUpdateDTO;
import cc.shiyi.message.mapper.SiteMessageMapper;
import cc.shiyi.message.mapper.SiteMessageReadMapper;
import cc.shiyi.message.table.SiteMessage;
import cc.shiyi.message.table.SiteMessageRead;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Setter(onMethod_ = @Autowired)
public class SiteMessageService {

    SiteMessageMapper siteMessageMapper;
    SiteMessageReadMapper siteMessageReadMapper;

    public SiteMessage createMessage(SiteMessageCreateDTO dto, Long creatorId) {
        SiteMessage message = new SiteMessage();
        message.setTitle(dto.getTitle());
        message.setContent(dto.getContent());
        message.setType(dto.getType() != null ? dto.getType() : "INFO");
        message.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        message.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        message.setPublishTime(dto.getPublishTime());
        message.setExpireTime(dto.getExpireTime());
        message.setCreatorId(creatorId);
        message.setCreatedTime(LocalDateTime.now());
        message.setUpdatedTime(LocalDateTime.now());
        message.setIsDeleted(false);
        siteMessageMapper.insert(message);
        return message;
    }

    public SiteMessage updateMessage(SiteMessageUpdateDTO dto) {
        SiteMessage message = siteMessageMapper.selectById(dto.getId());
        if (message == null) {
            return null;
        }
        if (dto.getTitle() != null) {
            message.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            message.setContent(dto.getContent());
        }
        if (dto.getType() != null) {
            message.setType(dto.getType());
        }
        if (dto.getPriority() != null) {
            message.setPriority(dto.getPriority());
        }
        if (dto.getIsActive() != null) {
            message.setIsActive(dto.getIsActive());
        }
        if (dto.getPublishTime() != null) {
            message.setPublishTime(dto.getPublishTime());
        }
        if (dto.getExpireTime() != null) {
            message.setExpireTime(dto.getExpireTime());
        }
        message.setUpdatedTime(LocalDateTime.now());
        siteMessageMapper.updateById(message);
        return message;
    }

    public boolean deleteMessage(Long id) {
        return siteMessageMapper.deleteById(id) > 0;
    }

    public SiteMessage getMessageById(Long id) {
        return siteMessageMapper.selectById(id);
    }

    public List<SiteMessage> getAllMessages() {
        LambdaQueryWrapper<SiteMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SiteMessage::getPriority)
                .orderByDesc(SiteMessage::getCreatedTime);
        return siteMessageMapper.selectList(wrapper);
    }

    public List<SiteMessage> getActiveMessages() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SiteMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SiteMessage::getIsActive, true)
                .and(w -> w.isNull(SiteMessage::getPublishTime)
                        .or().le(SiteMessage::getPublishTime, now))
                .and(w -> w.isNull(SiteMessage::getExpireTime)
                        .or().ge(SiteMessage::getExpireTime, now))
                .orderByDesc(SiteMessage::getPriority)
                .orderByDesc(SiteMessage::getCreatedTime);
        return siteMessageMapper.selectList(wrapper);
    }

    public List<SiteMessage> getUnreadMessagesForUser(Long userId) {
        List<SiteMessage> activeMessages = getActiveMessages();
        List<Long> readMessageIds = getReadMessageIdsForUser(userId);
        return activeMessages.stream()
                .filter(m -> !readMessageIds.contains(m.getId()))
                .toList();
    }

    public List<Long> getReadMessageIdsForUser(Long userId) {
        LambdaQueryWrapper<SiteMessageRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SiteMessageRead::getUserId, userId);
        return siteMessageReadMapper.selectList(wrapper).stream()
                .map(SiteMessageRead::getMessageId)
                .toList();
    }

    public boolean markAsRead(Long messageId, Long userId) {
        // Check if already read
        LambdaQueryWrapper<SiteMessageRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SiteMessageRead::getMessageId, messageId)
                .eq(SiteMessageRead::getUserId, userId);
        SiteMessageRead existing = siteMessageReadMapper.selectOne(wrapper);
        if (existing != null) {
            return true;
        }

        SiteMessageRead read = new SiteMessageRead();
        read.setMessageId(messageId);
        read.setUserId(userId);
        read.setReadTime(LocalDateTime.now());
        return siteMessageReadMapper.insert(read) > 0;
    }

    public boolean markAllAsRead(Long userId) {
        List<SiteMessage> activeMessages = getActiveMessages();
        List<Long> readMessageIds = getReadMessageIdsForUser(userId);
        boolean success = true;
        for (SiteMessage message : activeMessages) {
            if (!readMessageIds.contains(message.getId())) {
                try {
                    markAsRead(message.getId(), userId);
                } catch (Exception e) {
                    log.error("Failed to mark message {} as read for user {}", message.getId(), userId, e);
                    success = false;
                }
            }
        }
        return success;
    }

    public boolean isMessageRead(Long messageId, Long userId) {
        LambdaQueryWrapper<SiteMessageRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SiteMessageRead::getMessageId, messageId)
                .eq(SiteMessageRead::getUserId, userId);
        return siteMessageReadMapper.selectCount(wrapper) > 0;
    }

    /**
     * 便捷方法：广播系统消息（用于系统告警、通知等）
     */
    public SiteMessage broadcastMessage(String title, String content, String type, int priority) {
        SiteMessage message = new SiteMessage();
        message.setTitle(title);
        message.setContent(content);
        message.setType(type != null ? type : "INFO");
        message.setPriority(priority);
        message.setIsActive(true);
        message.setCreatedTime(LocalDateTime.now());
        message.setUpdatedTime(LocalDateTime.now());
        message.setIsDeleted(false);
        siteMessageMapper.insert(message);
        log.info("Broadcast message sent: [{}] {}", type, title);
        return message;
    }

    /**
     * 检查最近是否已经发送过相同标题的告警（避免重复告警）
     */
    public boolean hasRecentSimilarAlert(String title, int minutes) {
        LocalDateTime timeAgo = LocalDateTime.now().minusMinutes(minutes);
        LambdaQueryWrapper<SiteMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SiteMessage::getTitle, title)
                .ge(SiteMessage::getCreatedTime, timeAgo);
        return siteMessageMapper.selectCount(wrapper) > 0;
    }
}
