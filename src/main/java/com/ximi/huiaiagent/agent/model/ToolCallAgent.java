package com.ximi.huiaiagent.agent.model;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{
    // 可用工具
    private final ToolCallback[] availableTools;

    // 工具调用结果
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessages().add(userMessage);
        }
        List<Message> messages = getMessages();
        Prompt prompt = new Prompt(messages, chatOptions);

        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应， 用于act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String text = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();

            log.info(getName() + "的思考: " + text);
            log.info(getName() + "的调用工具: " + toolCalls);
            String toolCallInfo = toolCalls.stream().map(toolCall ->
                    String.format("工具名称：%s: 参数：%s", toolCall.name(), toolCall.arguments())
            ).collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            if (toolCalls.isEmpty()) {
                getMessages().add(assistantMessage);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessages().add(new AssistantMessage("处理遇到问题：" + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具可调用";
        }

        // 调用工具
        Prompt prompt = new Prompt(getMessages(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文
        setMessages(toolExecutionResult.conversationHistory());
        // 当前工具调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String result = toolResponseMessage.getResponses().stream()
                .map(responses -> "工具: " + responses.name() + "完成了它的任务！结果：" + responses.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否包含终止工具内容
        boolean b = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (b) {
           setState(AgentState.FINISHED);
        }
        log.info(result);
        return result;
    }
}
