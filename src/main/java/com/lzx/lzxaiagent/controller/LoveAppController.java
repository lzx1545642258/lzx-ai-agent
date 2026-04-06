package com.lzx.lzxaiagent.controller;

import com.lzx.lzxaiagent.sevice.LoveAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequestMapping("/love")
@Tag(name = "恋爱应用", description = "恋爱相关的API接口")
public class LoveAppController {

    @Autowired
    private LoveAppService loveAppService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> doChat(String chatId, String userMessage) {
        log.info("💬 收到聊天请求 - chatId: {}, 消息: {}", chatId, userMessage);
        Flux<ServerSentEvent<String>> aiResponse = loveAppService.doChat(chatId, userMessage);
        return aiResponse;
    }
}
