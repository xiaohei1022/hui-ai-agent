package com.ximi.huiaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * spring AI 调用大模型
 *
 */
@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel datascopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage call = datascopeChatModel.call(new Prompt("你好！我是小黑"))
                .getResult()
                .getOutput();
        System.out.println(call.getText());
    }
}
