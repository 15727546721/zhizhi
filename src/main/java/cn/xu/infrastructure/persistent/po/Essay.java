package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 话题持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Essay {
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
     * 话题图片URL数组，使用符号','分隔
     */
    private String images;

    /**
     * 话题分类ID
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
     * 获取图片URL列表
     */
    public List<String> getImageList() {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(images.split(","));
    }

    /**
     * 设置图片URL列表
     */
    public void setImageList(List<String> imageList) {
        if (imageList == null || imageList.isEmpty()) {
            this.images = null;
            return;
        }
        this.images = String.join(",", imageList);
    }
} 