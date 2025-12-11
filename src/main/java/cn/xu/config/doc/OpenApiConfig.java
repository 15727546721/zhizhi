package cn.xu.config.doc;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI文档配置类
 *
 * 
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                // 接口文档标题
                .info(new Info().title("API接口文档")
                        // 接口文档描述
                        .description("调用的接口文档，详细描述了所有可用的API接口和其功能")
                        // 接口文档版本
                        .version("v1.0")
                        // 开发者联系信息
                        .contact(new Contact().name("开发者团队").url("http://example.com")))
                .externalDocs(new ExternalDocumentation()
                        // 额外补充说明
                        .description("更多详细信息请访问开发文档")
                        // 额外补充链接
                        .url("http://example.com/docs"));
    }
}