package cc.shiyi.base.ai.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChantClientPool {

    //private static Map<String, ChatClient> pool = new ConcurrentHashMap<>();

    private static Cache<String, ChatClient> pool =
            Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(30, java.util.concurrent.TimeUnit.MINUTES)
                    .build();

    @Autowired
    ChatClient.Builder builder;

    @Autowired
    ChatMemory chatMemory;

    public ChatClient get(String conversationId) {
        ChatClient chatClient = pool.getIfPresent(conversationId);
        if (Objects.nonNull(chatClient)) {
            return chatClient;
        } else {
            chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(conversationId).build()).build();
            pool.put(conversationId, chatClient);
            return chatClient;
        }
    }
}
