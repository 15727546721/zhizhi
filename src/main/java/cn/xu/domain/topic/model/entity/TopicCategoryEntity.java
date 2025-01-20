package cn.xu.domain.topic.model.entity;

import cn.xu.application.common.ResponseCode;
import cn.xu.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicCategoryEntity {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序序号，值越小越靠前
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 验证分类数据
     */
    public void validate() {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "分类名称不能为空");
        }
        if (name.length() > 50) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "分类名称不能超过50个字符");
        }
        if (StringUtils.hasText(description) && description.length() > 255) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "分类描述不能超过255个字符");
        }
        if (sort == null) {
            sort = 0;
        }
    }

    /**
     * 更新分类内容
     */
    public void update(TopicCategoryEntity newCategory) {
        if (StringUtils.hasText(newCategory.getName())) {
            this.name = newCategory.getName();
        }
        if (StringUtils.hasText(newCategory.getDescription())) {
            this.description = newCategory.getDescription();
        }
        if (newCategory.getSort() != null) {
            this.sort = newCategory.getSort();
        }
    }
} 