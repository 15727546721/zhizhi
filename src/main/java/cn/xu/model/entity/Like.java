package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞实体类
 *
 * 用于记录用户对目标对象的点赞操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 状态 ====================

    /** 点赞状态：已点赞 */
    public static final int STATUS_LIKED = 1;

    /** 点赞状态：取消点赞 */
    public static final int STATUS_UNLIKED = 0;

    // ==================== 数据字段 ====================

    /** 点赞ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标对象ID */
    private Long targetId;

    /** 点赞类型：1-帖子，2-文章，3-评论 */
    private Integer type;

    /** 点赞状态：1-已点赞，0-取消点赞 */
    private Integer status;

    /** 点赞时间 */
    private LocalDateTime createTime;

    // ==================== 点赞类型枚举 ====================

    public enum LikeType {
        POST(1, "帖子"),
        ESSAY(2, "文章"),
        COMMENT(3, "评论");

        private final int code;
        private final String desc;

        LikeType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        /** 获取对应的Redis键名 */
        public String getRedisKeyName() {
            return this.name().toLowerCase();
        }

        /** 是否为帖子类型 */
        public boolean isPost() {
            return this == POST;
        }

        /** 是否为文章类型 */
        public boolean isEssay() {
            return this == ESSAY;
        }

        /** 是否为评论类型 */
        public boolean isComment() {
            return this == COMMENT;
        }

        /** 通过代码获取类型 */
        public static LikeType fromCode(Integer code) {
            if (code == null) {
                throw new BusinessException("点赞类型不能为空");
            }
            for (LikeType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new BusinessException("不存在的点赞类型 " + code);
        }

        /** 通过名称获取类型 */
        public static LikeType fromName(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /** 通过数字代码获取点赞类型 */
        public static LikeType valueOf(Integer code) {
            return fromCode(code);
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 创建点赞记录
     */
    public static Like createLike(Long userId, Long targetId, Integer type) {
        validateParams(userId, targetId, type);

        return Like.builder()
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .status(STATUS_LIKED)
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 执行点赞操作
     */
    public void like() {
        if (isLiked()) {
            throw new BusinessException("已经点赞，不能重复点赞");
        }
        this.status = STATUS_LIKED;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 执行取消点赞操作
     */
    public void unlike() {
        if (!isLiked()) {
            throw new BusinessException("未点赞，不能取消点赞");
        }
        this.status = STATUS_UNLIKED;
    }

    /**
     * 是否已点赞
     */
    public boolean isLiked() {
        return STATUS_LIKED == this.status;
    }

    /**
     * 验证点赞参数是否合法
     */
    private static void validateParams(Long userId, Long targetId, Integer type) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("点赞类型不能为空");
        }

        // 验证类型是否合法
        LikeType.fromCode(type);
    }

    /**
     * 验证当前对象的点赞参数是否合法
     */
    public void validate() {
        validateParams(this.userId, this.targetId, this.type);
    }
}
