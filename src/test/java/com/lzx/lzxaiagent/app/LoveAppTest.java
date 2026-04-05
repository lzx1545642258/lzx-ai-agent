package com.lzx.lzxaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String id = UUID.randomUUID().toString();
        String content = loveApp.doChat("你好，我是程序员李泽翔", id);
        Assertions.assertNotNull(content);
    }

    @Test
    void doChatWithReport() {
        String id = UUID.randomUUID().toString();
        LoveApp.LoveReport report = loveApp.doChatWithReport("我是程序员李泽翔，我想让另一半更爱我，但我不知道怎么做", id);
    }

    @Test
    void doChatWithSensitiveWords() {
        String id = UUID.randomUUID().toString();
        String content = loveApp.doChatWithSensitiveWords("你好，我是李泽翔,色情", id);
        System.out.println(content);
    }
}