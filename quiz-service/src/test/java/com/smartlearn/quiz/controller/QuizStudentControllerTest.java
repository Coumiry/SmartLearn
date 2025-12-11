package com.smartlearn.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlearn.quiz.service.QuizStudentService;
import com.smartlearn.quiz.vo.QuizStudentDetailVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizStudentController.class)
public class QuizStudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizStudentService quizStudentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetQuizDetail() throws Exception {
        // 准备测试数据
        QuizStudentDetailVO quizDetail = QuizStudentDetailVO.builder()
                .quizId("test-quiz-id")
                .title("第1章测验")
                .totalScore(2)
                .questions(Arrays.asList(
                        QuizStudentDetailVO.QuestionForQuizVO.builder()
                                .questionId("question-1")
                                .content("请简要说明 JVM 中堆和栈的区别。")
                                .score(1)
                                .knowledgePoint("Java 基础/JVM 内存结构")
                                .build(),
                        QuizStudentDetailVO.QuestionForQuizVO.builder()
                                .questionId("question-2")
                                .content("请说明 Java 中垃圾回收的作用。")
                                .score(1)
                                .knowledgePoint("Java 基础/GC")
                                .build()
                ))
                .build();

        when(quizStudentService.getQuizDetailForStudent(anyString()))
                .thenReturn(quizDetail);

        // 执行请求并验证结果
        mockMvc.perform(get("/student/detail/test-quiz-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.quizId").value("test-quiz-id"))
                .andExpect(jsonPath("$.data.title").value("第1章测验"))
                .andExpect(jsonPath("$.data.totalScore").value(2))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andExpect(jsonPath("$.data.questions.length()").value(2))
                .andExpect(jsonPath("$.data.questions[0].questionId").value("question-1"))
                .andExpect(jsonPath("$.data.questions[0].content").value("请简要说明 JVM 中堆和栈的区别。"))
                .andExpect(jsonPath("$.data.questions[0].score").value(1))
                .andExpect(jsonPath("$.data.questions[0].knowledgePoint").value("Java 基础/JVM 内存结构"));
    }
}