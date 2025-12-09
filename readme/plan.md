# PLAN——FirstWeek

------

## 一、整体策略：一个月怎么干？

总原则：

1. **优先保证业务闭环**
   - 用户注册/登录 → 选课 → 看课程 → 做题 → 看解析 → 看简单统计
2. **架构优先搭骨架，功能慢慢填肉**
   - 一开始就用 Spring Cloud / Nacos / Gateway，但很多地方可以先用“假数据 / 简版实现”撑着
3. **AI 服务先“能调通 + 输出正常”，再慢慢玩向量库、知识库**

------

## 二、4 周路线图（高层）

### 第 1 周：基础设施 + 用户体系 + 网关

目标：
 你能跑起来一套“多服务 + 网关 + 注册中心 + 配置中心 + 基础鉴权”。

要有这些成果：

- 父工程 + 多模块 / 多服务工程结构
- Nacos 注册中心 + 配置中心 OK
- gateway-service：能把请求转发到不同服务
- auth-service：
  - 用户注册、登录
  - 颁发 JWT
  - 网关拦截未登录请求
- 一个最简单的前端 Vue3 项目能调用登录接口成功

------

### 第 2 周：课程 / 章节 / 学习进度

目标：
 学生可以看到课程列表、章节信息，并能记录“浏览进度”。

- course-service：
  - 课程表、章节表设计 & MyBatis-Plus
  - 课程增删改查（先做后台接口，前端可以用简单管理页）
  - 学习进度表（user_id + course_id + chapter_id + 进度百分比）
- 对接对象存储先别折腾太深：
  - 视频 / 文档链接先用“假 URL”或本地静态文件路径占位
- 前端：
  - 课程列表页
  - 课程详情 + 章节列表
  - 标记某章节为“已学/学习中”

------

### 第 3 周：题库 / 测验 / AI 解题讲解

目标：
 学生能在课程章节下做题 → 提交答案 → 自动判分 → 查看 AI 解释。

- quiz-service：
  - 题目表（包含题型、选项、正确答案、知识点）
  - 测验表（某个课程章节的一组题）
  - 用户作答记录表（答案、得分、是否正确）
  - 判分逻辑（选择题 / 判断题自动判）
  - 错题本（基于作答记录的查询接口）
- ai-service：
  - 暴露 REST：
    - `POST /ai/explain`：入参（题目、正确答案、用户答案），返回解析文本
    - `POST /ai/qa`：先简版，只接收问题 + context（课程/章节标题）
  - 先直接调用一个大模型 API（比如 OpenAI 或你可用的国产模型），不急着玩向量库
- 前端：
  - 做题页面（单次测验）
  - 交卷后展示“对/错 + AI 解析”

------

### 第 4 周：统计 / 简单推荐 + 部署 & 优化

目标：
 搞定一个“还算像样”的学习分析页面，并能用 Docker 部署一套可演示环境。

- stats-service：
  - Kafka 接入（先搞 1~2 个 Topic 就行）：
    - `learning_behavior`：浏览章节
    - `quiz_behavior`：做题结果
  - 消费消息，聚合成：
    - 学生维度：学习时长、完成率、正确率
    - 简单“薄弱知识点列表”
- ai-service：
  - 新增 `POST /ai/plan`：
    - 入参：薄弱知识点列表 + 当前课程信息
    - 输出：一段自然语言的学习建议
- 前端：
  - 学生个人面板页面：
    - 完成率 / 正确率 / 薄弱点列表
    - “一键生成学习计划”按钮 → 调 `ai/plan`
- 部署：
  - 写 Dockerfile（至少给 gateway-service、auth-service、course-service、quiz-service、ai-service 各一个）
  - docker-compose 起一整套（含 nacos、mysql、redis、kafka）

------

## 三、第 1 周详细拆解（从今天可以直接开干）

### 第 1 天：搭建多模块工程 + 基础依赖

1. 建一个 Maven 父工程（比如：`smartlearn-parent`）
   - 统一管理依赖：Spring Boot、Spring Cloud Alibaba、MyBatis-Plus、Lombok、JWT、common-utils 等
2. 在父工程下建 module：
   - `gateway-service`
   - `auth-service`
   - `course-service`
   - `quiz-service`
   - `ai-service`
   - `stats-service`
   - `common`（放通用实体、工具类、统一结果返回、异常定义）
3. 起一个本地 Nacos（用官方 docker / zip 都行），确认能访问控制台。

> 这一天的目标：所有服务都是“Hello World Controller”，能启动，并且都能在 Nacos 里注册成功。

------

### 第 2 天：gateway-service + Nacos 配置中心

1. 在 `gateway-service` 中：
   - 引入：`spring-cloud-starter-gateway`、`nacos-discovery`、`nacos-config`
   - 写 `application.yml`：
     - 注册到 Nacos
     - 配置基础路由规则，比如：
       - `/api/auth/**` → `auth-service`
       - `/api/course/**` → `course-service`
2. 打通配置中心：
   - 在 Nacos 里建一个配置（DataId 类似：`gateway-service-dev.yml`）
   - 把路由规则放到 Nacos 配置里，测试热更新（改路由不用重启服务）。
3. 用 Postman/浏览器验证：
   - 访问 `http://localhost:网关端口/api/auth/hello` → 实际转发到 `auth-service` 的 `/hello`

------

### 第 3~4 天：auth-service 用户体系 + JWT

1. 数据库层：
   - 创建 `user` 表（字段：id、username、password、role、创建时间等）
   - MyBatis-Plus 配好（数据源、Mapper 扫描）
2. 后端接口：
   - `/auth/register`：注册
   - `/auth/login`：登录，校验用户名密码，颁发 JWT
3. JWT 方案：
   - 写一个 `JwtUtil`：
     - 生成 token（含 userId、role）
     - 解析 token
   - 写一个“登录用户信息”对象，例如 `LoginUserInfo`，后续可以放到请求上下文中。
4. 网关鉴权：
   - 在 gateway 里搞一个全局过滤器：
     - 解析 `Authorization: Bearer xxx`
     - 校验 JWT 是否有效
     - 根据路径决定是否放行（例如 `/auth/login`、`/auth/register` 放行）
     - 把 userId / role 放到请求头里转发给下游服务（比如 `X-User-Id`、`X-User-Role`）

> 完成标志：
>
> - 用 Postman 调 `/auth/register` 注册成功
> - `/auth/login` 拿到 token
> - 用这个 token 调其他服务的任意一个“受保护接口”（比如 `course-service` 的测试接口），能通过网关校验。

------

### 第 5~7 天：Vue3 + 登录页面 + 简易前后端联调

不需要做花哨 UI，目标很简单：**你能在浏览器上完成登录流程**。

1. 新建一个前端项目（`smartlearn-web`）：
   - Vue3 + Vite
   - axios 封装请求，统一加上 token
2. 做两个页面：
   - 登录页：表单 + 调 `/api/auth/login`
   - 注册页：表单 + 调 `/api/auth/register`
3. 登录成功后：
   - 把 token 存到 localStorage
   - 跳转到一个“占位首页”，比如展示一句：“登录成功，user = xxx”

## 第二周总目标（回顾一下）

**业务闭环：**

- 学生能看到课程列表、课程详情、章节列表
- 学生可以「选课」
- 学生可以记录自己在某个章节的学习进度（最简单可以是：已学 / 未学）
- 前端有基本的课程页与进度展示

**主要动的服务：**

- `course-service` 为主
- 前端 `smartlearn-web` 配合

------

## 第 8 天：课程领域建模 + 数据表设计

今天只干一件事：**把课程这块的数据库和实体模型定下来**。

### 1）数据表设计（建议）

最基础可以这几张：

- `course`
  - id
  - title
  - description
  - level（难度：BEGINNER / INTERMEDIATE / ADVANCED）
  - cover_url
  - teacher_id
  - status（DRAFT / PUBLISHED）
  - created_time, updated_time
- `course_chapter`
  - id
  - course_id
  - title
  - sort_order
  - video_url（先字符串占位）
  - doc_url（可选）
  - created_time, updated_time
- `course_category`（选做，怕复杂可以先不做）
  - id, name, parent_id
- `course_category_rel`（课程和分类多对多时）
  - id, course_id, category_id

### 2）在 `course-service` 中做的事

- 配好数据源、MyBatis-Plus（如果第一周已经搞了，就继续沿用）
- 为每张表建：
  - Entity（加上 `@TableName`、`@TableId` 等）
  - Mapper 接口
- 写简单的单元测试或控制器测试接口：
  - 新增课程
  - 查询课程列表

> 第 8 天完成标志：
>  你能在数据库里看到设计好的表，并通过 `course-service` 成功插入 & 查询课程。

------

## 第 9 天：课程查询 & 管理接口（REST API）

今天把 **课程相关的 API** 写完整，主要是“老师用”和“学生看”的。

### 1）对外暴露的接口（示例）

面向学生（开放查询）：

- `GET /course/public/list`
  - 查询已发布课程列表（支持分页、分类筛选）
- `GET /course/public/detail/{courseId}`
  - 课程基本信息 + 章节列表

面向老师（需要角色校验）：

- `POST /course/manage/create`
- `PUT /course/manage/update/{courseId}`
- `PUT /course/manage/publish/{courseId}`
- `GET /course/manage/my`（当前老师自己的课程列表）

### 2）权限控制（后端）

- 从网关透传过来的 `X-User-Id`、`X-User-Role` 拿到当前用户身份
- 在 `course-service` 里做一个简单拦截/工具：
  - 若调用 `/manage/**` 且角色不为 `TEACHER`/`ADMIN` → 拒绝
- 用统一的响应结构，比如 `CommonResult<T>`，保持和 `auth-service` 风格一致

### 3）Gateway 路由

在网关里把：

- `/api/course/**` → `course-service`

> 第 9 天完成标志：
>  用 Postman 可以完成：
>
> - 查询课程列表、课程详情
> - 老师（用一个 teacher 账号）可以创建、修改、发布课程

------

## 第 10 天：选课（Enrollment）+ 学习进度表设计

有课程还不够，要让学生“拥有”课程，才能追踪他在这门课里的进度。

### 1）数据表设计

- `user_course`（选课关系表）
  - id
  - user_id
  - course_id
  - status（ENROLLED / DROPPED / COMPLETED）
  - enrolled_time
  - completed_time（可空）
- `user_chapter_progress`（章节进度表，先做最简单）
  - id
  - user_id
  - course_id
  - chapter_id
  - status（NOT_STARTED / LEARNING / FINISHED）
  - last_learn_time

如果你想玩进阶一点，也可以加个 `progress_percent`，但第一版可以先只做“已完成/未完成”。

### 2）接口设计

选课相关：

- `POST /course/enroll/{courseId}`
  - 当前用户加入课程（`user_course` 插入记录）
- `GET /course/my`
  - 查询当前用户已选课程列表
- （选做）`DELETE /course/enroll/{courseId}` 或 `PUT /course/enroll/{courseId}/drop`

学习进度相关：

- `POST /course/progress/update`
  - 入参：`courseId`, `chapterId`, `status`
  - 逻辑：如果没有记录 → 插入；有记录 → 更新
- `GET /course/progress/{courseId}`
  - 返回该课程下所有章节对当前用户的进度 map

状态拿当前登录用户：从 header 里的 `X-User-Id` 取。

> 第 10 天完成标志：
>
> - 能调用“选课”接口成功，把课程加入 `user_course`
> - 能对某个章节调用“更新进度”接口
> - 能查到这个用户在某门课下每个章节的状态

------

## 第 11 天：前端课程列表 + 课程详情页

轮到 Vue 出场了，今天专注 **展示数据**，暂时不管进度按钮。

### 1）路由结构（示例）

- `/courses`：课程列表页
- `/courses/:id`：课程详情 + 章节列表
- `/my-courses`：我的课程（选课后才有）

### 2）页面功能

课程列表页：

- 调用 `GET /api/course/public/list`
- 展示基本信息：封面、标题、简介、难度等
- 点击某个课程 → 跳转详情页

课程详情页：

- 调用 `GET /api/course/public/detail/{id}`
- 展示：
  - 课程简介
  - 章节列表（按 `sort_order` 排序）
- 若用户已登录：
  - 显示 “选课/已选” 按钮，对接 `POST /api/course/enroll/{courseId}`
  - 选课成功后跳 `/my-courses` 或刷新页面状态

我的课程页：

- 调用 `GET /api/course/my`
- 展示当前登录用户的课程列表，点击进入详情

> 第 11 天完成标志：
>  在浏览器里，你能从登录开始 → 打开课程列表 → 看课程详情 → 点击选课 → 在“我的课程”看到它。

------

## 第 12 天：前端学习进度交互

今天给章节加“我学过了”能力，打通进度接口。

### 1）交互设计（先做最简单）

在 `课程详情页` 中的章节列表里：

- 每个章节右侧放一个按钮或勾选框：
  - 状态为：
    - 未开始：显示“开始学习”或“标记完成”
    - 已完成：显示“已完成 ✓”，可选地允许取消
- 点击时：
  - 调用 `POST /api/course/progress/update`
  - 更新章节 state 为 FINISHED

### 2）初始化进度

进入详情页时：

- 先调 `GET /api/course/public/detail/{id}` 拿章节列表
- 再调 `GET /api/course/progress/{id}` 拿用户进度
- 在前端合并：给每个章节打上 `progressStatus`

> 第 12 天完成标志：
>
> - 登录后打开某个课程 → 给几个章节点“标记完成”
> - 刷新页面后，这些章节仍显示为“已完成”

------

## 第 13 天：简单统计 & 课程完成度

这天做一点“小数据分析”，给后面 stats-service 热身。

### 1）后端简单统计（仍在 `course-service` 内）

在 `course-service` 中加一个简单接口：

- `GET /course/overview/{courseId}`
  - 返回内容示例：
    - totalChapters
    - finishedChapters（当前用户）
    - progressPercent = finished / total * 100

甚至可以直接在 `GET /course/progress/{courseId}` 的返回中附带一下。

### 2）前端展示

在课程详情页顶部：

- 显示一条小进度条或文字：
  - “本课程完成度： 3/10 章节（30%）”
- 这样后续你在 stats-service 做更复杂统计时，只要改数据源，不用改页面布局。

> 第 13 天完成标志：
>  你能在课程详情页看到当前用户在这门课程的整体完成度数据。

------

## 第 14 天：代码整理 + 联调回归测试

这一整天就做“老码农的仪式感”：**收尾 & 打磨**。

- 整理 `course-service` 的包结构：
  - `controller` / `service` / `mapper` / `entity` / `dto` / `vo`
- 把魔法值（状态字符串）提取成枚举：
  - `CourseStatusEnum`
  - `EnrollStatusEnum`
  - `ChapterProgressStatusEnum`
- 增加统一异常处理：
   比如选课接口里课程不存在 / 已经选过，返回友好错误信息
- 走一遍完整流程冒烟测试：
  1. 注册/登录学生账号
  2. 查看课程列表
  3. 选中某门课程 → 选课
  4. 在详情页标记多个章节“已完成”
  5. 刷新页面，检查进度与完成度是否正确

> 第 14 天完成标志：
>  你已经拥有一个“能完整使用”的课程 + 学习进度功能，虽简单但架子齐全。

好的，我们正式进入 **第三周：题库 / 测验 / AI 解题讲解** 的开发阶段。
这周是整个 SmartLearn 的“核心业务 + AI 能力接入”的关键节点，也是项目体验感提升最快的一周。

下面我给你一个 **可直接执行的 7 天工作计划（第 15～21 天）**，包含数据结构、接口、业务流、AI 接入点以及前端页面职责。

------

# 第三周总目标（第 15～21 天）

实现一个完整的“学生做题 → 判分 → 错题本 → AI 解题讲解”流程：

1. 教师能创建题目（选择题、判断题、简答题）
2. 教师能给某章节创建测验（quiz）
3. 学生能看到测验题目，提交答案
4. 系统自动判分（选择 / 判断）
5. 学生能查看判题结果（对/错）
6. 失败题目可以加入错题本
7. 通过 AI（ai-service）查看题目解析

------

# 目录式总览（你一周内要实现的模块）

服务侧（quiz-service）：

1. 题目表（question）
2. 选项表（question_option）
3. 测验表（quiz）
4. 测验题目关联（quiz_question）
5. 用户作答记录（user_quiz_answer）
6. 判分逻辑（选择题 / 判断题自动判）
7. 错题本接口（基于作答记录查询错误题）
8. AI 接口调用（调用 ai-service/explain）

前端侧：

- 章节页面 → “开始测验”按钮
- 测验页面（逐题展示）
- 自动判分页面（展示对错）
- “查看 AI 解析”按钮
- 错题本页面

------

# 具体每日计划（可直接执行）

------

# **第 15 天：数据表设计 + quiz-service 基础结构**

你要创建 quiz-service 的所有核心表：

### 1）题目表（question）

字段：

- id
- question_type（SINGLE、MULTI、TRUE_FALSE、SHORT）
- content（题干）
- correct_answer（JSON 或字符串）
- analysis（预置解析，可空）
- knowledge_point（知识点，可空）
- created_by（教师）
- created_time, updated_time

### 2）题目选项表（question_option）

用于选择题：

- id
- question_id
- option_key（A/B/C/D）
- content

### 3）测验表（quiz）

- id
- course_id
- chapter_id
- title
- total_score（默认每题 1 分，第一版可固定）
- created_by
- created_time

### 4）测验题目关联表（quiz_question）

- id
- quiz_id
- question_id

### 5）用户作答表（user_quiz_answer）

- id
- user_id
- quiz_id
- question_id
- user_answer（字符串或 JSON）
- is_correct（0/1）
- score
- submit_time

> 第 15 天目标：
>
> - 创建实体 + mapper
> - 数据库表确认可工作
> - quiz-service 的启动、路由在 gateway 中打通

------

# **第 16 天：题目管理接口（教师端）**

你要完成“老师创建题目”的功能，包括：

## 教师 API：

### `POST /quiz/manage/question/create`

入参 DTO 示例：

```json
{
  "questionType": "SINGLE",
  "content": "Java 中用于创建对象的是哪个关键字？",
  "correctAnswer": "new",
  "options": [
    {"optionKey": "A", "content": "class"},
    {"optionKey": "B", "content": "int"},
    {"optionKey": "C", "content": "new"},
    {"optionKey": "D", "content": "void"}
  ],
  "knowledgePoint": "Java 基础"
}
```

### `PUT /quiz/manage/question/update/{id}`

### `DELETE /quiz/manage/question/delete/{id}`

### `GET /quiz/manage/question/list?page=1&size=10`

可加筛选参数：知识点 / 题型。

> 第 16 天目标：
>
> - 完成题目的 CRUD
> - 前端管理员/老师页面可以管理题库（最简单表格即可）

------

# **第 17 天：测验（Quiz）管理接口**

完成章节测验创建能力。

## 教师 API：

### `POST /quiz/manage/create`

```json
{
  "courseId": 1,
  "chapterId": 10,
  "title": "Java 第 1 章测试",
  "questionIds": [1, 2, 3, 4, 5]
}
```

### `GET /quiz/manage/{chapterId}/list`

### `GET /quiz/manage/detail/{quizId}`

展示测验题目。

> 第 17 天目标：
> 章节能创建测验 → 学生页面可点击“开始测验”。

------

# **第 18 天：学生测验接口（答题 / 判分）**

学生端逻辑开始上线。

## 查询试卷

### `GET /quiz/student/detail/{quizId}`

返回题目 + 选项。

## 提交答案

### `POST /quiz/student/submit`

入参示例：

```json
{
  "quizId": 99,
  "answers": [
    {"questionId": 1, "userAnswer": "C"},
    {"questionId": 2, "userAnswer": "true"},
    {"questionId": 3, "userAnswer": "public class"},
    ...
  ]
}
```

服务端自动判分：

- SINGLE / TRUE_FALSE：直接对比
- 多选题：集合比较
- 简答题：先标记为 incorrect（后续可 AI 自动判分）

返回：

```json
{
  "totalScore": 4,
  "correctCount": 4,
  "wrongCount": 1,
  "details": [
    {"questionId": 1, "correct": true},
    {"questionId": 2, "correct": false}
  ]
}
```

同时写入 `user_quiz_answer` 表。

> 第 18 天目标：
> 学生可以正式提交测验，并看到系统判分。

------

# **第 19 天：AI 解题讲解功能**

调用 ai-service：

教师无需写解析，学生查错题就能看到 AI 自动生成解释。

## 接口

### `POST /ai/explain`

quiz-service 需要封装请求体：

```json
{
  "questionContent": "...",
  "correctAnswer": "...",
  "userAnswer": "...",
  "knowledgePoint": "Java 集合"
}
```

ai-service 返回：

```json
{
  "analysis": "正确答案是 C，因为 Java 中创建对象要使用 new 关键字。你的答案 B 不正确……"
}
```

## 学生端接口：

### `GET /quiz/student/explain/{questionId}`

逻辑：

1. 查用户这个 question 的 user_answer & correct_answer
2. 调 ai-service
3. 返回解析

> 第 19 天目标：
> 学生可以点击一个按钮 → 获得 AI 解析。

------

# **第 20 天：错题本（收藏错误记录）**

你需要基于 user_quiz_answer 实现：

## `GET /quiz/student/wrong`

返回：

```json
[
  {
    "questionId": 1,
    "content": "...",
    "userAnswer": "...",
    "correctAnswer": "...",
    "knowledgePoint": "Java 集合"
  },
  ...
]
```

## 增强功能（选做）

- 近期错题（按时间）
- 按知识点分类查看错题
- “再次练习”功能

> 第 20 天目标：
> 学生可以看到自己的错题本。

------

# **第 21 天：前端测验流程 + UI 联调**

前端要做三大页面：

------

### 1）测验开始页

从章节详情页进入：

- 显示测验标题
- 点击“开始答题”

### 2）答题页

- 单选题：单选按钮
- 判断题：true / false
- 简答题：文本框
- 底部按钮：提交试卷

### 3）判分结果页

- 显示得分与统计
- 每题是否正确
- “查看 AI 解析”按钮

### 4）错题本页面

- 列表展示错题
- 可以点击查看解析

> 第 21 天完成标志：
> 完整体验：进入测验 → 答题 → 判分 → AI 解析 → 错题本。

