package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.vo.topic.TopicDetailVO;
import cn.xu.api.web.model.vo.user.UserTopicItemVO;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.entity.TopicEntity;
import cn.xu.domain.post.repository.IPostTopicRepository;
import cn.xu.domain.post.service.IPostTopicService;
import cn.xu.domain.post.service.ITopicFollowService;
import cn.xu.domain.post.service.ITopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题控制器
 * 提供话题相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/topic")
@Tag(name = "话题接口", description = "话题相关接口")
public class TopicController {

    @Resource
    private ITopicService topicService;
    @Resource
    private IPostTopicService postTopicService;
    @Resource
    private IPostTopicRepository postTopicRepository;
    @Resource
    private ITopicFollowService topicFollowService;

    /**
     * 创建话题
     *
     * @param topicName 话题名称
     * @return 响应结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建话题")
    @ApiOperationLog(description = "创建话题")
    public ResponseEntity createTopic(@Parameter(description = "话题名称") @RequestParam String topicName) {
        try {
            topicService.addTopic(topicName);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("创建话题成功")
                    .build();
        } catch (Exception e) {
            log.error("创建话题失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("创建话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取话题详情
     *
     * @param id 话题ID
     * @return 话题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取话题详情")
    @ApiOperationLog(description = "获取话题详情")
    public ResponseEntity<TopicDetailVO> getTopicDetail(@Parameter(description = "话题ID") @PathVariable Long id) {
        try {
            TopicEntity topic = topicService.getTopicById(id);
            if (topic == null) {
                return ResponseEntity.<TopicDetailVO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("话题不存在")
                        .build();
            }
            
            Long followerCount = topicFollowService.getTopicFollowerCount(id);
            TopicDetailVO vo = TopicDetailVO.from(topic, followerCount);
            
            return ResponseEntity.<TopicDetailVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取话题详情成功")
                    .data(vo)
                    .build();
        } catch (Exception e) {
            log.error("获取话题详情失败", e);
            return ResponseEntity.<TopicDetailVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取话题详情失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 搜索话题
     *
     * @param keyword 搜索关键词
     * @return 话题列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索话题")
    @ApiOperationLog(description = "搜索话题")
    public ResponseEntity<List<TopicEntity>> searchTopics(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<TopicEntity> topics = topicService.searchTopics(keyword);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索话题成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("搜索话题失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取热门话题
     *
     * @param limit 限制数量
     * @return 热门话题列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门话题")
    @ApiOperationLog(description = "获取热门话题")
    public ResponseEntity<List<TopicEntity>> getHotTopics(@Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<TopicEntity> topics = topicService.getHotTopics(limit);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取热门话题成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("获取热门话题失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取热门话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取所有话题（分页）
     *
     * @param pageRequest 分页请求
     * @return 话题列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有话题")
    @ApiOperationLog(description = "获取所有话题")
    public ResponseEntity<List<TopicEntity>> getAllTopics(PageRequest pageRequest) {
        try {
            List<TopicEntity> topics = topicService.getAllTopics();
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取话题列表成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("获取话题列表失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取话题列表失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取帖子相关的话题详情
     *
     * @param postId 帖子ID
     * @return 话题详情列表
     */
    @GetMapping("/post/{postId}")
    @Operation(summary = "获取帖子相关的话题详情")
    @ApiOperationLog(description = "获取帖子相关的话题详情")
    public ResponseEntity<List<TopicEntity>> getTopicsByPostId(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        try {
            // 获取帖子关联的话题ID列表
            List<Long> topicIds = postTopicService.getTopicsByPostId(postId);
            
            // 根据话题ID获取话题详情
            List<TopicEntity> topics = new ArrayList<>();
            for (Long topicId : topicIds) {
                TopicEntity topic = topicService.getTopicById(topicId);
                if (topic != null) {
                    topics.add(topic);
                }
            }
            
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取帖子话题成功")
                    .data(topics)
                    .build();
        } catch (Exception e) {
            log.error("获取帖子话题失败", e);
            return ResponseEntity.<List<TopicEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 为帖子设置话题
     *
     * @param postId 帖子ID
     * @param topicIds 话题ID列表
     * @return 响应结果
     */
    @PostMapping("/post/{postId}")
    @Operation(summary = "为帖子设置话题")
    @ApiOperationLog(description = "为帖子设置话题")
    public ResponseEntity setPostTopics(@Parameter(description = "帖子ID") @PathVariable Long postId, @Parameter(description = "话题ID列表") @RequestBody List<Long> topicIds) {
        try {
            postTopicService.savePostTopics(postId, topicIds);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("设置帖子话题成功")
                    .build();
        } catch (Exception e) {
            log.error("设置帖子话题失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("设置帖子话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 关注话题
     *
     * @param topicId 话题ID
     * @return 响应结果
     */
    @PostMapping("/follow/{topicId}")
    @Operation(summary = "关注话题")
    @ApiOperationLog(description = "关注话题")
    public ResponseEntity followTopic(@Parameter(description = "话题ID") @PathVariable Long topicId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            topicFollowService.follow(userId, topicId);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("关注话题成功")
                    .build();
        } catch (Exception e) {
            log.error("关注话题失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("关注话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 取消关注话题
     *
     * @param topicId 话题ID
     * @return 响应结果
     */
    @PostMapping("/unfollow/{topicId}")
    @Operation(summary = "取消关注话题")
    @ApiOperationLog(description = "取消关注话题")
    public ResponseEntity unfollowTopic(@Parameter(description = "话题ID") @PathVariable Long topicId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            topicFollowService.unfollow(userId, topicId);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消关注话题成功")
                    .build();
        } catch (Exception e) {
            log.error("取消关注话题失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("取消关注话题失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 检查是否已关注话题
     *
     * @param topicId 话题ID
     * @return 是否已关注
     */
    @GetMapping("/isFollowing/{topicId}")
    @Operation(summary = "检查是否已关注话题")
    @ApiOperationLog(description = "检查是否已关注话题")
    public ResponseEntity<Boolean> isFollowingTopic(@Parameter(description = "话题ID") @PathVariable Long topicId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean isFollowing = topicFollowService.isFollowing(userId, topicId);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取关注状态成功")
                    .data(isFollowing)
                    .build();
        } catch (Exception e) {
            log.error("获取关注状态失败", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取关注状态失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取用户话题列表
     *
     * @param userId 用户ID
     * @param pageRequest 分页请求
     * @return 用户话题列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户话题列表", description = "根据用户ID获取该用户参与的话题列表")
    @ApiOperationLog(description = "获取用户话题列表")
    public ResponseEntity<PageResponse<List<UserTopicItemVO>>> getUserTopics(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            PageRequest pageRequest) {
        try {
            log.info("获取用户话题列表，用户ID：{}", userId);
            
            int pageNo = pageRequest.getPageNo() != null ? pageRequest.getPageNo() : 1;
            int pageSize = pageRequest.getPageSize() != null ? pageRequest.getPageSize() : 10;
            int offset = (pageNo - 1) * pageSize;
            
            // 获取用户话题统计信息
            List<IPostTopicRepository.UserTopicStats> topicStatsList = 
                    postTopicRepository.getTopicStatsByUserId(userId, offset, pageSize);
            
            // 统计总数
            Long total = postTopicRepository.countTopicsByUserId(userId);
            
            // 转换为VO
            List<UserTopicItemVO> topicItemList = convertToUserTopicItemVOList(topicStatsList);
            
            // 构建分页响应
            PageResponse<List<UserTopicItemVO>> pageResponse = 
                    PageResponse.ofList(pageNo, pageSize, total != null ? total : 0L, topicItemList);
            
            return ResponseEntity.<PageResponse<List<UserTopicItemVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .info("获取用户话题列表成功")
                    .build();
                    
        } catch (Exception e) {
            log.error("获取用户话题列表失败，用户ID：{}", userId, e);
            return ResponseEntity.<PageResponse<List<UserTopicItemVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取用户话题列表失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 将话题统计信息转换为VO列表
     */
    private List<UserTopicItemVO> convertToUserTopicItemVOList(List<IPostTopicRepository.UserTopicStats> statsList) {
        if (statsList == null || statsList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return statsList.stream().map(stats -> {
            // 获取话题详情
            TopicEntity topic = topicService.getTopicById(stats.getTopicId());
            
            if (topic == null) {
                // 如果话题不存在，返回基本信息
                return UserTopicItemVO.builder()
                        .topicId(stats.getTopicId())
                        .topicName("未知话题")
                        .postCount(stats.getPostCount())
                        .lastPostTime(stats.getLastPostTime())
                        .totalUsageCount(0L)
                        .build();
            }
            
            return UserTopicItemVO.builder()
                    .topicId(topic.getId())
                    .topicName(topic.getName())
                    .topicDescription(topic.getDescription())
                    .postCount(stats.getPostCount())
                    .lastPostTime(stats.getLastPostTime())
                    .totalUsageCount(topic.getUsageCount() != null ? (long) topic.getUsageCount() : 0L)
                    .build();
        }).collect(Collectors.toList());
    }
}