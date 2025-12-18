package com.example.yin.model.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户行为日志表实体类。
 * <p>
 * 用于记录用户在系统中的关键操作行为，例如播放、收藏、搜索等，
 * 便于后续进行行为分析、审计与问题排查。
 */
@Data
@TableName("user_action_log")
public class UserActionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer userId;

    private String action;

    private String detail;

    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

