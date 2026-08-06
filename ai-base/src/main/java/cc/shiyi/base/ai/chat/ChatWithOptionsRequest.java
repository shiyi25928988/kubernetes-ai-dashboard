package cc.shiyi.base.ai.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.openai.OpenAiChatOptions;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatWithOptionsRequest {

    private String message;
    private OpenAiChatOptions options;

}
