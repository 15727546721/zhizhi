package cn.xu.domain.essay.model.aggregate;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.valobj.EssayContent;
import cn.xu.domain.essay.model.valobj.EssayImages;
import cn.xu.domain.essay.model.valobj.EssayTopics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 随笔聚合根
 * 管理随笔的完整生命周期和业务一致性
 * 确保随笔相关操作的原子性和业务规则的执行
 */
@Slf4j
@Getter
public class EssayAggregate {
    
    /**
     * 随笔实体（聚合根实体）
     */
    private final EssayEntity essayEntity;
    
    /**
     * 聚合是否为新创建（用于标识是否需要插入）
     */
    private final boolean isNew;
    
    /**
     * 私有构造函数，确保通过工厂方法创建
     */
    private EssayAggregate(EssayEntity essayEntity, boolean isNew) {
        this.essayEntity = essayEntity;
        this.isNew = isNew;
    }
    
    /**
     * 创建新的随笔聚合根
     * 
     * @param userId 用户ID
     * @param content 内容
     * @param images 图片列表
     * @param topics 话题列表
     * @return 随笔聚合根
     */
    public static EssayAggregate create(Long userId, String content, List<String> images, List<String> topics) {
        EssayEntity entity = EssayEntity.create(userId, content, images, topics);
        return new EssayAggregate(entity, true);
    }
    
    /**
     * 从持久化数据恢复聚合根
     * 
     * @param id 随笔ID
     * @param userId 用户ID
     * @param content 内容
     * @param images 图片字符串
     * @param topics 话题字符串
     * @param likeCount 点赞数
     * @param commentCount 评论数
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 随笔聚合根
     */
    public static EssayAggregate restore(Long id, Long userId, String content, String images, 
                                        String topics, Long likeCount, Long commentCount,
                                        LocalDateTime createTime, LocalDateTime updateTime) {
        EssayEntity entity = EssayEntity.restore(id, userId, content, images, topics, 
                                                likeCount, commentCount, createTime, updateTime);
        return new EssayAggregate(entity, false);
    }
    
    /**
     * 更新随笔内容
     * 
     * @param newContent 新内容
     * @param newImages 新图片列表
     * @param newTopics 新话题列表
     * @param operatorUserId 操作用户ID
     */
    public void updateContent(String newContent, List<String> newImages, List<String> newTopics, Long operatorUserId) {
        // 检查权限：只有作者本人可以修改
        if (!essayEntity.belongsToUser(operatorUserId)) {
            throw new BusinessException("无权限修改他人的随笔");
        }
        
        essayEntity.update(newContent, newImages, newTopics);
        log.info("随笔内容已更新，随笔ID: {}, 操作用户: {}", essayEntity.getId(), operatorUserId);
    }
    
    /**
     * 点赞随笔
     * 
     * @param userId 点赞用户ID
     */
    public void like(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        // 不能给自己的随笔点赞
        if (essayEntity.belongsToUser(userId)) {
            throw new BusinessException("不能给自己的随笔点赞");
        }
        
        essayEntity.increaseLikeCount(1);
        log.info("随笔点赞成功，随笔ID: {}, 点赞用户: {}", essayEntity.getId(), userId);
    }
    
    /**
     * 取消点赞
     * 
     * @param userId 取消点赞的用户ID
     */
    public void unlike(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        essayEntity.decreaseLikeCount(1);
        log.info("取消点赞成功，随笔ID: {}, 用户: {}", essayEntity.getId(), userId);
    }
    
    /**
     * 添加评论
     * 
     * @param commentUserId 评论用户ID
     */
    public void addComment(Long commentUserId) {
        if (commentUserId == null || commentUserId <= 0) {
            throw new BusinessException("评论用户ID无效");
        }
        
        essayEntity.increaseCommentCount(1);
        log.info("随笔评论数增加，随笔ID: {}, 评论用户: {}", essayEntity.getId(), commentUserId);
    }
    
    /**
     * 删除评论
     * 
     * @param commentUserId 删除评论的用户ID
     */
    public void removeComment(Long commentUserId) {
        if (commentUserId == null || commentUserId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        essayEntity.decreaseCommentCount(1);
        log.info("随笔评论数减少，随笔ID: {}, 用户: {}", essayEntity.getId(), commentUserId);
    }
    
    /**
     * 删除随笔
     * 
     * @param operatorUserId 操作用户ID
     */
    public void delete(Long operatorUserId) {
        // 检查权限：只有作者本人可以删除
        if (!essayEntity.belongsToUser(operatorUserId)) {
            throw new BusinessException("无权限删除他人的随笔");
        }
        
        log.info("随笔已标记为删除，随笔ID: {}, 操作用户: {}", essayEntity.getId(), operatorUserId);
    }
    
    /**
     * 验证聚合的完整性
     * 
     * @throws BusinessException 当验证失败时
     */
    public void validate() {
        if (essayEntity == null) {
            throw new BusinessException("随笔实体不能为空");
        }
        essayEntity.validate();
    }
    
    /**
     * 获取随笔ID
     * 
     * @return 随笔ID
     */
    public Long getId() {
        return essayEntity.getId();
    }
    
    /**
     * 获取用户ID
     * 
     * @return 用户ID
     */
    public Long getUserId() {
        return essayEntity.getUserId();
    }
    
    /**
     * 获取内容
     * 
     * @return 内容值对象
     */
    public EssayContent getContent() {
        return essayEntity.getContent();
    }
    
    /**
     * 获取图片
     * 
     * @return 图片值对象
     */
    public EssayImages getImages() {
        return essayEntity.getImages();
    }
    
    /**
     * 获取话题
     * 
     * @return 话题值对象
     */
    public EssayTopics getTopics() {
        return essayEntity.getTopics();
    }
    
    /**
     * 获取点赞数
     * 
     * @return 点赞数
     */
    public Long getLikeCount() {
        return essayEntity.getLikeCount();
    }
    
    /**
     * 获取评论数
     * 
     * @return 评论数
     */
    public Long getCommentCount() {
        return essayEntity.getCommentCount();
    }
    
    /**
     * 获取创建时间
     * 
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return essayEntity.getCreateTime();
    }
    
    /**
     * 获取更新时间
     * 
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return essayEntity.getUpdateTime();
    }
    
    /**
     * 获取热度分数
     * 
     * @return 热度分数
     */
    public long getHotScore() {
        return essayEntity.getHotScore();
    }
    
    /**
     * 判断是否属于指定用户
     * 
     * @param userId 用户ID
     * @return 如果属于该用户返回true
     */
    public boolean belongsToUser(Long userId) {
        return essayEntity.belongsToUser(userId);
    }
    
    /**
     * 判断是否包含图片
     * 
     * @return 如果包含图片返回true
     */
    public boolean hasImages() {
        return essayEntity.hasImages();
    }
    
    /**
     * 判断是否包含话题
     * 
     * @return 如果包含话题返回true
     */
    public boolean hasTopics() {
        return essayEntity.hasTopics();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EssayAggregate that = (EssayAggregate) o;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    
    @Override
    public String toString() {
        return "EssayAggregate{" +
                "id=" + getId() +
                ", userId=" + getUserId() +
                ", isNew=" + isNew +
                "}";
    }
}