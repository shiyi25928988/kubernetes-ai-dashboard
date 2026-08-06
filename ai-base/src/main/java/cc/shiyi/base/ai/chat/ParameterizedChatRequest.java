package cc.shiyi.base.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterizedChatRequest {
    private String conversationId;
    private String template;
    private Map<String, Object> parameters;
}
