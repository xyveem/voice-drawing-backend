package top.xym.voicedrawingapi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("语音绘图 API")
                        .contact(new Contact().name("xym").email("xym.@qq.com"))
                        .version("1.0")
                        .description("语音绘图 API 接⼝⽂档")
                        .license(new License().name("Apache 2.0").url("http://doc.xiaominfo.com")));
    }
}