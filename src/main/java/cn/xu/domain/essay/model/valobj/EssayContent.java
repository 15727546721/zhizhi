package cn.xu.domain.essay.model.valobj;

import cn.xu.common.exception.BusinessException;
import cn.xu.common.utils.SensitiveWordFilter;
import lombok.Getter;

import java.util.Objects;

/**
 * 随笔内容值对象
 * 封装随笔内容的业务规则和验证逻辑
 */
@Getter
public class EssayContent {
    
    private static final int MIN_CONTENT_LENGTH = 1;
    private static final int MAX_CONTENT_LENGTH = 1000;
    
    private final String content;
    
    // 注入敏感词过滤器
    private static SensitiveWordFilter sensitiveWordFilter;
    
    public static void setSensitiveWordFilterStatic(SensitiveWordFilter filter) {
        EssayContent.sensitiveWordFilter = filter;
    }

    private EssayContent(String content) {
        this.content = content;
    }
    
    /**
     * 创建随笔内容值对象
     * 
     * @param content 内容
     * @return 随笔内容值对象
     */
    public static EssayContent of(String content) {
        return new EssayContent(content);
    }
    
    /**
     * 验证内容
     * 
     * @throws BusinessException 当验证失败时
     */
    public void validate() {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("随笔内容不能为空");
        }
        
        if (content.length() < MIN_CONTENT_LENGTH) {
            throw new BusinessException("随笔内容长度不能少于" + MIN_CONTENT_LENGTH + "个字符");
        }
        
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("随笔内容长度不能超过" + MAX_CONTENT_LENGTH + "个字符");
        }
        
        // 检查是否包含敏感内容
        if (sensitiveWordFilter != null && sensitiveWordFilter.containsSensitiveWord(content)) {
            throw new BusinessException("随笔内容包含敏感信息，请修改后重试");
        }
    }
    
    /**
     * 获取内容长度
     * 
     * @return 内容长度
     */
    public int getLength() {
        return content.length();
    }
    
    /**
     * 判断是否为空
     * 
     * @return 如果内容为空返回true
     */
    public boolean isEmpty() {
        return content == null || content.trim().isEmpty();
    }
    
    /**
     * 获取内容摘要
     * 
     * @param maxLength 最大长度
     * @return 内容摘要
     */
    public String getSummary(int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    @Override
    public String toString() {
        return content;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EssayContent that = (EssayContent) o;
        return Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}