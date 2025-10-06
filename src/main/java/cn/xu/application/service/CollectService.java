package cn.xu.application.service;

import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.collect.model.valobj.CollectStatus;
import cn.xu.domain.collect.repository.ICollectRepository;
import cn.xu.domain.collect.service.ICollectService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏服务实现
 */
@Service
public class CollectService implements ICollectService {

    @Resource
    private ICollectRepository collectRepository;

    @Override
    public void collect(Long userId, Long targetId, String targetType) {
        CollectEntity collectEntity = collectRepository.findByUserIdAndTargetId(userId, targetId, targetType);
        if (collectEntity == null) {
            // 创建新的收藏记录
            collectEntity = CollectEntity.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .targetType(targetType)
                    .status(CollectStatus.COLLECTED.getCode())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
        } else {
            // 更新现有收藏记录
            collectEntity.setStatus(CollectStatus.COLLECTED.getCode());
            collectEntity.setUpdateTime(LocalDateTime.now());
        }
        collectRepository.save(collectEntity);
    }

    @Override
    public void uncollect(Long userId, Long targetId, String targetType) {
        CollectEntity collectEntity = collectRepository.findByUserIdAndTargetId(userId, targetId, targetType);
        if (collectEntity != null) {
            collectEntity.setStatus(CollectStatus.UNCOLLECTED.getCode());
            collectEntity.setUpdateTime(LocalDateTime.now());
            collectRepository.save(collectEntity);
        }
    }

    @Override
    public boolean isCollected(Long userId, Long targetId, String targetType) {
        CollectEntity collectEntity = collectRepository.findByUserIdAndTargetId(userId, targetId, targetType);
        return collectEntity != null && CollectStatus.COLLECTED.getCode().equals(collectEntity.getStatus());
    }

    @Override
    public List<Long> getCollectedTargetIds(Long userId, String targetType) {
        return collectRepository.findCollectedTargetIdsByUserId(userId, targetType);
    }

    @Override
    public int countCollectedItems(Long userId, String targetType) {
        return collectRepository.countCollectedItemsByUserId(userId, targetType);
    }

    @Override
    public Long createFolder(Long userId, String name, String description) {
        CollectFolderEntity folderEntity = CollectFolderEntity.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .isDefault(0)
                .contentCount(0)
                .isPublic(0)
                .sort(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        collectRepository.saveFolder(folderEntity);
        return folderEntity.getId();
    }

    @Override
    public void updateFolder(Long folderId, String name, String description) {
        CollectFolderEntity folderEntity = collectRepository.findFolderById(folderId);
        if (folderEntity != null) {
            folderEntity.setName(name);
            folderEntity.setDescription(description);
            folderEntity.setUpdateTime(LocalDateTime.now());
            collectRepository.saveFolder(folderEntity);
        }
    }

    @Override
    public void deleteFolder(Long userId, Long folderId) {
        // 先将该收藏夹中的所有收藏移出收藏夹
        List<CollectEntity> collectEntities = collectRepository.findTargetsInFolder(userId, folderId);
        for (CollectEntity collectEntity : collectEntities) {
            collectEntity.setFolderId(null);
            collectRepository.save(collectEntity);
        }
        // 再删除收藏夹
        collectRepository.deleteFolderById(folderId);
    }

    @Override
    public List<CollectFolderEntity> getFoldersByUserId(Long userId) {
        return collectRepository.findFoldersByUserId(userId);
    }

    @Override
    public void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        collectRepository.addTargetToFolder(userId, targetId, targetType, folderId);
    }

    @Override
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        collectRepository.removeTargetFromFolder(userId, targetId, targetType, folderId);
    }

    @Override
    public List<CollectEntity> getTargetsInFolder(Long userId, Long folderId) {
        return collectRepository.findTargetsInFolder(userId, folderId);
    }
}