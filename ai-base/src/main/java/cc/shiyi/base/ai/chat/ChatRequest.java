package cc.shiyi.base.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String conversationId;
    private String message;
    private String systemPrompt;
    private java.util.List<java.util.Map<String, Object>> attachments;
}
