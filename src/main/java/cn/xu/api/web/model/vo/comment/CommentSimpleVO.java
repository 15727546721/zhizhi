package cn.xu.api.web.model.vo.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSimpleVO {

    private Long id;
    private Long parentId;
    private Long userId;
    private String nickname;
    private String avatar;

    private Long replyUserId;
    private String replyNickname;

    private String content;
    private List<String> imageUrls;

    private LocalDateTime createTime;
}

