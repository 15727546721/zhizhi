package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.user.SysUserRequest;
import cn.xu.model.entity.User;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 系统用户管理控制器
 * <p>提供后台用户管理相关接口</p>

 */
@Slf4j
@RestController
@RequestMapping("/api/system/user")
@Tag(name = "系统用户管理", description = "后台用户管理接口")
public class SysUserController {

    @Resource
    private UserService userService;

    /**
     * 获取用户列表
     * 
     * <p>分页查询用户列表，支持按用户名、状态、用户类型过滤
     * <p>需要system:user:list权限
     * 
     * @param pageNo 页码，从1开始，默认为1
     * @param pageSize 每页数量，默认为10
     * @param username 用户名（可选），支持模糊查询
     * @param status 状态（可选），0-禁用、1-正常
     * @param userType 用户类型（可选），0-普通、1-官方、2-管理员
     * @return 分页的用户列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    @SaCheckPermission("system:user:list")
    @Operation(summary = "获取用户列表")
    @ApiOperationLog(description = "获取用户列表")
    public ResponseEntity<PageResponse<List<User>>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "用户类型") @RequestParam(required = false) Integer userType) {
        try {
            List<User> users = userService.queryUserListWithFilters(username, status, userType, pageNo, pageSize);
            Long total = userService.countUsersByFilters(username, status, userType);
            
            PageResponse<List<User>> pageResponse = PageResponse.ofList(pageNo, pageSize, total, users);
            
            return ResponseEntity.<PageResponse<List<User>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户列表失败");
        }
    }

    /**
     * 获取用户详情
     * 
     * <p>根据用户ID查询用户详细信息
     * <p>需要system:user:query权限
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     * @throws BusinessException 当用户不存在时抛出
     */
    @GetMapping("/{userId}")
    @SaCheckLogin
    @SaCheckPermission("system:user:query")
    @Operation(summary = "获取用户详情")
    @ApiOperationLog(description = "获取用户详情")
    public ResponseEntity<User> getUserDetail(@PathVariable Long userId) {
        try {
            User user = userService.getUserInfo(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在");
            }
            return ResponseEntity.<User>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(user)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户详情失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户详情失败");
        }
    }

    @PostMapping
    @SaCheckLogin
    @SaCheckPermission("system:user:add")
    @Operation(summary = "新增用户")
    @ApiOperationLog(description = "新增用户")
    public ResponseEntity<Void> addUser(@Valid @RequestBody SysUserRequest request) {
        try {
            userService.addUser(request);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("新增用户成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增用户失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "新增用户失败");
        }
    }

    @PutMapping("/{userId}")
    @SaCheckLogin
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "修改用户")
    @ApiOperationLog(description = "修改用户")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody SysUserRequest request) {
        try {
            request.setId(userId);
            userService.updateUser(request);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("修改用户成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改用户失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "修改用户失败");
        }
    }

    @DeleteMapping("/{userId}")
    @SaCheckLogin
    @SaCheckPermission("system:user:delete")
    @Operation(summary = "删除用户")
    @ApiOperationLog(description = "删除用户")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            // 不能删除自己
            Long currentUserId = LoginUserUtil.getLoginUserId();
            if (currentUserId.equals(userId)) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "不能删除自己");
            }
            
            userService.deleteUser(userId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除用户成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除用户失败");
        }
    }

    @DeleteMapping("/batch")
    @SaCheckLogin
    @SaCheckPermission("system:user:delete")
    @Operation(summary = "批量删除用户")
    @ApiOperationLog(description = "批量删除用户")
    public ResponseEntity<Void> batchDeleteUsers(@RequestBody List<Long> userIds) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            // 过滤掉自己
            List<Long> filteredIds = userIds.stream()
                    .filter(id -> !currentUserId.equals(id))
                    .collect(java.util.stream.Collectors.toList());
            
            if (filteredIds.isEmpty()) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "没有可删除的用户");
            }
            
            userService.batchDeleteUsers(filteredIds);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("批量删除用户成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除用户失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除用户失败");
        }
    }

    @PutMapping("/{userId}/status")
    @SaCheckLogin
    @SaCheckPermission("system:user:edit")
    @Operation(summary = "修改用户状态")
    @ApiOperationLog(description = "修改用户状态")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @Parameter(description = "状态 0:正常 1:禁用)") @RequestParam Integer status) {
        try {
            if (status == 1) {
                userService.banUser(userId);
            } else {
                userService.unbanUser(userId);
            }
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("修改状态成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改用户状态失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "修改用户状态失败");
        }
    }

    @GetMapping("/current")
    @SaCheckLogin
    @Operation(summary = "获取当前登录用户信息")
    @ApiOperationLog(description = "获取当前登录用户信息")
    public ResponseEntity<User> getCurrentUser() {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            User user = userService.getUserInfo(userId);
            return ResponseEntity.<User>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(user)
                    .build();
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户信息失败");
        }
    }

    @GetMapping("/info/roles")
    @SaCheckLogin
    @Operation(summary = "获取当前登录用户信息（含角色权限）")
    @ApiOperationLog(description = "获取当前登录用户信息（含角色权限）")
    public ResponseEntity<java.util.Map<String, Object>> getCurrentUserWithRoles() {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            User user = userService.getUserInfo(userId);
            
            // 构建响应数据
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("gender", user.getGender());
            userInfo.put("description", user.getDescription());
            userInfo.put("status", user.getStatus());
            userInfo.put("userType", user.getUserType());
            userInfo.put("followCount", user.getFollowCount());
            userInfo.put("fansCount", user.getFansCount());
            userInfo.put("likeCount", user.getLikeCount());
            userInfo.put("createTime", user.getCreateTime());
            userInfo.put("updateTime", user.getUpdateTime());
            
            // 根据用户类型设置角色和权限
            java.util.List<String> roles = new java.util.ArrayList<>();
            java.util.List<String> perms = new java.util.ArrayList<>();
            
            if (user.getUserType() != null) {
                if (user.getUserType() >= 3) {
                    // 超级管理员 - ROOT 角色拥有所有权限
                    roles.add("ROOT");
                    perms.add("*:*:*");
                } else if (user.getUserType() >= 2) {
                    // 官方账号 - ADMIN 角色
                    roles.add("ADMIN");
                    perms.add("system:post:*");
                    perms.add("system:comment:*");
                    perms.add("system:tag:*");
                    perms.add("system:user:query");
                    perms.add("system:statistics:view");
                } else {
                    roles.add("USER");
                }
            } else {
                roles.add("USER");
            }
            
            userInfo.put("roles", roles);
            userInfo.put("perms", perms);
            
            return ResponseEntity.<java.util.Map<String, Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(userInfo)
                    .build();
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户信息失败");
        }
    }

    // ==================== 在线用户管理 ====================

    @GetMapping("/online")
    @SaCheckLogin
    @SaCheckPermission("system:user:list")
    @Operation(summary = "获取在线用户列表")
    @ApiOperationLog(description = "获取在线用户列表")
    public ResponseEntity<PageResponse<List<OnlineUserVO>>> getOnlineUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username) {
        try {
            // 获取所有在线会话
            java.util.List<String> sessionIds = StpUtil.searchSessionId("", 0, -1, false);
            java.util.List<OnlineUserVO> onlineUsers = new java.util.ArrayList<>();
            
            for (String sessionId : sessionIds) {
                try {
                    cn.dev33.satoken.session.SaSession session = StpUtil.getSessionBySessionId(sessionId);
                    if (session != null) {
                        Object loginId = session.getLoginId();
                        if (loginId != null) {
                            Long userId = Long.parseLong(loginId.toString());
                            User user = userService.getUserInfo(userId);
                            if (user != null) {
                                // 如果指定了用户名过滤条件
                                if (username != null && !username.isEmpty() 
                                        && !user.getUsername().contains(username)) {
                                    continue;
                                }
                                
                                OnlineUserVO onlineUser = OnlineUserVO.builder()
                                        .userId(userId)
                                        .username(user.getUsername())
                                        .nickname(user.getNickname())
                                        .avatar(user.getAvatar())
                                        .tokenValue(session.getToken())
                                        .loginTime(java.time.LocalDateTime.now()) // Sa-Token不直接提供登录时间
                                        .build();
                                onlineUsers.add(onlineUser);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取会话信息失败: sessionId={}", sessionId);
                }
            }
            
            // 分页处理
            int total = onlineUsers.size();
            int start = (pageNo - 1) * pageSize;
            int end = Math.min(start + pageSize, total);
            java.util.List<OnlineUserVO> pagedUsers = start < total 
                    ? onlineUsers.subList(start, end) 
                    : new java.util.ArrayList<>();
            
            PageResponse<List<OnlineUserVO>> pageResponse = PageResponse.ofList(
                    pageNo, pageSize, (long) total, pagedUsers);
            
            return ResponseEntity.<PageResponse<List<OnlineUserVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取在线用户列表失败");
        }
    }

    @GetMapping("/kick")
    @SaCheckLogin
    @SaCheckPermission("system:user:kick")
    @Operation(summary = "踢出用户")
    @ApiOperationLog(description = "踢出用户")
    public ResponseEntity<Void> kickUser(
            @Parameter(description = "用户Token") @RequestParam String token) {
        try {
            // 根据token踢出用户
            StpUtil.kickoutByTokenValue(token);
            log.info("踢出用户成功: token={}", token);
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("踢出成功")
                    .build();
        } catch (Exception e) {
            log.error("踢出用户失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "踢出用户失败");
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OnlineUserVO {
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private String tokenValue;
        private String ip;
        private String browser;
        private String os;
        private java.time.LocalDateTime loginTime;
    }
}