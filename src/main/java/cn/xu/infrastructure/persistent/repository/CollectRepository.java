package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.collect.repository.ICollectRepository;
import cn.xu.infrastructure.persistent.dao.ICollectDao;
import cn.xu.infrastructure.persistent.dao.ICollectFolderDao;
import cn.xu.infrastructure.persistent.po.CollectFolderPO;
import cn.xu.infrastructure.persistent.po.CollectPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏仓储实现
 */
@Repository
public class CollectRepository implements ICollectRepository {

    @Resource
    private ICollectDao collectDao;

    @Resource
    private ICollectFolderDao collectFolderDao;

    @Override
    public void save(CollectEntity collectEntity) {
        CollectPO collectPO = new CollectPO();
        collectPO.setId(collectEntity.getId());
        collectPO.setUserId(collectEntity.getUserId());
        collectPO.setTargetId(collectEntity.getTargetId());
        collectPO.setFolderId(collectEntity.getFolderId());
        collectPO.setTargetType(collectEntity.getTargetType());
        collectPO.setStatus(collectEntity.getStatus());
        collectPO.setCreateTime(collectEntity.getCreateTime());
        collectPO.setUpdateTime(collectEntity.getUpdateTime());
        collectDao.insertOrUpdate(collectPO);
    }

    @Override
    public CollectEntity findByUserIdAndTargetId(Long userId, Long targetId, String targetType) {
        CollectPO collectPO = collectDao.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (collectPO == null) {
            return null;
        }
        return CollectEntity.builder()
                .id(collectPO.getId())
                .userId(collectPO.getUserId())
                .targetId(collectPO.getTargetId())
                .folderId(collectPO.getFolderId())
                .targetType(collectPO.getTargetType())
                .status(collectPO.getStatus())
                .createTime(collectPO.getCreateTime())
                .updateTime(collectPO.getUpdateTime())
                .build();
    }

    @Override
    public void deleteByUserIdAndTargetId(Long userId, Long targetId, String targetType) {
        collectDao.deleteByUserIdAndTargetId(userId, targetId, targetType);
    }

    @Override
    public List<Long> findCollectedTargetIdsByUserId(Long userId, String targetType) {
        return collectDao.selectCollectedTargetIdsByUserId(userId, targetType);
    }

    @Override
    public int countCollectedItemsByUserId(Long userId, String targetType) {
        return collectDao.countCollectedItemsByUserId(userId, targetType);
    }

    @Override
    public void saveFolder(CollectFolderEntity folderEntity) {
        CollectFolderPO folderPO = new CollectFolderPO();
        folderPO.setId(folderEntity.getId());
        folderPO.setUserId(folderEntity.getUserId());
        folderPO.setName(folderEntity.getName());
        folderPO.setDescription(folderEntity.getDescription());
        folderPO.setIsDefault(folderEntity.getIsDefault());
        folderPO.setContentCount(folderEntity.getContentCount());
        folderPO.setIsPublic(folderEntity.getIsPublic());
        folderPO.setSort(folderEntity.getSort());
        folderPO.setCreateTime(folderEntity.getCreateTime());
        folderPO.setUpdateTime(folderEntity.getUpdateTime());
        collectFolderDao.insert(folderPO);
    }

    @Override
    public CollectFolderEntity findFolderById(Long folderId) {
        CollectFolderPO folderPO = collectFolderDao.selectById(folderId);
        if (folderPO == null) {
            return null;
        }
        return CollectFolderEntity.builder()
                .id(folderPO.getId())
                .userId(folderPO.getUserId())
                .name(folderPO.getName())
                .description(folderPO.getDescription())
                .isDefault(folderPO.getIsDefault())
                .contentCount(folderPO.getContentCount())
                .isPublic(folderPO.getIsPublic())
                .sort(folderPO.getSort())
                .createTime(folderPO.getCreateTime())
                .updateTime(folderPO.getUpdateTime())
                .build();
    }

    @Override
    public List<CollectFolderEntity> findFoldersByUserId(Long userId) {
        List<CollectFolderPO> folderPOs = collectFolderDao.selectByUserId(userId);
        return folderPOs.stream().map(folderPO -> CollectFolderEntity.builder()
                .id(folderPO.getId())
                .userId(folderPO.getUserId())
                .name(folderPO.getName())
                .description(folderPO.getDescription())
                .isDefault(folderPO.getIsDefault())
                .contentCount(folderPO.getContentCount())
                .isPublic(folderPO.getIsPublic())
                .sort(folderPO.getSort())
                .createTime(folderPO.getCreateTime())
                .updateTime(folderPO.getUpdateTime())
                .build()).collect(Collectors.toList());
    }

    @Override
    public void deleteFolderById(Long folderId) {
        collectFolderDao.deleteById(folderId);
    }

    @Override
    public void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        CollectPO collectPO = collectDao.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (collectPO != null) {
            collectPO.setFolderId(folderId);
            collectDao.updateFolderId(collectPO.getId(), folderId);
        }
    }

    @Override
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        CollectPO collectPO = collectDao.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (collectPO != null && folderId.equals(collectPO.getFolderId())) {
            collectPO.setFolderId(null);
            collectDao.updateFolderId(collectPO.getId(), null);
        }
    }

    @Override
    public List<CollectEntity> findTargetsInFolder(Long userId, Long folderId) {
        List<CollectPO> collectPOs = collectDao.selectByFolderId(userId, folderId);
        return collectPOs.stream().map(po -> CollectEntity.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .targetId(po.getTargetId())
                .folderId(po.getFolderId())
                .targetType(po.getTargetType())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build()).collect(Collectors.toList());
    }
}