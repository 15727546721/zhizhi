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
 * 随笔话题值对象
 * 封装随笔话题列表的业务规则和验证逻辑
 */
@Slf4j
@Getter
public class EssayTopics {
    
    /**
     * 最大话题数量限制
     */
    private static final int MAX_TOPIC_COUNT = 5;
    
    /**
     * 话题名称最大长度
     */
    private static final int MAX_TOPIC_LENGTH = 20;
    
    /**
     * 话题列表
     */
    private final List<String> topics;
    
    /**
     * 私有构造函数，确保通过工厂方法创建
     */
    private EssayTopics(List<String> topics) {
        this.topics = Collections.unmodifiableList(new ArrayList<>(topics));
    }
    
    /**
     * 从字符串列表创建EssayTopics
     * 
     * @param topics 话题列表
     * @return EssayTopics实例
     */
    public static EssayTopics of(List<String> topics) {
        if (topics == null) {
            return new EssayTopics(Collections.emptyList());
        }
        
        List<String> validTopics = topics.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(topic -> !topic.isEmpty())
                .distinct() // 去重
                .collect(Collectors.toList());
        
        return new EssayTopics(validTopics);
    }
    
    /**
     * 从逗号分隔的字符串创建EssayTopics
     * 
     * @param topicsStr 逗号分隔的话题字符串
     * @return EssayTopics实例
     */
    public static EssayTopics fromString(String topicsStr) {
        if (topicsStr == null || topicsStr.trim().isEmpty()) {
            return new EssayTopics(Collections.emptyList());
        }
        
        List<String> topics = Arrays.stream(topicsStr.split(","))
                .map(String::trim)
                .filter(topic -> !topic.isEmpty())
                .distinct() // 去重
                .collect(Collectors.toList());
        
        return new EssayTopics(topics);
    }
    
    /**
     * 创建空的话题列表
     * 
     * @return 空的EssayTopics实例
     */
    public static EssayTopics empty() {
        return new EssayTopics(Collections.emptyList());
    }
    
    /**
     * 验证话题列表
     * 
     * @throws BusinessException 当验证失败时
     */
    public void validate() {
        if (topics.size() > MAX_TOPIC_COUNT) {
            throw new BusinessException("话题数量不能超过" + MAX_TOPIC_COUNT + "个");
        }
        
        // 验证每个话题
        for (String topic : topics) {
            validateTopic(topic);
        }
    }
    
    /**
     * 验证单个话题
     * 
     * @param topic 话题名称
     * @throws BusinessException 当话题无效时
     */
    private void validateTopic(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new BusinessException("话题名称不能为空");
        }
        
        if (topic.length() > MAX_TOPIC_LENGTH) {
            throw new BusinessException("话题名称长度不能超过" + MAX_TOPIC_LENGTH + "个字符");
        }
        
        // 验证话题名称是否包含非法字符
        if (topic.contains("#") || topic.contains("@") || topic.contains("&")) {
            throw new BusinessException("话题名称不能包含特殊字符: #, @, &");
        }
    }
    
    /**
     * 转换为逗号分隔的字符串
     * 
     * @return 逗号分隔的话题字符串
     */
    public String toString() {
        if (topics.isEmpty()) {
            return "";
        }
        return String.join(",", topics);
    }
    
    /**
     * 获取话题数量
     * 
     * @return 话题数量
     */
    public int getTopicCount() {
        return topics.size();
    }
    
    /**
     * 判断是否为空
     * 
     * @return 如果没有话题返回true
     */
    public boolean isEmpty() {
        return topics.isEmpty();
    }
    
    /**
     * 获取话题数组
     * 
     * @return 话题数组
     */
    public String[] toArray() {
        return topics.toArray(new String[0]);
    }
    
    /**
     * 判断是否包含指定话题
     * 
     * @param topic 话题名称
     * @return 如果包含返回true
     */
    public boolean contains(String topic) {
        return topics.contains(topic);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EssayTopics that = (EssayTopics) o;
        return Objects.equals(topics, that.topics);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(topics);
    }
}