package com.smartlearn.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlearn.quiz.entity.Quiz;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizMapper extends BaseMapper<Quiz> {
}
