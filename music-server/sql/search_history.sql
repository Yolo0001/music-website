-- 搜索历史表
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE `search_history` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned DEFAULT NULL COMMENT '用户ID，可为空表示未登录用户',
  `keyword` varchar(255) NOT NULL COMMENT '搜索关键词',
  `search_type` varchar(20) DEFAULT 'song' COMMENT '搜索类型：song-歌曲',
  `create_time` datetime NOT NULL COMMENT '搜索时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='搜索历史表';














