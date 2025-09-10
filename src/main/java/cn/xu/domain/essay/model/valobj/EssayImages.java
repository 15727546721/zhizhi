package cn.xu.domain.essay.model.valobj;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 随笔图片值对象
 * 封装随笔图片列表的业务规则和验证逻辑
 */
@Slf4j
@Getter
public class EssayImages {
    
    /**
     * 最大图片数量限制
     */
    private static final int MAX_IMAGE_COUNT = 9;
    
    /**
     * 图片URL列表
     */
    private final List<String> imageUrls;
    
    /**
     * 私有构造函数，确保通过工厂方法创建
     */
    private EssayImages(List<String> imageUrls) {
        this.imageUrls = Collections.unmodifiableList(new ArrayList<>(imageUrls));
    }
    
    /**
     * 从字符串列表创建EssayImages
     * 
     * @param images 图片URL列表
     * @return EssayImages实例
     */
    public static EssayImages of(List<String> images) {
        if (images == null) {
            return new EssayImages(Collections.emptyList());
        }
        
        List<String> validImages = images.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());
        
        return new EssayImages(validImages);
    }
    
    /**
     * 从逗号分隔的字符串创建EssayImages
     * 
     * @param imagesStr 逗号分隔的图片URL字符串
     * @return EssayImages实例
     */
    public static EssayImages fromString(String imagesStr) {
        if (imagesStr == null || imagesStr.trim().isEmpty()) {
            return new EssayImages(Collections.emptyList());
        }
        
        List<String> images = Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toList());
        
        return new EssayImages(images);
    }
    
    /**
     * 创建空的图片列表
     * 
     * @return 空的EssayImages实例
     */
    public static EssayImages empty() {
        return new EssayImages(Collections.emptyList());
    }
    
    /**
     * 验证图片列表
     * 
     * @throws BusinessException 当验证失败时
     */
    public void validate() {
        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new BusinessException("图片数量不能超过" + MAX_IMAGE_COUNT + "张");
        }
        
        // 验证图片URL格式
        for (String imageUrl : imageUrls) {
            validateImageUrl(imageUrl);
        }
    }
    
    /**
     * 验证单个图片URL
     * 
     * @param imageUrl 图片URL
     * @throws BusinessException 当URL格式无效时
     */
    private void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BusinessException("图片URL不能为空");
        }
        
        // 简单的URL格式验证
        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            throw new BusinessException("无效的图片URL格式: " + imageUrl);
        }
    }
    
    /**
     * 转换为逗号分隔的字符串
     * 
     * @return 逗号分隔的图片URL字符串
     */
    public String toString() {
        if (imageUrls.isEmpty()) {
            return "";
        }
        return String.join(",", imageUrls);
    }
    
    /**
     * 获取图片数量
     * 
     * @return 图片数量
     */
    public int getImageCount() {
        return imageUrls.size();
    }
    
    /**
     * 判断是否为空
     * 
     * @return 如果没有图片返回true
     */
    public boolean isEmpty() {
        return imageUrls.isEmpty();
    }
    
    /**
     * 获取图片URL数组
     * 
     * @return 图片URL数组
     */
    public String[] toArray() {
        return imageUrls.toArray(new String[0]);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EssayImages that = (EssayImages) o;
        return Objects.equals(imageUrls, that.imageUrls);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(imageUrls);
    }
}