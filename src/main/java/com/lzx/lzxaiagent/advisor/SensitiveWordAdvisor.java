package com.lzx.lzxaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class SensitiveWordAdvisor implements CallAdvisor, StreamAdvisor {

    // 简单的敏感词列表
    private static final List<String> SENSITIVE_WORDS = List.of(
            "敏感词",
            "暴力",
            "色情",
            "赌博",
            "毒品"
    );

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        Boolean hasSensitiveWords = containsSentitiveWords(chatClientRequest.prompt().getUserMessage().getText());
        if (hasSensitiveWords){
            ChatClientResponse chatClientResponse = ChatClientResponse.builder()
                    .chatResponse(null)
                    .context(Map.of("message","检测到敏感词，请重新输入"))
                    .build();
            return chatClientResponse;
        } else {
            ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
            return chatClientResponse;
        }
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Boolean hasSensitiveWords = containsSentitiveWords(chatClientRequest.prompt().getUserMessage().getText());
        if (hasSensitiveWords){
            ChatClientResponse chatClientResponse = ChatClientResponse.builder()
                    .chatResponse(null)
                    .context(Map.of("message","检测到敏感词，请重新输入"))
                    .build();
            return Flux.just(chatClientResponse);
        }else {
            return streamAdvisorChain.nextStream(chatClientRequest);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }


    private Boolean containsSentitiveWords(String text) {
        if (text == null || text.isEmpty()){
            return false;
        }
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (text.contains(sensitiveWord)){
                return true;
            }
        }
        return false;
    }
}
