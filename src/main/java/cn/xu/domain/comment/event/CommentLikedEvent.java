package cn.xu.domain.comment.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentLikedEvent {
    private final Long commentId;
}