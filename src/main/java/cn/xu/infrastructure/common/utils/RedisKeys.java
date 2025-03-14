package cn.xu.infrastructure.common.utils;

// 点赞关系 Hash（兼容 type 字段）
// Key 格式: like:relation:{type}:{target_id}
// Field: user_id
// Value: status（1点赞/0取消） + 时间戳（用于排序）
public class RedisKeys {
    public static String likeRelationKey(Integer type, Long targetId) {
        return "like:relation:" + type + ":" + targetId;
    }

    // 点赞计数器 String
    public static String likeCountKey(Integer type, Long targetId) {
        return "like:count:" + type + ":" + targetId;
    }
}
