package com.smartlearn.auth.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlearn.auth.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
