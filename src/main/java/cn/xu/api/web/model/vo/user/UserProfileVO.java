package cn.xu.api.web.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 个人主页视图对象
 * 聚合用户基本信息、统计数据等个人主页所需的所有数据
 * 符合DDD规范，作为应用层向展示层传输的数据载体
 * 
 * @author zhizhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "个人主页视图对象")
public class UserProfileVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户基本信息
     */
    @Schema(description = "用户基本信息")
    private UserBasicInfoVO basicInfo;
    
    /**
     * 统计数据
     */
    @Schema(description = "统计数据")
    private UserProfileStatsVO stats;
    
    /**
     * 是否为当前用户自己的主页
     */
    @Schema(description = "是否为当前用户自己的主页")
    private Boolean isOwnProfile;
    
    /**
     * 是否已关注（查看他人主页时有效）
     * 表示当前用户是否关注了目标用户
     */
    @Schema(description = "是否已关注（查看他人主页时有效，表示当前用户是否关注了目标用户）")
    private Boolean isFollowing;
    
    /**
     * 是否被关注（查看他人主页时有效）
     * 表示目标用户是否关注了当前用户
     */
    @Schema(description = "是否被关注（查看他人主页时有效，表示目标用户是否关注了当前用户）")
    private Boolean isFollowedBy;
    
    /**
     * 用户基本信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "用户基本信息")
    public static class UserBasicInfoVO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "用户ID")
        private Long id;
        
        @Schema(description = "用户名")
        private String username;
        
        @Schema(description = "昵称")
        private String nickname;
        
        @Schema(description = "头像")
        private String avatar;
        
        @Schema(description = "性别：1-男，2-女")
        private Integer gender;
        
        @Schema(description = "个人描述")
        private String description;
        
        @Schema(description = "手机号")
        private String phone;
        
        @Schema(description = "邮箱")
        private String email;
        
        @Schema(description = "地区")
        private String region;
        
        @Schema(description = "生日")
        private String birthday;
        
        @Schema(description = "学校")
        private String school;
        
        @Schema(description = "专业")
        private String major;
        
        @Schema(description = "学历")
        private String education;
        
        @Schema(description = "毕业年份")
        private String graduationYear;
        
        @Schema(description = "工作状态")
        private String workStatus;
        
        @Schema(description = "公司")
        private String company;
        
        @Schema(description = "职位")
        private String position;
        
        @Schema(description = "工作年限")
        private String workYears;
        
        @Schema(description = "兴趣")
        private String interests;
        
        @Schema(description = "主攻方向")
        private String direction;
        
        @Schema(description = "目标")
        private String goal;
        
        @Schema(description = "创建时间")
        private java.time.LocalDateTime createTime;
    }
    
    /**
     * 统计数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "个人主页统计数据")
    public static class UserProfileStatsVO implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "帖子数")
        private Long postCount;
        
        @Schema(description = "关注数")
        private Long followCount;
        
        @Schema(description = "粉丝数")
        private Long fansCount;
        
        @Schema(description = "获赞数")
        private Long likeCount;
        
        @Schema(description = "评论数")
        private Long commentCount;
        
        @Schema(description = "收藏数")
        private Long collectionCount;
        
        @Schema(description = "话题数")
        private Long topicCount;
    }
}

