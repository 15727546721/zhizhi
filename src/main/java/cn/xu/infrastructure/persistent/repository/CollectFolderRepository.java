package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.model.entity.CollectFolderEntity;
import cn.xu.domain.article.repository.ICollectFolderRepository;
import cn.xu.infrastructure.persistent.dao.CollectFolderMapper;
import cn.xu.infrastructure.persistent.po.CollectFolder;
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
        CollectFolder collectFolder = convertToPO(collectFolderEntity);
        collectFolderMapper.insert(collectFolder);
        return collectFolder.getId();
    }

    @Override
    public void update(CollectFolderEntity collectFolderEntity) {
        CollectFolder collectFolder = convertToPO(collectFolderEntity);
        collectFolderMapper.update(collectFolder);
    }

    @Override
    public void deleteById(Long id) {
        collectFolderMapper.deleteById(id);
    }

    @Override
    public Optional<CollectFolderEntity> findById(Long id) {
        CollectFolder collectFolder = collectFolderMapper.selectById(id);
        return Optional.ofNullable(convertToEntity(collectFolder));
    }

    @Override
    public List<CollectFolderEntity> findByUserId(Long userId) {
        List<CollectFolder> collectFolders = collectFolderMapper.selectByUserId(userId);
        return collectFolders.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CollectFolderEntity> findByUserIdAndName(Long userId, String name) {
        CollectFolder collectFolder = collectFolderMapper.selectByUserIdAndName(userId, name);
        return Optional.ofNullable(convertToEntity(collectFolder));
    }

    @Override
    public Optional<CollectFolderEntity> findDefaultFolderByUserId(Long userId) {
        CollectFolder collectFolder = collectFolderMapper.selectDefaultFolderByUserId(userId);
        return Optional.ofNullable(convertToEntity(collectFolder));
    }

    @Override
    public void updateArticleCount(Long folderId, Integer count) {
        collectFolderMapper.updateArticleCount(folderId, count);
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param collectFolder 持久化对象
     * @return 领域实体
     */
    private CollectFolderEntity convertToEntity(CollectFolder collectFolder) {
        if (collectFolder == null) {
            return null;
        }

        return CollectFolderEntity.builder()
                .id(collectFolder.getId())
                .userId(collectFolder.getUserId())
                .name(collectFolder.getName())
                .description(collectFolder.getDescription())
                .isDefault(collectFolder.getIsDefault() != null && collectFolder.getIsDefault() == 1)
                .articleCount(collectFolder.getArticleCount())
                .isPublic(collectFolder.getIsPublic() != null && collectFolder.getIsPublic() == 1)
                .sort(collectFolder.getSort())
                .createTime(collectFolder.getCreateTime())
                .updateTime(collectFolder.getUpdateTime())
                .build();
    }

    /**
     * 将领域实体转换为持久化对象
     *
     * @param collectFolderEntity 领域实体
     * @return 持久化对象
     */
    private CollectFolder convertToPO(CollectFolderEntity collectFolderEntity) {
        if (collectFolderEntity == null) {
            return null;
        }

        CollectFolder collectFolder = new CollectFolder();
        collectFolder.setId(collectFolderEntity.getId());
        collectFolder.setUserId(collectFolderEntity.getUserId());
        collectFolder.setName(collectFolderEntity.getName());
        collectFolder.setDescription(collectFolderEntity.getDescription());
        collectFolder.setIsDefault(collectFolderEntity.getIsDefault() != null && collectFolderEntity.getIsDefault() ? 1 : 0);
        collectFolder.setArticleCount(collectFolderEntity.getArticleCount());
        collectFolder.setIsPublic(collectFolderEntity.getIsPublic() != null && collectFolderEntity.getIsPublic() ? 1 : 0);
        collectFolder.setSort(collectFolderEntity.getSort());
        collectFolder.setCreateTime(collectFolderEntity.getCreateTime());
        collectFolder.setUpdateTime(collectFolderEntity.getUpdateTime());
        return collectFolder;
    }
}