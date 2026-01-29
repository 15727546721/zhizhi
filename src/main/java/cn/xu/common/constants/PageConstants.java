package cn.xu.common.constants;

/**
 * 分页常量和工具方法
 * 
 * <p>统一管理分页相关的常量和参数校验逻辑</p>
 */
public final class PageConstants {
    
    private PageConstants() {
        // 防止实例化
    }
    
    // ==================== 分页常量 ====================
    
    /** 默认页码 */
    public static final int DEFAULT_PAGE = 1;
    
    /** 默认每页大小 */
    public static final int DEFAULT_SIZE = 10;
    
    /** 最小页码 */
    public static final int MIN_PAGE = 1;
    
    /** 最小每页大小 */
    public static final int MIN_SIZE = 1;
    
    /** 最大每页大小 */
    public static final int MAX_SIZE = 100;
    
    /** 大数据量最大每页大小 */
    public static final int MAX_SIZE_LARGE = 500;
    
    // ==================== 工具方法 ====================
    
    /**
     * 校验并修正页码
     * @param page 原始页码
     * @return 修正后的页码（最小为1）
     */
    public static int normalizePage(Integer page) {
        if (page == null || page < MIN_PAGE) {
            return DEFAULT_PAGE;
        }
        return page;
    }
    
    /**
     * 校验并修正每页大小
     * @param size 原始每页大小
     * @return 修正后的每页大小（范围：1-100）
     */
    public static int normalizeSize(Integer size) {
        return normalizeSize(size, MAX_SIZE);
    }
    
    /**
     * 校验并修正每页大小（自定义最大值）
     * @param size 原始每页大小
     * @param maxSize 最大每页大小
     * @return 修正后的每页大小
     */
    public static int normalizeSize(Integer size, int maxSize) {
        if (size == null || size < MIN_SIZE) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, maxSize);
    }
    
    /**
     * 计算偏移量
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 偏移量
     */
    public static int calculateOffset(int page, int size) {
        return (normalizePage(page) - 1) * normalizeSize(size);
    }
    
    /**
     * 一次性校验分页参数并计算偏移量
     * @param page 原始页码
     * @param size 原始每页大小
     * @return 包含校验后的page、size和offset的数组 [page, size, offset]
     */
    public static int[] normalizeAndCalculate(Integer page, Integer size) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        int offset = (normalizedPage - 1) * normalizedSize;
        return new int[]{normalizedPage, normalizedSize, offset};
    }
}
