package cn.xu.support.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 用户名生成器
 * 用于生成随机的、唯一的、符合社区风格的username
 * 
 * 生成规则：
 * 1. 前缀：zhizhi（知知） + 随机词汇
 * 2. 后缀：4位随机数字
 * 3. 总长度：8-15字符
 * 4. 符合规范：字母数字下划线
 * 
 * @author xu
 */
@Slf4j
public class UsernameGenerator {
    
    private static final Random RANDOM = new SecureRandom();
    
    /**
     * 随机形容词（描述用户特征）
     */
    private static final String[] ADJECTIVES = {
        "cool", "smart", "happy", "brave", "lucky",
        "quick", "wise", "kind", "strong", "gentle",
        "creative", "active", "sunny", "bright", "dream"
    };
    
    /**
     * 随机名词（社区相关）
     */
    private static final String[] NOUNS = {
        "coder", "dev", "geek", "tech", "master",
        "pro", "ninja", "guru", "expert", "hero",
        "learner", "builder", "maker", "hacker", "star"
    };
    
    /**
     * 生成随机username
     * 格式：zhizhi_形容词_名词_数字
     * 示例：zhizhi_cool_coder_1234
     * 
     * @return 随机username
     */
    public static String generate() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        int number = 1000 + RANDOM.nextInt(9000); // 4位数字
        
        return String.format("zhizhi_%s_%s_%d", adjective, noun, number);
    }
    
    /**
     * 生成简短的随机username
     * 格式：zhizhi + 名词 + 数字
     * 示例：zhizhi_coder_1234
     * 
     * @return 简短username
     */
    public static String generateShort() {
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        int number = 1000 + RANDOM.nextInt(9000);
        
        return String.format("zhizhi_%s_%d", noun, number);
    }
    
    /**
     * 基于邮箱前缀生成username
     * 如果邮箱前缀合法，使用邮箱前缀 + 随机数字
     * 否则使用完全随机生成
     * 
     * @param email 用户邮箱
     * @return username
     */
    public static String generateFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return generate();
        }
        
        // 提取邮箱前缀
        String prefix = email.substring(0, email.indexOf("@"));
        
        // 清理非法字符，只保留字母数字下划线
        prefix = prefix.replaceAll("[^a-zA-Z0-9_]", "");
        
        // 如果前缀太短或为空，使用随机生成
        if (prefix.length() < 3) {
            return generate();
        }
        
        // 如果前缀太长，截取前10位
        if (prefix.length() > 10) {
            prefix = prefix.substring(0, 10);
        }
        
        // 添加随机后缀避免重复
        int suffix = 100 + RANDOM.nextInt(900); // 3位数字
        
        return prefix + "_" + suffix;
    }
    
    /**
     * 生成纯数字username（备用方案）
     * 格式：user + 时间戳后8位 + 随机2位
     * 示例：user12345678
     * 
     * @return 纯数字username
     */
    public static String generateNumeric() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        String last8 = timestampStr.substring(timestampStr.length() - 8);
        int random2 = 10 + RANDOM.nextInt(90);
        
        return "user" + last8 + random2;
    }
    
    /**
     * 验证username是否符合规范
     * 规则：4-20位，只能包含字母、数字、下划线
     * 
     * @param username 用户名
     * @return 是否合法
     */
    public static boolean isValid(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        
        // 长度检查
        if (trimmed.length() < 4 || trimmed.length() > 20) {
            return false;
        }
        
        // 格式检查：只允许字母、数字、下划线
        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * 生成多个候选username（用于重复时重试）
     * 
     * @param count 生成数量
     * @return username列表
     */
    public static String[] generateMultiple(int count) {
        String[] usernames = new String[count];
        for (int i = 0; i < count; i++) {
            usernames[i] = generate();
        }
        return usernames;
    }
}
