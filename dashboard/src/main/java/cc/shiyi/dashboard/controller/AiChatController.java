package cc.shiyi.dashboard.controller;


import cc.shiyi.base.ai.chat.ChatRequest;
import cc.shiyi.base.ai.chat.ParameterizedChatRequest;
import cc.shiyi.base.ai.chat.SystemChatRequest;
import cc.shiyi.base.ai.config.ChantClientPool;
import cc.shiyi.base.ai.utils.ConversationIdGenerator;
import cc.shiyi.dashboard.service.KnowledgeIndexService;
import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.Conversation;
import cc.shiyi.dashboard.service.AttachmentParserService;
import cc.shiyi.dashboard.service.ConversationService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@Setter(onMethod_ = @Autowired)
@Tag(name = "AI对话接口-ChatController")
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private ChantClientPool chatClientPool;

    private ToolCallbackProvider toolCallbackProvider;

    private ConversationService conversationService;

    private AttachmentParserService attachmentParserService;

    private KnowledgeIndexService knowledgeIndexService;

    /**
     * 简单聊天接口
     */
    @Operation(summary = "简单聊天接口")
    @RequestMapping(value = "/simpleChat", method = RequestMethod.POST)
    public ResponseWrapper<String> simpleChat(@RequestBody ChatRequest request) {

        Long userId = StpUtil.getLoginIdAsLong();

        ChatClient chatClient = chatClientPool.get();
        String response = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                .user(request.getMessage())
                .call()
                .content();

        // 更新对话元数据
        updateConversationMetadata(userId, request.getConversationId(), request.getMessage(), response);

        return new ResponseWrapper<>().success(response);
    }

    /**
     * 流式聊天接口
     */
    @Operation(summary = "流式聊天接口")
    @RequestMapping(value = "/chat", method = RequestMethod.POST, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {

        Long userId = StpUtil.getLoginIdAsLong();

        ChatClient chatClient = chatClientPool.get();
        StringBuilder fullResponse = new StringBuilder();

        var promptBuilder = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()));
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            promptBuilder.system(request.getSystemPrompt());
        } else {
            // 未指定专家系统提示时，自动检索知识库命中内容注入系统语境（简单 RAG）
            String knowledgePrompt = buildKnowledgeSystemPrompt(request.getMessage());
            if (!knowledgePrompt.isEmpty()) {
                promptBuilder.system(knowledgePrompt);
            }
        }

        // 处理附件，解析文件内容并注入 prompt
        String userMessage = request.getMessage();
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            String attachmentContent = attachmentParserService.parseAttachments(request.getAttachments());
            userMessage = attachmentContent + userMessage;
        }

        return promptBuilder
                .user(userMessage)
                .toolCallbacks(toolCallbackProvider)
                .stream()
                .content()
                .doOnNext(content -> fullResponse.append(content))
                .doOnComplete(() -> updateConversationMetadata(userId, request.getConversationId(), request.getMessage(), fullResponse.toString()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 带参数的聊天接口
     */
    @Operation(summary = "带参数的聊天接口")
    @PostMapping("/parameterizedChat")
    public Flux<String> parameterizedChat(@RequestBody ParameterizedChatRequest request) {

        Long userId = StpUtil.getLoginIdAsLong();

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(request.getTemplate())
                .variables(request.getParameters())
                .renderer(StTemplateRenderer.builder().build())
                .build();
        ChatClient chatClient = chatClientPool.get();
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt(promptTemplate.create())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                .stream()
                .content()
                .doOnNext(content -> fullResponse.append(content))
                .doOnComplete(() -> updateConversationMetadata(userId, request.getConversationId(),
                        request.getParameters().toString(), fullResponse.toString()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 带系统提示的聊天接口
     */
    @Operation(summary = "带系统提示的聊天接口")
    @RequestMapping(value = "/chatWithSystemPrompt", method = RequestMethod.POST, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithSystemPrompt(@RequestBody SystemChatRequest request) {

        Long userId = StpUtil.getLoginIdAsLong();

        ChatClient chatClient = chatClientPool.get();
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, request.getConversationId()))
                .system(request.getSystemMessage())
                .user(request.getUserMessage())
                .stream()
                .content()
                .doOnNext(content -> fullResponse.append(content))
                .doOnComplete(() -> updateConversationMetadata(userId, request.getConversationId(),
                        request.getUserMessage(), fullResponse.toString()))
                .subscribeOn(Schedulers.boundedElastic());
    }



    @Operation(summary = "生成新的conversation ID")
    @GetMapping("/generateNewConversationId")
    public ResponseWrapper<String> generateNewConversationId() {
        String conversationId = ConversationIdGenerator.generateNewId(StpUtil.getLoginIdAsLong());
        return new ResponseWrapper<>().success(conversationId, "");
    }

    /**
     * 从知识库检索与用户问题相关的内容，组装系统级 RAG 提示；
     * 无命中或检索异常时返回空串（不打扰正常对话）。
     */
    private String buildKnowledgeSystemPrompt(String userMessage) {
        try {
            List<KnowledgeIndexService.KnowledgeHit> hits = knowledgeIndexService.search(userMessage, 3);
            if (hits.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("以下是知识库中与用户问题可能相关的内容，回答时优先参考这些内容，并注意与用户问题的相关性：\n\n");
            for (KnowledgeIndexService.KnowledgeHit hit : hits) {
                sb.append("### ").append(hit.title());
                if (hit.category() != null && !hit.category().isBlank()) {
                    sb.append("（").append(hit.category()).append("）");
                }
                sb.append("\n").append(hit.snippet() == null ? "" : hit.snippet()).append("\n\n");
            }
            log.debug("知识库RAG注入: hits={}", hits.size());
            return sb.toString();
        } catch (Exception e) {
            log.warn("知识库RAG检索失败（忽略继续对话）: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 更新对话元数据
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param assistantResponse 助手回复
     */
    private void updateConversationMetadata(Long userId, String conversationId, String userMessage, String assistantResponse) {
        // 检查对话是否已存在
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            // 新对话，使用用户消息作为标题
            String title = userMessage.length() > 30 ? userMessage.substring(0, 30) + "..." : userMessage;
            conversationService.createConversation(userId, conversationId, title);
        }

        // 更新最后消息
        conversationService.updateLastMessage(conversationId, assistantResponse);
    }

}
