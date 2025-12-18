package com.example.yin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yin.model.domain.UserActionLog;

public interface UserActionLogService extends IService<UserActionLog> {

    void recordAction(Integer userId, String action, String detail, String ip);
}

