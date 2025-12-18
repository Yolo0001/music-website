package com.example.yin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.mapper.UserActionLogMapper;
import com.example.yin.model.domain.UserActionLog;
import com.example.yin.service.UserActionLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class UserActionLogServiceImpl extends ServiceImpl<UserActionLogMapper, UserActionLog>
        implements UserActionLogService {

    @Override
    public void recordAction(Integer userId, String action, String detail, String ip) {
        if (userId == null || StringUtils.isBlank(action)) {
            return;
        }
        UserActionLog log = new UserActionLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetail(detail);
        log.setIp(StringUtils.defaultIfBlank(ip, "UNKNOWN"));
        this.save(log);
    }
}

