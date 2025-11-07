package cn.xu.application.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.model.valobj.TargetType;
import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.service.IPostService;
import cn.xu.infrastructure.cache.FavoriteCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteApplicationService {
    
    private final IFavoriteService favoriteService;
    private final IPostService postService;
    private final FavoriteCacheRepository favoriteCacheRepository;
    
    /**
     * 收藏帖子
     */
    @Transactional
    public boolean favoritePost(Long userId, Long postId) {
        log.info("[收藏应用服务] 用户收藏帖子，用户ID: {}, 帖子ID: {}", userId, postId);
        
        // 检查帖子是否存在
        PostEntity post = postService.findPostEntityById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        
        // 执行收藏操作
        favoriteService.favorite(userId, postId, TargetType.POST.getApiCode());
        
        // 注意：不需要再次更新缓存，favoriteService.favorite() 内部已经处理了缓存更新
        
        return true;
    }
    
    /**
     * 取消收藏帖子
     */
    @Transactional
    public boolean unfavoritePost(Long userId, Long postId) {
        log.info("[收藏应用服务] 用户取消收藏帖子，用户ID: {}, 帖子ID: {}", userId, postId);
        
        // 检查是否已收藏
        if (!favoriteService.isFavorited(userId, postId, TargetType.POST.getApiCode())) {
            return false;
        }

        // 执行取消收藏操作
        favoriteService.unfavorite(userId, postId, TargetType.POST.getApiCode());
        
        // 注意：不需要再次更新缓存，favoriteService.unfavorite() 内部已经处理了缓存更新

        return true;
    }
    
    /**
     * 检查是否已收藏帖子
     */
    public boolean isPostFavorited(Long userId, Long postId) {
        try {
            log.info("[收藏应用服务] 检查帖子收藏状态，用户ID: {}, 帖子ID: {}", userId, postId);
            // 使用枚举的数据库代码进行缓存查询
            String dbTargetType = TargetType.POST.getDbCode();
            // 先从缓存检查用户收藏关系
            Boolean cachedFavorited = favoriteCacheRepository.checkUserFavoriteRelation(userId, postId, dbTargetType);
            if (cachedFavorited != null) {
                log.debug("[收藏应用服务] 从缓存中获取收藏状态：{}", cachedFavorited ? "已收藏" : "未收藏");
                return cachedFavorited;
            }
            
            // 缓存未命中，调用服务层方法
            boolean favorited = favoriteService.isFavorited(userId, postId, TargetType.POST.getApiCode());
            
            // 更新缓存
            favoriteCacheRepository.updateUserFavoriteRelation(userId, postId, dbTargetType, favorited);
            
            return favorited;
        } catch (Exception e) {
            log.error("[收藏应用服务] 检查帖子收藏状态失败，用户ID: {}, 帖子ID: {}", userId, postId, e);
            // 降级处理，直接调用服务层方法
            return favoriteService.isFavorited(userId, postId, TargetType.POST.getApiCode());
        }
    }
    
    /**
     * 获取用户收藏的帖子数量
     */
    public int getFavoriteCount(Long userId) {
        try {
            log.info("[收藏应用服务] 获取用户收藏的帖子数量，用户ID: {}", userId);
            
            // 使用枚举的数据库代码进行缓存查询
            String dbTargetType = TargetType.POST.getDbCode();
            // 优先从缓存获取
            Long cachedCount = favoriteCacheRepository.getUserFavoriteCount(userId, dbTargetType);
            if (cachedCount != null) {
                log.debug("[收藏应用服务] 从缓存中获取用户收藏数: {}", cachedCount);
                return Math.max(0, cachedCount.intValue());
            }
            
            // 缓存未命中，从数据库获取
            int dbCount = favoriteService.countFavoritedItems(userId, TargetType.POST.getApiCode());
            
            // 将数据库结果同步到缓存
            favoriteCacheRepository.setUserFavoriteCount(userId, dbTargetType, (long) dbCount);
            log.debug("[收藏应用服务] 从数据库获取用户收藏数并同步到缓存: {}", dbCount);
            
            return dbCount;
        } catch (Exception e) {
            log.error("[收藏应用服务] 获取用户收藏的帖子数量失败，用户ID: {}", userId, e);
            // 降级处理，直接从数据库获取
            return favoriteService.countFavoritedItems(userId, TargetType.POST.getApiCode());
        }
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
        try {
            log.info("[收藏应用服务] 用户收藏帖子到文件夹，用户ID: {}, 帖子ID: {}, 文件夹ID: {}", userId, postId, folderId);
            
            // 检查帖子是否存在
            PostEntity post = postService.findPostEntityById(postId)
                    .orElseThrow(() -> new BusinessException("帖子不存在"));
            
            // 检查收藏夹是否存在且属于该用户
            FavoriteFolderEntity folder = favoriteService.getFoldersByUserId(userId).stream()
                    .filter(f -> f.getId().equals(folderId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("收藏夹不存在或无权限"));
            
            // 检查是否已经收藏了该帖子
            boolean alreadyFavorited = favoriteService.isFavorited(userId, postId, TargetType.POST.getApiCode());
            
            // 执行收藏操作
            if (!alreadyFavorited) {
                favoriteService.favorite(userId, postId, TargetType.POST.getApiCode());
                // 更新缓存
                String dbTargetType = TargetType.POST.getDbCode();
                favoriteCacheRepository.updateUserFavoriteRelation(userId, postId, dbTargetType, true);
                log.info("[收藏应用服务] 用户首次收藏帖子，已同步更新收藏状态和缓存，用户ID: {}, 帖子ID: {}", userId, postId);
            } else {
                log.info("[收藏应用服务] 用户已收藏该帖子，仅更新文件夹关系，用户ID: {}, 帖子ID: {}", userId, postId);
            }
            
            // 将帖子添加到收藏夹
            favoriteService.addTargetToFolder(userId, postId, TargetType.POST.getApiCode(), folderId);
            
            // 更新收藏夹的内容数量
            folder.setContentCount(folder.getContentCount() + 1);
            
            // 更新文件夹缓存
            favoriteCacheRepository.incrementFolderContentCount(folderId);
            
            log.info("[收藏应用服务] 收藏帖子到文件夹成功完成");
            return true;
        } catch (BusinessException e) {
            log.error("[收藏应用服务] 收藏帖子到文件夹业务异常，用户ID: {}, 帖子ID: {}, 文件夹ID: {}", userId, postId, folderId, e);
            throw e;
        } catch (Exception e) {
            log.error("[收藏应用服务] 收藏帖子到文件夹系统异常，用户ID: {}, 帖子ID: {}, 文件夹ID: {}", userId, postId, folderId, e);
            throw new BusinessException("系统异常");
        }
    }
}