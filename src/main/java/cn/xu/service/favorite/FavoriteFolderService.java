package cn.xu.service.favorite;

import cn.xu.model.entity.FavoriteFolder;
import cn.xu.repository.FavoriteRepository;
import cn.xu.repository.FavoriteFolderRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏夹服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteFolderService {

    private final FavoriteFolderRepository favoriteFolderRepository;
    private final FavoriteRepository favoriteRepository;
    
    private static final int MAX_FOLDER_COUNT = 20;

    /**
     * 创建收藏夹
     */
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolder createFolder(Long userId, String name, String description, boolean isPublic) {
        // 检查收藏夹数量限制
        int count = favoriteFolderRepository.countByUserId(userId);
        if (count >= MAX_FOLDER_COUNT) {
            throw new BusinessException("收藏夹数量已达上限（" + MAX_FOLDER_COUNT + "个）");
        }
        
        // 检查名称长度
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("收藏夹名称不能为空");
        }
        if (name.length() > 50) {
            throw new BusinessException("收藏夹名称不能超过50个字符");
        }
        
        FavoriteFolder folder = FavoriteFolder.create(userId, name.trim(), description, isPublic);
        favoriteFolderRepository.save(folder);
        
        log.info("[收藏夹] 创建成功 - userId: {}, folderId: {}, name: {}", userId, folder.getId(), name);
        return folder;
    }

    /**
     * 更新收藏夹
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(Long userId, Long folderId, String name, String description, Boolean isPublic) {
        FavoriteFolder folder = favoriteFolderRepository.findById(folderId);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此收藏夹");
        }
        
        // 默认收藏夹可以编辑名称、描述和公开状态，但不能修改其默认标志
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() > 50) {
                throw new BusinessException("收藏夹名称不能超过50个字符");
            }
            folder.setName(name.trim());
        }
        if (description != null) {
            folder.setDescription(description);
        }
        if (isPublic != null) {
            folder.setIsPublic(isPublic ? FavoriteFolder.PUBLIC_YES : FavoriteFolder.PUBLIC_NO);
        }
        
        favoriteFolderRepository.update(folder);
        log.info("[收藏夹] 更新成功 - userId: {}, folderId: {}, isDefault: {}", userId, folderId, folder.isDefaultFolder());
    }

    /**
     * 删除收藏夹
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long userId, Long folderId) {
        FavoriteFolder folder = favoriteFolderRepository.findById(folderId);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }
        if (!folder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此收藏夹");
        }
        if (folder.isDefaultFolder()) {
            throw new BusinessException("默认收藏夹不能删除");
        }
        
        log.info("[收藏夹] 开始删除 - userId: {}, folderId: {}, itemCount: {}, isPublic: {}", 
                userId, folderId, folder.getItemCount(), folder.isPublicFolder());
        
        // 将该收藏夹中的收藏移到默认收藏夹
        if (folder.getItemCount() > 0) {
            try {
                FavoriteFolder defaultFolder = getOrCreateDefaultFolder(userId);
                
                // 批量迁移收藏
                int movedCount = favoriteRepository.moveFavoritesToFolder(userId, folderId, defaultFolder.getId());
                log.info("[收藏夹] 迁移收藏内容 - 已迁移 {} 条记录到默认收藏夹", movedCount);
                
                // 批量更新默认收藏夹计数
                if (movedCount > 0) {
                    // 重新加载默认收藏夹获取最新计数
                    FavoriteFolder refreshedDefault = favoriteFolderRepository.findById(defaultFolder.getId());
                    if (refreshedDefault != null) {
                        log.info("[收藏夹] 默认收藏夹计数已更新 - 当前: {}", refreshedDefault.getItemCount());
                    }
                }
            } catch (Exception e) {
                log.error("[收藏夹] 迁移收藏内容失败 - userId: {}, folderId: {}", userId, folderId, e);
                throw new BusinessException("删除失败：收藏内容迁移出错，请稍后重试");
            }
        }
        
        // 删除收藏夹
        try {
            favoriteFolderRepository.deleteById(folderId);
            log.info("[收藏夹] 删除成功 - userId: {}, folderId: {}", userId, folderId);
        } catch (Exception e) {
            log.error("[收藏夹] 删除失败 - userId: {}, folderId: {}", userId, folderId, e);
            throw new BusinessException("删除失败，请稍后重试");
        }
    }

    /**
     * 获取用户的所有收藏夹
     */
    public List<FavoriteFolder> getUserFolders(Long userId) {
        return favoriteFolderRepository.findByUserId(userId);
    }

    /**
     * 获取用户的公开收藏夹
     */
    public List<FavoriteFolder> getPublicFolders(Long userId) {
        return favoriteFolderRepository.findPublicByUserId(userId);
    }

    /**
     * 获取或创建默认收藏夹
     */
    @Transactional(rollbackFor = Exception.class)
    public FavoriteFolder getOrCreateDefaultFolder(Long userId) {
        FavoriteFolder defaultFolder = favoriteFolderRepository.findDefaultByUserId(userId);
        if (defaultFolder == null) {
            defaultFolder = FavoriteFolder.createDefault(userId);
            favoriteFolderRepository.save(defaultFolder);
            log.info("[收藏夹] 创建默认收藏夹 - userId: {}, folderId: {}", userId, defaultFolder.getId());
        }
        return defaultFolder;
    }

    /**
     * 获取收藏夹详情
     */
    public FavoriteFolder getFolderById(Long folderId) {
        return favoriteFolderRepository.findById(folderId);
    }

    /**
     * 检查用户是否有权访问收藏夹
     */
    public boolean canAccessFolder(Long userId, Long folderId) {
        FavoriteFolder folder = favoriteFolderRepository.findById(folderId);
        if (folder == null) {
            return false;
        }
        // 自己的收藏夹或公开的收藏夹
        return folder.getUserId().equals(userId) || folder.isPublicFolder();
    }

    /**
     * 增加收藏夹计数
     */
    public void incrementItemCount(Long folderId) {
        if (folderId != null) {
            favoriteFolderRepository.incrementItemCount(folderId);
        }
    }

    /**
     * 减少收藏夹计数
     */
    public void decrementItemCount(Long folderId) {
        if (folderId != null) {
            favoriteFolderRepository.decrementItemCount(folderId);
        }
    }
    
    /**
     * 迁移收藏夹内容到另一个收藏夹
     */
    @Transactional(rollbackFor = Exception.class)
    public int moveFolderContents(Long userId, Long sourceFolderId, Long targetFolderId) {
        // 验证源收藏夹
        FavoriteFolder sourceFolder = favoriteFolderRepository.findById(sourceFolderId);
        if (sourceFolder == null) {
            throw new BusinessException("源收藏夹不存在");
        }
        if (!sourceFolder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此收藏夹");
        }
        
        // 验证目标收藏夹
        FavoriteFolder targetFolder = favoriteFolderRepository.findById(targetFolderId);
        if (targetFolder == null) {
            throw new BusinessException("目标收藏夹不存在");
        }
        if (!targetFolder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作目标收藏夹");
        }
        
        // 不能迁移到自己
        if (sourceFolderId.equals(targetFolderId)) {
            throw new BusinessException("不能迁移到同一个收藏夹");
        }
        
        log.info("[收藏夹] 开始迁移内容 - userId: {}, source: {}, target: {}, itemCount: {}", 
                userId, sourceFolderId, targetFolderId, sourceFolder.getItemCount());
        
        if (sourceFolder.getItemCount() == 0) {
            log.info("[收藏夹] 源收藏夹为空,无需迁移");
            return 0;
        }
        
        try {
            // 执行迁移
            int movedCount = favoriteRepository.moveFavoritesToFolder(userId, sourceFolderId, targetFolderId);
            log.info("[收藏夹] 迁移完成 - 已迁移 {} 条记录", movedCount);
            
            // 更新源收藏夹计数(清零)
            favoriteFolderRepository.updateItemCount(sourceFolderId, 0);
            
            // 更新目标收藏夹计数
            int targetCount = targetFolder.getItemCount() + movedCount;
            favoriteFolderRepository.updateItemCount(targetFolderId, targetCount);
            
            return movedCount;
        } catch (Exception e) {
            log.error("[收藏夹] 迁移失败 - userId: {}, source: {}, target: {}", 
                    userId, sourceFolderId, targetFolderId, e);
            throw new BusinessException("迁移失败,请稍后重试");
        }
    }
    
    /**
     * 合并收藏夹(迁移内容后删除源收藏夹)
     */
    @Transactional(rollbackFor = Exception.class)
    public int mergeFolders(Long userId, Long sourceFolderId, Long targetFolderId) {
        // 验证源收藏夹
        FavoriteFolder sourceFolder = favoriteFolderRepository.findById(sourceFolderId);
        if (sourceFolder == null) {
            throw new BusinessException("源收藏夹不存在");
        }
        if (!sourceFolder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此收藏夹");
        }
        if (sourceFolder.isDefaultFolder()) {
            throw new BusinessException("默认收藏夹不能被合并");
        }
        
        // 验证目标收藏夹
        FavoriteFolder targetFolder = favoriteFolderRepository.findById(targetFolderId);
        if (targetFolder == null) {
            throw new BusinessException("目标收藏夹不存在");
        }
        if (!targetFolder.getUserId().equals(userId)) {
            throw new BusinessException("无权操作目标收藏夹");
        }
        
        // 不能合并到自己
        if (sourceFolderId.equals(targetFolderId)) {
            throw new BusinessException("不能合并到同一个收藏夹");
        }
        
        log.info("[收藏夹] 开始合并 - userId: {}, source: {} ({}项), target: {}", 
                userId, sourceFolderId, sourceFolder.getItemCount(), targetFolderId);
        
        try {
            // 先迁移内容
            int movedCount = 0;
            if (sourceFolder.getItemCount() > 0) {
                movedCount = favoriteRepository.moveFavoritesToFolder(userId, sourceFolderId, targetFolderId);
                log.info("[收藏夹] 内容迁移完成 - 已迁移 {} 条记录", movedCount);
                
                // 更新目标收藏夹计数
                int targetCount = targetFolder.getItemCount() + movedCount;
                favoriteFolderRepository.updateItemCount(targetFolderId, targetCount);
            }
            
            // 删除源收藏夹
            favoriteFolderRepository.deleteById(sourceFolderId);
            log.info("[收藏夹] 合并完成 - 源收藏夹已删除");
            
            return movedCount;
        } catch (Exception e) {
            log.error("[收藏夹] 合并失败 - userId: {}, source: {}, target: {}", 
                    userId, sourceFolderId, targetFolderId, e);
            throw new BusinessException("合并失败,请稍后重试");
        }
    }
}
