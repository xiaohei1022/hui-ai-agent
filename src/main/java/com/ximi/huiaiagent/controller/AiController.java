package com.ximi.huiaiagent.controller;

import com.ximi.huiaiagent.agent.model.HeiManus;
import com.ximi.huiaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping( "/love_app/chat")
    public String doChat(String userMessage, String chatId) {
        return loveApp.doChat(userMessage, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatFlux(String message, String chatId) {
        return loveApp.doChatByFlux(message, chatId);
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        loveApp.doChatByFlux(message, chatId)
                .subscribe(chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @GetMapping("/manus/chat")
    public SseEmitter doChatManus(String message) {
        HeiManus heiManus = new HeiManus(allTools, dashscopeChatModel);
        return heiManus.runStream(message);
    }
}
