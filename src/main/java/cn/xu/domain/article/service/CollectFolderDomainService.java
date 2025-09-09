package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.CollectFolderArticleEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;
import cn.xu.domain.article.repository.ICollectFolderArticleRepository;
import cn.xu.domain.article.repository.ICollectFolderRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 收藏夹领域服务
 * 处理收藏夹相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectFolderDomainService implements ICollectFolderDomainService {

    private final ICollectFolderRepository collectFolderRepository;
    private final ICollectFolderArticleRepository collectFolderArticleRepository;

    /**
     * 创建收藏夹
     *
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     * @return 收藏夹ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(Long userId, String name, String description, Boolean isPublic) {
        log.info("开始创建收藏夹 - userId: {}, name: {}", userId, name);

        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }
            if (name == null || name.trim().isEmpty()) {
                throw new BusinessException("收藏夹名称不能为空");
            }

            // 检查是否已存在同名收藏夹
            Optional<CollectFolderEntity> existingFolder = collectFolderRepository.findByUserIdAndName(userId, name);
            if (existingFolder.isPresent()) {
                throw new BusinessException("已存在同名收藏夹");
            }

            // 创建收藏夹实体
            CollectFolderEntity folderEntity = CollectFolderEntity.createFolder(userId, name, description, isPublic);

            // 保存收藏夹
            Long folderId = collectFolderRepository.save(folderEntity);

            log.info("创建收藏夹成功 - folderId: {}", folderId);
            return folderId;
        } catch (Exception e) {
            log.error("创建收藏夹失败 - userId: {}, name: {}", userId, name, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("创建收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 更新收藏夹信息
     *
     * @param folderId    收藏夹ID
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFolder(Long folderId, Long userId, String name, String description, Boolean isPublic) {
        log.info("开始更新收藏夹 - folderId: {}, userId: {}", folderId, userId);

        try {
            // 参数校验
            if (folderId == null || folderId <= 0) {
                throw new BusinessException("收藏夹ID不能为空");
            }
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 检查是否已存在同名收藏夹（排除当前收藏夹）
            if (name != null && !name.trim().isEmpty() && !name.equals(folderEntity.getName())) {
                Optional<CollectFolderEntity> existingFolder = collectFolderRepository.findByUserIdAndName(userId, name);
                if (existingFolder.isPresent() && !existingFolder.get().getId().equals(folderId)) {
                    throw new BusinessException("已存在同名收藏夹");
                }
            }

            // 更新收藏夹信息
            folderEntity.updateFolderInfo(name, description, isPublic);

            // 保存更新
            collectFolderRepository.update(folderEntity);

            log.info("更新收藏夹成功 - folderId: {}", folderId);
        } catch (Exception e) {
            log.error("更新收藏夹失败 - folderId: {}, userId: {}", folderId, userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("更新收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 删除收藏夹
     *
     * @param folderId 收藏夹ID
     * @param userId   用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId, Long userId) {
        log.info("开始删除收藏夹 - folderId: {}, userId: {}", folderId, userId);

        try {
            // 参数校验
            if (folderId == null || folderId <= 0) {
                throw new BusinessException("收藏夹ID不能为空");
            }
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 验证是否可以删除
            if (!folderEntity.canDelete()) {
                throw new BusinessException("默认收藏夹或非空收藏夹不能删除");
            }

            // 删除收藏夹关联的文章记录
            collectFolderArticleRepository.deleteByFolderId(folderId);

            // 删除收藏夹
            collectFolderRepository.deleteById(folderId);

            log.info("删除收藏夹成功 - folderId: {}", folderId);
        } catch (Exception e) {
            log.error("删除收藏夹失败 - folderId: {}, userId: {}", folderId, userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("删除收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    @Override
    public List<CollectFolderEntity> getUserFolders(Long userId) {
        log.info("开始获取用户收藏夹列表 - userId: {}", userId);

        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }

            List<CollectFolderEntity> folders = collectFolderRepository.findByUserId(userId);

            log.info("获取用户收藏夹列表成功 - userId: {}, folderCount: {}", userId, folders.size());
            return folders;
        } catch (Exception e) {
            log.error("获取用户收藏夹列表失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏夹列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    @Override
    public CollectFolderEntity getUserDefaultFolder(Long userId) {
        log.info("开始获取用户默认收藏夹 - userId: {}", userId);

        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }

            // 查找默认收藏夹
            Optional<CollectFolderEntity> defaultFolderOpt = collectFolderRepository.findDefaultFolderByUserId(userId);

            // 如果不存在默认收藏夹，则创建一个
            if (!defaultFolderOpt.isPresent()) {
                log.info("用户默认收藏夹不存在，创建默认收藏夹 - userId: {}", userId);

                // 创建默认收藏夹
                CollectFolderEntity defaultFolder = CollectFolderEntity.createFolder(
                        userId, "默认收藏夹", "系统默认创建的收藏夹", false);
                defaultFolder.setAsDefault();

                // 保存默认收藏夹
                Long folderId = collectFolderRepository.save(defaultFolder);

                // 重新查询创建的默认收藏夹
                return collectFolderRepository.findById(folderId)
                        .orElseThrow(() -> new BusinessException("创建默认收藏夹失败"));
            }

            return defaultFolderOpt.get();
        } catch (Exception e) {
            log.error("获取用户默认收藏夹失败 - userId: {}", userId, e);
            throw new BusinessException("获取默认收藏夹失败: " + e.getMessage());
        }
    }

    /**
     * 收藏文章到收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticleToFolder(Long folderId, Long articleId, Long userId) {
        log.info("开始收藏文章到收藏夹 - folderId: {}, articleId: {}, userId: {}", folderId, articleId, userId);

        try {
            // 参数校验
            CollectFolderEntity.validateFolderId(folderId);
            CollectFolderEntity.validateArticleId(articleId);
            CollectFolderEntity.validateUserId(userId);

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 检查是否已收藏
            if (collectFolderArticleRepository.existsByFolderIdAndArticleId(folderId, articleId)) {
                throw new BusinessException("文章已收藏到该收藏夹");
            }

            // 检查收藏夹文章数量是否达到上限
            if (folderEntity.getArticleCount() != null && folderEntity.getArticleCount() >= 1000) { // 假设单个收藏夹最大文章数为1000
                throw new BusinessException("收藏夹已达到最大文章数量限制");
            }

            // 创建收藏夹文章关联记录
            CollectFolderArticleEntity relationEntity = CollectFolderArticleEntity.createRelation(
                    folderId, articleId, userId);

            // 保存关联记录
            collectFolderArticleRepository.save(relationEntity);

            // 更新收藏夹文章数量
            folderEntity.incrementArticleCount();
            collectFolderRepository.update(folderEntity);

            log.info("收藏文章到收藏夹成功 - folderId: {}, articleId: {}", folderId, articleId);
        } catch (Exception e) {
            log.error("收藏文章到收藏夹失败 - folderId: {}, articleId: {}, userId: {}", folderId, articleId, userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("收藏文章失败: " + e.getMessage());
            }
        }
    }

    /**
     * 从收藏夹取消收藏文章
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticleFromFolder(Long folderId, Long articleId, Long userId) {
        log.info("开始从收藏夹取消收藏文章 - folderId: {}, articleId: {}, userId: {}", folderId, articleId, userId);

        try {
            // 参数校验
            CollectFolderEntity.validateFolderId(folderId);
            CollectFolderEntity.validateArticleId(articleId);
            CollectFolderEntity.validateUserId(userId);

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 检查是否已收藏
            if (!collectFolderArticleRepository.existsByFolderIdAndArticleId(folderId, articleId)) {
                throw new BusinessException("文章未收藏到该收藏夹");
            }

            // 删除关联记录
            collectFolderArticleRepository.deleteByFolderIdAndArticleId(folderId, articleId);

            // 更新收藏夹文章数量
            folderEntity.decrementArticleCount();
            collectFolderRepository.update(folderEntity);

            log.info("从收藏夹取消收藏文章成功 - folderId: {}, articleId: {}", folderId, articleId);
        } catch (Exception e) {
            log.error("从收藏夹取消收藏文章失败 - folderId: {}, articleId: {}, userId: {}", folderId, articleId, userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("取消收藏文章失败: " + e.getMessage());
            }
        }
    }

    /**
     * 检查文章是否已收藏到指定收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 是否已收藏
     */
    @Override
    public boolean isArticleCollectedToFolder(Long folderId, Long articleId, Long userId) {
        try {
            // 参数校验
            CollectFolderEntity.validateFolderId(folderId);
            CollectFolderEntity.validateArticleId(articleId);
            CollectFolderEntity.validateUserId(userId);

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 检查是否已收藏
            return collectFolderArticleRepository.existsByFolderIdAndArticleId(folderId, articleId);
        } catch (Exception e) {
            log.error("检查文章收藏状态失败 - folderId: {}, articleId: {}, userId: {}", folderId, articleId, userId, e);
            throw new BusinessException("检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取收藏夹中的文章列表
     *
     * @param folderId 收藏夹ID
     * @param userId   用户ID
     * @return 文章ID列表
     */
    @Override
    public List<CollectFolderArticleEntity> getFolderArticles(Long folderId, Long userId) {
        log.info("开始获取收藏夹中的文章列表 - folderId: {}, userId: {}", folderId, userId);

        try {
            // 参数校验
            if (folderId == null || folderId <= 0) {
                throw new BusinessException("收藏夹ID不能为空");
            }
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 获取收藏夹中的文章列表
            List<CollectFolderArticleEntity> articles = collectFolderArticleRepository.findByFolderId(folderId);

            log.info("获取收藏夹中的文章列表成功 - folderId: {}, articleCount: {}", folderId, articles.size());
            return articles;
        } catch (Exception e) {
            log.error("获取收藏夹中的文章列表失败 - folderId: {}, userId: {}", folderId, userId, e);
            throw new BusinessException("获取收藏夹文章列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量收藏文章到收藏夹
     *
     * @param folderId   收藏夹ID
     * @param articleIds 文章ID列表
     * @param userId     用户ID
     * @return 成功收藏的文章数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int collectArticlesToFolder(Long folderId, List<Long> articleIds, Long userId) {
        log.info("开始批量收藏文章到收藏夹 - folderId: {}, articleCount: {}, userId: {}", folderId, articleIds.size(), userId);

        try {
            // 参数校验
            CollectFolderEntity.validateFolderId(folderId);
            if (articleIds == null || articleIds.isEmpty()) {
                return 0;
            }
            CollectFolderEntity.validateUserId(userId);

            // 查找收藏夹
            CollectFolderEntity folderEntity = collectFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));

            // 验证用户权限
            if (!folderEntity.getUserId().equals(userId)) {
                throw new BusinessException("无权限操作该收藏夹");
            }

            // 检查收藏夹文章数量是否达到上限
            int currentCount = folderEntity.getArticleCount() != null ? folderEntity.getArticleCount() : 0;
            if (currentCount >= 1000) { // 假设单个收藏夹最大文章数为1000
                throw new BusinessException("收藏夹已达到最大文章数量限制");
            }

            int successCount = 0;
            for (Long articleId : articleIds) {
                try {
                    CollectFolderEntity.validateArticleId(articleId);
                    // 检查是否已收藏
                    if (!collectFolderArticleRepository.existsByFolderIdAndArticleId(folderId, articleId)) {
                        // 检查添加后是否会超过限制
                        if (currentCount + successCount >= 1000) {
                            throw new BusinessException("收藏夹已达到最大文章数量限制");
                        }
                        
                        // 创建收藏夹文章关联记录
                        CollectFolderArticleEntity relationEntity = CollectFolderArticleEntity.createRelation(
                                folderId, articleId, userId);

                        // 保存关联记录
                        collectFolderArticleRepository.save(relationEntity);
                        successCount++;
                    }
                } catch (BusinessException e) {
                    log.warn("收藏文章到收藏夹失败 - folderId: {}, articleId: {}, userId: {}, reason: {}", folderId, articleId, userId, e.getMessage());
                    // 继续处理其他文章
                }
            }

            // 更新收藏夹文章数量
            if (successCount > 0) {
                int newCount = collectFolderArticleRepository.countByFolderId(folderId);
                folderEntity.setArticleCount(newCount);
                collectFolderRepository.update(folderEntity);
            }

            log.info("批量收藏文章到收藏夹成功 - folderId: {}, successCount: {}", folderId, successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量收藏文章到收藏夹失败 - folderId: {}, articleCount: {}, userId: {}", folderId, articleIds.size(), userId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("批量收藏文章失败: " + e.getMessage());
            }
        }
    }

    /**
     * 批量取消收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功取消收藏的文章数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uncollectArticles(Long userId, List<Long> articleIds) {
        log.info("开始批量取消收藏文章 - userId: {}, articleCount: {}", userId, articleIds.size());

        if (articleIds == null || articleIds.isEmpty()) {
            return 0;
        }

        try {
            // 参数校验
            CollectFolderEntity.validateUserId(userId);

            // 批量删除
            int successCount = 0;
            for (Long articleId : articleIds) {
                try {
                    CollectFolderEntity.validateArticleId(articleId);
                    // 查找所有包含该文章的收藏夹
                    List<CollectFolderArticleEntity> folderArticles = collectFolderArticleRepository.findByUserIdAndArticleId(userId, articleId);
                    
                    // 删除所有关联记录
                    for (CollectFolderArticleEntity folderArticle : folderArticles) {
                        collectFolderArticleRepository.deleteByFolderIdAndArticleId(folderArticle.getFolderId(), articleId);
                        successCount++;
                        
                        // 更新收藏夹文章数量
                        CollectFolderEntity folderEntity = collectFolderRepository.findById(folderArticle.getFolderId())
                                .orElse(null);
                        if (folderEntity != null) {
                            folderEntity.decrementArticleCount();
                            collectFolderRepository.update(folderEntity);
                        }
                    }
                } catch (BusinessException e) {
                    log.warn("取消收藏文章失败 - userId: {}, articleId: {}, reason: {}", userId, articleId, e.getMessage());
                    // 继续处理其他文章
                }
            }

            log.info("批量取消收藏文章成功 - userId: {}, successCount: {}", userId, successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量取消收藏文章失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            throw new BusinessException("批量取消收藏文章失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏的文章数量统计
     *
     * @param userId 用户ID
     * @return 各收藏夹的文章数量统计
     */
    @Override
    public List<CollectFolderEntity> getUserFolderStats(Long userId) {
        log.info("开始获取用户收藏夹统计信息 - userId: {}", userId);

        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                throw new BusinessException("用户ID不能为空");
            }

            // 获取用户的所有收藏夹
            List<CollectFolderEntity> folders = collectFolderRepository.findByUserId(userId);

            // 更新每个收藏夹的文章数量
            for (CollectFolderEntity folder : folders) {
                int count = collectFolderArticleRepository.countByFolderId(folder.getId());
                folder.setArticleCount(count);
            }

            log.info("获取用户收藏夹统计信息成功 - userId: {}, folderCount: {}", userId, folders.size());
            return folders;
        } catch (Exception e) {
            log.error("获取用户收藏夹统计信息失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏夹统计信息失败: " + e.getMessage());
        }
    }
}