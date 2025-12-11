package cn.xu.support.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 登录用户工具类
 * <p>统一处理登录用户ID获取，避免直接调用StpUtil导致的异常处理不统一问题</p>
 
 */
@Slf4j
public class LoginUserUtil {

    /**
     * 获取当前登录用户ID（已登录场景）
     *
     * <p>使用场景：Controller 方法已添加 @SaCheckLogin 注解，确保用户已登录
     * <p>如果未登录会被 @SaCheckLogin 拦截，此方法不会被执行
     *
     * @return 用户ID
     * @throws BusinessException 如果获取失败
     */
    public static Long getLoginUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.error("获取登录用户ID失败", e);
            throw new BusinessException(ResponseCode.NOT_LOGIN.getCode(), "获取登录用户信息失败");
        }
    }

    /**
     * 获取当前登录用户ID（可选登录场景）
     *
     * <p>使用场景：Controller 方法未添加 @SaCheckLogin 注解，用户可能未登录
     * <p>如帖子详情页面，未登录用户也可以查看，但登录用户可以看到点赞收藏状态
     *
     * @return Optional<Long> 登录返回用户ID，未登录返回 Optional.empty()
     */
    public static Optional<Long> getLoginUserIdOptional() {
        try {
            if (StpUtil.isLogin()) {
                return Optional.of(StpUtil.getLoginIdAsLong());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.debug("获取登录用户ID失败，用户未登录", e);
            return Optional.empty();
        }
    }

    /**
     * 判断用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public static boolean isLogin() {
        try {
            return StpUtil.isLogin();
        } catch (Exception e) {
            log.debug("判断用户登录状态失败", e);
            return false;
        }
    }

    /**
     * 验证用户是否有权限操作指定资源
     *
     * <p>使用场景：验证当前登录用户是否是资源的所有者
     *
     * @param resourceOwnerId 资源所有者ID
     * @throws BusinessException 如果不是资源所有者
     */
    public static void validateOwnership(Long resourceOwnerId) {
        Long currentUserId = getLoginUserId();
        if (!currentUserId.equals(resourceOwnerId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN.getCode(), "无权操作此资源");
        }
    }

    /**
     * 验证用户是否有权限操作指定资源（含管理员豁免）
     *
     * @param resourceOwnerId 资源所有者ID
     * @param isAdmin 是否为管理员
     * @throws BusinessException 如果不是资源所有者且不是管理员
     */
    public static void validateOwnershipWithAdmin(Long resourceOwnerId, boolean isAdmin) {
        if (isAdmin) {
            return; // 管理员可以操作任何资源
        }
        validateOwnership(resourceOwnerId);
    }
}