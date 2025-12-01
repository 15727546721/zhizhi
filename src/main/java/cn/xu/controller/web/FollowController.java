package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.vo.FollowUserVO;
import cn.xu.service.follow.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 关注控制器
 * 
 * <p>提供用户关注、取消关注、关注列表、粉丝列表等功能接口
 * 
 * @author xu
 * @since 2025-11-25
 */
@Tag(name = "关注接口", description = "用户关注关系管理API")
@RestController
@RequestMapping("/api/follows")
@Validated
@Slf4j
public class FollowController {

    @Resource
    private FollowService followService;

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{userId}")
    @SaCheckLogin
    @ApiOperationLog(description = "关注用户")
    public ResponseEntity<Void> follow(@PathVariable Long userId) {
        log.info("关注用户，请求参数：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        followService.follow(currentUserId, userId); 
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
        followService.unfollow(currentUserId, userId); 
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("取消关注成功")
                .build();
    }

    @Operation(summary = "查询是否关注某用户")
    @GetMapping("/status/{userId}")
    @SaCheckLogin
    @ApiOperationLog(description = "查询是否关注某用户")
    public ResponseEntity<Boolean> isFollowing(
            @Parameter(description = "被关注用户ID", required = true)
            @PathVariable Long userId) {
        log.info("查询关注状态，被关注用户ID：{}", userId);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        boolean following = followService.isFollowed(currentUserId, userId);
        return ResponseEntity.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(following)
                .build();
    }

    @Operation(summary = "获取我的关注列表")
    @GetMapping("/following")
    @ApiOperationLog(description = "获取我的关注列表")
    public ResponseEntity<List<FollowUserVO>> getFollowingList(
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取关注列表，页码：{}，每页数量：{}", pageNum, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<FollowUserVO> followingList = followService.getFollowingListWithUserInfo(currentUserId, pageNum, pageSize);
        return ResponseEntity.<List<FollowUserVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followingList)
                .build();
    }

    @Operation(summary = "获取指定用户的关注列表")
    @GetMapping("/following/{userId}")
    @ApiOperationLog(description = "获取指定用户的关注列表")
    public ResponseEntity<List<FollowUserVO>> getUserFollowingList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取指定用户的关注列表，用户ID：{}，页码：{}，每页数量：{}", userId, pageNum, pageSize);
        if (userId == null || userId <= 0) {
            return ResponseEntity.<List<FollowUserVO>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("用户ID不能为空")
                    .build();
        }
        List<FollowUserVO> followingList = followService.getFollowingListWithUserInfo(userId, pageNum, pageSize);
        return ResponseEntity.<List<FollowUserVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followingList)
                .build();
    }

    @Operation(summary = "获取我的粉丝列表")
    @GetMapping("/followers")
    @ApiOperationLog(description = "获取我的粉丝列表")
    public ResponseEntity<List<FollowUserVO>> getFollowersList(
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取粉丝列表，页码：{}，每页数量：{}", pageNum, pageSize);
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<FollowUserVO> followersList = followService.getFollowersListWithUserInfo(currentUserId, pageNum, pageSize);
        return ResponseEntity.<List<FollowUserVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followersList)
                .build();
    }

    @Operation(summary = "获取指定用户的粉丝列表")
    @GetMapping("/followers/{userId}")
    @ApiOperationLog(description = "获取指定用户的粉丝列表")
    public ResponseEntity<List<FollowUserVO>> getUserFollowersList(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "页码") 
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") 
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取指定用户的粉丝列表，用户ID：{}，页码：{}，每页数量：{}", userId, pageNum, pageSize);
        if (userId == null || userId <= 0) {
            return ResponseEntity.<List<FollowUserVO>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("用户ID不能为空")
                    .build();
        }
        List<FollowUserVO> followersList = followService.getFollowersListWithUserInfo(userId, pageNum, pageSize);
        return ResponseEntity.<List<FollowUserVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(followersList)
                .build();
    }

    @Operation(summary = "获取我的关注数量")
    @GetMapping("/following/count")
    @ApiOperationLog(description = "获取我的关注数量")
    public ResponseEntity<Long> getFollowingCount() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取关注数量，用户ID：{}", currentUserId);
        Long count = followService.countFollowing(currentUserId);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @Operation(summary = "获取我的粉丝数量")
    @GetMapping("/followers/count")
    @ApiOperationLog(description = "获取我的粉丝数量")
    public ResponseEntity<Long> getFollowersCount() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取粉丝数量，用户ID：{}", currentUserId);
        Long count = followService.countFollowers(currentUserId);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @Operation(summary = "获取指定用户的关注数量")
    @GetMapping("/following/count/{userId}")
    @ApiOperationLog(description = "获取指定用户的关注数量")
    public ResponseEntity<Long> getUserFollowingCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取指定用户关注数量，用户ID：{}", userId);
        Long count = followService.countFollowing(userId);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @Operation(summary = "获取指定用户的粉丝数量")
    @GetMapping("/followers/count/{userId}")
    @ApiOperationLog(description = "获取指定用户的粉丝数量")
    public ResponseEntity<Long> getUserFollowersCount(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("获取指定用户粉丝数量，用户ID：{}", userId);
        Long count = followService.countFollowers(userId);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }
}
