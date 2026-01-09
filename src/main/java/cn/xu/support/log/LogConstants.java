package cn.xu.support.log;

/**
 * 日志常量定义
 * <p>统一日志格式和模块标识</p>
 */
public final class LogConstants {

    private LogConstants() {}

    // ==================== 模块标识 ====================
    
    public static final String MODULE_USER = "用户";
    public static final String MODULE_POST = "帖子";
    public static final String MODULE_COMMENT = "评论";
    public static final String MODULE_LIKE = "点赞";
    public static final String MODULE_FAVORITE = "收藏";
    public static final String MODULE_FOLLOW = "关注";
    public static final String MODULE_MESSAGE = "消息";
    public static final String MODULE_FILE = "文件";
    public static final String MODULE_AUTH = "认证";
    public static final String MODULE_ADMIN = "管理";
    public static final String MODULE_SEARCH = "搜索";
    public static final String MODULE_CACHE = "缓存";
    public static final String MODULE_SYSTEM = "系统";

    // ==================== 操作类型 ====================
    
    public static final String OP_CREATE = "创建";
    public static final String OP_UPDATE = "更新";
    public static final String OP_DELETE = "删除";
    public static final String OP_QUERY = "查询";
    public static final String OP_LOGIN = "登录";
    public static final String OP_LOGOUT = "登出";
    public static final String OP_REGISTER = "注册";
    public static final String OP_PUBLISH = "发布";
    public static final String OP_WITHDRAW = "撤回";
    public static final String OP_UPLOAD = "上传";
    public static final String OP_EXPORT = "导出";
    public static final String OP_IMPORT = "导入";

    // ==================== 结果状态 ====================
    
    public static final String RESULT_SUCCESS = "成功";
    public static final String RESULT_FAIL = "失败";
    public static final String RESULT_ERROR = "异常";
}
