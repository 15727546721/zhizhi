package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Column;
import cn.xu.model.vo.column.ColumnVO;
import cn.xu.repository.ColumnRepository;
import cn.xu.repository.UserRepository;
import cn.xu.service.column.ColumnService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专栏管理控制器（管理端）
 * <p>
 * 提供后台专栏管理功能：
 * <ul>
 *   <li>专栏列表查看、搜索、筛选</li>
 *   <li>违规处理：删除、归档专栏</li>
 *   <li>推荐管理：设置/取消推荐</li>
 *   <li>数据统计</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/system/column")
@Tag(name = "专栏管理", description = "后台专栏管理接口")
@RequiredArgsConstructor
public class SysColumnController {

    private final ColumnRepository columnRepository;
    private final ColumnService columnService;
    private final UserRepository userRepository;

    /**
     * 获取专栏列表（分页、搜索、筛选）
     */
    @GetMapping("/list")
    @Operation(summary = "获取专栏列表")
    @SaCheckLogin
    @SaCheckPermission("system:column:list")
    @ApiOperationLog(description = "获取专栏列表")
    public ResponseEntity<PageResponse<List<ColumnVO>>> getColumnList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选: 0-草稿 1-已发布 2-已归档") @RequestParam(required = false) Integer status,
            @Parameter(description = "是否推荐: 0-否 1-是") @RequestParam(required = false) Integer isRecommended,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId) {
        log.info("[管理后台] 获取专栏列表: page={}, size={}, keyword={}, status={}, isRecommended={}, userId={}", 
                page, size, keyword, status, isRecommended, userId);
        try {
            int offset = (page - 1) * size;
            
            // 构建查询条件
            List<Column> columns;
            int total;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 搜索模式
                columns = columnRepository.searchByKeyword(keyword.trim(), offset, size);
                total = columnRepository.countSearchByKeyword(keyword.trim());
            } else {
                // 列表模式（支持筛选）
                columns = columnRepository.findByConditions(status, isRecommended, userId, offset, size);
                total = columnRepository.countByConditions(status, isRecommended, userId);
            }
            
            // 转换为VO
            List<ColumnVO> voList = columns.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.<PageResponse<List<ColumnVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(PageResponse.ofList(page, size, (long) total, voList))
                    .build();
        } catch (Exception e) {
            log.error("获取专栏列表失败", e);
            return ResponseEntity.<PageResponse<List<ColumnVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取专栏列表失败")
                    .build();
        }
    }

    /**
     * 获取专栏统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取专栏统计数据")
    @SaCheckLogin
    @SaCheckPermission("system:column:list")
    @ApiOperationLog(description = "获取专栏统计数据")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("[管理后台] 获取专栏统计数据");
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 总数统计
            stats.put("totalCount", columnRepository.countAll());
            stats.put("publishedCount", columnRepository.countByStatus(Column.STATUS_PUBLISHED));
            stats.put("draftCount", columnRepository.countByStatus(Column.STATUS_DRAFT));
            stats.put("archivedCount", columnRepository.countByStatus(Column.STATUS_ARCHIVED));
            stats.put("recommendedCount", columnRepository.countRecommended());
            
            // 订阅数统计
            stats.put("totalSubscriptions", columnRepository.sumSubscribeCount());
            
            return ResponseEntity.<Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(stats)
                    .build();
        } catch (Exception e) {
            log.error("获取专栏统计数据失败", e);
            return ResponseEntity.<Map<String, Object>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取统计数据失败")
                    .build();
        }
    }

    /**
     * 删除专栏（违规处理）
     */
    @PostMapping("/{id}/delete")
    @Operation(summary = "删除专栏")
    @SaCheckLogin
    @SaCheckPermission("system:column:delete")
    @ApiOperationLog(description = "删除专栏")
    public ResponseEntity<Void> deleteColumn(
            @Parameter(description = "专栏ID") @PathVariable Long id) {
        try {
            Column column = columnRepository.findById(id);
            if (column == null) {
                return ResponseEntity.<Void>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("专栏不存在")
                        .build();
            }
            
            columnService.deleteColumn(id, column.getUserId());
            
            log.info("管理员删除专栏成功: columnId={}", id);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("删除专栏失败: columnId={}", id, e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除失败")
                    .build();
        }
    }

    /**
     * 归档专栏
     */
    @PostMapping("/{id}/archive")
    @Operation(summary = "归档专栏")
    @SaCheckLogin
    @SaCheckPermission("system:column:update")
    @ApiOperationLog(description = "归档专栏")
    public ResponseEntity<Void> archiveColumn(
            @Parameter(description = "专栏ID") @PathVariable Long id) {
        try {
            Column column = columnRepository.findById(id);
            if (column == null) {
                return ResponseEntity.<Void>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("专栏不存在")
                        .build();
            }
            
            columnRepository.updateStatus(id, Column.STATUS_ARCHIVED);
            
            log.info("管理员归档专栏成功: columnId={}", id);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("归档成功")
                    .build();
        } catch (Exception e) {
            log.error("归档专栏失败: columnId={}", id, e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("归档失败")
                    .build();
        }
    }

    /**
     * 设置推荐
     */
    @PostMapping("/{id}/recommend")
    @Operation(summary = "设置推荐")
    @SaCheckLogin
    @SaCheckPermission("system:column:update")
    @ApiOperationLog(description = "设置专栏推荐")
    public ResponseEntity<Void> setRecommend(
            @Parameter(description = "专栏ID") @PathVariable Long id,
            @Parameter(description = "是否推荐: 0-否 1-是") @RequestParam Integer isRecommended) {
        try {
            Column column = columnRepository.findById(id);
            if (column == null) {
                return ResponseEntity.<Void>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("专栏不存在")
                        .build();
            }
            
            if (isRecommended != 0 && isRecommended != 1) {
                return ResponseEntity.<Void>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("参数错误")
                        .build();
            }
            
            columnRepository.updateRecommended(id, isRecommended);
            
            log.info("管理员设置专栏推荐: columnId={}, isRecommended={}", id, isRecommended);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(isRecommended == 1 ? "设置推荐成功" : "取消推荐成功")
                    .build();
        } catch (Exception e) {
            log.error("设置专栏推荐失败: columnId={}", id, e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("操作失败")
                    .build();
        }
    }

    /**
     * 批量删除专栏
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除专栏")
    @SaCheckLogin
    @SaCheckPermission("system:column:delete")
    @ApiOperationLog(description = "批量删除专栏")
    public ResponseEntity<Void> batchDelete(
            @Parameter(description = "专栏ID列表") @RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.<Void>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("请选择要删除的专栏")
                        .build();
            }
            
            int successCount = 0;
            for (Long id : ids) {
                try {
                    Column column = columnRepository.findById(id);
                    if (column != null) {
                        columnService.deleteColumn(id, column.getUserId());
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("删除专栏失败: columnId={}", id, e);
                }
            }
            
            log.info("管理员批量删除专栏: 成功{}个, 总共{}个", successCount, ids.size());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(String.format("成功删除%d个专栏", successCount))
                    .build();
        } catch (Exception e) {
            log.error("批量删除专栏失败", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("批量删除失败")
                    .build();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换为VO
     */
    private ColumnVO convertToVO(Column column) {
        ColumnVO vo = new ColumnVO();
        vo.setId(column.getId());
        vo.setUserId(column.getUserId());
        vo.setName(column.getName());
        vo.setDescription(column.getDescription());
        vo.setCoverUrl(column.getCoverUrl());
        vo.setStatus(column.getStatus());
        vo.setPostCount(column.getPostCount());
        vo.setSubscribeCount(column.getSubscribeCount());
        vo.setLastPostTime(column.getLastPostTime());
        vo.setCreateTime(column.getCreateTime());
        vo.setIsRecommended(column.getIsRecommended() == 1);
        
        // 查询用户信息
        userRepository.findById(column.getUserId()).ifPresent(user -> {
            vo.setUserName(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        });
        
        return vo;
    }
}
