package cn.xu.infrastructure.search.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * 搜索关键词规范化工具类
 */
public class SearchKeywordNormalizer {
    
    private static final char[] FULL_WIDTH_CHARS = {'　', '！', '＂', '＃', '＄', '％', '＆', '＇', 
        '（', '）', '＊', '＋', '，', '－', '．', '／', '：', '；', '＜', '＝', '＞', '？', '＠', 
        '［', '＼', '］', '＾', '＿', '｀', '｛', '｜', '｝', '～', '０', '１', '２', '３', '４', 
        '５', '６', '７', '８', '９', 'Ａ', 'Ｂ', 'Ｃ', 'Ｄ', 'Ｅ', 'Ｆ', 'Ｇ', 'Ｈ', 'Ｉ', 'Ｊ', 
        'Ｋ', 'Ｌ', 'Ｍ', 'Ｎ', 'Ｏ', 'Ｐ', 'Ｑ', 'Ｒ', 'Ｓ', 'Ｔ', 'Ｕ', 'Ｖ', 'Ｗ', 'Ｘ', 'Ｙ', 
        'Ｚ', 'ａ', 'ｂ', 'ｃ', 'ｄ', 'ｅ', 'ｆ', 'ｇ', 'ｈ', 'ｉ', 'ｊ', 'ｋ', 'ｌ', 'ｍ', 'ｎ', 
        'ｏ', 'ｐ', 'ｑ', 'ｒ', 'ｓ', 'ｔ', 'ｕ', 'ｖ', 'ｗ', 'ｘ', 'ｙ', 'ｚ'};
    
    private static final char[] HALF_WIDTH_CHARS = {' ', '!', '"', '#', '$', '%', '&', '\'', 
        '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', 
        '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~', '0', '1', '2', '3', '4', 
        '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 
        'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    
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
    
    private static String fullWidthToHalfWidth(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            int index = -1;
            for (int i = 0; i < FULL_WIDTH_CHARS.length; i++) {
                if (FULL_WIDTH_CHARS[i] == c) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                sb.append(HALF_WIDTH_CHARS[index]);
            } else {
                sb.append(c);
            }
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

