package com.smartlearn.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlearn.course.entity.UserCourse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCourseMapper extends BaseMapper<UserCourse> {
}
