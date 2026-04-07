# LZX AI Agent

## 项目简介

LZX AI Agent是一个基于Spring Boot和Spring AI框架开发的智能代理系统，集成了阿里巴巴DashScope AI模型，提供了丰富的AI能力和交互界面。

## 技术栈

### 后端
- Java 21
- Spring Boot 3.5.13
- Spring AI 1.1.0
- MyBatis Plus 3.5.12
- MySQL
- Lombok
- Hutool
- Knife4j (API文档)

### 前端
- React
- TypeScript
- Vite
- Tailwind CSS

## 项目结构

```
lzx-ai-agent/
├── .mvn/                # Maven包装器
├── docs/                # 文档目录
│   ├── BUG_FIXES.md     #  bug修复记录
│   └── LEARNING_NOTES.md # 学习笔记
├── lzx-ai-agent-fronted/ # 前端项目
│   ├── src/             # 前端源代码
│   └── package.json     # 前端依赖配置
├── src/                 # 后端源代码
│   ├── main/java/com/lzx/lzxaiagent/ # 后端Java代码
│   │   ├── advisor/     # 切面类
│   │   ├── app/         # 应用核心类
│   │   ├── config/      # 配置类
│   │   ├── controller/  # 控制器
│   │   ├── service/     # 服务层
│   │   └── LzxAiAgentApplication.java # 应用入口
│   ├── main/resources/  # 资源文件
│   │   ├── prompts/     # 提示模板
│   │   └── application.yml # 应用配置
│   └── test/            # 测试代码
├── .gitattributes       # Git属性配置
├── .gitignore           # Git忽略配置
├── mvnw                 # Maven包装器脚本
├── mvnw.cmd             # Maven包装器脚本(Windows)
├── pom.xml              # Maven项目配置
└── README.md            # 项目说明文档
```

## 核心功能

1. **AI代理框架**：基于Spring AI和阿里巴巴Agent框架，提供智能代理能力
2. **DashScope集成**：集成阿里巴巴DashScope AI模型
3. **RESTful API**：提供标准化的API接口
4. **前端交互界面**：基于React的用户交互界面
5. **敏感词过滤**：内置敏感词过滤功能
6. **日志记录**：完善的日志记录系统

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+

### 安装与运行

#### 后端安装

1. 克隆项目
   ```bash
   git clone <项目地址>
   cd lzx-ai-agent
   ```

2. 配置数据库
   - 创建MySQL数据库
   - 修改 `src/main/resources/application.yml` 中的数据库配置

3. 构建项目
   ```bash
   ./mvnw clean install
   ```

4. 运行项目
   ```bash
   ./mvnw spring-boot:run
   ```

#### 前端安装

1. 进入前端目录
   ```bash
   cd lzx-ai-agent-fronted
   ```

2. 安装依赖
   ```bash
   npm install
   ```

3. 运行前端
   ```bash
   npm run dev
   ```

### API文档

项目集成了Knife4j，启动后端服务后，可通过以下地址访问API文档：

```
http://localhost:8080/doc.html
```

## 配置说明

### 主要配置文件

- `src/main/resources/application.yml`：应用主配置文件
- `src/main/resources/prompts/system-message.st`：系统提示模板

### 环境变量

| 环境变量 | 描述 | 默认值 |
|---------|------|-------|
| `DASHSCOPE_API_KEY` | DashScope API密钥 | - |
| `SPRING_DATASOURCE_URL` | 数据库连接URL | - |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | - |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | - |

## 开发指南

### 代码规范

- 遵循Java代码规范
- 使用Lombok简化代码
- 采用三层架构：控制器(Controller) → 服务(Service) → 数据访问层

### 新增功能

1. 在 `src/main/java/com/lzx/lzxaiagent/` 下创建相应的类
2. 在 `src/main/resources/` 中添加必要的配置
3. 在前端 `lzx-ai-agent-fronted/src/` 中添加相应的组件和服务

## 测试

### 运行单元测试

```bash
./mvnw test
```

### 运行集成测试

```bash
./mvnw verify
```

## 部署

### 构建生产版本

1. 后端构建
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. 前端构建
   ```bash
   cd lzx-ai-agent-fronted
   npm run build
   ```

### 部署方式

- **Docker容器化部署**
- **传统服务器部署**
- **云服务部署**

## 常见问题

### Q: 启动失败怎么办？
A: 检查数据库连接配置和DashScope API密钥是否正确

### Q: 前端无法连接后端？
A: 检查CORS配置和后端服务是否正常运行

### Q: AI模型调用失败？
A: 检查DashScope API密钥是否有效，以及网络连接是否正常

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 许可证

本项目采用MIT许可证。

## 联系方式

- 项目维护者：LZX
- 邮箱：[your-email@example.com]
- 项目地址：[https://github.com/yourusername/lzx-ai-agent](https://github.com/yourusername/lzx-ai-agent)
