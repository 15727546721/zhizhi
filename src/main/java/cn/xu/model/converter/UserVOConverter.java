package cn.xu.model.converter;

import cn.xu.model.entity.User;
import cn.xu.model.vo.user.UserRankingResponse;
import cn.xu.model.vo.user.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User对象转换器
 * 用于将User PO转换为VO对象
 * 
 * @author xu
 */
@Component
public class UserVOConverter {

    /**
     * 将User转换为UserResponse
     */
    public UserResponse convertToUserResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setGender(user.getGender());
        response.setPhone(user.getPhone());
        response.setRegion(user.getRegion());
        response.setBirthday(user.getBirthday());
        response.setDescription(user.getDescription());
        response.setStatus(user.getStatus());
        response.setFollowCount(user.getFollowCount());
        response.setFansCount(user.getFansCount());
        response.setLikeCount(user.getLikeCount());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        
        return response;
    }
    
    /**
     * 批量转换User为UserResponse
     */
    public List<UserResponse> convertToUserResponseList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 将User转换为UserRankingResponse（用于排行榜）
     */
    public UserRankingResponse convertToUserRankingResponse(User user) {
        if (user == null) {
            return null;
        }
        
        UserRankingResponse response = new UserRankingResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setFollowCount(user.getFollowCount() != null ? user.getFollowCount() : 0L);
        response.setFansCount(user.getFansCount() != null ? user.getFansCount() : 0L);
        response.setLikeCount(user.getLikeCount() != null ? user.getLikeCount() : 0L);
        response.setPostCount(user.getPostCount() != null ? user.getPostCount() : 0L);
        
        return response;
    }
    
    /**
     * 批量转换User为UserRankingResponse
     */
    public List<UserRankingResponse> convertToUserRankingResponseList(List<User> users) {
        if (users == null) {
            return null;
        }
        
        return users.stream()
                .map(this::convertToUserRankingResponse)
                .collect(Collectors.toList());
    }
}
