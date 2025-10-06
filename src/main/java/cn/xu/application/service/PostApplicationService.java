package cn.xu.application.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.service.IPostService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 帖子应用服务
 * 负责协调领域服务完成复杂的业务流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final IPostService postService;
    private final IUserService userService;

    /**
     * 保存帖子
     * 
     * @param post 帖子实体
     * @return 帖子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long savePost(PostEntity post) {
        return postService.createPost(post);
    }

    /**
     * 发布帖子
     * 
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPost(Long userId, Long postId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }
        
        // 验证用户状态
        user.validateCanPerformAction();
        
        // 获取帖子
        Optional<PostEntity> postOpt = postService.findPostEntityById(postId);
        if (!postOpt.isPresent()) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在");
        }
        
        PostEntity post = postOpt.get();
        
        // 验证权限
        post.validateOwnership(userId);
        
        // 发布帖子
        post.publish();
        
        // 保存帖子
        postService.updatePost(post);
        
        return true;
    }
    
    /**
     * 获取用户的帖子列表
     * 
     * @param userId 用户ID
     * @param status 帖子状态
     * @param page 页码
     * @param size 每页大小
     * @return 帖子列表
     */
    public List<PostEntity> getUserPosts(Long userId, String status, int page, int size) {
        return postService.getUserPosts(userId, status, page, size);
    }
    
    /**
     * 删除帖子
     * 
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePost(Long userId, Long postId) {
        // 验证用户是否存在
        UserEntity user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }
        
        // 获取帖子
        Optional<PostEntity> postOpt = postService.findPostEntityById(postId);
        if (!postOpt.isPresent()) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在");
        }
        
        PostEntity post = postOpt.get();
        
        // 验证权限（管理员可以删除任何帖子）
        post.validateOwnership(userId, user.isAdmin());
        
        // 删除帖子
        post.delete();
        
        // 保存帖子
        postService.updatePost(post);
        
        return true;
    }
}