package cn.xu.application.query.comment;

import cn.xu.api.web.model.dto.comment.FindChildCommentItemVO;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.api.web.model.dto.comment.FindCommentReq;
import cn.xu.api.web.model.dto.comment.FindReplyReq;
import cn.xu.domain.comment.repository.ICommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {
    private final ICommentRepository commentRepository;

    public List<FindCommentItemVO> findTopComments(FindCommentReq request) {
        return commentRepository.findRootCommentWithUser(
                request.getTargetType(),
                request.getTargetId(),
                request.getPageNo(),
                request.getPageSize()
        );
    }

    public List<FindChildCommentItemVO> findChildComments(FindReplyReq request) {
        return commentRepository.findReplyPageWithUser(
                request.getCommentId(),
                request.getPageNo(),
                request.getPageSize()
        );
    }

}