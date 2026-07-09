# 🤖 SmartAgent - AI Agent 服务平台

基于 **Spring Boot 3 + JDK 17 + 智谱 GLM** 构建的 AI Agent 服务平台，支持工具调用、RAG 知识库检索和 Web 交互。

## ✨ 功能特性

- **🧠 ReAct Agent 引擎** — 手写 Thought→Action→Observation 循环，支持多步推理
- **🔧 工具调用** — 计算器、联网搜索（Bing）、Python 代码执行
- **📚 RAG 知识库** — 上传文档自动分块、向量化存储、语义搜索
- **🎨 Web 界面** — 深色主题对话页面 + 知识库管理
- **🔌 厂商抽象** — `AIClient` 接口设计，可切换不同 AI 模型

## 🏗️ 项目结构

```
SmartAgent/
├── pom.xml                              # Maven 配置
├── src/main/java/com/smartagent/
│   ├── SmartAgentApplication.java       # 启动类
│   ├── config/AppConfig.java            # Spring Bean 配置
│   ├── ai/                              # AI 模型抽象层
│   │   ├── AIClient.java                # 统一接口
│   │   ├── ChatMessage.java             # 消息体
│   │   ├── ToolCall.java                # 工具调用结构
│   │   └── impl/ZhipuAIClient.java      # 智谱 GLM 实现
│   ├── agent/                           # Agent 核心
│   │   ├── Agent.java                   # ReAct 循环引擎
│   │   ├── ToolRegistry.java            # 工具注册中心
│   │   └── Memory.java                  # 对话记忆
│   ├── tool/                            # 工具库
│   │   ├── Tool.java / ToolDefinition.java
│   │   └── impl/
│   │       ├── CalculatorTool.java      # 计算器
│   │       ├── WebSearchTool.java       # 联网搜索
│   │       └── CodeExecutorTool.java    # 代码执行
│   ├── rag/                             # RAG 模块
│   │   ├── DocumentService.java         # 文档处理
│   │   ├── VectorStore.java             # 向量存储接口
│   │   └── impl/LocalVectorStore.java   # 内存向量检索
│   └── web/                             # Web 层
│       ├── controller/                  # API 控制器
│       └── dto/                         # 数据传输对象
└── src/main/resources/
    ├── application.yml                  # 配置文件
    ├── templates/                       # 页面模板
    └── static/                          # 静态资源
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- 智谱 API Key（[免费申请](https://open.bigmodel.cn/)）

### 配置

```bash
set ZHIPU_API_KEY=你的智谱APIKey
```

### 启动

```bash
git clone https://github.com/87u0/SmartAgent.git
cd SmartAgent
set ZHIPU_API_KEY=你的Key
mvn spring-boot:run
```

访问 [http://localhost:8080](http://localhost:8080)

## 💡 使用指南

| 功能 | 操作 |
|------|------|
| **Agent 对话** | 打开对话页，开启"启用工具调用" |
| **联网搜索** | 开启工具调用后，询问需要联网搜索的问题 |
| **RAG 知识库** | 上传 TXT/MD/PDF 文档，开启"启用 RAG 检索" |

## 🧪 技术架构

```
用户输入 → Agent(ReAct循环) → 调用工具 → 返回结果 → 最终回答
               ↕
         RAG 知识库(向量检索)
```

当前内置 3 个工具：`calculator`（计算器）、`web_search`（联网搜索）、`code_executor`（Python 代码执行）
