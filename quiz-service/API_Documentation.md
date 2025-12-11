# 题目管理 API 文档 (Day 16 - 已完成)

## 概述
本文档描述了 SmartLearn 项目中 quiz-service 的题目管理相关 API 接口。所有接口都需要教师权限（通过 `@TeacherOnly` 注解控制）。

## 基础信息
- **基础路径**: `/quiz/manage/question`
- **认证方式**: 通过网关传递的 Header (`X-User-Id`, `X-User-Role`)
- **权限要求**: `TEACHER` 或 `ADMIN` 角色

## API 接口

### 1. 创建题目
**POST** `/quiz/manage/question/create`

创建一道新题目，支持单选题、多选题、判断题和简答题。

**请求头**:
```
X-User-Id: {教师ID}
X-User-Role: TEACHER
```

**请求体**:
```json
{
  "questionType": "SINGLE",              // 题型: SINGLE, MULTI, TRUE_FALSE, SHORT
  "content": "Java 中用于创建对象的是哪个关键字？",
  "correctAnswer": "C",                  // 正确答案
  "analysis": "Java 中使用 new 关键字创建对象",  // 解析(可选)
  "knowledgePoint": "Java 基础",          // 知识点(可选)
  "options": [                           // 选项(仅选择题需要)
    {
      "optionKey": "A",
      "content": "class"
    },
    {
      "optionKey": "B",
      "content": "int"
    },
    {
      "optionKey": "C",
      "content": "new"
    },
    {
      "optionKey": "D",
      "content": "void"
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "1698765432109876543",
    "questionType": "SINGLE",
    "content": "Java 中用于创建对象的是哪个关键字？",
    "correctAnswer": "C",
    "analysis": "Java 中使用 new 关键字创建对象",
    "knowledgePoint": "Java 基础",
    "createdBy": "teacher123",
    "createdTime": "2024-01-10T10:30:00",
    "updatedTime": "2024-01-10T10:30:00",
    "options": [
      {
        "id": "1698765432109876544",
        "optionKey": "A",
        "content": "class"
      },
      {
        "id": "1698765432109876545",
        "optionKey": "B",
        "content": "int"
      },
      {
        "id": "1698765432109876546",
        "optionKey": "C",
        "content": "new"
      },
      {
        "id": "1698765432109876547",
        "optionKey": "D",
        "content": "void"
      }
    ]
  }
}
```

### 2. 更新题目
**PUT** `/quiz/manage/question/update/{id}`

更新已有题目的内容。

**路径参数**:
- `id`: 题目ID

**请求体** (与创建题目相同结构)

**响应**: 返回更新后的题目详情

### 3. 删除题目
**DELETE** `/quiz/manage/question/delete/{id}`

删除指定题目及其所有选项。

**路径参数**:
- `id`: 题目ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 4. 查询题目列表
**GET** `/quiz/manage/question/list`

分页查询题目列表，支持筛选条件。

**查询参数**:
- `pageNo`: 页码 (默认: 1, 最小值: 1)
- `pageSize`: 每页大小 (默认: 10, 范围: 1-100)
- `questionType`: 题型筛选 (可选)
- `knowledgePoint`: 知识点筛选 (可选，模糊匹配)
- `keyword`: 关键词搜索 (可选，在题干中模糊匹配)

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "records": [
      {
        "id": "1698765432109876543",
        "questionType": "SINGLE",
        "content": "Java 中用于创建对象的是哪个关键字？",
        "knowledgePoint": "Java 基础",
        "createdBy": "teacher123",
        "createdTime": "2024-01-10T10:30:00"
      }
    ]
  }
}
```

## 数据验证规则

### 创建/更新题目
- `content`: 必填，题干不能为空
- `questionType`: 创建时必填，必须是 SINGLE/MULTI/TRUE_FALSE/SHORT 之一
- `correctAnswer`: 必填，正确答案不能为空
- `options`:
  - 单选题/多选题: 必须提供选项
  - 判断题/简答题: 不需要选项
  - 每个选项必须包含 `optionKey` 和 `content`

### 查询列表
- `pageNo`: 必须 >= 1
- `pageSize`: 必须在 1-100 之间

## 权限控制
- 只有题目创建者或管理员可以修改/删除题目
- 所有接口都需要 TEACHER 或 ADMIN 角色
- 权限通过 `@TeacherOnly` 切面实现

## 错误处理
- 400: 参数验证失败
- 403: 权限不足
- 404: 题目不存在
- 500: 服务器内部错误

## 日志记录
- 所有操作都会记录关键日志
- 包含用户ID、操作类型、题目ID等信息

## 测试用例
项目包含完整的单元测试和集成测试，覆盖所有场景：
- 各种题型的创建
- 参数验证
- 权限控制
- 分页查询和筛选



⏺ 完成总结

我已经成功根据plan.md的Day16要求对quiz-service进行了优化与补充，具体完成了以下工作：

✅ 已完成的功能

1. 补充缺失的测验管理接口：
   - GET /api/quiz/manage/quiz/list - 查询某章节下的测验列表
   - DELETE /api/quiz/manage/quiz/delete/{quizId} - 删除测验
2. 优化测验创建接口：
   - 根据题目数量自动设置totalScore（每题默认1分）
   - 返回完整的QuizCreateResponseVO，包含所有plan.md要求的字段
   - 改进了题目不存在时的错误提示，列出具体不存在的题目ID
3. 完善DTO/VO类：
   - 创建了QuizCreateResponseVO符合plan.md规范
   - 更新了QuizListItemVO添加了totalScore和createdTime字段
   - 重构了QuizDetailVO添加了courseId、chapterId、totalScore等字段
   - 创建了QuizQuestionItemVO用于返回题目简要信息
4. 添加异常处理：
   - 创建了QuizException业务异常类
   - 添加了QuizExceptionHandler全局异常处理器
   - 提供了更友好的错误响应

🔧 核心改进

1. 数据验证优化：原来只检查题目数量是否匹配，现在会具体列出哪些题目ID不存在
2. 返回数据完善：所有接口的返回数据都严格按照plan.md规范设计
3. 事务管理：创建和删除测验操作都添加了@Transactional注解确保数据一致性
4. 代码结构优化：清理了不必要的import，保持代码整洁

📋 接口验证

所有接口都符合plan.md Day16的规范：
- ✅ 创建测验：返回包含totalScore等完整信息
- ✅ 查询列表：返回totalScore和createdTime
- ✅ 查询详情：包含courseId、chapterId、totalScore和题目简要信息
- ✅ 删除测验：返回boolean结果
- ✅ 权限控制：所有接口都需要教师或管理员权限

---

# 学生测验接口 (Day 17)

## 概述
学生测验接口允许学生获取试卷详情，进行答题操作。此部分接口专门为学生端设计，不包含标准答案等敏感信息。

## 基础信息
- **基础路径**: `/api/quiz/student`
- **认证方式**: 通过网关传递的 Header (`X-User-Id`, `X-User-Role`)
- **权限要求**: `STUDENT` 角色

## API 接口

### 1. 获取试卷详情
**GET** `/api/quiz/student/detail/{quizId}`

学生根据测验ID获取题目列表，用于开始答题。不返回标准答案和解析信息。

**路径参数**:
- `quizId`: 测验ID

**请求头**:
```
X-User-Id: {学生ID}
X-User-Role: STUDENT
```

**响应**:
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

**字段说明**:
- `quizId`: 测验唯一标识
- `title`: 测验标题
- `totalScore`: 测验总分
- `questions`: 题目列表
  - `questionId`: 题目ID
  - `content`: 题干内容
  - `score`: 该题分值（目前默认每题1分）
  - `knowledgePoint`: 知识点

**注意事项**:
1. 不会返回 `correctAnswer` 和 `analysis` 字段，防止答案泄露
2. 题目按照测验中设置的顺序返回
3. 只有状态为正常的测验才能被获取

### 2. 错误响应

**测验不存在** (404):
```json
{
  "code": 404,
  "msg": "测验不存在",
  "data": null
}
```

**测验中没有题目** (400):
```json
{
  "code": 400,
  "msg": "测验中没有题目",
  "data": null
}
```

**权限不足** (403):
```json
{
  "code": 403,
  "msg": "权限不足",
  "data": null
}
```

## 数据模型

### QuizStudentDetailVO
学生测验详情视图对象

```java
@Data
@Builder
public class QuizStudentDetailVO {
    private String quizId;                    // 测验ID
    private String title;                     // 测验标题
    private Integer totalScore;               // 总分
    private List<QuestionForQuizVO> questions; // 题目列表

    @Data
    @Builder
    public static class QuestionForQuizVO {
        private String questionId;    // 题目ID
        private String content;       // 题干内容
        private Integer score;        // 分值
        private String knowledgePoint;// 知识点
    }
}
```

## 权限控制
- 所有学生接口都需要 `STUDENT` 角色
- 通过网关传递的 `X-User-Id` 获取当前用户信息
- 验证测验是否存在且有效

## 业务规则
1. 学生只能获取已发布的测验详情
2. 不返回任何与答案相关的信息
3. 题目顺序按照测验创建时设置的顺序返回
4. 每题默认分值为1分

## 日志记录
- 记录学生获取试卷的操作日志
- 包含用户ID、测验ID、操作时间等信息

## 后续扩展
此接口为Day 17功能，后续将扩展：
- Day 18: 学生提交答案接口
- Day 19: AI 解析接口
- Day 20: 错题本接口
- Day 21: 测验结果查看接口

---

⏺ Day 17 完成总结

✅ 已完成的功能

1. **学生端试卷获取接口**:
   - GET /api/quiz/student/detail/{quizId}
   - 返回测验基本信息和题目列表
   - 不包含标准答案和解析，防止作弊

2. **数据模型设计**:
   - 创建了 QuizStudentDetailVO 用于学生端响应
   - 包含测验基本信息和题目列表（不含答案）
   - 支持题目分值和知识点展示

3. **服务层实现**:
   - QuizStudentService 业务逻辑实现
   - 验证测验存在性
   - 按序返回题目列表
   - 过滤敏感信息（答案、解析）

4. **单元测试**:
   - 编写了完整的 Controller 单元测试
   - 验证接口响应格式和数据正确性

🔧 核心特性

1. **安全性**: 不返回任何答案相关信息
2. **顺序性**: 题目按照预设顺序返回
3. **完整性**: 包含测验所需的全部信息（题干、分值、知识点）
4. **扩展性**: 为后续提交答案等功能预留接口

📋 符合规范

所有实现严格按照 plan.md Day17 要求：
- ✅ 学生根据 quizId 获取题目列表
- ✅ 不暴露标准答案和解析
- ✅ 返回完整的测验信息（标题、总分、题目列表）
- ✅ 每题包含题干、分值、知识点信息
