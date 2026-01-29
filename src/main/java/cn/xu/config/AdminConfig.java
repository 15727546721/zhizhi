package cn.xu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 管理员配置
 * <p>从配置文件读取管理员账号信息，避免硬编码</p>
 * 
 * <p>配置示例（application.yml）：</p>
 * <pre>
 * app:
 *   admin:
 *     username: admin
 *     password: ${ADMIN_PASSWORD:ChangeMe123!}
 *     email: admin@example.com
 * </pre>
 * 
 * <p>⚠️ 安全提示：</p>
 * <ul>
 *   <li>生产环境必须通过环境变量 ADMIN_PASSWORD 设置密码</li>
 *   <li>如果使用默认密码，系统启动时会自动生成随机密码</li>
 *   <li>首次登录后请立即修改密码</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminConfig {
    
    /**
     * 管理员用户名
     */
    private String username = "admin";
    
    /**
     * 管理员密码（建议通过环境变量设置）
     */
    private String password = "ChangeMe123!";
    
    /**
     * 管理员邮箱
     */
    private String email = "admin@example.com";
    
    /**
     * 管理员昵称
     */
    private String nickname = "系统管理员";
}
