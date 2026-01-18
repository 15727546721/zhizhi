package cn.xu.service.column;

import cn.xu.model.entity.Column;
import cn.xu.repository.ColumnRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专栏领域服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnService {

    private final ColumnRepository columnRepository;
    
    private static final int MAX_COLUMN_COUNT = 10;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    /**
     * 创建专栏
     */
    @Transactional(rollbackFor = Exception.class)
    public Column createColumn(Long userId, String name, String description, String coverUrl, Integer status) {
        // 1. 验证用户专栏数量
        int count = columnRepository.countByUserId(userId);
        if (count >= MAX_COLUMN_COUNT) {
            throw new BusinessException("专栏数量已达上限(" + MAX_COLUMN_COUNT + "个)");
        }
        
        // 2. 验证名称
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("专栏名称不能为空");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException("专栏名称不能超过" + MAX_NAME_LENGTH + "个字符");
        }
        
        // 3. 验证描述
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException("专栏描述不能超过" + MAX_DESCRIPTION_LENGTH + "个字符");
        }
        
        // 4. 创建专栏
        Column column = Column.create(userId, name.trim(), description, coverUrl, status);
        columnRepository.save(column);
        
        log.info("[专栏] 创建成功 - userId: {}, columnId: {}, name: {}, status: {}", 
                userId, column.getId(), name, status);
        
        return column;
    }

    /**
     * 更新专栏
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateColumn(Long userId, Long columnId, String name, String description, 
                            String coverUrl, Integer status) {
        // 1. 查询专栏
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        
        // 2. 验证权限
        if (!column.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此专栏");
        }
        
        // 3. 验证并更新名称
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() > MAX_NAME_LENGTH) {
                throw new BusinessException("专栏名称不能超过" + MAX_NAME_LENGTH + "个字符");
            }
            column.setName(name.trim());
        }
        
        // 4. 验证并更新描述
        if (description != null) {
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                throw new BusinessException("专栏描述不能超过" + MAX_DESCRIPTION_LENGTH + "个字符");
            }
            column.setDescription(description);
        }
        
        // 5. 更新封面和状态
        if (coverUrl != null) {
            column.setCoverUrl(coverUrl);
        }
        if (status != null) {
            column.setStatus(status);
        }
        
        column.setUpdateTime(LocalDateTime.now());
        columnRepository.update(column);
        
        log.info("[专栏] 更新成功 - userId: {}, columnId: {}", userId, columnId);
    }

    /**
     * 删除专栏
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteColumn(Long userId, Long columnId) {
        // 1. 查询专栏
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            throw new BusinessException("专栏不存在");
        }
        
        // 2. 验证权限
        if (!column.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此专栏");
        }
        
        // 3. 删除专栏（注意：关联的文章不会被删除，只是解除关联）
        columnRepository.deleteById(columnId);
        
        log.info("[专栏] 删除成功 - userId: {}, columnId: {}, name: {}", 
                userId, columnId, column.getName());
    }

    /**
     * 获取专栏详情
     */
    public Column getColumnById(Long columnId) {
        return columnRepository.findById(columnId);
    }

    /**
     * 获取用户的所有专栏
     */
    public List<Column> getUserColumns(Long userId, boolean includePrivate) {
        if (includePrivate) {
            return columnRepository.findByUserId(userId);
        } else {
            return columnRepository.findByUserIdAndStatus(userId, Column.STATUS_PUBLISHED);
        }
    }

    /**
     * 增加文章计数
     */
    public void incrementPostCount(Long columnId) {
        if (columnId != null) {
            columnRepository.incrementPostCount(columnId);
            log.debug("[专栏] 增加文章计数 - columnId: {}", columnId);
        }
    }

    /**
     * 减少文章计数
     */
    public void decrementPostCount(Long columnId) {
        if (columnId != null) {
            columnRepository.decrementPostCount(columnId);
            log.debug("[专栏] 减少文章计数 - columnId: {}", columnId);
        }
    }

    /**
     * 增加订阅计数
     */
    public void incrementSubscribeCount(Long columnId) {
        if (columnId != null) {
            columnRepository.incrementSubscribeCount(columnId);
            log.debug("[专栏] 增加订阅计数 - columnId: {}", columnId);
        }
    }

    /**
     * 减少订阅计数
     */
    public void decrementSubscribeCount(Long columnId) {
        if (columnId != null) {
            columnRepository.decrementSubscribeCount(columnId);
            log.debug("[专栏] 减少订阅计数 - columnId: {}", columnId);
        }
    }

    /**
     * 更新最后发文时间
     */
    public void updateLastPostTime(Long columnId, LocalDateTime time) {
        if (columnId != null && time != null) {
            columnRepository.updateLastPostTime(columnId, time);
            log.debug("[专栏] 更新最后发文时间 - columnId: {}, time: {}", columnId, time);
        }
    }

    /**
     * 检查用户是否有权访问专栏
     */
    public boolean canAccessColumn(Long userId, Long columnId) {
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            return false;
        }
        // 自己的专栏或已发布的专栏
        return column.getUserId().equals(userId) || column.isPublished();
    }
}
