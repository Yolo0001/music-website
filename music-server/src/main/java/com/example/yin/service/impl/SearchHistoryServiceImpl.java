package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.common.R;
import com.example.yin.mapper.SearchHistoryMapper;
import com.example.yin.model.domain.SearchHistory;
import com.example.yin.service.SearchHistoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchHistoryService {

    @Override
    public R recordSearchHistory(Integer userId, String keyword, String searchType) {
        // 允许userId为null（未登录用户）
        if (StringUtils.isBlank(keyword)) {
            return R.error("搜索关键词不能为空");
        }
        
        try {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(userId);
            searchHistory.setKeyword(keyword.trim());
            searchHistory.setSearchType(StringUtils.defaultIfBlank(searchType, "song"));
            
            if (this.save(searchHistory)) {
                return R.success("搜索历史记录成功", true);
            } else {
                return R.error("搜索历史记录失败");
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常，避免影响搜索功能
            System.err.println("记录搜索历史异常: " + e.getMessage());
            return R.error("搜索历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public R getUserSearchHistory(Integer userId, Integer limit) {
        if (userId == null) {
            return R.error("用户ID不能为空");
        }
        
        QueryWrapper<SearchHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("create_time");
        
        if (limit != null && limit > 0) {
            Page<SearchHistory> page = new Page<>(1, limit);
            Page<SearchHistory> result = this.page(page, queryWrapper);
            return R.success("获取搜索历史成功", result.getRecords());
        } else {
            List<SearchHistory> list = this.list(queryWrapper);
            return R.success("获取搜索历史成功", list);
        }
    }
}

