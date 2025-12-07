package cn.xu.support.util;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 敏感词过滤器
 * <p>通过DFA算法辅助敏感词过滤的工具</p>
 
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    /**
     * 敏感词文件路径
     */
    private static final String SENSITIVE_WORD_FILE = "sensitive-words.txt";

    /**
     * 敏感词的字典
     */
    private final Map<Character, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 结束标记
     */
    private static final Character END_MARK = '\0'; // 用于标识敏感词的结束

    /**
     * 最小匹配长度，避免匹配过短的词
     */
    private int minMatchLength = Integer.MAX_VALUE;

    /**
     * 初始化方法，加载敏感词字典
     */
    @PostConstruct
    public void init() {
        try {
            loadSensitiveWords();
            log.info("敏感词库初始化成功，共加载了 {} 个敏感词", sensitiveWordMap.size());
        } catch (Exception e) {
            log.error("加载敏感词库失败，错误信息: {}", e.getMessage(), e);
            // 加载默认敏感词库
            loadDefaultSensitiveWords();
        }
    }

    /**
     * 加载敏感词文件
     */
    private void loadSensitiveWords() {
        ClassPathResource resource = new ClassPathResource(SENSITIVE_WORD_FILE);
        if (!resource.exists()) {
            log.warn("未找到敏感词文件: {}，将使用默认敏感词库", SENSITIVE_WORD_FILE);
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
            log.error("读取敏感词文件出错: {}", SENSITIVE_WORD_FILE, e);
            loadDefaultSensitiveWords();
        }
    }

    /**
     * 加载默认的敏感词
     */
    private void loadDefaultSensitiveWords() {
        log.info("加载默认敏感词库");
        String[] defaultWords = {
                "敏感词1", "敏感词2", "敏感词3", "敏感词4"
        };

        for (String word : defaultWords) {
            addSensitiveWord(word);
            minMatchLength = Math.min(minMatchLength, word.length());
        }
    }

    /**
     * 添加敏感词到字典
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
     * 检查文本中是否包含敏感词
     *
     * @param text 输入文本
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
     * 获取文本中的敏感词列表
     *
     * @param text 输入文本
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
     * @param text        输入文本
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
     * 检查指定位置的文本是否包含敏感词
     *
     * @param text       输入文本
     * @param beginIndex 检查的起始位置
     * @return 返回匹配的敏感词长度，0表示没有匹配
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        if (beginIndex >= text.length() || beginIndex < 0) {
            return 0;
        }

        // 判断剩余长度是否足够匹配最小敏感词
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
