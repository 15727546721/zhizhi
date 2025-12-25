package cn.xu.integration.mail;

import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮件服务
 * <p>提供邮件发送功能，支持纯文本和HTML格式</p>

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name:知之社区}")
    private String appName;

    /**
     * 发送验证码邮件
     *
     * @param to   收件人邮箱
     * @param code 验证码
     */
    public void sendVerifyCode(String to, String code) {
        String subject = "[" + appName + "] 邮箱验证码";
        String content = buildVerifyCodeContent(code);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送密码重置邮件
     *
     * @param to       收件人邮箱
     * @param token    重置令牌
     * @param resetUrl 重置链接基础URL
     */
    public void sendPasswordResetEmail(String to, String token, String resetUrl) {
        String subject = "[" + appName + "] 密码重置";
        String content = buildPasswordResetContent(token, resetUrl);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送邮箱验证邮件
     *
     * @param to         收件人邮箱
     * @param token      验证令牌
     * @param expiration 过期时间
     */
    public void sendVerificationEmail(String to, String token, java.time.LocalDateTime expiration) {
        String subject = "[" + appName + "] 邮箱验证";
        String content = buildVerificationEmailContent(token, expiration);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送欢迎邮件（异步，不影响主流程）
     *
     * @param to       收件人邮箱
     * @param nickname 用户昵称
     */
    @Async
    public void sendWelcomeEmail(String to, String nickname) {
        String subject = "欢迎加入" + appName + "社区";
        String content = buildWelcomeContent(nickname);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 发送HTML格式邮件
     *
     * @param to      收件人邮箱
     * @param subject 主题
     * @param content HTML内容
     */
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式

            mailSender.send(message);
            log.info("[邮件服务] 发送成功 - to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("[邮件服务] 发送失败 - to: {}, subject: {}, error: {}", to, subject, e.getMessage());
            throw new BusinessException(50001, "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 发送纯文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 主题
     * @param content 文本内容
     */
    public void sendTextEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false); // false表示纯文本

            mailSender.send(message);
            log.info("[邮件服务] 发送成功 - to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("[邮件服务] 发送失败 - to: {}, subject: {}, error: {}", to, subject, e.getMessage());
            throw new BusinessException(50001, "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建验证码邮件内容
     */
    private String buildVerifyCodeContent(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
                "<h2 style='color: #333; text-align: center;'>邮箱验证码</h2>" +
                "<p style='color: #666; font-size: 14px;'>您好，</p>" +
                "<p style='color: #666; font-size: 14px;'>您正在进行邮箱验证，验证码为：</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<span style='font-size: 32px; font-weight: bold; color: #1890ff; letter-spacing: 5px; background: #e6f7ff; padding: 15px 30px; border-radius: 8px;'>" + code + "</span>" +
                "</div>" +
                "<p style='color: #999; font-size: 12px;'>验证码有效期为 <strong>5分钟</strong>，请尽快完成验证。</p>" +
                "<p style='color: #999; font-size: 12px;'>如果这不是您的操作，请忽略此邮件。</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>" +
                "<p style='color: #bbb; font-size: 11px; text-align: center;'>" + appName + " 团队</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 构建密码重置邮件内容
     */
    private String buildPasswordResetContent(String token, String resetUrl) {
        String fullResetUrl = resetUrl + "?token=" + token;
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
                "<h2 style='color: #333; text-align: center;'>密码重置</h2>" +
                "<p style='color: #666; font-size: 14px;'>您好，</p>" +
                "<p style='color: #666; font-size: 14px;'>您正在申请重置密码，请点击下方按钮完成操作：</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + fullResetUrl + "' style='display: inline-block; padding: 12px 40px; background: #1890ff; color: #fff; text-decoration: none; border-radius: 5px; font-size: 16px;'>重置密码</a>" +
                "</div>" +
                "<p style='color: #999; font-size: 12px;'>如果按钮无法点击，请复制以下链接到浏览器打开：</p>" +
                "<p style='color: #1890ff; font-size: 12px; word-break: break-all;'>" + fullResetUrl + "</p>" +
                "<p style='color: #999; font-size: 12px;'>链接有效期为 <strong>24小时</strong>。</p>" +
                "<p style='color: #999; font-size: 12px;'>如果这不是您的操作，请忽略此邮件。</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>" +
                "<p style='color: #bbb; font-size: 11px; text-align: center;'>" + appName + " 团队</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 构建邮箱验证邮件内容
     */
    private String buildVerificationEmailContent(String token, java.time.LocalDateTime expiration) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
                "<h2 style='color: #333; text-align: center;'>邮箱验证</h2>" +
                "<p style='color: #666; font-size: 14px;'>您好，</p>" +
                "<p style='color: #666; font-size: 14px;'>请点击以下链接完成邮箱验证：</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<p style='color: #1890ff; font-size: 14px;'>验证令牌: " + token + "</p>" +
                "</div>" +
                "<p style='color: #999; font-size: 12px;'>链接有效期为 <strong>24小时</strong>。</p>" +
                "<p style='color: #999; font-size: 12px;'>如果这不是您的操作，请忽略此邮件。</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>" +
                "<p style='color: #bbb; font-size: 11px; text-align: center;'>" + appName + " 团队</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 构建欢迎邮件内容
     */
    private String buildWelcomeContent(String nickname) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
                "<h2 style='color: #333; text-align: center;'>欢迎加入" + appName + "社区</h2>" +
                "<p style='color: #666; font-size: 14px;'>亲爱的 <strong>" + nickname + "</strong>，</p>" +
                "<p style='color: #666; font-size: 14px;'>欢迎您加入" + appName + "，在这里您可以：</p>" +
                "<ul style='color: #666; font-size: 14px;'>" +
                "<li>发布和分享您的技术帖子</li>" +
                "<li>与其他开发者交流讨论</li>" +
                "<li>关注感兴趣的话题和作者</li>" +
                "</ul>" +
                "<p style='color: #666; font-size: 14px;'>期待您的精彩内容！</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>" +
                "<p style='color: #bbb; font-size: 11px; text-align: center;'>" + appName + " 团队</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
