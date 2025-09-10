package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.article.model.entity.CollectFolderArticleEntity;
import cn.xu.domain.article.repository.ICollectFolderArticleRepository;
import cn.xu.infrastructure.persistent.dao.CollectFolderArticleMapper;
import cn.xu.infrastructure.persistent.po.CollectFolderArticle;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 收藏夹文章关联仓储实现类
 */
@Repository
public class CollectFolderArticleRepository implements ICollectFolderArticleRepository {

    @Resource
    private CollectFolderArticleMapper collectFolderArticleMapper;

    @Override
    public Long save(CollectFolderArticleEntity collectFolderArticleEntity) {
        CollectFolderArticle collectFolderArticle = convertToPO(collectFolderArticleEntity);
        collectFolderArticleMapper.insert(collectFolderArticle);
        return collectFolderArticle.getId();
    }

    @Override
    public void deleteByFolderIdAndArticleId(Long folderId, Long articleId) {
        collectFolderArticleMapper.deleteByFolderIdAndArticleId(folderId, articleId);
    }

    @Override
    public void deleteByFolderId(Long folderId) {
        collectFolderArticleMapper.deleteByFolderId(folderId);
    }

    @Override
    public void deleteByArticleId(Long articleId) {
        collectFolderArticleMapper.deleteByArticleId(articleId);
    }

    @Override
    public List<CollectFolderArticleEntity> findByFolderId(Long folderId) {
        List<CollectFolderArticle> collectFolderArticles = collectFolderArticleMapper.selectByFolderId(folderId);
        return collectFolderArticles.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectFolderArticleEntity> findByUserIdAndArticleId(Long userId, Long articleId) {
        List<CollectFolderArticle> collectFolderArticles = collectFolderArticleMapper.selectByUserIdAndArticleId(userId, articleId);
        return collectFolderArticles.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CollectFolderArticleEntity> findByFolderIdAndArticleId(Long folderId, Long articleId) {
        CollectFolderArticle collectFolderArticle = collectFolderArticleMapper.selectByFolderIdAndArticleId(folderId, articleId);
        return Optional.ofNullable(convertToEntity(collectFolderArticle));
    }

    @Override
    public boolean existsByFolderIdAndArticleId(Long folderId, Long articleId) {
        return collectFolderArticleMapper.existsByFolderIdAndArticleId(folderId, articleId);
    }

    @Override
    public int countByFolderId(Long folderId) {
        return collectFolderArticleMapper.countByFolderId(folderId);
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param collectFolderArticle 持久化对象
     * @return 领域实体
     */
    private CollectFolderArticleEntity convertToEntity(CollectFolderArticle collectFolderArticle) {
        if (collectFolderArticle == null) {
            return null;
        }

        return CollectFolderArticleEntity.builder()
                .id(collectFolderArticle.getId())
                .folderId(collectFolderArticle.getFolderId())
                .articleId(collectFolderArticle.getArticleId())
                .userId(collectFolderArticle.getUserId())
                .createTime(collectFolderArticle.getCreateTime())
                .build();
    }

    /**
     * 将领域实体转换为持久化对象
     *
     * @param collectFolderArticleEntity 领域实体
     * @return 持久化对象
     */
    private CollectFolderArticle convertToPO(CollectFolderArticleEntity collectFolderArticleEntity) {
        if (collectFolderArticleEntity == null) {
            return null;
        }

        CollectFolderArticle collectFolderArticle = new CollectFolderArticle();
        collectFolderArticle.setId(collectFolderArticleEntity.getId());
        collectFolderArticle.setFolderId(collectFolderArticleEntity.getFolderId());
        collectFolderArticle.setArticleId(collectFolderArticleEntity.getArticleId());
        collectFolderArticle.setUserId(collectFolderArticleEntity.getUserId());
        collectFolderArticle.setCreateTime(collectFolderArticleEntity.getCreateTime());
        return collectFolderArticle;
    }
}