package cn.xu.domain.topic.model.entity;

import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicEntity {
    
    /**
     * 话题ID
     */
    private Long id;
    
    /**
     * 发布话题的用户ID
     */
    private Long userId;
    
    /**
     * 话题内容
     */
    private String content;
    
    /**
     * 话题图片URL列表
     */
    private List<String> images;
    
    /**
     * 话题分类ID（可选）
     */
    private Long categoryId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 验证话题数据
     */
    public void validate() {
        if (userId == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "用户ID不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "话题内容不能为空");
        }
    }
    
    /**
     * 更新话题内容
     */
    public void update(TopicEntity newTopic) {
        if (StringUtils.hasText(newTopic.getContent())) {
            this.content = newTopic.getContent();
        }
        if (!CollectionUtils.isEmpty(newTopic.getImages())) {
            this.images = newTopic.getImages();
        }
        if (newTopic.getCategoryId() != null) {
            this.categoryId = newTopic.getCategoryId();
        }
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public List<String> getImages() {
        return images != null ? images : new ArrayList<>();
    }
} 