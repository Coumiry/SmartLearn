## 项目概念：智能学习与知识助手平台（SmartLearn AI）

**一句话概括**：
 一个面向「个人学习者 + 小型培训机构」的在线学习平台，支持课程学习、题库练习、学习数据分析，并内置一个 **AI 学习助手**（问答 + 解题 + 学习路径推荐），整体采用 **微服务 + 分布式基础设施** 的架构。

技术栈：Spring Boot、Spring Cloud Gateway、Nacos、OpenFeign、MySQL、Redis、Kafka、Elasticsearch、Docker、Vue3、langChain4j

------

## 核心业务功能（从业务出发，而不是从技术出发）

1. **用户 & 角色系统**
   - 用户注册/登录（学生/教师/管理员）
   - JWT + RBAC 权限控制
   - 教师管理课程，学生选课学习
2. **课程 & 学习模块**
   - 课程列表、课程章节、视频/文档资源
   - 学习进度记录（看过哪些章节、完成度）
3. **题库 & 测验**
   - 选择题 / 判断题 / 简答题
   - 自动判分 + 错题本
   - 支持按知识点分类（Java 基础、集合、多线程、数据库等）
4. **学习数据分析**
   - 学生个人面板：学习时长、完成率、正确率、薄弱知识点
   - 教师后台：课程热度、活跃用户、整体正确率
5. **AI 学习助手（重点亮点）**
   - **智能问答**：学生在某个知识点页面提问，AI 用自然语言解释（可调用大模型 API）
   - **解题讲解**：提交题目或错误答案，AI 给出解析 & 知识点说明
   - **学习路径推荐**：基于学习记录 & 错题数据，生成个性化学习计划（简单规则 + AI 文本润色/扩写）

------

## 微服务拆分设计

1. **gateway-service（API 网关）**
   - 统一入口，路由到各个微服务
   - 负责鉴权（校验 JWT）、限流、跨域
   - 技术：Spring Cloud Gateway
2. **auth-service（认证与用户服务）**
   - 用户注册/登录、JWT 颁发与刷新
   - 用户信息 / 角色 / 权限
   - 技术点：Spring Security、OAuth2、JWT、Redis 缓存登录状态
3. **course-service（课程 & 学习进度服务）**
   - 课程信息、章节、资源地址
   - 学习进度记录、学习时长统计
   - 对接对象存储（如 MinIO / 阿里云 OSS）
4. **quiz-service（题库 & 测验服务）**
   - 题目管理（增删改查）
   - 测验创建、作答记录、判分逻辑
   - 错题本、知识点维度统计
5. **ai-service（AI 能力服务）**
   - 封装所有 AI 相关功能，对外提供 REST 接口：
     - /qa：知识问答
     - /explain：题目解析
     - /plan：学习路径推荐
   - 内部可以：
     - 调用大模型 API（如 OpenAI、国内大模型、企业接口等）
     - 或调用一个 Python 服务（通过 HTTP / gRPC）做向量检索、embedding 等
6. **stats-service（统计与推荐服务）**
   - 做一些异步统计和推荐逻辑
   - 消费消息队列（学习记录、答题记录），聚合数据后存入 ES / Redis

------

## 分布式 & 微服务相关技术栈设计

### 服务治理 & 基础设施

- **Spring Cloud Alibaba**
  - 注册中心：Nacos
  - 配置中心：Nacos Config
  - 负载均衡：Spring Cloud LoadBalancer / Ribbon
  - 服务间调用：OpenFeign
  - 熔断 & 限流：Sentinel / Resilience4j
- **网关**
  - Spring Cloud Gateway：统一路由、鉴权、限流
- **链路追踪**
  - SkyWalking / Zipkin / Sleuth：展示一次请求经过哪些服务

### 数据存储 & 缓存

- 关系型数据库：MySQL / PostgreSQL（课程、用户、题库、记录）
- ORM：MyBatis-Plus 
- 缓存：Redis（token、热点课程、排行榜）
- 全文检索：Elasticsearch（课程搜索、题目搜索）
- 分布式 ID：雪花算法（可用 MyBatis-Plus 内置）

### 消息队列

- Kafka 
  - 生产消息：学习行为（看视频、做题）、登录日志等
  - 消费消息：统计服务异步聚合数据，而不是同步写大批统计表

------

## AI 部分

1. **封装出一个独立的 ai-service**

   - 所有其他服务（如 quiz-service、course-service）只知道调用 `ai-service` 的 REST API
   - 例如：
     - quiz-service 遇到学生请求“查看解析”时：
       调 `POST /ai/explain`，把题目 & 学生作答发过去
     - course-service 遇到学生在某一章节点了“提问”：
       调 `POST /ai/qa`，把上下文（章节主题 + 用户问题）发过去

2. **AI 服务内部做的事**

   - 1）调用外部大模型 API：
     - 构造 prompt，把题目、背景、用户历史错误等打包进去
   - 2）做一个简单的“知识库问答”
     - 把课程笔记 / 文档 做成向量（embedding），存到向量库（pgvector、Milvus、Elasticsearch dense vector）
     - 按相似度检索相关知识片段，再交给大模型“组织语言输出”

3. **学习路径推荐（AI + 规则结合）**

   - 规则引擎部分（自己写逻辑）：

     - 如果某知识点错误率 > 60%，标记为薄弱点
     - 对薄弱点排序，给出一个「推荐学习顺序」

   - AI 部分：

     - 把这些结构化结果丢给大模型，让它生成自然语言学习计划，例如：

       > 先补基础：推荐你从 Java 集合开始复习，建议顺序：List → Set → Map …