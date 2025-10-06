package cn.xu.domain.user.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.user.model.valobj.Email;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 用户参数校验服务
 */
@Service
public class UserValidationService {
    
    /**
     * 校验用户注册参数
     * @param username 用户名
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param email 邮箱
     */
    public void validateRegisterParams(String username, String password, String confirmPassword, String email) {
        // 校验用户名
        if (StringUtils.isBlank(username)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户名不能为空");
        }
        
        if (username.length() < 4 || username.length() > 20) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户名长度必须在4-20个字符之间");
        }
        
        // 校验密码
        if (StringUtils.isBlank(password)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }
        
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码长度必须在6-20个字符之间");
        }
        
        // 校验确认密码
        if (StringUtils.isBlank(confirmPassword)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "确认密码不能为空");
        }
        
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "两次密码输入不一致");
        }
        
        // 校验邮箱
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        
        try {
            new Email(email);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱格式不正确");
        }
    }
    
    /**
     * 校验用户登录参数
     * @param email 邮箱
     * @param password 密码
     */
    public void validateLoginParams(String email, String password) {
        // 校验邮箱
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        
        try {
            new Email(email);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱格式不正确");
        }
        
        // 校验密码
        if (StringUtils.isBlank(password)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }
        
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码长度必须在6-20个字符之间");
        }
    }
}