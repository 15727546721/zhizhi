package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.follow.model.entity.UserFollowEntity;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "用户关注关系接口")
@RestController
@RequestMapping("/api/follows")
@Validated
@Slf4j
public class FollowController {

    @Resource
    private IFollowService userFollowService;

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{userId}")
    @SaCheckLogin
    public ResponseEntity<Void> follow(@PathVariable Long userId) {
        log.info("关注用户，请求参数：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        userFollowService.follow(currentUserId, userId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("关注成功")
                .build();
    }

    @Operation(summary = "取消关注")
    @PostMapping("/unfollow/{userId}")
    @SaCheckLogin
    public ResponseEntity<Void> unfollow(
            @Parameter(description = "被关注用户ID", required = true)
            @PathVariable Long userId) {
        log.info("取消关注，被关注用户ID：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        userFollowService.unfollow(currentUserId, userId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("取消关注成功")
                .build();
    }

    @Operation(summary = "查询是否关注某用户")
    @GetMapping("/status/{userId}")
    public ResponseEntity<Boolean> isFollowing(
            @Parameter(description = "被关注用户ID", required = true)
            @PathVariable Long userId) {
        log.info("查询关注状态，被关注用户ID：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        boolean following = userFollowService.isFollowing(currentUserId, userId);
        return ResponseEntity.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(following)
                .build();
    }

    @Operation(summary = "获取我的关注列表")
    @GetMapping("/following")
    public ResponseEntity<List<UserFollowEntity>> getFollowingList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取关注列表，页码：{}，每页数量：{}", pageNum, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<UserFollowEntity> followingList = userFollowService.getFollowingList(currentUserId);
        return ResponseEntity.<List<UserFollowEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followingList)
                .build();
    }

    @Operation(summary = "获取我的粉丝列表")
    @GetMapping("/followers")
    public ResponseEntity<List<UserFollowEntity>> getFollowersList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取粉丝列表，页码：{}，每页数量：{}", pageNum, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<UserFollowEntity> followersList = userFollowService.getFollowersList(currentUserId);
        return ResponseEntity.<List<UserFollowEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followersList)
                .build();
    }

    @Operation(summary = "获取我的关注数量")
    @GetMapping("/following/count")
    public ResponseEntity<Integer> getFollowingCount() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取关注数量，用户ID：{}", currentUserId);
        int count = userFollowService.getFollowingCount(currentUserId);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @Operation(summary = "获取我的粉丝数量")
    @GetMapping("/followers/count")
    public ResponseEntity<Integer> getFollowersCount() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取粉丝数量，用户ID：{}", currentUserId);
        int count = userFollowService.getFollowersCount(currentUserId);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @Operation(summary = "获取指定用户的关注数量")
    @GetMapping("/following/count/{userId}")
    public ResponseEntity<Integer> getUserFollowingCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取指定用户关注数量，用户ID：{}", userId);
        int count = userFollowService.getFollowingCount(userId);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @Operation(summary = "获取指定用户的粉丝数量")
    @GetMapping("/followers/count/{userId}")
    public ResponseEntity<Integer> getUserFollowersCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取指定用户粉丝数量，用户ID：{}", userId);
        int count = userFollowService.getFollowersCount(userId);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }
} 