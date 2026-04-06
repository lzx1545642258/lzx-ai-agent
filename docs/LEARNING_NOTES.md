# 学习笔记

本文档记录项目开发过程中学到的新技术、知识点和最佳实践。

---

## 记录模板

复制以下模板用于新的学习记录：

```markdown
## YYYY-MM-DD: [技术名称/主题]

### 学习内容
简要说明学了什么

### 核心要点
- 关键点 1
- 关键点 2
- 关键点 3

### 实践应用
如何在当前项目中应用

### 注意事项
需要特别注意的地方

### 参考资料
相关文档链接、教程等

**标签：** #标签1 #标签2 #学习笔记
```

---

## 知识分类索引

### 框架与技术
- [Spring AI](#2026-04-05-spring-ai-chatmemory-持久化机制)
- [Maven](#2026-04-04-maven-wrapper-的使用)

### 编程语言
- [Java Stream API](#2026-04-02-java-stream-api-常用操作)

### 数据库
- MySQL 优化
- SQL 最佳实践

### 开发工具
- IDEA 技巧
- Git 工作流

### 设计模式
- 单例模式
- 工厂模式
- 观察者模式

---

*最后更新：2026-04-05*


## 2026-04-05：Spring AI Advisor

### 学习内容
- Advisor 是什么？
- 怎么使用Advisor？
- 如何实现自定义Advisor

### 核心要点
- Advisor就是Spring AI中的拦截器。在调用AI前和调用AI后可以执行一些额外的操作，比如：
  - 前置增强：调用AI前改写一下Prompt提示词，检查一下提示词是否安全
  - 后置增强：调用AI后记录一下日志，处理一下返回的结果
- Advisor可以在构建ChatClient时注册，也可以在运行时设置advisor参数
- 如果要实现自定义Advisor，只需要实现CallAdvisor, StreamAdvisor两个接口即可，然后在构造ChatClient时传入自定义Advisor对象即可。

### 实践应用
```java
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
```
实现自定义Advisor的核心就是实现CallAdvisor和StreamAdvisor两个接口，重写adviseStream()和adviseCall()两个方法。

```java
@Component
@Slf4j
public class LoveApp {

    private ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "you are a helpful assistant";

    public LoveApp(ChatModel dashscopeChatModel, JdbcChatMemoryRepository chatMemoryRepository){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        chatClient = builder.defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SensitiveWordAdvisor(), 
                        new MyLoggerAdvisor())
                .build();
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
```
经过debug发现，如果没有特意设置advisor的order值的话（都是0），那么Spring AI 会按照注册顺序的逆序来处理请求和响应。在该代码中，会先由MyLoggerAdvisor处理请求，再由SensitiveWordAdvisor处理请求，再由SensitiveWordAdvisor处理响应，最后由MyLoggerAdvisor处理响应。
MyLoggerAdvisor应该要放到最后（即最先处理请求，最后处理响应），这样在用户交互时输入违禁词时，同样可以打印出用户输入日志，反之则不行。

### 参考资料
[Spring AI Advisor](https://springdoc.cn/spring-ai/api/advisors.html)
https://www.codefather.cn/course/1915010091721236482/section/1916676331948027906

---

## 2026-04-05：record语法
record语法是在Java 14中引入的，它可以用来创建一个不可变类。record语法的语法结构如下：
```java
public record Person(String name, int age) {}
```
特点：
自动生成构造函数：你不需要写 this.chatResponse = chatResponse; 这种赋值代码，编译器会自动帮你做。
自动生成 Getter 方法：比如 chatResponse() 和 context()。注意，它们的名字没有 get 前缀，这是 record 的约定。
不可变性（Immutable）：一旦创建了这个对象，里面的属性就不能再改了（除非属性本身是可变的，比如 Map）。

---

## 2026-04-05：Builder
Builder模式是在Java 5中引入的，它可以用来创建一个对象，对象属性可以有默认值。Builder模式可以避免对象属性的赋值错误，比如：
```java
// 产品类：不可变 User
public class User {
    // 必选（final）
    private final long id;
    private final String username;
    // 可选
    private final Integer age;
    private final String email;

    // 私有构造器：强制走 Builder
    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.age = builder.age;
        this.email = builder.email;
    }

    // 静态入口：获取 Builder
    public static Builder builder() {
        return new Builder();
    }

    // 🔴 Builder 类（静态内部类）
    public static class Builder {
        // 复制所有字段
        private long id;
        private String username;
        private Integer age;
        private String email;

        // 链式 setter
        public Builder id(long id) {
            this.id = id;
            return this;
        }
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder age(Integer age) {
            this.age = age;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        // 🔴 最终构建 + 校验
        public User build() {
            if (id <= 0) throw new IllegalArgumentException("id 非法");
            if (username == null) throw new NullPointerException("username 不能为空");
            return new User(this);
        }
    }

    // 只提供 getter（不可变）
    public long getId() { return id; }
    public String getUsername() { return username; }
    public Integer getAge() { return age; }
    public String getEmail() { return email; }
}
```
使用（链式调用）：
```java
User user = User.builder()
        .id(1001)
        .username("zhangsan")
        .age(28)
        .email("zhangsan@example.com")
        .build();
```

---

## 2026-04-05：提示词模板PromptTemplate
PromptTemplate是SpringAI框架中用于构建和管理提示词的核心组件。允许开发者创建带有占位符的文本模板，然后在运行时动态替换这些占位符。
```java
PromptTemplate promptTemplate = new PromptTemplate("Tell me a {adjective} joke about {topic}");
Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));
return chatModel.call(prompt).getResult();
```
```java
String userText = """
    Tell me about three famous pirates from the Golden Age of Piracy and why they did.
    Write at least a sentence for each pirate.
    """;

Message userMessage = new UserMessage(userText);

String systemText = """
  You are a helpful AI assistant that helps people find information.
  Your name is {name}
  You should reply to the user's request with your name and also in the style of a {voice}.
  """;

SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

List<Generation> response = chatModel.call(prompt).getResults();
```
Spring AI 支持 org.springframework.core.io.Resource 抽象，因此可将提示数据存入文件并直接用于 PromptTemplate。例如：在 Spring 托管组件中定义字段来获取 Resource。
```java
@Value("classpath:/prompts/system-message.st")
private Resource systemResource;
```

### 实践应用
- 在src/main/resources下创建prompts目录，并创建system-message.st文件。
- 将资源作为构造函数参数注入
- 构建提示词模板
- 将提示词加入chatClient
```java
public LoveApp(ChatModel dashscopeChatModel, JdbcChatMemoryRepository chatMemoryRepository, 
              @Value("classpath:/prompts/system-message.st") Resource systemPrompt) {
    Map<String, Object> variables = Map.of("field", "恋爱", "type", "单身");
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
    Message systemMessage = systemPromptTemplate.createMessage(variables);
    String renderedSystemPrompt = systemMessage.getText();
    
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
```
### 注意
- system-message.st应该在src/main/resources目录下，而不是 src/main/java/com/xxx/aiagent/prompts/下
- @Value注入资源应该在构造函数作为参数注入，而不是作为一个字段进行注入。否则构造函数会在资源注入之前就完成，导致资源注入失败

---

## 2026-04-06：CORS

### 概念
CORS（Cross-Origin Resource Sharing，跨域资源共享）是一种浏览器安全机制，用于控制网页从一个源（域名、协议、端口）请求另一个源的资源。

### 配置方法
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 所有路径
                .allowedOrigins("http://localhost:3000")  // 允许的源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 允许携带凭证
                .maxAge(3600);  // 预检请求缓存时间
    }
}
```

---

## 2026-04-07: 
