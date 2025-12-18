package com.example.yin.model.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 搜索历史记录表实体类。
 * <p>
 * 用于保存用户的搜索关键字及搜索类型，
 * 便于实现搜索历史展示、个性化推荐等功能。
 */
@TableName(value = "search_history")
@Data
public class SearchHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String keyword;

    private String searchType;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}








