package com.ximi.huiaiagent.agent.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象的代理类，用于管理代理状态和执行流程
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 默认状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory(自主维护上下文会话)
    private List<Message> messages = new ArrayList<>();

    public String run(String userPrompt) {
        // 基础参数校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run with emppty user prompt");
        }

        // 修改状态
        this.state = AgentState.RUNNING;
        // 记录上下文消息
        this.messages.add(new UserMessage(userPrompt));
        // 保存结果
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                int step = i + 1;
                currentStep = step;

                // 单步执行
                String stepResult = this.step();
                String result = "Step" + step + ": " + stepResult;
                results.add(result);
            }

            // 检查是否超出最大限制
            if (currentStep >= maxSteps) {
                this.state = AgentState.FINISHED;
                results.add("Max steps reached" + maxSteps);
            }
            return String.join("\n", results);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("Error running agent", e);
            return "Error running agent: " + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L);

        // 使用异步线程处理，避免主线程阻塞
        CompletableFuture.runAsync(() -> {
            // 基础参数校验
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("当前状态不支持：" + this.state);
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("提示词不能为空");
                    emitter.complete();
                    return;
                }

                // 修改状态
                this.state = AgentState.RUNNING;
                // 记录上下文消息
                this.messages.add(new UserMessage(userPrompt));
                try {
                    for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                        int step = i + 1;
                        currentStep = step;

                        // 单步执行
                        String stepResult = this.step();
                        String result = "Step" + step + ": " + stepResult;
                        emitter.send(result);
                    }

                    // 检查是否超出最大限制
                    if (currentStep >= maxSteps) {
                        this.state = AgentState.FINISHED;
                        emitter.send("已超出最大步骤限制：" + maxSteps);
                    }
                    emitter.complete();
                } catch (Exception e) {
                    this.state = AgentState.ERROR;
                    log.error("Error running agent", e);
                    emitter.send("执行错误：" + e.getMessage());
                    emitter.complete();
                } finally {
                    this.cleanup();
                }
            } catch (IOException e) {
                emitter.completeWithError(e);
            }

            // 超时处理
            emitter.onTimeout(() -> {
                this.state = AgentState.ERROR;
                this.cleanup();
                log.warn("处理超时");
            });

            emitter.onCompletion(() -> {
                if (this.state == AgentState.RUNNING) {
                    this.state = AgentState.FINISHED;
                }
                this.cleanup();
                log.info("处理完成");
            });
        });
        return emitter;
    }

    /**
     * 执行单个步骤
     * @return
     */
    public abstract String step();

    /*
     * 清理资源
     */
    protected void cleanup() {

    }
}
