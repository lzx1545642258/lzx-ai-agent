package com.lzx.lzxaiagent.sevice;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class LoveAppServiceImpl implements LoveAppService{

    @Resource
    private ChatClient chatClient;

    @Override
    public Flux<ServerSentEvent<String>> doChat(String chatId, String userMessage) {

        return chatClient.prompt()
                .user(userMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content()
                .map(content -> ServerSentEvent.<String>builder()
                        .data(content)
                        .event("message")
                        .build())
                .concatWith(doneEvent())
                .onErrorResume(this::errorEvent);
    }

    // 辅助方法:创建done事件
    private Flux<ServerSentEvent<String>> doneEvent() {
        return Flux.just(
                ServerSentEvent.<String>builder()
                        .data("[DONE]")
                        .event("done")
                        .build()
        );
    }

    // 辅助方法:创建error事件
    private Flux<ServerSentEvent<String>> errorEvent(Throwable error) {
        return Flux.just(
                ServerSentEvent.<String>builder()
                        .data("抱歉，服务暂时不可用")
                        .event("error")
                        .build()
        );
    }
}
