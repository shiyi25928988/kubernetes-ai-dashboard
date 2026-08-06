package cc.shiyi.base.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemChatRequest {
    private String conversationId;
    private String systemMessage;
    private String userMessage;
}
