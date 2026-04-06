# Bug 修复记录

本文档记录项目开发过程中遇到的 Bug 及其解决方案，便于后续查阅和避免重复踩坑。

---


## 记录模板

复制以下模板用于新的 Bug 记录：

```markdown
## YYYY-MM-DD: [Bug 简短描述]

### 问题现象
描述遇到的错误信息或异常行为

### 原因分析
分析问题产生的根本原因

### 解决方案
具体如何修复的，包括代码示例

### 经验总结
学到了什么，如何避免类似问题

**标签：** #标签1 #标签2 #BugFix
```

---

## 常见问题分类

- **依赖问题：** Maven、Gradle 相关
- **配置问题：** application.yml、环境变量
- **代码逻辑：** 空指针、类型转换、业务逻辑错误
- **数据库：** SQL 错误、连接问题、事务
- **框架集成：** Spring AI、第三方 SDK
- **环境问题：** JDK 版本、操作系统差异

---

## 2026-04-05：prompt位置错误

### 问题现象
resource cannot be null

### 原因分析
我通过下面代码注入prompt
```java
    @Value("classpath:/prompts/system-message.st")
    private Resource systemPrompt;
```
这里默认地址应该是src/main/resources/prompts/system-message.st，但是我把文件放到了src/main/java/com/lzx/lzxaiagent/prompts/system-message.st

### 解决方案
修改注入地址为src/main/resources/prompts/system-message.st

### 经验总结
一般资源文件放在src/main/resources下，代码文件放在src/main/java下

---

## 2026-04-05：在构造函数前注入资源错误

### 问题现象
resource cannot be null

### 原因分析
代码原来是这样写的：

```java
import com.lzx.lzxaiagent.app.LoveApp;

@Value("classpath:/prompts/system-message.st")
private Resource systemPrompt;

public LoveApp(ChatModel dashscopeChatModel, JdbcChatMemoryRepository chatMemoryRepository){
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
这样可能导致Spring 的 @Value 注入在构造函数执行时可能还未完成。

### 解决方案
**方案一**：将资源作为构造函数参数注入，确保在对象创建时就有可用的资源（推荐）
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
**方案二**：使用 @PostConstruct 注解，在构造函数执行完成后执行初始化逻辑
```java
@Value("classpath:/prompts/system-message.st")
private Resource systemPrompt;

private ChatModel dashscopeChatModel;
private JdbcChatMemoryRepository chatMemoryRepository;

public LoveApp(ChatModel dashscopeChatModel, JdbcChatMemoryRepository chatMemoryRepository) {
    this.dashscopeChatModel = dashscopeChatModel;
    this.chatMemoryRepository = chatMemoryRepository;
}

@PostConstruct
public void init() {
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
### 经验总结
Spring 的 @Value 注入通常在构造函数执行时完成，因此如果对象在构造函数执行期间需要使用这些资源，则需要确保这些资源在构造函数执行期间已就绪。

---

