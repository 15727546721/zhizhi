package cn.xu.service.column;

import cn.xu.model.entity.Column;
import cn.xu.model.vo.column.ColumnDetailVO;
import cn.xu.model.vo.column.ColumnPostVO;
import cn.xu.model.vo.column.ColumnVO;
import cn.xu.repository.ColumnRepository;
import cn.xu.repository.UserRepository;
import cn.xu.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专栏应用服务
 * 提供专栏广场、搜索、推荐等聚合查询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnApplicationService {

    private final ColumnRepository columnRepository;
    private final ColumnSubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final ColumnPostService columnPostService;
    private final cn.xu.repository.PostRepository postRepository;

    /**
     * 专栏广场 - 热门专栏
     */
    public PageResponse<List<ColumnVO>> getHotColumns(String sortType, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        int offset = (page - 1) * size;
        
        List<Column> columns;
        if ("subscribe".equals(sortType)) {
            // 按订阅数排序
            columns = columnRepository.findPublishedBySubscribeCount(offset, size);
        } else {
            // 默认按最后发文时间排序
            columns = columnRepository.findPublishedByLastPostTime(offset, size);
        }
        
        int total = columnRepository.countPublished();
        
        List<ColumnVO> voList = convertToVOList(columns, null);
        
        return PageResponse.ofList(page, size, (long) total, voList);
    }

    /**
     * 专栏广场 - 搜索专栏
     */
    public PageResponse<List<ColumnVO>> searchColumns(String keyword, Integer page, Integer size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return PageResponse.emptyList(page != null ? page : 1, size != null ? size : 20);
        }
        
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
        int offset = (page - 1) * size;
        
        List<Column> columns = columnRepository.searchByKeyword(keyword.trim(), offset, size);
        int total = columnRepository.countSearchByKeyword(keyword.trim());
        
        List<ColumnVO> voList = convertToVOList(columns, null);
        
        return PageResponse.ofList(page, size, (long) total, voList);
    }

    /**
     * 推荐专栏
     */
    public List<ColumnVO> getRecommendedColumns(Integer limit) {
        if (limit == null || limit < 1) limit = 10;
        
        List<Column> columns = columnRepository.findRecommended(limit);
        return convertToVOList(columns, null);
    }

    /**
     * 获取专栏详情(包含统计信息)
     */
    public ColumnDetailVO getColumnDetail(Long columnId, Long currentUserId) {
        Column column = columnRepository.findById(columnId);
        if (column == null) {
            return null;
        }
        
        // 检查访问权限
        if (!column.isPublished() && !column.getUserId().equals(currentUserId)) {
            return null;
        }
        
        ColumnDetailVO vo = convertToDetailVO(column, currentUserId);
        return vo;
    }

    /**
     * 获取用户主页的专栏列表
     */
    public List<ColumnVO> getUserProfileColumns(Long userId, Long currentUserId) {
        List<Column> columns;
        
        if (userId.equals(currentUserId)) {
            // 查看自己的主页,显示所有专栏
            columns = columnRepository.findByUserId(userId);
        } else {
            // 查看他人主页,只显示已发布的专栏
            columns = columnRepository.findByUserIdAndStatus(userId, Column.STATUS_PUBLISHED);
        }
        
        return convertToVOList(columns, currentUserId);
    }

    /**
     * 获取文章所属的专栏列表
     */
    public List<ColumnVO> getPostColumns(Long postId, Long currentUserId) {
        try {
            // 通过专栏-文章关联表查询文章所属的专栏
            List<Column> columns = columnRepository.findByPostId(postId);
            
            // 过滤权限：只返回已发布的专栏，或者当前用户自己的专栏
            List<Column> accessibleColumns = columns.stream()
                    .filter(column -> column.isPublished() || 
                            (currentUserId != null && column.getUserId().equals(currentUserId)))
                    .collect(Collectors.toList());
            
            return convertToVOList(accessibleColumns, currentUserId);
        } catch (Exception e) {
            log.error("获取文章专栏列表失败: postId={}", postId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取专栏的文章列表（带详情）
     */
    public List<ColumnPostVO> getColumnPostsWithDetails(Long columnId, Integer page, Integer size) {
        try {
            // 1. 获取专栏文章关联
            List<cn.xu.model.entity.ColumnPost> columnPosts = columnPostService.getColumnPosts(columnId, page, size);
            
            if (columnPosts.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 批量查询文章详情
            List<Long> postIds = columnPosts.stream()
                    .map(cn.xu.model.entity.ColumnPost::getPostId)
                    .collect(Collectors.toList());
            
            Map<Long, cn.xu.model.entity.Post> postMap = new java.util.HashMap<>();
            for (Long postId : postIds) {
                postRepository.findById(postId).ifPresent(post -> postMap.put(postId, post));
            }
            
            // 3. 转换为VO
            return columnPosts.stream()
                    .map(cp -> {
                        cn.xu.model.entity.Post post = postMap.get(cp.getPostId());
                        if (post == null) {
                            return null;
                        }
                        
                        return ColumnPostVO.builder()
                                .postId(post.getId())
                                .title(post.getTitle())
                                .description(post.getDescription())
                                .coverUrl(post.getCoverUrl())
                                .viewCount(post.getViewCount() != null ? post.getViewCount().intValue() : 0)
                                .likeCount(post.getLikeCount() != null ? post.getLikeCount().intValue() : 0)
                                .commentCount(post.getCommentCount() != null ? post.getCommentCount().intValue() : 0)
                                .sort(cp.getSort())
                                .createTime(post.getCreateTime())
                                .build();
                    })
                    .filter(vo -> vo != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取专栏文章列表失败: columnId={}", columnId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取文章在专栏中的导航信息（上一篇/下一篇）
     */
    public cn.xu.model.vo.column.ColumnPostNavigationVO getPostNavigation(Long columnId, Long postId) {
        try {
            // 1. 获取上一篇和下一篇
            cn.xu.model.entity.ColumnPost previous = columnPostService.getPreviousPost(columnId, postId);
            cn.xu.model.entity.ColumnPost next = columnPostService.getNextPost(columnId, postId);
            
            // 2. 构建导航VO
            cn.xu.model.vo.column.ColumnPostNavigationVO.ColumnPostNavigationVOBuilder builder = 
                cn.xu.model.vo.column.ColumnPostNavigationVO.builder();
            
            // 3. 设置上一篇信息
            if (previous != null) {
                builder.previousPostId(previous.getPostId());
                postRepository.findById(previous.getPostId()).ifPresent(post -> {
                    builder.previousPostTitle(post.getTitle());
                });
            }
            
            // 4. 设置下一篇信息
            if (next != null) {
                builder.nextPostId(next.getPostId());
                postRepository.findById(next.getPostId()).ifPresent(post -> {
                    builder.nextPostTitle(post.getTitle());
                });
            }
            
            return builder.build();
        } catch (Exception e) {
            log.error("获取文章导航失败: columnId={}, postId={}", columnId, postId, e);
            return cn.xu.model.vo.column.ColumnPostNavigationVO.builder().build();
        }
    }

    /**
     * 获取用户订阅的专栏列表
     */
    public PageResponse<List<ColumnVO>> getUserSubscriptions(Long userId, Integer page, Integer size) {
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 20;
            int offset = (page - 1) * size;
            
            // 1. 获取用户订阅的专栏ID列表
            List<Long> columnIds = subscriptionService.getUserSubscribedColumnIds(userId, offset, size);
            
            if (columnIds.isEmpty()) {
                return PageResponse.emptyList(page, size);
            }
            
            // 2. 批量查询专栏信息
            List<Column> columns = columnRepository.findByIds(columnIds);
            
            // 3. 按订阅时间排序（保持原顺序）
            Map<Long, Integer> orderMap = new java.util.HashMap<>();
            for (int i = 0; i < columnIds.size(); i++) {
                orderMap.put(columnIds.get(i), i);
            }
            columns.sort((a, b) -> {
                Integer orderA = orderMap.getOrDefault(a.getId(), Integer.MAX_VALUE);
                Integer orderB = orderMap.getOrDefault(b.getId(), Integer.MAX_VALUE);
                return orderA.compareTo(orderB);
            });
            
            // 4. 转换为VO
            List<ColumnVO> voList = convertToVOList(columns, userId);
            
            // 5. 获取总数
            int total = subscriptionService.getUserSubscriptionCount(userId);
            
            return PageResponse.ofList(page, size, (long) total, voList);
        } catch (Exception e) {
            log.error("获取用户订阅列表失败: userId={}", userId, e);
            return PageResponse.emptyList(page != null ? page : 1, size != null ? size : 20);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换为VO列表
     */
    private List<ColumnVO> convertToVOList(List<Column> columns, Long currentUserId) {
        if (columns == null || columns.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询订阅状态
        List<Long> subscribedIds = new ArrayList<>();
        if (currentUserId != null) {
            List<Long> columnIds = columns.stream().map(Column::getId).collect(Collectors.toList());
            subscribedIds = subscriptionService.batchCheckSubscribed(currentUserId, columnIds);
        }
        
        final List<Long> finalSubscribedIds = subscribedIds;
        
        return columns.stream()
                .map(column -> convertToVO(column, currentUserId, finalSubscribedIds.contains(column.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private ColumnVO convertToVO(Column column, Long currentUserId, boolean isSubscribed) {
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
        vo.setIsRecommended(column.getIsRecommended() == 1);  // 设置推荐状态
        
        // 设置订阅状态
        if (currentUserId != null) {
            vo.setIsSubscribed(isSubscribed);
        }
        
        // 查询用户信息
        userRepository.findById(column.getUserId()).ifPresent(user -> {
            vo.setUserName(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        });
        
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private ColumnDetailVO convertToDetailVO(Column column, Long currentUserId) {
        ColumnDetailVO vo = new ColumnDetailVO();
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
        
        // 设置订阅状态
        if (currentUserId != null) {
            vo.setIsSubscribed(subscriptionService.isSubscribed(currentUserId, column.getId()));
            vo.setIsOwner(column.getUserId().equals(currentUserId));
        }
        
        // 查询用户信息
        userRepository.findById(column.getUserId()).ifPresent(user -> {
            vo.setUserName(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        });
        
        // TODO: 查询最近文章(前5篇) - 需要在后续实现
        vo.setRecentPosts(new ArrayList<>());
        
        return vo;
    }
}
