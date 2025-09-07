package cn.xu.domain.article.service;

import cn.xu.domain.article.model.valobj.ArticleContent;
import cn.xu.domain.article.model.valobj.ArticleTitle;
import cn.xu.infrastructure.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 文章内容验证领域服务
 * 处理跨实体的文章内容验证业务逻辑
 */
@Service
public class ArticleContentValidationDomainService {

    private static final int MIN_QUALITY_SCORE = 60;

    /**
     * 验证文章标题和内容的质量
     * @param title 文章标题
     * @param content 文章内容
     * @return 是否通过验证
     */
    public boolean validateArticleQuality(ArticleTitle title, ArticleContent content) {
        if (title == null || content == null) {
            return false;
        }

        // 计算内容质量分数
        int qualityScore = calculateQualityScore(title.getValue(), content.getValue());
        
        if (qualityScore < MIN_QUALITY_SCORE) {
            throw new BusinessException("文章质量不达标，建议完善内容后再发布");
        }

        return true;
    }

    /**
     * 计算内容质量分数
     */
    private int calculateQualityScore(String title, String content) {
        int score = 0;

        // 标题长度评分 (0-20分)
        int titleLength = title.length();
        if (titleLength >= 10 && titleLength <= 50) {
            score += 20;
        } else if (titleLength >= 5) {
            score += 10;
        }

        // 内容长度评分 (0-30分)
        int contentLength = content.length();
        if (contentLength >= 500) {
            score += 30;
        } else if (contentLength >= 200) {
            score += 20;
        } else if (contentLength >= 100) {
            score += 10;
        }

        // 内容丰富度评分 (0-50分)
        // 计算段落数量
        long paragraphCount = countLines(content);
        if (paragraphCount >= 10) {
            score += 20;
        } else if (paragraphCount >= 5) {
            score += 10;
        }

        // 计算句子数量（简单按句号、问号、感叹号分割）
        long sentenceCount = content.split("[.!?。！？]").length;
        if (sentenceCount >= 20) {
            score += 20;
        } else if (sentenceCount >= 10) {
            score += 10;
        }

        return Math.min(score, 100); // 最高100分
    }
    
    /**
     * 计算文本行数（替代Java 11的lines()方法）
     */
    private long countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        long count = 1; // 至少有一行
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }
}