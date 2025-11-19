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

import java.util.ArrayList;
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
            // 注意：Redis缓存可能是部分加载（Partial Cache），checkUserFavoriteRelation返回false可能表示：
            // 1. 用户确实没收藏（且缓存完整）
            // 2. 用户收藏了，但该条目未加载到缓存（缓存不完整）
            // 3. 缓存Key根本不存在
            // 因此，我们只能信任 true (已收藏)。如果返回 false 或 null，都必须查数据库兜底。
            Boolean cachedFavorited = favoriteCacheRepository.checkUserFavoriteRelation(userId, postId, dbTargetType);
            if (Boolean.TRUE.equals(cachedFavorited)) {
                log.debug("[收藏应用服务] 从缓存中获取收藏状态：已收藏");
                return true;
            }
            
            // 缓存未命中或返回false，调用服务层方法查数据库
            boolean favorited = favoriteService.isFavorited(userId, postId, TargetType.POST.getApiCode());
            
            // 如果数据库确认已收藏，同步到缓存
            if (favorited) {
                favoriteCacheRepository.addUserFavoriteRelation(userId, postId, dbTargetType);
                log.debug("[收藏应用服务] 从数据库确认已收藏并同步到缓存");
            }
            
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
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始，默认为1）
     * @param size 每页大小（默认为20，最大不超过100）
     * @return 收藏夹列表
     */
    public List<FavoriteFolderEntity> getUserFolders(Long userId, Integer page, Integer size) {
        log.info("[收藏应用服务] 获取用户收藏夹列表，用户ID: {}, 页码: {}, 每页大小: {}", userId, page, size);
        
        // 参数校验和默认值设置
        int safePage = page != null && page > 0 ? page : 1;
        int safeSize = size != null && size > 0 ? Math.min(size, 100) : 20;
        
        // 计算偏移量，并添加硬编码最大限制防止OOM
        int offset = (safePage - 1) * safeSize;
        int maxLimit = 1000; // 硬编码最大限制，防止恶意调用
        
        if (offset >= maxLimit) {
            log.warn("[收藏应用服务] 用户收藏夹查询偏移量超出限制，用户ID: {}, 偏移量: {}, 最大限制: {}", userId, offset, maxLimit);
            return new ArrayList<>(); // 超出限制返回空列表
        }
        
        // 调整查询大小，确保不超过最大限制
        int adjustedSize = Math.min(safeSize, maxLimit - offset);
        
        try {
            // 降级处理：使用无分页方法但限制结果数量
            List<FavoriteFolderEntity> allFolders = favoriteService.getFoldersByUserId(userId);
            if (allFolders != null && allFolders.size() > maxLimit) {
                log.warn("[收藏应用服务] 用户收藏夹数量过多，截取前{}项，用户ID: {}, 总数量: {}", maxLimit, userId, allFolders.size());
                return allFolders.subList(0, maxLimit);
            }
            // 手动实现分页
            int startIndex = offset;
            int endIndex = Math.min(offset + adjustedSize, allFolders.size());
            if (startIndex >= allFolders.size()) {
                return new ArrayList<>();
            }
            return allFolders.subList(startIndex, endIndex);
        } catch (Exception e) {
            log.error("[收藏应用服务] 获取用户收藏夹列表失败，用户ID: {}, 页码: {}, 每页大小: {}", userId, safePage, adjustedSize, e);
            // 降级处理：使用无分页方法但限制结果数量
            List<FavoriteFolderEntity> allFolders = favoriteService.getFoldersByUserId(userId);
            if (allFolders != null && allFolders.size() > maxLimit) {
                log.warn("[收藏应用服务] 用户收藏夹数量过多，截取前{}项，用户ID: {}, 总数量: {}", maxLimit, userId, allFolders.size());
                return allFolders.subList(0, maxLimit);
            }
            return allFolders != null ? allFolders : new ArrayList<>();
        }
    }
    
    /**
     * 获取用户的收藏夹列表（兼容性方法，建议使用带分页参数的方法）
     * 
     * @deprecated 建议使用 getUserFolders(Long userId, Integer page, Integer size) 方法
     */
    @Deprecated
    public List<FavoriteFolderEntity> getUserFolders(Long userId) {
        log.warn("[收藏应用服务] 使用了已弃用的无分页方法，用户ID: {}", userId);
        return getUserFolders(userId, 1, 20); // 默认第一页，20条记录
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
            
            // 执行收藏操作（如果未收藏，favorite方法内部已经更新了缓存和用户收藏数量）
            if (!alreadyFavorited) {
                favoriteService.favorite(userId, postId, TargetType.POST.getApiCode());
                log.info("[收藏应用服务] 用户首次收藏帖子，已同步更新收藏状态和缓存，用户ID: {}, 帖子ID: {}", userId, postId);
            } else {
                log.info("[收藏应用服务] 用户已收藏该帖子，仅更新文件夹关系，用户ID: {}, 帖子ID: {}", userId, postId);
            }
            
            // 将帖子添加到收藏夹（addTargetToFolder方法内部已经处理了收藏夹数量更新和缓存更新）
            favoriteService.addTargetToFolder(userId, postId, TargetType.POST.getApiCode(), folderId);
            
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