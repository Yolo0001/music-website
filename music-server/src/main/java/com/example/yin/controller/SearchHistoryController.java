package com.example.yin.controller;

import com.example.yin.common.R;
import com.example.yin.model.request.SearchHistoryRequest;
import com.example.yin.service.SearchHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    /**
     * 记录搜索历史
     */
    @PostMapping("/search/history/record")
    public R recordSearchHistory(@RequestBody SearchHistoryRequest request) {
        if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            return R.error("搜索关键词不能为空");
        }
        String searchType = request.getSearchType();
        if (searchType == null || searchType.trim().isEmpty()) {
            searchType = "song";
        }
        return searchHistoryService.recordSearchHistory(request.getUserId(), request.getKeyword(), searchType);
    }

    /**
     * 获取用户的搜索历史
     */
    @GetMapping("/search/history/user")
    public R getUserSearchHistory(@RequestParam Integer userId,
                                   @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return searchHistoryService.getUserSearchHistory(userId, limit);
    }
}

