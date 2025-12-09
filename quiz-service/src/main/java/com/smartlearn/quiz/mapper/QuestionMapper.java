package com.smartlearn.quiz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlearn.quiz.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {}
