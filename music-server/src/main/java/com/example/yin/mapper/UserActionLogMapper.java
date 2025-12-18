package com.example.yin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.yin.model.domain.UserActionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserActionLogMapper extends BaseMapper<UserActionLog> {
}

