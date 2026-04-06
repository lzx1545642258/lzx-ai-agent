package com.lzx.lzxaiagent.app;

import com.lzx.lzxaiagent.advisor.MyLoggerAdvisor;
import com.lzx.lzxaiagent.advisor.SensitiveWordAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LoveApp {

    private ChatClient chatClient;


//    private static final String SYSTEM_PROMPT = "你叫林微，扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

//    private static final String SYSTEM_PROMPT = "you are a helpful assistant";

    public LoveApp(ChatModel dashscopeChatModel, JdbcChatMemoryRepository chatMemoryRepository,@Value("classpath:/prompts/system-message.st") Resource systemPrompt){

        Map<String, Object> variables = Map.of("field", "恋爱", "type", "单身");

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        String renderedSystemPrompt = systemPromptTemplate.createMessage(variables).getText();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        chatClient = builder.defaultSystem(renderedSystemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SensitiveWordAdvisor(),
                        new MyLoggerAdvisor())
                .build();
    }

    public String doChat(String userInput, String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(userInput)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions){}

    public LoveReport doChatWithReport(String userInput, String chatId){
        LoveReport loveReport = chatClient.prompt()
//                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .system("每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(userInput)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("content: {}", loveReport);
        return loveReport;
    }

    public String doChatWithSensitiveWords(String userInput, String chatId){
        ChatClientResponse chatClientResponse = chatClient.prompt()
                .user(userInput)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatClientResponse();
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if (chatResponse == null){
            return chatClientResponse.context().get("message").toString();
        }
        else {
            Object message = chatClientResponse.context().get("message");
            return message != null ? message.toString() : "未知错误";
        }
    }
}
