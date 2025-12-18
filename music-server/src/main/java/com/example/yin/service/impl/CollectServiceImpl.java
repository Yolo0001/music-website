package com.example.yin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.yin.common.R;
import com.example.yin.mapper.CollectMapper;
import com.example.yin.model.domain.Collect;
import com.example.yin.model.request.CollectRequest;
import com.example.yin.service.CollectService;
import com.example.yin.service.UserActionLogService;
import com.example.yin.utils.IpUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {
    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private UserActionLogService userActionLogService;

    private static final String ACTION_ADD_COLLECT = "ADD_COLLECT";
    private static final String ACTION_DELETE_COLLECT = "DELETE_COLLECT";

    @Override
    public R addCollection(CollectRequest addCollectRequest) {
        //作者用type来判断收藏的是歌还是歌单
        Collect collect = new Collect();
        BeanUtils.copyProperties(addCollectRequest, collect);
        if (collectMapper.insert(collect) > 0) {
            // 记录收藏操作
            String detail = String.format("收藏操作: type=%s, songId=%s, songListId=%s", 
                    collect.getType(), 
                    collect.getSongId() != null ? collect.getSongId() : "null",
                    collect.getSongListId() != null ? collect.getSongListId() : "null");
            userActionLogService.recordAction(addCollectRequest.getUserId(), ACTION_ADD_COLLECT, detail, IpUtils.getClientIp());
            
            return R.success("收藏成功", true);
        } else {
            return R.error("收藏失败");
        }
    }

    @Override
    public R existSongId(CollectRequest isCollectRequest) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",isCollectRequest.getUserId());
        queryWrapper.eq("song_id",isCollectRequest.getSongId());
        if (collectMapper.selectCount(queryWrapper) > 0) {
            return R.success("已收藏", true);
        } else {
            return R.success("未收藏", false);
        }
    }

    @Override
    public R deleteCollect(Integer userId, Integer songId) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("song_id",songId);
        if (collectMapper.delete(queryWrapper) > 0) {
            // 记录取消收藏操作
            String detail = String.format("取消收藏: songId=%s", songId);
            userActionLogService.recordAction(userId, ACTION_DELETE_COLLECT, detail, IpUtils.getClientIp());
            
            return R.success("取消收藏", false);
        } else {
            return R.error("取消收藏失败");
        }
    }

    @Override
    public R collectionOfUser(Integer userId) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        return R.success("用户收藏", collectMapper.selectList(queryWrapper));
    }
}
