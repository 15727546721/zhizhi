package cn.xu.infrastructure.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 敏感词过滤工具类
 * 基于DFA算法实现敏感词过滤
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    /**
     * 敏感词库文件路径
     */
    private static final String SENSITIVE_WORD_FILE = "sensitive-words.txt";

    /**
     * 敏感词树的根节点
     */
    private final Map<Character, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 用于标记单词结尾的特殊字符
     */
    private static final Character END_MARK = '\0'; // 使用空字符作为单词结尾标记

    /**
     * 最小匹配长度（用于优化性能）
     */
    private int minMatchLength = Integer.MAX_VALUE;

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        try {
            loadSensitiveWords();
            log.info("敏感词库初始化完成，共加载 {} 个敏感词", sensitiveWordMap.size());
        } catch (Exception e) {
            log.error("敏感词库初始化失败", e);
            // 加载默认敏感词
            loadDefaultSensitiveWords();
        }
    }

    /**
     * 从文件加载敏感词库
     */
    private void loadSensitiveWords() {
        ClassPathResource resource = new ClassPathResource(SENSITIVE_WORD_FILE);
        if (!resource.exists()) {
            log.warn("未找到敏感词库文件: {}, 使用默认敏感词", SENSITIVE_WORD_FILE);
            loadDefaultSensitiveWords();
            return;
        }

        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (StringUtils.hasText(word) && !word.startsWith("#")) {
                    addSensitiveWord(word);
                    minMatchLength = Math.min(minMatchLength, word.length());
                }
            }
        } catch (IOException e) {
            log.error("读取敏感词库文件失败: {}", SENSITIVE_WORD_FILE, e);
            loadDefaultSensitiveWords();
        }
    }

    /**
     * 加载默认敏感词
     */
    private void loadDefaultSensitiveWords() {
        log.info("加载默认敏感词库");
        String[] defaultWords = {
                "广告", "色情", "赌博", "暴力", "政治敏感", "反动", "违法", "毒品", "枪支", "色情网站",
                "赌博网站", "暴力内容", "政治话题", "违法信息", "毒品交易", "枪支弹药", "成人内容", "性交易",
                "网络诈骗", "非法集资", "传销", "洗钱", "恐怖主义", "极端主义", "分裂国家", "颠覆国家政权"
        };

        for (String word : defaultWords) {
            addSensitiveWord(word);
            minMatchLength = Math.min(minMatchLength, word.length());
        }
    }

    /**
     * 添加敏感词到词库
     *
     * @param word 敏感词
     */
    private void addSensitiveWord(String word) {
        if (!StringUtils.hasText(word)) {
            return;
        }

        Map<Character, Object> currentMap = sensitiveWordMap;
        for (int i = 0; i < word.length(); i++) {
            char keyChar = word.charAt(i);
            Object wordMap = currentMap.get(keyChar);

            if (wordMap != null) {
                currentMap = (Map<Character, Object>) wordMap;
            } else {
                Map<Character, Object> newWordMap = new HashMap<>();
                newWordMap.put(END_MARK, false);
                currentMap.put(keyChar, newWordMap);
                currentMap = newWordMap;
            }

            if (i == word.length() - 1) {
                currentMap.put(END_MARK, true);
            }
        }
    }

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检查的文本
     * @return 是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            int matchFlag = checkSensitiveWord(text, i);
            if (matchFlag > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文本中的所有敏感词
     *
     * @param text 待检查的文本
     * @return 敏感词列表
     */
    public List<String> getSensitiveWords(String text) {
        List<String> sensitiveWords = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return sensitiveWords;
        }

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                sensitiveWords.add(text.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return sensitiveWords;
    }

    /**
     * 替换文本中的敏感词
     *
     * @param text        待处理的文本
     * @param replaceChar 替换字符
     * @return 处理后的文本
     */
    public String filterSensitiveWords(String text, char replaceChar) {
        if (!StringUtils.hasText(text)) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < result.length(); i++) {
            int length = checkSensitiveWord(result.toString(), i);
            if (length > 0) {
                for (int j = 0; j < length; j++) {
                    result.setCharAt(i + j, replaceChar);
                }
                i = i + length - 1;
            }
        }
        return result.toString();
    }

    /**
     * 检查从指定位置开始是否包含敏感词
     *
     * @param text 待检查的文本
     * @param beginIndex 开始检查的位置
     * @return 匹配到的敏感词长度，0表示未匹配到
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        if (beginIndex >= text.length() || beginIndex < 0) {
            return 0;
        }

        // 优化：如果剩余字符数小于最小匹配长度，则直接返回
        if (text.length() - beginIndex < minMatchLength) {
            return 0;
        }

        Map<Character, Object> currentMap = sensitiveWordMap;
        int matchFlag = 0;
        boolean flag = false;

        for (int i = beginIndex; i < text.length(); i++) {
            char word = text.charAt(i);
            Map<Character, Object> wordMap = (Map<Character, Object>) currentMap.get(word);

            if (wordMap != null) {
                matchFlag++;
                currentMap = wordMap;
                if (Boolean.TRUE.equals(wordMap.get(END_MARK))) {
                    flag = true;
                    break;
                }
            } else {
                break;
            }
        }

        if (matchFlag < minMatchLength || !flag) {
            matchFlag = 0;
        }

        return matchFlag;
    }
}