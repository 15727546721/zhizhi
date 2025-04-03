package cn.xu.infrastructure.common.utils;

import cn.xu.domain.like.model.LikeType;

import java.util.Objects;

// 点赞关系 Hash（兼容 type 字段）
// Key 格式: like:relation:{type}:{target_id}
// Field: user_id
// Value: status（1点赞/0取消） + 时间戳（用于排序）
public class RedisKeys {
    public static String likeRelationKey(Integer type, Long targetId) {
        switch (Objects.requireNonNull(LikeType.valueOf(type))) {
            case ARTICLE:
                return "like:article:" + targetId;
            case COMMENT:
                return "like:comment:" + targetId;
            case ESSAY:
                return "like:essay:" + targetId;
            default:
                throw new IllegalArgumentException("点赞类型错误: " + type);
        }
    }

}

