package cn.xu.domain.article.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签领域实体
 * 封装标签相关的业务逻辑和规则
 * 
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {
    private Long id;
    private String name;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新标签
     */
    public static TagEntity create(String name) {
        validateTagName(name);
        
        return TagEntity.builder()
                .name(name.trim())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 更新标签名称
     */
    public void updateName(String newName) {
        validateTagName(newName);
        
        if (!this.name.equals(newName.trim())) {
            this.name = newName.trim();
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 判断标签名称是否相同
     */
    public boolean hasSameName(String otherName) {
        if (otherName == null || this.name == null) {
            return false;
        }
        return this.name.equalsIgnoreCase(otherName.trim());
    }
    
    /**
     * 判断标签是否刚创建
     */
    public boolean isNewlyCreated() {
        if (createTime == null) {
            return false;
        }
        return java.time.Duration.between(createTime, LocalDateTime.now()).toHours() < 24;
    }
    
    /**
     * 获取标签的显示名称（带前缀）
     */
    public String getDisplayName() {
        return "#" + this.name;
    }
    
    /**
     * 验证标签名称
     */
    public static void validateTagName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("标签名称不能为空");
        }
        
        String trimmedName = name.trim();
        
        if (trimmedName.length() > 20) {
            throw new BusinessException("标签名称不能超过20个字符");
        }
        
        if (trimmedName.length() < 2) {
            throw new BusinessException("标签名称不能少于2个字符");
        }
        
        // 禁止特殊字符
        if (trimmedName.matches(".*[<>\"'&]")) {
            throw new BusinessException("标签名称不能包含特殊字符");
        }
    }
    
    /**
     * 获取标签的简化信息（用于列表显示）
     */
    public String getSimpleInfo() {
        return String.format("Tag[%d:%s]", id, name);
    }
    
    /**
     * 判断两个标签是否相同（按名称匹配）
     */
    public boolean isSameTag(TagEntity other) {
        if (other == null) {
            return false;
        }
        return hasSameName(other.getName());
    }
}
