package com.ximi.huiaiagent.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String id = UUID.randomUUID().toString();

        //  1
        String message = "你好！我是小黑";
        String s = loveApp.doChat(message, id);
        Assertions.assertNotNull(s);

        // 2
        message = "我想让我的另一半酱更加爱我";
        String s1 = loveApp.doChat(message, id);
        Assertions.assertNotNull(s1);

        // 3
        message = "我的另一半叫什么来着？刚我跟你说过，帮我回忆一下";
        String s2 = loveApp.doChat(message, id);
        Assertions.assertNotNull(s2);
    }
}