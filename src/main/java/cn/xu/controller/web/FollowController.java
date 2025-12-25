package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.vo.follow.FollowUserVO;
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
 * <p>提供用户关注、取消关注、关注列表、粉丝列表等功能接口</p>

 */
@Tag(name = "关注接口", description = "用户关注关系管理API")
@RestController
@RequestMapping("/api/follows")
@Validated
@Slf4j
public class FollowController {

    @Resource
    private FollowService followService;

    /**
     * 关注用户
     *
     * <p>当前登录用户关注指定用户，建立关注关系
     * <p>需要登录后才能访问
     *
     * @param userId 被关注用户的ID
     * @return 操作结果
     */
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

    /**
     * 取消关注
     *
     * <p>当前登录用户取消关注指定用户，解除关注关系
     * <p>需要登录后才能访问
     *
     * @param userId 被关注用户的ID
     * @return 操作结果
     */
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

    /**
     * 查询是否关注某用户
     *
     * <p>检查当前登录用户是否已关注指定用户
     * <p>需要登录后才能访问
     *
     * @param userId 被查询用户的ID
     * @return true-已关注，false-未关注
     */
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

    /**
     * 获取我的关注列表
     *
     * <p>获取当前登录用户关注的所有用户列表，包含用户基本信息
     *
     * @param pageNum 页码，从1开始，默认1
     * @param pageSize 每页数量，默认为10
     * @return 关注用户列表
     */
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

    /**
     * 获取我的粉丝列表
     *
     * <p>获取关注当前登录用户的所有粉丝列表，包含用户基本信息
     *
     * @param pageNum 页码，从1开始，默认1
     * @param pageSize 每页数量，默认为10
     * @return 粉丝用户列表
     */
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

    /**
     * 搜索关注用户（用于@提及）
     *
     * <p>在当前用户的关注列表中搜索，支持按昵称或用户名模糊匹配
     *
     * @param keyword 搜索关键词
     * @param limit 返回数量限制（默认10）
     * @return 匹配的关注用户列表
     */
    @Operation(summary = "搜索关注用户（@提及用）")
    @GetMapping("/following/search")
    @SaCheckLogin
    @ApiOperationLog(description = "搜索关注用户")
    public ResponseEntity<List<FollowUserVO>> searchFollowing(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false, defaultValue = "") String keyword,
            @Parameter(description = "返回数量限制")
            @RequestParam(defaultValue = "10") Integer limit) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.debug("搜索关注用户，关键词：{}，限制：{}", keyword, limit);
        List<FollowUserVO> result = followService.searchFollowingUsers(currentUserId, keyword, limit);
        return ResponseEntity.<List<FollowUserVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }
}