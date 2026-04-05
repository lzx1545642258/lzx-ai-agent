package com.lzx.lzxaiagent.study;

import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
public class PromptTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Test
    void testPromptTemplate() {
        String adjective = "funny";
        String topic = "chickens";

        PromptTemplate promptTemplate = new PromptTemplate("Tell me a {adjective} joke about {topic}");

        Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));

        String text = dashscopeChatModel.call(prompt).getResult().getOutput().getText();
        log.info("text: {}", text);
    }

    @Test
    void testPromptTemplateWithSystem() {

        String name = "刘慈欣";
        String voice = "文言文";

        String userText = """
跟我说说海盗黄金时代的三个著名海盗，以及他们为什么这么做。
为每个海盗至少写一句话。
    """;
        Message userMessage = new UserMessage(userText);
        String systemText = """
你是一名乐于助人的人工智能助手，负责帮助人们查找信息。你的名字是 {name}。你在回应用户的请求时，应报上自己的名字，并且采用 {voice} 的风格进行回复。
  """;
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        String text = dashscopeChatModel.call(prompt).getResult().getOutput().getText();
        log.info("text: {}", text);
    }
}
