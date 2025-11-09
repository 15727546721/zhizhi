package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.follow.model.entity.FollowRelationEntity;
import cn.xu.domain.follow.service.IFollowService;
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
    @ApiOperationLog(description = "关注用户")
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
    @ApiOperationLog(description = "取消关注")
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
    @ApiOperationLog(description = "查询是否关注某用户")
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
    @ApiOperationLog(description = "获取我的关注列表")
    public ResponseEntity<List<FollowRelationEntity>> getFollowingList(
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取关注列表，页码：{}，每页数量：{}", pageNum, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<FollowRelationEntity> followingList = userFollowService.getFollowingList(currentUserId, pageNum, pageSize);
        return ResponseEntity.<List<FollowRelationEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followingList)
                .build();
    }

    @Operation(summary = "获取指定用户的关注列表")
    @GetMapping("/following/{userId}")
    @ApiOperationLog(description = "获取指定用户的关注列表")
    public ResponseEntity<List<FollowRelationEntity>> getUserFollowingList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取指定用户的关注列表，用户ID：{}，页码：{}，每页数量：{}", userId, pageNum, pageSize);
        if (userId == null || userId <= 0) {
            return ResponseEntity.<List<FollowRelationEntity>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("用户ID不能为空")
                    .build();
        }
        List<FollowRelationEntity> followingList = userFollowService.getFollowingList(userId, pageNum, pageSize);
        return ResponseEntity.<List<FollowRelationEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followingList)
                .build();
    }

    @Operation(summary = "获取我的粉丝列表")
    @GetMapping("/followers")
    @ApiOperationLog(description = "获取我的粉丝列表")
    public ResponseEntity<List<FollowRelationEntity>> getFollowersList(
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取粉丝列表，页码：{}，每页数量：{}", pageNum, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<FollowRelationEntity> followersList = userFollowService.getFollowersList(currentUserId, pageNum, pageSize);
        return ResponseEntity.<List<FollowRelationEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followersList)
                .build();
    }

    @Operation(summary = "获取指定用户的粉丝列表")
    @GetMapping("/followers/{userId}")
    @ApiOperationLog(description = "获取指定用户的粉丝列表")
    public ResponseEntity<List<FollowRelationEntity>> getUserFollowersList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取指定用户的粉丝列表，用户ID：{}，页码：{}，每页数量：{}", userId, pageNum, pageSize);
        if (userId == null || userId <= 0) {
            return ResponseEntity.<List<FollowRelationEntity>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("用户ID不能为空")
                    .build();
        }
        List<FollowRelationEntity> followersList = userFollowService.getFollowersList(userId, pageNum, pageSize);
        return ResponseEntity.<List<FollowRelationEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followersList)
                .build();
    }

    @Operation(summary = "获取我的关注数量")
    @GetMapping("/following/count")
    @ApiOperationLog(description = "获取我的关注数量")
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
    @ApiOperationLog(description = "获取我的粉丝数量")
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
    @ApiOperationLog(description = "获取指定用户的关注数量")
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
    @ApiOperationLog(description = "获取指定用户的粉丝数量")
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