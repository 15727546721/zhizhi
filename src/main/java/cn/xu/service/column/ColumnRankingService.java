package cn.xu.service.column;

import cn.xu.model.vo.column.ColumnVO;
import cn.xu.repository.ColumnRepository;
import cn.xu.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 专栏排行榜服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnRankingService {
    
    private final ColumnRepository columnRepository;
    private final CacheService cacheService;
    
    private static final String CACHE_KEY_HOT = "column:ranking:hot";
    private static final String CACHE_KEY_NEW = "column:ranking:new";
    private static final String CACHE_KEY_SUBSCRIBE = "column:ranking:subscribe";
    private static final int CACHE_EXPIRE_HOURS = 1;
    private static final int RANKING_SIZE = 50;
    
    /**
     * 获取热门专栏排行榜
     * 综合考虑阅读量、订阅数、文章数等因素
     */
    public List<ColumnVO> getHotRanking() {
        // 先从缓存获取
        List<ColumnVO> cached = cacheService.get(CACHE_KEY_HOT);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        
        // 从数据库查询
        List<ColumnVO> ranking = columnRepository.findHotColumns(RANKING_SIZE);
        
        // 缓存结果
        cacheService.set(CACHE_KEY_HOT, ranking, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        return ranking;
    }
    
    /**
     * 获取新增专栏排行榜
     * 按创建时间倒序
     */
    public List<ColumnVO> getNewRanking() {
        List<ColumnVO> cached = cacheService.get(CACHE_KEY_NEW);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        
        List<ColumnVO> ranking = columnRepository.findNewColumns(RANKING_SIZE);
        cacheService.set(CACHE_KEY_NEW, ranking, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        return ranking;
    }
    
    /**
     * 获取订阅最多专栏排行榜
     */
    public List<ColumnVO> getSubscribeRanking() {
        List<ColumnVO> cached = cacheService.get(CACHE_KEY_SUBSCRIBE);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        
        List<ColumnVO> ranking = columnRepository.findMostSubscribedColumns(RANKING_SIZE);
        cacheService.set(CACHE_KEY_SUBSCRIBE, ranking, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        return ranking;
    }
    
    /**
     * 每小时更新一次排行榜缓存
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshRankingCache() {
        log.info("开始刷新专栏排行榜缓存");
        
        try {
            // 清除旧缓存
            cacheService.delete(CACHE_KEY_HOT);
            cacheService.delete(CACHE_KEY_NEW);
            cacheService.delete(CACHE_KEY_SUBSCRIBE);
            
            // 预热新缓存
            getHotRanking();
            getNewRanking();
            getSubscribeRanking();
            
            log.info("专栏排行榜缓存刷新完成");
        } catch (Exception e) {
            log.error("刷新专栏排行榜缓存失败", e);
        }
    }
    
    /**
     * 手动刷新排行榜
     */
    public void manualRefresh() {
        refreshRankingCache();
    }
}
