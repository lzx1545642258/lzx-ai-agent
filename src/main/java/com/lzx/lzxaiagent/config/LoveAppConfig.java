package com.lzx.lzxaiagent.config;

import com.lzx.lzxaiagent.advisor.MyLoggerAdvisor;
import com.lzx.lzxaiagent.advisor.SensitiveWordAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoveAppConfig {

    private static final String SYSTEM_PROMPT = "你叫林微，扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

//    private static final String SYSTEM_PROMPT = "you are a helpful assistant.";

    @Bean
    public ChatClient chatClient(ChatModel dashscopeChatModel, JdbcChatMemoryRepository chatMemoryRepository) {

        // 创建一个消息窗口的聊天记忆
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        MessageChatMemoryAdvisor chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(messageWindowChatMemory).build();

        // 创建一个 ChatClient 对象，并设置默认的 system prompt 和默认的 ChatMemoryAdvisor
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        chatMemoryAdvisor,
                        new SensitiveWordAdvisor(),
                        new MyLoggerAdvisor())
                .build();
        return chatClient;
    }
}
