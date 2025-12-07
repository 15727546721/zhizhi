package cn.xu.integration.search.util;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 搜索关键词规范化工具
 * <p>提供关键词标准化处理，包括全角转半角、去除多余空格等</p>
 
 */
public class SearchKeywordNormalizer {

    // 全角字符数组（用于全角转半角）
    private static final char[] FULL_WIDTH_CHARS = {
            '\u3000', '！', '"', '"', '＇', '＇', '。', '，', '：', '、', '；', '？', '（',
            '）', '【', '】', '｛', '｝', '·', '《', '》', '～', '０', '１', '２', '３', '４',
            '５', '６', '７', '８', '９', 'Ａ', 'Ｂ', 'Ｃ', 'Ｄ', 'Ｅ', 'Ｆ', 'Ｇ', 'Ｈ', 'Ｉ',
            'Ｊ', 'Ｋ', 'Ｌ', 'Ｍ', 'Ｎ', 'Ｏ', 'Ｐ', 'Ｑ', 'Ｒ', 'Ｓ', 'Ｔ', 'Ｕ', 'Ｖ', 'Ｗ',
            'Ｘ', 'Ｙ', 'Ｚ', 'ａ', 'ｂ', 'ｃ', 'ｄ', 'ｅ', 'ｆ', 'ｇ', 'ｈ', 'ｉ', 'ｊ', 'ｋ',
            'ｌ', 'ｍ', 'ｎ', 'ｏ', 'ｐ', 'ｑ', 'ｒ', 'ｓ', 'ｔ', 'ｕ', 'ｖ', 'ｗ', 'ｘ', 'ｙ', 'ｚ'};

    private static final char[] HALF_WIDTH_CHARS = {' ', '!', '"', '#', '$', '%', '&', '\'',
            '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@',
            '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    // 使用 HashMap 优化全角转半角的查找性能，时间复杂度 O(1)
    private static final Map<Character, Character> CHAR_MAP = new HashMap<>();

    static {
        // 初始化全角到半角的映射
        for (int i = 0; i < FULL_WIDTH_CHARS.length && i < HALF_WIDTH_CHARS.length; i++) {
            CHAR_MAP.put(FULL_WIDTH_CHARS[i], HALF_WIDTH_CHARS[i]);
        }
    }

    public static String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }

        String normalized = keyword.trim();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFC);
        normalized = fullWidthToHalfWidth(normalized);
        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");
        normalized = normalized.trim();

        return normalized;
    }

    /**
     * 全角字符转半角字符
     * <p>
     * 优化：使用 HashMap 实现 O(1) 查找，替代原来的 O(n) 线性查找
     * </p>
     */
    private static String fullWidthToHalfWidth(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            // 使用 Map 查找，时间复杂度 O(1)
            sb.append(CHAR_MAP.getOrDefault(c, c));
        }
        return sb.toString();
    }

    public static java.util.List<String> extractKeywords(String text) {
        java.util.List<String> keywords = new java.util.ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return keywords;
        }

        String normalized = normalize(text);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[\\u4e00-\\u9fa5a-zA-Z0-9]+");
        java.util.regex.Matcher matcher = pattern.matcher(normalized);

        while (matcher.find()) {
            String keyword = matcher.group();
            if (keyword.length() >= 2) {
                keywords.add(keyword);
            }
        }

        return keywords;
    }
}