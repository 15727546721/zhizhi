package cn.xu.infrastructure.config;

import cn.xu.domain.article.model.valobj.ArticleContent;
import cn.xu.domain.article.model.valobj.ArticleTitle;
import cn.xu.domain.comment.model.valueobject.CommentContent;
import cn.xu.domain.essay.model.valobj.EssayContent;
import cn.xu.infrastructure.common.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 值对象配置类
 * 用于初始化值对象中的静态依赖
 */
@Configuration
public class ValueObjectConfig {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @PostConstruct
    public void initValueObjects() {
        // 初始化各个值对象中的静态依赖
        ArticleTitle.setSensitiveWordFilterStatic(sensitiveWordFilter);
        ArticleContent.setSensitiveWordFilterStatic(sensitiveWordFilter);
        CommentContent.setSensitiveWordFilterStatic(sensitiveWordFilter);
        EssayContent.setSensitiveWordFilterStatic(sensitiveWordFilter);
    }
}