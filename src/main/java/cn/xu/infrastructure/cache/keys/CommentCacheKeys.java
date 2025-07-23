package cn.xu.infrastructure.cache.keys;

public class CommentCacheKeys {
    private static final String PREFIX = "comment:";

    // 带预览回复的评论列表key
    public static String previewCommentsKey(Integer targetType, Long targetId,
                                            Integer pageNo, Integer pageSize) {
        return String.format("%spreview:%d:%d:%d:%d",
                PREFIX, targetType, targetId, pageNo, pageSize);
    }

    // 评论回复分页key
    public static String commentRepliesKey(Long commentId,
                                           Integer pageNo, Integer pageSize) {
        return String.format("%sreplies:%d:%d:%d",
                PREFIX, commentId, pageNo, pageSize);
    }

    // 评论最后更新时间key（用于缓存版本控制）
    public static String commentsLastUpdateKey() {
        return PREFIX + "last_update";
    }
}