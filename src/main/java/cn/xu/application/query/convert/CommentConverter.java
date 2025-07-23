package cn.xu.application.query.convert;

import cn.xu.application.query.comment.dto.CommentDTO;
import cn.xu.application.query.user.UserDTO;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.user.model.entity.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentConverter {

    public static UserDTO convertToUserDTO(UserEntity userEntity) {
        if (userEntity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(userEntity.getId());
        dto.setNickname(userEntity.getNickname());
        dto.setAvatar(userEntity.getAvatar());
        // ...其他字段
        return dto;
    }

    public static List<CommentDTO> convertToCommentDTOs(List<CommentEntity> comments, Map<Long, UserEntity> userMap) {
        return comments.stream().map(comment -> {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());
            dto.setCreateTime(comment.getCreateTime());
            dto.setUser(convertToUserDTO(userMap.get(comment.getUserId())));
            dto.setReplyUser(convertToUserDTO(userMap.get(comment.getReplyUserId())));
            return dto;
        }).collect(Collectors.toList());
    }
}
