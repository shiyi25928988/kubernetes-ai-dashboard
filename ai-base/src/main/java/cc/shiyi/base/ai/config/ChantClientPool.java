package cc.shiyi.base.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

/**
 * 提供共享的 ChatClient（内置 JDBC 聊天记忆 advisor）。
 * Spring AI 1.1.6 起 conversationId 不再支持在 advisor 构建时固定，
 * 需在每次请求时通过 advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)) 传入。
 */
@Component
public class ChantClientPool {

    private final ChatClient chatClient;

    public ChantClientPool(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public ChatClient get() {
        return chatClient;
    }
}
