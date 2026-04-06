# 前端开发文档：AI智能体聊天应用

## 1. 项目概述

### 1.1 项目背景
开发一个前端应用，允许用户选择不同的AI智能体并与它们进行对话。目前暂定包含两个智能体：恋爱大师智能体和测试智能体。

### 1.2 核心功能
- 智能体选择：用户可以选择与哪个智能体进行对话
- 聊天功能：与选定的智能体进行实时对话
- 响应式设计：适配不同设备尺寸

## 2. 技术栈选择

### 2.1 前端框架
- **框架**：React
- **语言**：TypeScript
- **构建工具**：Vite
- **样式方案**：Tailwind CSS

### 2.2 依赖库
- **状态管理**：React Context API + useReducer
- **SSE处理**：原生EventSource API
- **UI组件**：shadcn/ui
- **图标**：Lucide React

## 3. 项目结构

```
├── src/
│   ├── components/
│   │   ├── AgentSelector.tsx       # 智能体选择组件
│   │   ├── ChatInterface.tsx       # 聊天界面组件
│   │   ├── MessageList.tsx         # 消息列表组件
│   │   ├── MessageInput.tsx        # 消息输入组件
│   │   └── Layout.tsx              # 布局组件
│   ├── contexts/
│   │   └── ChatContext.tsx         # 聊天状态管理
│   ├── services/
│   │   └── api.ts                  # API调用服务
│   ├── types/
│   │   └── index.ts                # 类型定义
│   ├── App.tsx                     # 应用入口
│   ├── main.tsx                    # 应用渲染
│   └── index.css                   # 全局样式
├── public/                         # 静态资源
├── package.json                    # 项目配置
├── tsconfig.json                   # TypeScript配置
├── vite.config.ts                  # Vite配置
├── .gitignore                      # Git忽略文件
└── README.md                       # 项目说明
```

## 4. 组件设计

### 4.1 AgentSelector 组件
- **功能**：展示可用的智能体列表，允许用户选择
- **UI**：卡片式布局，每个智能体显示名称和简短描述
- **交互**：点击智能体卡片后切换到聊天界面

### 4.2 ChatInterface 组件
- **功能**：聊天主界面，包含消息列表和输入框
- **布局**：顶部显示当前智能体信息，中部显示消息历史，底部为输入区域
- **响应式**：在移动设备上优化布局

### 4.3 MessageList 组件
- **功能**：显示聊天消息历史
- **样式**：区分用户消息和智能体消息
- **滚动**：自动滚动到最新消息

### 4.4 MessageInput 组件
- **功能**：输入消息并发送
- **特性**：支持回车键发送，显示发送状态

## 5. 状态管理

### 5.1 状态结构
```typescript
interface ChatState {
  currentAgent: 'love' | 'test' | null;
  chatId: string | number;
  messages: Message[];
  isLoading: boolean;
  isStreaming: boolean;
  streamingMessageId: string | null;
  error: string | null;
}

interface Message {
  id: string;
  content: string;
  sender: 'user' | 'agent';
  timestamp: number;
  isComplete: boolean;
}
```

### 5.2 状态管理实现
使用 React Context API + useReducer 管理应用状态，提供以下功能：
- 智能体选择
- 消息发送与接收
- 流式消息处理（累积更新）
- 加载状态管理
- 错误处理

### 5.3 SSE 消息处理逻辑
1. 用户发送消息时，立即添加到消息列表
2. 初始化 SSE 连接，设置 `isStreaming` 为 true
3. 接收 SSE 消息片段时，累积更新智能体回复
4. 接收 `[DONE]` 信号时，标记消息为完成，设置 `isStreaming` 为 false
5. 处理错误时，关闭 SSE 连接并显示错误信息

## 6. API 集成

### 6.1 恋爱大师智能体 API
- **URL**：`http://localhost:8123/api/love/chat`
- **方法**：GET (SSE)
- **参数**：
  - `chatId`：聊天ID，动态生成
  - `userMessage`：用户消息内容
- **响应**：SSE 流式输出
  ```
  data: {"response": "智能体回复内容片段1"}
  data: {"response": "智能体回复内容片段2"}
  data: {"response": "智能体回复内容片段3"}
  data: [DONE]
  ```

### 6.2 测试智能体 API
- **URL**：`http://localhost:8123/api/test/chat`
- **方法**：GET (SSE)
- **参数**：
  - `chatId`：聊天ID，动态生成
  - `userMessage`：用户消息内容
- **响应**：SSE 流式输出
  ```
  data: {"response": "智能体回复内容片段1"}
  data: {"response": "智能体回复内容片段2"}
  data: [DONE]
  ```

### 6.3 API 服务实现
```typescript
// services/api.ts
export const chatWithAgent = (agent: 'love' | 'test', message: string, chatId: string | number, onMessage: (content: string) => void, onComplete: () => void, onError: (error: Event) => void) => {
  const url = `http://localhost:8123/api/${agent}/chat?chatId=${chatId}&userMessage=${encodeURIComponent(message)}`;
  const eventSource = new EventSource(url);
  
  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      eventSource.close();
      onComplete();
      return;
    }
    
    try {
      const data = JSON.parse(event.data);
      onMessage(data.response);
    } catch (error) {
      console.error('Error parsing SSE message:', error);
    }
  };
  
  eventSource.onerror = (error) => {
    eventSource.close();
    onError(error);
  };
  
  return () => {
    eventSource.close();
  };
};
```

## 7. UI/UX 设计

### 7.1 整体风格
- **设计风格**：现代、简洁、友好
- **配色方案**：
  - 主色调：#3b82f6（蓝色）
  - 辅助色：#10b981（绿色）
  - 背景色：#f3f4f6
  - 文本色：#1f2937

### 7.2 页面设计

#### 智能体选择页面
- 居中布局，显示两个智能体卡片
- 每个卡片包含智能体名称、图标和简短描述
- 卡片有悬停效果和点击动画

#### 聊天界面
- 顶部导航栏：显示当前智能体名称和返回按钮
- 消息区域：
  - 用户消息：靠右对齐，蓝色气泡
  - 智能体消息：靠左对齐，灰色气泡
- 输入区域：
  - 文本输入框
  - 发送按钮
  - 支持表情和附件（可选）

### 7.3 响应式设计
- **桌面端**：侧边栏显示智能体列表，主区域显示聊天界面
- **移动端**：底部导航栏切换智能体，全屏显示聊天界面

## 8. 开发与部署

### 8.1 开发环境设置
1. 初始化项目：`npm create vite@latest . -- --template react-ts`
2. 安装依赖：`npm install`
3. 安装额外依赖：`npm install axios tailwindcss shadcn/ui lucide-react`
4. 配置 Tailwind CSS：`npx tailwindcss init -p`
5. 启动开发服务器：`npm run dev`

### 8.2 构建与部署
1. 构建生产版本：`npm run build`
2. 部署到静态网站托管服务（如 Vercel、Netlify 等）

### 8.3 性能优化
- 使用 React.memo 优化组件渲染
- 实现消息列表虚拟滚动（大量消息时）
- 图片懒加载
- 代码分割

## 9. 测试策略

### 9.1 单元测试
- 组件测试：使用 Vitest 和 React Testing Library
- API 服务测试：使用 Mock 数据

### 9.2 端到端测试
- 使用 Cypress 测试完整用户流程

## 10. 未来扩展

### 10.1 功能扩展
- 支持更多智能体类型
- 添加消息历史记录
- 实现语音输入功能
- 支持多媒体消息（图片、视频等）

### 10.2 技术扩展
- 集成 WebSocket 实现实时聊天
- 添加用户认证系统
- 实现多语言支持

## 11. 结论

本前端开发文档详细描述了 AI 智能体聊天应用的设计和实现方案。通过使用 React、TypeScript 和现代前端工具，我们可以构建一个功能完整、用户友好的聊天应用，满足用户与不同智能体进行对话的需求。