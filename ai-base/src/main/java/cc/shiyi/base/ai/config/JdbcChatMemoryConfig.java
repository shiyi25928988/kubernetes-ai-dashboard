package cc.shiyi.base.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;

@Configuration
public class JdbcChatMemoryConfig {

    /**
     * 创建JdbcChatMemoryRepository实例的Bean工厂方法
     * 该方法配置并构建一个基于JDBC的聊天记忆存储库，用于持久化聊天对话历史
     *
     * @param jdbcTemplate JDBC模板，用于执行数据库操作
     * @return 配置完成的JdbcChatMemoryRepository实例
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new MysqlChatMemoryRepositoryDialect())
                .transactionManager(new JdbcTransactionManager(jdbcTemplate.getDataSource()))
                .build();

    }

    /**
     * 创建并配置ChatMemory Bean实例
     * 该方法用于初始化聊天记忆存储组件，基于消息窗口机制实现
     *
     * @param chatMemoryRepository 聊天记忆存储仓库，用于持久化聊天记录
     * @return 配置完成的ChatMemory实例，支持基于消息窗口的记忆管理
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        // 保留最近100条消息作为上下文
        return MessageWindowChatMemory.builder().chatMemoryRepository(chatMemoryRepository).maxMessages(100).build();
    }
}
