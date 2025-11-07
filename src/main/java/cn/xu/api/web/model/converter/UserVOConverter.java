package cn.xu.api.web.model.converter;

import cn.xu.api.web.model.vo.comment.CommentUserResponse;
import cn.xu.api.web.model.vo.follow.FollowUserResponse;
import cn.xu.api.web.model.vo.user.UserRankingResponse;
import cn.xu.api.web.model.vo.user.UserResponse;
import cn.xu.domain.user.model.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 用户VO转换器
 * 负责将UserEntity转换为各种前端响应VO
 * 符合DDD分层原则，避免领域实体直接暴露给前端
 * 
 * @author zhizhi
 */
@Component
public class UserVOConverter {

    /**
     * 将UserEntity转换为UserResponse
     * 返回用户的完整信息
     */
    public UserResponse convertToUserResponse(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsernameValue())
                .email(userEntity.getEmailValue())
                .nickname(userEntity.getNickname())
                .avatar(userEntity.getAvatar())
                .gender(userEntity.getGender())
                .phone(userEntity.getPhoneValue() != null ? userEntity.getPhoneValue() : "")
                .region(userEntity.getRegion())
                .birthday(userEntity.getBirthday())
                .description(userEntity.getDescription())
                .status(userEntity.getStatusCode())
                .followCount(userEntity.getFollowCount())
                .fansCount(userEntity.getFansCount())
                .likeCount(userEntity.getLikeCount())
                .createTime(userEntity.getCreateTime())
                .updateTime(userEntity.getUpdateTime())
                .build();
    }
    
    /**
     * 将UserEntity转换为CommentUserResponse
     * 用于评论接口返回的用户信息（精简版）
     */
    public CommentUserResponse convertToCommentUserResponse(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        
        CommentUserResponse response = new CommentUserResponse();
        response.setId(userEntity.getId());
        response.setNickname(userEntity.getNickname());
        response.setAvatar(userEntity.getAvatar());
        // 注意：UserEntity中没有level属性，这里可能需要从其他地方获取
        // 如果有用户等级服务，可以在这里注入并获取
        // response.setLevel(userLevelService.getUserLevel(userEntity.getId()));
        
        return response;
    }
    
    /**
     * 将UserEntity转换为FollowUserResponse
     * 用于关注列表返回的用户信息
     */
    public FollowUserResponse convertToFollowUserResponse(UserEntity userEntity, boolean isFollowed) {
        if (userEntity == null) {
            return null;
        }
        
        return new FollowUserResponse()
                .setUserId(userEntity.getId())
                .setUsername(userEntity.getUsernameValue())
                .setAvatar(userEntity.getAvatar())
                .setFollowTime(LocalDateTime.now()) // 实际应用中应该从数据库获取
                .setStatus(isFollowed ? 1 : 0);
    }
    
    /**
     * 获取用户的简化信息
     * 用于需要返回用户基本信息的场景
     */
    public UserResponse getUserBasicInfo(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(userEntity.getId())
                .nickname(userEntity.getNickname())
                .avatar(userEntity.getAvatar())
                .build();
    }
    
    /**
     * 将UserEntity转换为UserRankingResponse
     * 用于用户排行榜接口返回
     */
    public UserRankingResponse convertToUserRankingResponse(UserEntity userEntity, Integer rank) {
        if (userEntity == null) {
            return null;
        }
        
        return UserRankingResponse.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsernameValue())
                .nickname(userEntity.getNickname())
                .avatar(userEntity.getAvatar())
                .description(userEntity.getDescription())
                .fansCount(userEntity.getFansCount())
                .followCount(userEntity.getFollowCount())
                .likeCount(userEntity.getLikeCount())
                .postCount(userEntity.getPostCount() != null ? userEntity.getPostCount() : 0L)
                .rank(rank)
                .rankChange(0) // 排名变化，需要历史数据支持，暂时设为0
                .build();
    }
    
    /**
     * 将UserEntity列表转换为UserRankingResponse列表
     * 用于用户排行榜接口返回
     */
    public List<UserRankingResponse> convertToUserRankingResponseList(List<UserEntity> userEntities) {
        if (userEntities == null || userEntities.isEmpty()) {
            return Collections.emptyList();
        }
        
        return IntStream.range(0, userEntities.size())
                .mapToObj(index -> convertToUserRankingResponse(userEntities.get(index), index + 1))
                .collect(Collectors.toList());
    }
}