package com.example.yin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.yin.common.R;
import com.example.yin.model.domain.SearchHistory;

public interface SearchHistoryService extends IService<SearchHistory> {
    /**
     * 记录搜索历史
     */
    R recordSearchHistory(Integer userId, String keyword, String searchType);

    /**
     * 获取用户的搜索历史
     */
    R getUserSearchHistory(Integer userId, Integer limit);
}














