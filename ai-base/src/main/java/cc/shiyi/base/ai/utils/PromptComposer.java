package cc.shiyi.base.ai.utils;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;

public class PromptComposer {

    /**
     * 组合生成一个包含系统消息和用户消息的提示对象
     *
     * @param query 用户的查询问题
     * @param documents 相关文档列表
     * @return 包含系统消息和用户消息的提示对象
     */
    public static Prompt compose(String query, List<Document> documents) {
        String documentContent = documents.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));
        SystemMessage systemMessage = new SystemMessage("You are a helpful assistant. Use the provided documents to answer the user's question accurately.");
        UserMessage userMessage = new UserMessage("Question: " + query + "\n\nRelevant Documents:\n" + documentContent);
        Prompt prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .build();
        return prompt;
    }

}
