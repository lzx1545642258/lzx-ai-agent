package com.lzx.lzxaiagent.sevice;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public interface LoveAppService {
    Flux<ServerSentEvent<String>> doChat(String chatId, String userMessage);
}
