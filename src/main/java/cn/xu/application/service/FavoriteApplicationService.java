package cn.xu.application.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.service.IPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏应用服务
 */
@Service
@RequiredArgsConstructor
public class FavoriteApplicationService {
    
    private final IFavoriteService favoriteService;
    private final IPostService postService;
    
    /**
     * 收藏帖子
     */
    @Transactional
    public boolean favoritePost(Long userId, Long postId) {
        // 检查帖子是否存在
        PostEntity post = postService.findPostEntityById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        
        // 执行收藏操作
        favoriteService.favorite(userId, postId, "post");
        
        // 更新帖子的收藏数量
        post.increaseFavoriteCount();
        postService.updatePost(post);
        
        return true;
    }
    
    /**
     * 取消收藏帖子
     */
    @Transactional
    public boolean unfavoritePost(Long userId, Long postId) {
        // 检查是否已收藏
        if (!favoriteService.isFavorited(userId, postId, "post")) {
            return false;
        }

        // 执行取消收藏操作
        favoriteService.unfavorite(userId, postId, "post");

        // 更新帖子的收藏数量
        PostEntity post = postService.findPostEntityById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        post.decreaseFavoriteCount();
        postService.updatePost(post);

        return true;
    }
    
    /**
     * 检查是否已收藏帖子
     */
    public boolean isPostFavorited(Long userId, Long postId) {
        return favoriteService.isFavorited(userId, postId, "post");
    }
    
    /**
     * 获取用户收藏的帖子数量
     */
    public int getFavoriteCount(Long userId) {
        return favoriteService.countFavoritedItems(userId, "post");
    }
    
    /**
     * 获取用户的收藏夹列表
     */
    public List<FavoriteFolderEntity> getUserFolders(Long userId) {
        return favoriteService.getFoldersByUserId(userId);
    }
    
    /**
     * 收藏帖子到指定收藏夹
     */
    @Transactional
    public boolean favoritePostToFolder(Long userId, Long folderId, Long postId) {
        // 检查帖子是否存在
        PostEntity post = postService.findPostEntityById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        
        // 检查收藏夹是否存在且属于该用户
        FavoriteFolderEntity folder = favoriteService.getFoldersByUserId(userId).stream()
                .filter(f -> f.getId().equals(folderId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("收藏夹不存在或无权限"));
        
        // 执行收藏操作
        favoriteService.favorite(userId, postId, "post");
        
        // 将帖子添加到收藏夹
        favoriteService.addTargetToFolder(userId, postId, "post", folderId);
        
        // 更新帖子的收藏数量
        post.increaseFavoriteCount();
        postService.updatePost(post);
        
        // 更新收藏夹的内容数量
        folder.setContentCount(folder.getContentCount() + 1);
        
        return true;
    }
}