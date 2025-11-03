-- 为帖子表添加分享数字段
ALTER TABLE `post` 
ADD COLUMN `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享次数' 
AFTER `comment_count`;

-- 更新现有数据的热度分数计算（如果需要）
-- UPDATE `post` SET `hot_score` = `view_count` + `like_count` * 5 + `collect_count` * 3 + `comment_count` * 2 + `share_count` * 4;