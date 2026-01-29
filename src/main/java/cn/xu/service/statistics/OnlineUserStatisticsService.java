package cn.xu.service.statistics;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 在线用户统计服务
 * <p>基于 Sa-Token 获取实时在线用户数据</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserStatisticsService {

    /**
     * 获取当前在线用户数
     */
    public int getOnlineUserCount() {
        try {
            List<String> sessionIds = StpUtil.searchSessionId("", 0, -1, false);
            return sessionIds != null ? sessionIds.size() : 0;
        } catch (Exception e) {
            log.warn("获取在线用户数失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 获取当前在线用户ID列表
     */
    public List<String> getOnlineUserIds() {
        try {
            return StpUtil.searchSessionId("", 0, -1, false);
        } catch (Exception e) {
            log.warn("获取在线用户列表失败: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        try {
            return StpUtil.isLogin(userId);
        } catch (Exception e) {
            log.warn("检查用户在线状态失败: userId={}", userId, e);
            return false;
        }
    }
}
