package cc.shiyi.dashboard.controller;

import cc.shiyi.base.ai.utils.ConversationIdGenerator;
import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.Conversation;
import cc.shiyi.dashboard.service.ConversationService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI聊天记忆管理控制器
 * 提供对话记忆的查询、删除等管理功能
 */
@RestController
@Setter(onMethod_ = @Autowired)
@Tag(name = "AI聊天记忆管理接口")
@RequestMapping("/api/v1/ai/memory")
public class ChatMemoryController {

    private ChatMemory chatMemory;

    private ChatMemoryRepository chatMemoryRepository;

    private ConversationService conversationService;

    /**
     * 获取指定对话的历史消息
     *
     * @param conversationId 对话ID
     * @return 历史消息列表
     */
    @Operation(summary = "获取对话历史")
    @GetMapping("/history/{conversationId}")
    public ResponseWrapper<List<Message>> getConversationHistory(@PathVariable String conversationId) {
        // 验证用户是否有权限访问这个对话
        validateConversationOwnership(conversationId);
        List<Message> messages = chatMemory.get(conversationId);
        return new ResponseWrapper<>().success(messages);
    }

    /**
     * 删除指定对话的所有记忆
     *
     * @param conversationId 对话ID
     * @return 删除结果
     */
    @Operation(summary = "删除对话记忆")
    @DeleteMapping("/{conversationId}")
    public ResponseWrapper<Void> deleteConversationMemory(@PathVariable String conversationId) {
        // 验证用户是否有权限操作这个对话
        validateConversationOwnership(conversationId);
        chatMemory.clear(conversationId);
        conversationService.deleteConversation(conversationId);
        return new ResponseWrapper<>().success("对话记忆已删除");
    }

    /**
     * 获取当前用户的所有对话列表
     *
     * @return 对话列表
     */
    @Operation(summary = "获取当前用户的对话列表")
    @GetMapping("/conversations")
    public ResponseWrapper<List<Conversation>> getUserConversations() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Conversation> conversations = conversationService.
                getUserConversations(userId);
        return new ResponseWrapper<>().success(conversations);
    }

    /**
     * 删除当前用户的所有对话记忆
     *
     * @return 删除结果
     */
    @Operation(summary = "删除当前用户所有对话记忆")
    @DeleteMapping("/all")
    public ResponseWrapper<Map<String, Object>> deleteAllUserMemory() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Conversation> conversations = conversationService.getUserConversations(userId);

        // 逐个删除对话记忆和元数据
        for (Conversation conversation : conversations) {
            chatMemory.clear(conversation.getId());
            conversationService.deleteConversation(conversation.getId());
        }

        Map<String, Object> result = Map.of("success", true, "message", "所有对话记忆已删除", "count", conversations.size());
        return new ResponseWrapper<>().success(result);
    }

    /**
     * 更新对话标题
     *
     * @param conversationId 对话ID
     * @param request 包含新标题的请求体
     * @return 更新结果
     */
    @Operation(summary = "更新对话标题")
    @PutMapping("/{conversationId}/title")
    public ResponseWrapper<Void> updateConversationTitle(@PathVariable String conversationId, @RequestBody Map<String, String> request) {
        validateConversationOwnership(conversationId);
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return new ResponseWrapper<>().fail("对话不存在");
        }
        String newTitle = request.get("title");
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            conversation.setTitle(newTitle);
            conversationService.updateById(conversation);
            return new ResponseWrapper<>().success("标题已更新");
        }
        return new ResponseWrapper<>().fail("标题不能为空");
    }

    /**
     * 验证对话所有权
     * 确保当前用户是该对话的创建者
     *
     * @param conversationId 对话ID
     */
    private void validateConversationOwnership(String conversationId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long conversationOwnerId = ConversationIdGenerator.extractUserId(conversationId);
        if (!currentUserId.equals(conversationOwnerId)) {
            throw new RuntimeException("无权访问该对话");
        }
    }
}
