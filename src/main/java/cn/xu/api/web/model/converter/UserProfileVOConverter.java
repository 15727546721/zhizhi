package cn.xu.api.web.model.converter;

import cn.xu.api.web.model.vo.user.UserProfileVO;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.UserProfileApplicationService;
import org.springframework.stereotype.Component;

/**
 * 个人主页VO转换器
 * 负责将领域层的UserProfileData转换为前端需要的UserProfileVO
 * 符合DDD分层原则，避免领域对象直接暴露给前端
 * 
 * @author zhizhi
 */
@Component
public class UserProfileVOConverter {
    
    /**
     * 将UserProfileData转换为UserProfileVO
     * 
     * @param profileData 个人主页数据
     * @return 个人主页VO
     */
    public UserProfileVO convertToUserProfileVO(UserProfileApplicationService.UserProfileData profileData) {
        if (profileData == null || profileData.getUser() == null) {
            return null;
        }
        
        UserEntity user = profileData.getUser();
        UserProfileApplicationService.UserProfileStats stats = profileData.getStats();
        
        // 构建用户基本信息
        // 注意：UserEntity中可能没有school、major等字段，这些字段可能在其他表中
        // 这里先使用UserEntity已有的字段，扩展字段可以后续从UserInfoEntity获取
        UserProfileVO.UserBasicInfoVO basicInfo = UserProfileVO.UserBasicInfoVO.builder()
                .id(user.getId())
                .username(user.getUsernameValue())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .description(user.getDescription())
                .phone(user.getPhoneValue())
                .email(user.getEmailValue())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                // 以下字段如果UserEntity中没有，可以后续从UserInfoEntity或其他表获取
                .school(null)
                .major(null)
                .education(null)
                .graduationYear(null)
                .workStatus(null)
                .company(null)
                .position(null)
                .workYears(null)
                .interests(null)
                .direction(null)
                .goal(null)
                .createTime(user.getCreateTime())
                .build();
        
        // 构建统计数据
        UserProfileVO.UserProfileStatsVO statsVO = UserProfileVO.UserProfileStatsVO.builder()
                .postCount(stats != null ? stats.getPostCount() : 0L)
                .followCount(stats != null ? stats.getFollowCount() : 0L)
                .fansCount(stats != null ? stats.getFansCount() : 0L)
                .likeCount(stats != null ? stats.getLikeCount() : 0L)
                .commentCount(stats != null ? stats.getCommentCount() : 0L)
                .collectionCount(stats != null ? stats.getCollectionCount() : 0L)
                .topicCount(stats != null ? stats.getTopicCount() : 0L)
                .build();
        
        // 构建个人主页VO
        return UserProfileVO.builder()
                .basicInfo(basicInfo)
                .stats(statsVO)
                .isOwnProfile(profileData.isOwnProfile())
                .isFollowing(profileData.getIsFollowing())
                .isFollowedBy(profileData.getIsFollowedBy())
                .build();
    }
}

