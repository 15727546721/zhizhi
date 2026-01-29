package cn.xu.service.share;

import cn.xu.cache.core.RedisOperations;
import cn.xu.common.ResponseCode;
import cn.xu.event.events.ShareEvent;
import cn.xu.model.dto.share.ShareRequest;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.Share;
import cn.xu.model.vo.share.ShareStatsVO;
import cn.xu.repository.PostRepository;
import cn.xu.repository.mapper.ShareMapper;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
import cn.xu.support.util.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分享服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareMapper shareMapper;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisOperations redisOps;

    // 限流器：同一用户/IP 对同一帖子，24小时内最多分享1次（记录1次）
    private static final int SHARE_RATE_LIMIT_SECONDS = 86400; // 24小时
    private RateLimiter shareRateLimiter;

    @PostConstruct
    public void init() {
        // 24小时内同一用户对同一帖子只能增加1次分享数
        shareRateLimiter = new RateLimiter(redisOps, "rate:share", 1, SHARE_RATE_LIMIT_SECONDS);
    }

    /**
     * 分享帖子
     *
     * @param request   分享请求
     * @param ip        IP地址
     * @param userAgent 用户代理
     * @return 分享结果，包含是否增加了计数
     */
    @Transactional(rollbackFor = Exception.class)
    public ShareResult share(ShareRequest request, String ip, String userAgent) {
        Long postId = request.getPostId();
        String platform = request.getPlatform();

        // 验证帖子是否存在
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
        
        if (post.isDeleted()) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在");
        }

        // 验证平台
        if (platform != null && !Share.isValidPlatform(platform)) {
            platform = Share.PLATFORM_OTHER;
        }

        // 获取当前用户ID（可能为空）
        Long userId = LoginUserUtil.getLoginUserIdOptional().orElse(null);

        // 构建限流标识：用户ID或IP + 帖子ID
        String rateLimitKey = buildRateLimitKey(userId, ip, postId);
        
        // 检查是否允许增加分享数（防刷）
        boolean allowIncrement = shareRateLimiter.allowRequest(rateLimitKey);
        log.info("分享限流检查: key={}, allowIncrement={}", rateLimitKey, allowIncrement);

        // 创建分享记录（始终记录，用于统计分析）
        Share share = Share.create(postId, userId, platform, ip, userAgent);
        shareMapper.insert(share);

        // 只有在限流允许时才增加分享数
        if (allowIncrement) {
            post.increaseShareCount();
            postRepository.update(post, null);
            postRepository.updateHotScore(postId);
            
            // 发布分享事件
            eventPublisher.publishEvent(new ShareEvent(postId, post.getUserId(), userId, platform));
            
            log.info("分享成功(计数+1): postId={}, userId={}, platform={}", postId, userId, platform);
        } else {
            log.debug("分享记录已保存(计数未增加，24小时内重复): postId={}, userId={}, platform={}", postId, userId, platform);
        }

        return new ShareResult(share.getId(), allowIncrement, post.getShareCount());
    }

    /**
     * 分享结果内部类
     */
    public record ShareResult(Long shareId, boolean countIncreased, Long totalShareCount) {}

    /**
     * 构建限流key
     * 优先使用用户ID，未登录则使用IP
     */
    private String buildRateLimitKey(Long userId, String ip, Long postId) {
        String identifier = userId != null ? "u:" + userId : "ip:" + (ip != null ? ip : "unknown");
        return identifier + ":p:" + postId;
    }

    /**
     * 获取帖子分享统计
     */
    public ShareStatsVO getShareStats(Long postId) {
        // 验证帖子是否存在
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));

        // 获取各平台分享数
        List<Map<String, Object>> platformCounts = shareMapper.countByPostIdGroupByPlatform(postId);

        Map<String, Long> platformStats = new HashMap<>();
        long copyCount = 0, weiboCount = 0, qqCount = 0, wechatCount = 0;

        for (Map<String, Object> item : platformCounts) {
            String platform = (String) item.get("platform");
            Long count = ((Number) item.get("count")).longValue();
            platformStats.put(platform, count);

            switch (platform) {
                case Share.PLATFORM_COPY -> copyCount = count;
                case Share.PLATFORM_WEIBO -> weiboCount = count;
                case Share.PLATFORM_QQ -> qqCount = count;
                case Share.PLATFORM_WECHAT -> wechatCount = count;
            }
        }

        return ShareStatsVO.builder()
                .postId(postId)
                .totalCount(post.getShareCount())
                .platformStats(platformStats)
                .copyCount(copyCount)
                .weiboCount(weiboCount)
                .qqCount(qqCount)
                .wechatCount(wechatCount)
                .build();
    }

    /**
     * 获取用户分享数
     */
    public long getUserShareCount(Long userId) {
        return shareMapper.countByUserId(userId);
    }

    /**
     * 检查用户是否分享过某帖子
     */
    public boolean hasUserShared(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        return shareMapper.countByPostIdAndUserId(postId, userId) > 0;
    }

    /**
     * 获取分享排行榜
     *
     * @param days  统计天数
     * @param limit 排行榜数量
     */
    public List<Map<String, Object>> getShareRanking(int days, int limit) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return shareMapper.getShareRanking(startTime, limit);
    }

    /**
     * 统计时间范围内的分享数
     */
    public long countSharesInRange(LocalDateTime startTime, LocalDateTime endTime) {
        return shareMapper.countByTimeRange(startTime, endTime);
    }
}
