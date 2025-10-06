package cn.xu.domain.post.service;

import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.collect.model.valobj.CollectType;
import cn.xu.domain.collect.service.ICollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 帖子收藏服务适配器
 * 适配通用的收藏服务到帖子收藏服务
 */
@Service
public class PostCollectServiceAdapter implements IPostCollectService {

    private final ICollectService collectService;

    @Autowired
    public PostCollectServiceAdapter(ICollectService collectService) {
        this.collectService = collectService;
    }

    @Override
    public void collectPost(Long userId, Long postId) {
        collectService.collect(userId, postId, CollectType.POST.getCode());
    }

    @Override
    public void uncollectPost(Long userId, Long postId) {
        collectService.uncollect(userId, postId, CollectType.POST.getCode());
    }

    @Override
    public boolean isPostCollected(Long userId, Long postId) {
        return collectService.isCollected(userId, postId, CollectType.POST.getCode());
    }

    @Override
    public List<Long> getCollectedPostIds(Long userId) {
        return collectService.getCollectedTargetIds(userId, CollectType.POST.getCode());
    }

    @Override
    public int countCollectedPosts(Long userId) {
        return collectService.countCollectedItems(userId, CollectType.POST.getCode());
    }

    @Override
    public Long createFolder(Long userId, String name, String description) {
        return collectService.createFolder(userId, name, description);
    }

    @Override
    public void updateFolder(Long folderId, String name, String description) {
        collectService.updateFolder(folderId, name, description);
    }

    @Override
    public void deleteFolder(Long userId, Long folderId) {
        collectService.deleteFolder(userId, folderId);
    }

    @Override
    public List<CollectFolderEntity> getFoldersByUserId(Long userId) {
        return collectService.getFoldersByUserId(userId);
    }

    @Override
    public void addPostToFolder(Long userId, Long postId, Long folderId) {
        collectService.addTargetToFolder(userId, postId, CollectType.POST.getCode(), folderId);
    }

    @Override
    public void removePostFromFolder(Long userId, Long postId, Long folderId) {
        collectService.removeTargetFromFolder(userId, postId, CollectType.POST.getCode(), folderId);
    }

    @Override
    public List<CollectEntity> getPostsInFolder(Long userId, Long folderId) {
        return collectService.getTargetsInFolder(userId, folderId);
    }

    // 实现ICollectService接口的方法，直接委托给collectService
    @Override
    public void collect(Long userId, Long targetId, String targetType) {
        collectService.collect(userId, targetId, targetType);
    }

    @Override
    public void uncollect(Long userId, Long targetId, String targetType) {
        collectService.uncollect(userId, targetId, targetType);
    }

    @Override
    public boolean isCollected(Long userId, Long targetId, String targetType) {
        return collectService.isCollected(userId, targetId, targetType);
    }

    @Override
    public List<Long> getCollectedTargetIds(Long userId, String targetType) {
        return collectService.getCollectedTargetIds(userId, targetType);
    }

    @Override
    public int countCollectedItems(Long userId, String targetType) {
        return collectService.countCollectedItems(userId, targetType);
    }

    @Override
    public void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        collectService.addTargetToFolder(userId, targetId, targetType, folderId);
    }

    @Override
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        collectService.removeTargetFromFolder(userId, targetId, targetType, folderId);
    }

    @Override
    public List<CollectEntity> getTargetsInFolder(Long userId, Long folderId) {
        return collectService.getTargetsInFolder(userId, folderId);
    }
}