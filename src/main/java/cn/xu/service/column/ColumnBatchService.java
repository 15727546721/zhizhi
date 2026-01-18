package cn.xu.service.column;

import cn.xu.model.vo.column.ColumnVO;
import cn.xu.repository.ColumnRepository;
import cn.xu.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 专栏批量查询优化服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnBatchService {
    
    private final ColumnRepository columnRepository;
    private final CacheService cacheService;
    
    private static final String CACHE_KEY_PREFIX = "column:detail:";
    private static final int CACHE_EXPIRE_MINUTES = 30;
    private static final int BATCH_SIZE = 100; // 单次批量查询最大数量
    
    /**
     * 批量获取专栏信息（带缓存优化）
     * 
     * @param columnIds 专栏ID列表
     * @return 专栏信息Map，key为专栏ID
     */
    public Map<Long, ColumnVO> batchGetColumns(List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 去重
        Set<Long> uniqueIds = new HashSet<>(columnIds);
        
        // 分批处理，避免一次查询过多
        if (uniqueIds.size() > BATCH_SIZE) {
            return batchGetColumnsInChunks(new ArrayList<>(uniqueIds));
        }
        
        Map<Long, ColumnVO> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();
        
        // 1. 先从缓存获取
        for (Long id : uniqueIds) {
            String cacheKey = CACHE_KEY_PREFIX + id;
            ColumnVO cached = cacheService.get(cacheKey);
            if (cached != null) {
                result.put(id, cached);
            } else {
                missedIds.add(id);
            }
        }
        
        log.debug("批量查询专栏: 总数={}, 缓存命中={}, 需查询={}", 
                uniqueIds.size(), result.size(), missedIds.size());
        
        // 2. 批量查询未命中的数据
        if (!missedIds.isEmpty()) {
            List<ColumnVO> columns = columnRepository.findByIds(missedIds);
            
            // 3. 更新缓存并添加到结果
            for (ColumnVO column : columns) {
                result.put(column.getId(), column);
                String cacheKey = CACHE_KEY_PREFIX + column.getId();
                cacheService.set(cacheKey, column, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
        }
        
        return result;
    }
    
    /**
     * 分批批量查询（处理大量ID的情况）
     */
    private Map<Long, ColumnVO> batchGetColumnsInChunks(List<Long> columnIds) {
        Map<Long, ColumnVO> result = new HashMap<>();
        
        // 分批处理
        for (int i = 0; i < columnIds.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, columnIds.size());
            List<Long> chunk = columnIds.subList(i, end);
            
            Map<Long, ColumnVO> chunkResult = batchGetColumns(chunk);
            result.putAll(chunkResult);
        }
        
        return result;
    }
    
    /**
     * 预热缓存 - 将热门专栏加载到缓存
     */
    public void warmUpCache() {
        log.info("开始预热专栏缓存");
        
        try {
            // 获取热门专栏ID
            List<Long> hotColumnIds = columnRepository.findHotColumnIds(100);
            
            // 批量查询并缓存
            batchGetColumns(hotColumnIds);
            
            log.info("专栏缓存预热完成，共预热 {} 个专栏", hotColumnIds.size());
        } catch (Exception e) {
            log.error("预热专栏缓存失败", e);
        }
    }
    
    /**
     * 清除专栏缓存
     */
    public void evictCache(Long columnId) {
        String cacheKey = CACHE_KEY_PREFIX + columnId;
        cacheService.delete(cacheKey);
        log.debug("已清除专栏缓存: columnId={}", columnId);
    }
    
    /**
     * 批量清除专栏缓存
     */
    public void batchEvictCache(List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return;
        }
        
        for (Long columnId : columnIds) {
            evictCache(columnId);
        }
        
        log.debug("已批量清除专栏缓存: count={}", columnIds.size());
    }
    
    /**
     * 获取专栏的订阅用户ID列表（批量优化）
     */
    public Map<Long, List<Long>> batchGetSubscriberIds(List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 使用IN查询一次性获取所有数据
        return columnRepository.findSubscriberIdsByColumnIds(columnIds);
    }
    
    /**
     * 批量检查用户是否订阅了专栏
     */
    public Map<Long, Boolean> batchCheckSubscription(Long userId, List<Long> columnIds) {
        if (columnIds == null || columnIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 一次查询获取用户订阅的所有专栏ID
        Set<Long> subscribedIds = columnRepository.findSubscribedColumnIds(userId, columnIds);
        
        // 构建结果Map
        return columnIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        subscribedIds::contains
                ));
    }
}
