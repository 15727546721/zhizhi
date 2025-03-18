package cn.xu.application.task;

import cn.xu.domain.like.model.LikeType;
import cn.xu.infrastructure.persistent.dao.IArticleDao;
import cn.xu.infrastructure.persistent.dao.ICommentDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataSyncTask {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private IArticleDao articleDao;
    @Resource
    private ICommentDao commentDao;

    @Scheduled(cron = "0/10 * * * * ?")
    public void syncLikeCountToMySQL() {
        log.info("开始同步 Redis的点赞数据 到 MySQL");
        // 1. 扫描 Redis 中所有 like_count:* 的键
        Set<String> keys = redisTemplate.keys("like:count:*");

        // 2. 批量获取键值对
        Map<String, Integer> redisCounts = keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> Integer.parseInt(redisTemplate.opsForValue().get(key))
                ));

        // 3. 更新到 MySQL
        redisCounts.forEach((key, count) -> {
            String[] parts = key.split(":");
            String type = parts[1];
            long targetId = Long.parseLong(parts[2]);

            switch (LikeType.valueOf(type.toUpperCase())) {
                case ARTICLE:
                    articleDao.updateLikeCount(targetId, count);
                    break;
                case COMMENT:
                    commentDao.updateLikeCount(targetId, count);
                    break;
                default:
                    break;
            }
        });
    }
}
