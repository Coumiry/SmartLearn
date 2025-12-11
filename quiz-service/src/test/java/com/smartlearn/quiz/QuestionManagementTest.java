package com.smartlearn.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlearn.quiz.dto.QuestionCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class QuestionManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateSingleChoiceQuestion() throws Exception {
        // 创建单选题
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setQuestionType("SINGLE");
        request.setContent("Java 中用于创建对象的是哪个关键字？");
        request.setCorrectAnswer("C");
        request.setKnowledgePoint("Java 基础");
        request.setAnalysis("Java 中使用 new 关键字来创建对象实例");

        // 添加选项
        List<QuestionCreateRequest.OptionDTO> options = Arrays.asList(
                createOption("A", "class"),
                createOption("B", "int"),
                createOption("C", "new"),
                createOption("D", "void")
        );
        request.setOptions(options);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/quiz/manage/question/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questionType").value("SINGLE"))
                .andExpect(jsonPath("$.data.content").value("Java 中用于创建对象的是哪个关键字？"))
                .andExpect(jsonPath("$.data.options").isArray())
                .andExpect(jsonPath("$.data.options.length()").value(4));
    }

    @Test
    public void testCreateTrueFalseQuestion() throws Exception {
        // 创建判断题
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setQuestionType("TRUE_FALSE");
        request.setContent("Java 是一门编译型语言");
        request.setCorrectAnswer("true");
        request.setKnowledgePoint("Java 基础");
        request.setAnalysis("Java 既是编译型也是解释型语言，源代码先编译成字节码，然后由JVM解释执行");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/quiz/manage/question/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questionType").value("TRUE_FALSE"))
                .andExpect(jsonPath("$.data.correctAnswer").value("true"));
    }

    @Test
    public void testCreateShortAnswerQuestion() throws Exception {
        // 创建简答题
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setQuestionType("SHORT");
        request.setContent("请简述面向对象编程的三个基本特征");
        request.setCorrectAnswer("封装、继承、多态");
        request.setKnowledgePoint("面向对象编程");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/quiz/manage/question/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questionType").value("SHORT"))
                .andExpect(jsonPath("$.data.correctAnswer").value("封装、继承、多态"));
    }

    @Test
    public void testQuestionList() throws Exception {
        mockMvc.perform(get("/quiz/manage/question/list")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testQuestionListWithFilters() throws Exception {
        mockMvc.perform(get("/quiz/manage/question/list")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("questionType", "SINGLE")
                .param("knowledgePoint", "Java")
                .param("keyword", "对象")
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    public void testValidationErrors() throws Exception {
        // 测试空题干
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setQuestionType("SINGLE");
        request.setContent(""); // 空题干应该触发验证错误

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/quiz/manage/question/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPageValidation() throws Exception {
        // 测试页码小于1
        mockMvc.perform(get("/quiz/manage/question/list")
                .param("pageNo", "0")
                .param("pageSize", "10")
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isBadRequest());

        // 测试每页大小超过100
        mockMvc.perform(get("/quiz/manage/question/list")
                .param("pageNo", "1")
                .param("pageSize", "101")
                .header("X-User-Id", "teacher123")
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isBadRequest());
    }

    private QuestionCreateRequest.OptionDTO createOption(String key, String content) {
        QuestionCreateRequest.OptionDTO option = new QuestionCreateRequest.OptionDTO();
        option.setOptionKey(key);
        option.setContent(content);
        return option;
    }
}