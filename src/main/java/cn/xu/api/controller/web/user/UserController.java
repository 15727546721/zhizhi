package cn.xu.api.controller.web.user;

import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("api/user")
@RestController
public class UserController {

    @Resource
    private IUserService userService;

    @GetMapping("/info/{id}")
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable Long id) {
        UserEntity userEntity = userService.getUserInfo(id);
        return ResponseEntity.<UserEntity>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(userEntity)
                .build();
    }
}
