package cn.xu.domain.comment.service.comment;

import cn.xu.api.controller.web.comment.CommentListDTO;
import cn.xu.api.controller.web.comment.CommentRequest;
import cn.xu.domain.comment.model.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.domain.comment.service.ICommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService implements ICommentService {

    @Resource
    private ICommentRepository commentRepository;
    @Override
    public void addComment(CommentRequest comment) {
        commentRepository.addComment(comment);
    }

    @Override
    public void replyComment(CommentRequest comment) {
        commentRepository.replyComment(comment);
    }

    @Override
    public List<CommentEntity> getArticleComments(Long articleId) {
        List<CommentEntity> comments = commentRepository.getArticleComments(articleId);
        return buildNestedComments(comments);
    }

    // 构建嵌套评论列表的方法
    public List<CommentEntity> buildNestedComments(List<CommentEntity> comments) {
        // 使用Map将评论按父评论ID分组
        Map<Long, List<CommentEntity>> commentMap = new HashMap<>();
        for (CommentEntity comment : comments) {
            System.out.println(comment);
            commentMap.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
        }

        // 构建嵌套评论列表
        List<CommentEntity> nestedComments = new ArrayList<>();
        for (CommentEntity comment : comments) {
            if (comment.getParentId() == null) { // 顶级评论
                nestedComments.add(comment);
                // 添加子评论
                List<CommentEntity> children = commentMap.get(comment.getId());
                if (children != null) {
                    comment.setChildren(children);
                }
            }
        }
        return nestedComments;
    }
}
