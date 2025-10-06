package cn.xu.infrastructure.config;

import cn.xu.common.utils.SensitiveWordFilter;
import cn.xu.domain.comment.model.valueobject.CommentContent;
import cn.xu.domain.essay.model.valobj.EssayContent;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostTitle;
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
        PostTitle.setSensitiveWordFilterStatic(sensitiveWordFilter);
        PostContent.setSensitiveWordFilterStatic(sensitiveWordFilter);
        CommentContent.setSensitiveWordFilterStatic(sensitiveWordFilter);
        EssayContent.setSensitiveWordFilterStatic(sensitiveWordFilter);
    }
}