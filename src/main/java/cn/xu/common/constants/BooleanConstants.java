package cn.xu.common.constants;

/**
 * 布尔值常量
 * 
 * <p>用于数据库中使用Integer存储布尔值的场景</p>
 */
public final class BooleanConstants {
    
    private BooleanConstants() {
        // 防止实例化
    }
    
    /** 真值 */
    public static final Integer TRUE = 1;
    
    /** 假值 */
    public static final Integer FALSE = 0;
    
    /**
     * 将Integer转换为boolean
     * @param value Integer值
     * @return 如果value为1返回true，否则返回false
     */
    public static boolean isTrue(Integer value) {
        return value != null && value.equals(TRUE);
    }
    
    /**
     * 将Integer转换为boolean（null安全）
     * @param value Integer值
     * @return 如果value为0或null返回true，否则返回false
     */
    public static boolean isFalse(Integer value) {
        return value == null || value.equals(FALSE);
    }
    
    /**
     * 将boolean转换为Integer
     * @param value boolean值
     * @return 如果value为true返回1，否则返回0
     */
    public static Integer toInteger(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    /**
     * 切换布尔值
     * @param value 当前Integer值
     * @return 如果当前为1返回0，否则返回1
     */
    public static Integer toggle(Integer value) {
        return isTrue(value) ? FALSE : TRUE;
    }
}
