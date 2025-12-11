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

统一约定：

- 网关前缀为 `/api`，quiz-service 的所有接口对外形如：`/api/quiz/...`。
- 返回结构统一为：

```json
{
  "code": 0,
  "msg": "success",
  "data": ...
}
```

------

## Day15：梳理题目模型 + 完成简答题题库管理（教师端）

### 当日目标

- 明确并对齐题库只支持简答题（`question_type` 字段实际仅用 `SHORT`）的模型。
- 完成题目管理的完整 CRUD 接口（教师端），与项目进度文档保持一致。

### 涉及表

- `question`：题目表，只用简答题相关字段。

------

### 1. 创建简答题

- **URL**：`POST /api/quiz/manage/question/create`
- **权限**：教师 / ADMIN
- **功能**：教师创建简答题，保存到 `question` 表。

**请求体示例**

```json
{
  "questionType": "SHORT",
  "content": "请简要说明 JVM 中堆和栈的区别。",
  "correctAnswer": "堆用于存放对象实例，栈用于存放局部变量和方法调用栈帧……",
  "analysis": "从存储内容、生命周期、是否线程私有等角度描述。",
  "knowledgePoint": "Java 基础/JVM 内存结构"
}
```

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1991004071962023001",
    "questionType": "SHORT",
    "content": "请简要说明 JVM 中堆和栈的区别。",
    "correctAnswer": "堆用于存放对象实例，栈用于存放局部变量和方法调用栈帧……",
    "analysis": "从存储内容、生命周期、是否线程私有等角度描述。",
    "knowledgePoint": "Java 基础/JVM 内存结构",
    "createdBy": "当前教师ID",
    "createdTime": "2025-11-27T14:30:00",
    "updatedTime": "2025-11-27T14:30:00"
  }
}
```

------

### 2. 更新简答题

- **URL**：`PUT /api/quiz/manage/question/update/{id}`
- **功能**：更新题干、标准答案、解析、知识点等。

**请求体示例**

```json
{
  "questionType": "SHORT",
  "content": "更新后的题干内容...",
  "correctAnswer": "更新后的标准答案...",
  "analysis": "更新后的解析...",
  "knowledgePoint": "更新后的知识点标签"
}
```

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1991004071962023001",
    "questionType": "SHORT",
    "content": "更新后的题干内容...",
    "correctAnswer": "更新后的标准答案...",
    "analysis": "更新后的解析...",
    "knowledgePoint": "更新后的知识点标签",
    "createdBy": "创建教师ID",
    "createdTime": "2025-11-27T14:30:00",
    "updatedTime": "2025-11-27T14:40:00"
  }
}
```

------

### 3. 删除简答题

- **URL**：`DELETE /api/quiz/manage/question/delete/{id}`
- **功能**：删除题目，并删除对应 `quiz_question` 关联记录。

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": true
}
```

------

### 4. 分页查询题目列表（教师端）

- **URL**：`GET /api/quiz/manage/question/list`
- **查询参数**

```text
pageNo        Integer  否  默认 1
pageSize      Integer  否  默认 10
questionType  String   否  题型过滤，如 SHORT
knowledgePoint String  否  按知识点模糊搜索，如 "JVM"
keyword       String   否  按题干内容模糊搜索，如 "堆和栈"
```

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": "1991004071962023001",
        "questionType": "SHORT",
        "content": "请简要说明 JVM 中堆和栈的区别。",
        "knowledgePoint": "Java 基础/JVM 内存结构",
        "createdBy": "教师ID",
        "createdTime": "2025-11-27T14:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

------

## Day16：测验（Quiz）管理接口（教师端）

### 当日目标

- 基于 `quiz`、`quiz_question` 表，实现章节级测验管理：创建、查看、删除测验。
- 每个测验是一组简答题，与课程/章节绑定。

### 涉及表

- `quiz`：测验主表
- `quiz_question`：测验题目关联表

------

### 1. 创建测验

- **URL**：`POST /api/quiz/manage/quiz/create`
- **功能**：为某课程章节创建一个测验，并关联一组题目。

**请求体示例**

```json
{
  "courseId": "1991004071962021890",
  "chapterId": "1991004071962022001",
  "title": "第 1 章测验",
  "questionIds": [
    "1991004071962023001",
    "1991004071962023002"
  ]
}
```

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1991004071962024001",
    "courseId": "1991004071962021890",
    "chapterId": "1991004071962022001",
    "title": "第 1 章测验",
    "totalScore": 2,
    "createdBy": "教师ID",
    "createdTime": "2025-11-28T10:00:00",
    "updatedTime": "2025-11-28T10:00:00"
  }
}
```

> `totalScore` 第一版可以简单设为题目数量（每题 1 分）2. 查询某章节下的测验列表（教师端）

- **URL**：`GET /api/quiz/manage/quiz/list`
- **查询参数**

```text
courseId   String  是
chapterId  String  是
```

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": "1991004071962024001",
      "title": "第 1 章测验",
      "totalScore": 2,
      "createdTime": "2025-11-28T10:00:00"
    }
  ]
}
```

------

### 3. 查询测验详情（含题目列表，教师视角）

- **URL**：`GET /api/quiz/manage/quiz/detail/{quizId}`
- **功能**：返回测验基本信息 + 题目简要信息，用于教师确认/编辑。

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1991004071962024001",
    "courseId": "1991004071962021890",
    "chapterId": "1991004071962022001",
    "title": "第 1 章测验",
    "totalScore": 2,
    "questions": [
      {
        "questionId": "1991004071962023001",
        "content": "请简要说明 JVM 中堆和栈的区别。",
        "knowledgePoint": "Java 基础/JVM 内存结构"
      }
    ]
  }
}
```

------

### 4. 删除测验

- **URL**：`DELETE /api/quiz/manage/quiz/delete/{quizId}`
- **功能**：删除测验，并删除 `quiz_question` 中关联记录；可约定不影响历史 `user_quiz_answer` 记录（用于历史统计）。

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": true
}
```

------

## Day17：学生测验接口（获取试卷）

### 当日目标

- 面向学生端提供“查询试卷详情”的接口：
  - 学生根据 `quizId` 获取题目列表。
  - 不暴露标准答案，只给题干和分值。

### 涉及表

- `quiz`
- `quiz_question`
- `question`

------

### 1. 学生端查询试卷详情

- **URL**：`GET /api/quiz/student/detail/{quizId}`
- **功能**：学生进入测验前拉取题目列表。

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "quizId": "1991004071962024001",
    "title": "第 1 章测验",
    "totalScore": 2,
    "questions": [
      {
        "questionId": "1991004071962023001",
        "content": "请简要说明 JVM 中堆和栈的区别。",
        "score": 1,
        "knowledgePoint": "Java 基础/JVM 内存结构"
      },
      {
        "questionId": "1991004071962023002",
        "content": "请说明 Java 中垃圾回收的作用。",
        "score": 1,
        "knowledgePoint": "Java 基础/GC"
      }
    ]
  }
}
```

> 说明：
>
> - 题目内容来自 `question.content`
> - 不返回 `correctAnswer` 和 `analysis` 字段，防止提前泄露答案。

---

## Day18：AI 判分 + 基础反馈（分数 + 评语 + 建议）

### 当日进度目标

- 完成“学生提交测验答案 → quiz-service 调用 ai-service → 写入 user_quiz_answer → 返回分数 + 评语 + 建议”的闭环。
- 针对每题，AI 至少返回：
  - `score`（0 ~ 1）
  - `comment`（简短评语）
  - `suggestion`（简短学习建议）

### 涉及表

- `user_quiz_answer`：保存学生答案 + AI 给出的分数和对错。
- `question`：提供 `content` 和 `correct_answer`，供 AI 判分参考。

------

### 1）学生提交测验答案（核心入口）

**URL**

- `POST /api/quiz/student/submit`

**功能**

- 接收某次测验的所有题目的学生答案；
- 对每道题调用 ai-service 做语义评分；
- 将结果写入 `user_quiz_answer`；
- 汇总整个测验的总分，并将“分数 + 评语 + 建议 + 解析”一并返回。

**请求体示例**

```json
{
  "quizId": "1991004071962024001",
  "answers": [
    {
      "questionId": "1991004071962023001",
      "userAnswer": "堆用来存储对象实例，栈用来存储局部变量和方法调用栈帧"
    },
    {
      "questionId": "1991004071962023002",
      "userAnswer": "垃圾回收用于自动回收不再使用的对象内存"
    }
  ]
}
```

说明：

- `quizId`：本次测验 id。
- `answers`：一题一个对象，绑定 `questionId` 和学生的简答内容。

------

### 2）quiz-service 内部业务流程（概念级）

1. 根据 `quizId` 校验测验存在性；
2. 读取该测验的题目列表（通过 `quiz_question` + `question`）；
3. 遍历前端传来的每个 `answers[i]`：
   - 找到对应题目 `question`：
     - `content`（题干）
     - `correct_answer`
   - 构造 AI 判分请求（见后文 ai-service 接口）。
   - 接收 AI 返回的：
     - `score`（例如 0.8）
     - `comment`（评语）
     - `suggestion`（后续学习建议）
   - 向 `user_quiz_answer` 插入记录：
     - `user_id`：从网关 `X-User-Id`
     - `quiz_id` / `question_id` / `user_answer`
     - `score`、`submit_time`
4. 汇总整张卷子的总分：
   - `totalScore = 所有题目 score 之和`
   - `maxTotalScore = 题目数 * 每题满分 (每题一分)`

------

### 3）Day18 返回体设计（包含 AI 评语和建议）

在 Day18 结束时，`POST /api/quiz/student/submit` 的返回体建议长这样：

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "quizId": "1991004071962024001",
    "totalScore": 1.6,
    "maxTotalScore": 2.0,
    "details": [
      {
        "questionId": "1991004071962023001",
        "score": 0.8,
        "maxScore": 1.0,
        "comment": "回答基本覆盖了堆和栈的核心区别，还可以补充生命周期和线程私有性。",
        "suggestion": "建议复习 JVM 内存结构相关章节，尤其是各内存区域的生命周期和线程可见性。"
      },
      {
        "questionId": "1991004071962023002",
        "score": 0.8,
        "maxScore": 1.0,
        "comment": "说到了自动回收未使用对象，是核心点。",
        "suggestion": "可以再了解一下 GC 触发条件和常见垃圾回收器的区别。"
      }
    ]
  }
}
```

Day18 的重点：

- 这一步**先不返回解析（analysis）**，只完成“AI 打分 + 评语 + 建议”的链路；
- 解析在 Day19 用 DB 字段接上。

------

### 4）ai-service：简答题评分接口（Day18 引入）

**URL**

- `POST /api/ai/grade-short-answer`

**功能**

- 根据题干、标准答案、学生答案为简答题打分，并返回语义相似度、简短评语和学习建议。

**请求体（由 quiz-service 调用）**

```json
{
  "questionContent": "请简要说明 JVM 中堆和栈的区别。",
  "correctAnswer": "堆用于存放对象实例，栈用于存放局部变量和方法调用栈帧……",
  "userAnswer": "堆存对象实例，栈存局部变量和调用栈帧",
  "maxScore": 1.0
}
```

**返回体（ai-service 内部约定）**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "score": 0.8,
    "maxScore": 1.0,
    "comment": "回答抓住了对象/局部变量/栈帧等核心要点，略有简略。",
    "suggestion": "建议再对比一下堆和栈在生命周期和线程可见性上的区别。"
  }
}
```

------

## Day19：补全结果视图 + 接上“解析（analysis）”字段

Day18 已经让 `/submit` 返回了分数 + 评语 + 建议，Day19 的重点是：

- 把“解析”接上：从 `question.analysis` 返回；
- 明确“即时返回”和“结果查询”的一致结构，为 Day21 的 `/result/{quizId}` 复用。

### 当日进度目标

1. **扩展 Day18 返回体，在 details 中增加“解析（analysis）”字段：**
   - 解析来源：`question.analysis` 字段；
   - 不是 AI 生成。
2. **定义一个统一的“题目结果 DTO 结构”，既用于 `/submit` 返回，也用于 `/result/{quizId}`。**

------

### 1）解析字段整合到 /submit 返回中

在 Day19，我们在 Day18 的 `details[]` 中，增加解析字段：

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "quizId": "1991004071962024001",
    "totalScore": 1.6,
    "maxTotalScore": 2.0,
    "details": [
      {
        "questionId": "1991004071962023001",
        "content": "请简要说明 JVM 中堆和栈的区别。",
        "userAnswer": "堆存对象实例，栈存局部变量和调用栈帧",
        "correctAnswer": "堆用于存放对象实例，栈用于存放局部变量和方法调用栈帧……",
        "analysis": "解析：可从存储内容、生命周期、线程私有性三个方面比较堆与栈。",
        "knowledgePoint": "Java 基础/JVM 内存结构",
        "score": 0.8,
        "maxScore": 1.0,
        "comment": "回答抓住了对象/局部变量/栈帧等核心要点。",
        "suggestion": "建议复习 JVM 内存结构相关章节。"
      }
    ]
  }
}
```

说明：

- `analysis`：从 `question.analysis` 直接查出来；
- 现在“这个接口一个 response，就同时带上：**分数 + 评语 + 建议 + 解析**”，满足你想合并 Day18/Day19 的要求。

------

### 2）统一“结果 DTO”，为 Day21 的 `/result/{quizId}` 做准备

在结构设计层面，你可以把上面的 `details[]` 抽象为一个统一的 **QuestionResultDTO**，字段包含：

- `questionId`
- `content`
- `userAnswer`
- `correctAnswer`
- `analysis`（来自 DB）
- `knowledgePoint`
- `score`（AI打分）
- `maxScore`
- `comment`（AI 评语）
- `suggestion`（AI 建议）

然后：

- `POST /api/quiz/student/submit` 直接返回这一结构（“即时结果视图”）；
- `GET /api/quiz/student/result/{quizId}` 在 Day21 基于 `user_quiz_answer` + `question` 再组装一次同样的结构（“历史结果视图”）。

这样，前端只需要针对一种数据结构做结果展示组件：

- 提交后立即展示一版；
- 以后从“错题本”或“历史测验列表”里再进入查看结果，也是同样的结构。

------

## 小结：Day18+Day19 

到 Day19 结束时，应该达到这样一个状态：

1. 学生调用 `POST /api/quiz/student/submit` 时：
   - 后端使用 ai-service 对每题进行语义判分；
   - `user_quiz_answer` 落库了分数和对错；
   - 前端得到的 response 中，每题都有：
     - 分数（score）
     - AI 评语（comment）
     - AI 建议（suggestion）
     - 静态解析（analysis，来自 question 表）
2. 这个 per-question 结果结构已经稳定，可以被：
   - Day20 的错题本（/student/wrong）；
   - Day21 的测验结果查看（/student/result/{quizId}）
     直接复用或简化后复用。

## Day20：错题本接口

### 当日目标

- 基于 `user_quiz_answer` 提供学生错题本接口：
  - 查询当前学生所有错误题目（`is_correct = false`），可选支持按时间、知识点过滤。
  - 为每道错题返回题干、学生答案、标准答案、知识点等。

### 涉及表

- `user_quiz_answer`
- `question`

------

### 1. 查询错题列表

- **URL**：`GET /api/quiz/student/wrong`
- **查询参数（可选）**

```text
knowledgePoint  String  否   按知识点过滤
sinceDays       Integer 否   仅查询最近 N 天的错题（例如 30）
```

**返回体示例**（每一项是“最近一次错误记录”）

```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "questionId": "1991004071962023001",
      "quizId": "1991004071962024001",
      "content": "请简要说明 JVM 中堆和栈的区别。",
      "userAnswer": "只写了堆存对象",
      "correctAnswer": "堆用于存放对象实例，栈用于存放局部变量和方法调用栈帧……",
      "knowledgePoint": "Java 基础/JVM 内存结构",
      "lastSubmitTime": "2025-11-30T10:20:00",
      "score": 0
    }
  ]
}
```

> 查询逻辑：
>
> - 从 `user_quiz_answer` 中筛选当前用户 `user_id`，`is_correct = false`
> - 按 `submit_time` 倒序；
> - 按 `question_id` 分组可取最近一次错误记录；
> - 再关联 `question` 表取 `content`、`correct_answer`、`knowledge_point`。

------

## Day21：结果查看接口 + 一次测验的总览视图

### 当日目标

- 给学生提供一个“查看某次测验结果”的接口：
  - 包含：总分、对错统计、每题详情（题干、学生答案、标准答案、是否正确）。
- 为前端判分结果页和错题本页提供统一数据源。

### 涉及表

- `user_quiz_answer`
- `question`
- `quiz`

------

### 1. 学生查看某次测验结果

- **URL**：`GET /api/quiz/student/result/{quizId}`
- **功能**：
  - 读取当前学生该 `quizId` 下的所有 `user_quiz_answer` 记录；
  - 关联 `question` 表补全题干、正确答案；
  - 汇总总分、对错数量。

**返回体示例**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "quizId": "1991004071962024001",
    "title": "第 1 章测验",
    "totalScore": 2,
    "maxTotalScore": 2,
    "correctCount": 2,
    "wrongCount": 0,
    "details": [
      {
        "questionId": "1991004071962023001",
        "content": "请简要说明 JVM 中堆和栈的区别。",
        "userAnswer": "堆存对象，栈存变量和调用栈",
        "correctAnswer": "堆用于存放对象实例，栈用于存放局部变量和方法调用栈帧……",
        "knowledgePoint": "Java 基础/JVM 内存结构",
        "score": 1,
        "isCorrect": true
      },
      {
        "questionId": "1991004071962023002",
        "content": "请说明 Java 中垃圾回收的作用。",
        "userAnswer": "自动回收不再使用的对象内存",
        "correctAnswer": "自动回收不再使用的对象内存",
        "knowledgePoint": "Java 基础/GC",
        "score": 1,
        "isCorrect": true
      }
    ]
  }
}
```

> 前端可以：
>
> - 在“判分结果页”使用此接口；
> - 在错题本点击某次测验时跳转到结果页；
> - 每题附带“查看 AI 解析”按钮 → 调 `GET /api/quiz/student/explain/{questionId}`（Day19）。

