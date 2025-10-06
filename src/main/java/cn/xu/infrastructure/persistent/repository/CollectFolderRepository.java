package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.collect.repository.ICollectFolderRepository;
import cn.xu.infrastructure.persistent.dao.CollectFolderMapper;
import cn.xu.infrastructure.persistent.po.CollectFolderPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 收藏夹仓储实现类
 */
@Repository
public class CollectFolderRepository implements ICollectFolderRepository {

    @Resource
    private CollectFolderMapper collectFolderMapper;

    @Override
    public Long save(CollectFolderEntity collectFolderEntity) {
        CollectFolderPO collectFolder = convertToPO(collectFolderEntity);
        collectFolderMapper.insert(collectFolder);
        return collectFolder.getId();
    }

    @Override
    public void update(CollectFolderEntity collectFolderEntity) {
        CollectFolderPO collectFolder = convertToPO(collectFolderEntity);
        collectFolderMapper.update(collectFolder);
    }

    @Override
    public void deleteById(Long id) {
        collectFolderMapper.deleteById(id);
    }

    @Override
    public Optional<CollectFolderEntity> findById(Long id) {
        CollectFolderPO collectFolder = collectFolderMapper.selectById(id);
        return Optional.ofNullable(convertToEntity(collectFolder));
    }

    @Override
    public List<CollectFolderEntity> findByUserId(Long userId) {
        List<CollectFolderPO> collectFolders = collectFolderMapper.selectByUserId(userId);
        return collectFolders.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CollectFolderEntity> findByUserIdAndName(Long userId, String name) {
        CollectFolderPO collectFolder = collectFolderMapper.selectByUserIdAndName(userId, name);
        return Optional.ofNullable(convertToEntity(collectFolder));
    }

    @Override
    public Optional<CollectFolderEntity> findDefaultFolderByUserId(Long userId) {
        CollectFolderPO collectFolder = collectFolderMapper.selectDefaultFolderByUserId(userId);
        return Optional.ofNullable(convertToEntity(collectFolder));
    }

    @Override
    public void updateContentCount(Long folderId, Integer count) {
        collectFolderMapper.updatePostCount(folderId, count);
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param collectFolderPO 持久化对象
     * @return 领域实体
     */
    private CollectFolderEntity convertToEntity(CollectFolderPO collectFolderPO) {
        if (collectFolderPO == null) {
            return null;
        }

        return CollectFolderEntity.builder()
                .id(collectFolderPO.getId())
                .userId(collectFolderPO.getUserId())
                .name(collectFolderPO.getName())
                .description(collectFolderPO.getDescription())
                .isDefault(collectFolderPO.getIsDefault())
                .contentCount(collectFolderPO.getContentCount())
                .isPublic(collectFolderPO.getIsPublic())
                .sort(collectFolderPO.getSort())
                .createTime(collectFolderPO.getCreateTime())
                .updateTime(collectFolderPO.getUpdateTime())
                .build();
    }

    /**
     * 将领域实体转换为持久化对象
     *
     * @param collectFolderEntity 领域实体
     * @return 持久化对象
     */
    private CollectFolderPO convertToPO(CollectFolderEntity collectFolderEntity) {
        if (collectFolderEntity == null) {
            return null;
        }

        CollectFolderPO collectFolder = new CollectFolderPO();
        collectFolder.setId(collectFolderEntity.getId());
        collectFolder.setUserId(collectFolderEntity.getUserId());
        collectFolder.setName(collectFolderEntity.getName());
        collectFolder.setDescription(collectFolderEntity.getDescription());
        collectFolder.setIsDefault(collectFolderEntity.getIsDefault());
        collectFolder.setContentCount(collectFolderEntity.getContentCount());
        collectFolder.setIsPublic(collectFolderEntity.getIsPublic());
        collectFolder.setSort(collectFolderEntity.getSort());
        collectFolder.setCreateTime(collectFolderEntity.getCreateTime());
        collectFolder.setUpdateTime(collectFolderEntity.getUpdateTime());
        return collectFolder;
    }
}