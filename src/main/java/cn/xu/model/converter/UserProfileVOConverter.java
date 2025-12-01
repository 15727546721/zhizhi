package cn.xu.model.converter;

import cn.xu.model.entity.User;
import cn.xu.model.vo.user.UserProfileVO;
import cn.xu.service.user.UserProfileService;
import org.springframework.stereotype.Component;

/**
 * 个人主页VO转换器
 * 负责将UserProfileData转换为前端需要的UserProfileVO
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
    public UserProfileVO convertToUserProfileVO(UserProfileService.UserProfileData profileData) {
        if (profileData == null || profileData.getUser() == null) {
            return null;
        }
        
        User user = profileData.getUser();
        UserProfileService.UserProfileStats stats = profileData.getStats();
        
        // 构建用户基本信息
        UserProfileVO.UserBasicInfoVO basicInfo = UserProfileVO.UserBasicInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .description(user.getDescription())
                .phone(user.getPhone())
                .email(user.getEmail())
                .region(user.getRegion())
                .birthday(user.getBirthday())
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
                .build();
        
        // 构建个人主页VO
        return UserProfileVO.builder()
                .basicInfo(basicInfo)
                .stats(statsVO)
                .isOwnProfile(profileData.getIsOwnProfile())
                .isFollowing(profileData.getIsFollowing())
                .isFollowedBy(profileData.getIsFollowedBy())
                .build();
    }
}

