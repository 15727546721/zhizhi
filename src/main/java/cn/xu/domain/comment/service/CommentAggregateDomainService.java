package cn.xu.domain.comment.service;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论聚合领域服务
 * 负责评论聚合相关的业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentAggregateDomainService {

    private final IUserService userService;

    /**
     * 为评论填充用户信息
     * @param comments 评论列表
     */
    public void fillUserInfo(List<CommentEntity> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        
        try {
            Set<Long> userIds = new HashSet<>();
            comments.forEach(comment -> collectUserIdsRecursive(comment, userIds));
            
            if (userIds.isEmpty()) {
                return;
            }
            
            Map<Long, UserEntity> userMap = userService.getBatchUserInfo(userIds);
            comments.forEach(comment -> fillUserInfoRecursive(comment, userMap));
            
            log.debug("为{}条评论填充用户信息成功，涉及{}个用户", comments.size(), userIds.size());
        } catch (Exception e) {
            log.error("填充评论用户信息失败", e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 合并子评论到父评论中
     * @param parents 父评论列表
     * @param children 子评论列表
     */
    public void mergeChildren(List<CommentEntity> parents, List<CommentEntity> children) {
        if (parents == null || parents.isEmpty() || children == null || children.isEmpty()) {
            return;
        }
        
        try {
            Map<Long, List<CommentEntity>> childMap = children.stream()
                    .collect(Collectors.groupingBy(CommentEntity::getParentId));
            
            parents.forEach(parent -> {
                List<CommentEntity> childList = childMap.getOrDefault(parent.getId(), Collections.emptyList());
                parent.setChildren(childList);
            });
            
            log.debug("合并子评论成功：{}个父评论，{}个子评论", parents.size(), children.size());
        } catch (Exception e) {
            log.error("合并子评论失败", e);
            // 设置空的子评论列表，避免影响主流程
            parents.forEach(parent -> parent.setChildren(Collections.emptyList()));
        }
    }

    /**
     * 递归收集用户ID
     */
    private void collectUserIdsRecursive(CommentEntity comment, Set<Long> userIds) {
        if (comment == null) {
            return;
        }
        
        if (comment.getUserId() != null) {
            userIds.add(comment.getUserId());
        }
        if (comment.getReplyUserId() != null) {
            userIds.add(comment.getReplyUserId());
        }
        
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            comment.getChildren().forEach(child -> collectUserIdsRecursive(child, userIds));
        }
    }

    /**
     * 递归填充用户信息
     */
    private void fillUserInfoRecursive(CommentEntity comment, Map<Long, UserEntity> userMap) {
        if (comment == null || userMap == null) {
            return;
        }
        
        comment.setUser(userMap.get(comment.getUserId()));
        comment.setReplyUser(userMap.get(comment.getReplyUserId()));
        
        if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
            comment.getChildren().forEach(child -> fillUserInfoRecursive(child, userMap));
        }
    }
}