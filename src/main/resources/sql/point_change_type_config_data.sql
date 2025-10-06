-- 积分变动类型配置初始化数据
-- 创建时间: 2025-09-23

-- 插入积分变动类型配置数据
INSERT INTO `point_change_type_config` (`change_type`, `change_name`, `point_value`, `daily_limit`, `description`, `is_active`) VALUES
('LIKE_GIVE', '点赞', 1, 1, '点赞文章/交流/问题/评论/回复可获得', 1),
('LIKE_RECEIVE', '被点赞', 1, -1, '文章/交流/问题/评论/回复被点赞可获得', 1),
('FAVORITE_GIVE', '收藏', 1, 1, '收藏文章/交流/问题可获得', 1),
('FAVORITE_RECEIVE', '被收藏', 1, -1, '文章/交流/问题被收藏可获得', 1),
('PUBLISH', '发布', 5, 2, '发布文章/交流/问题/评论/回复可获得', 1),
('FOLLOW_GIVE', '关注', 3, 1, '关注其他用户，本人可获得', 1),
('FOLLOW_RECEIVE', '被关注', 3, -1, '被其他用户关注可获得', 1),
('INFO_COMPLETE', '信息完善', 5, 1, '绑定手机号、上传头像、修改个人昵称', 1),
('VIP_JOIN', '成为会员', 10, -1, '成为/续费会员可获得', 1),
('INVITE', '邀请推广', 10, -1, '邀请好友成为会员可获得', 1),
('BUY_COURSE', '购买课程', 5, -1, '购买/兑换单个课程加分', 1),
('POST_FEATURED', '帖子被精选', 20, -1, '文章/交流/问题被精选', 1),
('COMMENT_FEATURED', '评论被精选', 10, -1, '评论/回复被精选', 1),
('ANSWER_ACCEPTED', '回答被采纳', 10, -1, '「问题」板块的回答被采纳', 1),
('POST_RECOMMENDED', '被推荐', 5, -1, '文章/交流/问题被推荐可获得', 1);