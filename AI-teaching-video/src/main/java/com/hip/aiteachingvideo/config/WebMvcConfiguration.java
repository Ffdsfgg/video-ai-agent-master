package com.hip.aiteachingvideo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * WebMvc配置类
 */
@Slf4j
@Configuration
public class
WebMvcConfiguration extends WebMvcConfigurationSupport {
    @Value("${hip.url}")
    private String url;
    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    /**
     * 静态资源配置
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源访问路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/", "file:" + url);

        //配置swagger文档访问路径
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 跨域配置
     *
     * @param
     */
    //解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/*/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(1800)
                .allowedOrigins("*");
    }

    //注册拦截器
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");

        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui.html",
                        "/v2/api-docs",
                        "/webjars/**",
                        "/doc.html/**",
                        "/swagger-resources/**",
                        "/static/**",
                        "/error",
                        "/favicon.ico",
                        "/actuator/**"
                ).
                excludePathPatterns(
                        "/user",
                        "/user/admin",
                        "/user/register",
                        "/utils/**",
                        "/qwenTextAI/**",
                        "/api/**",
                        "/file");
    }

    /**
     * 自定义OpenAPI配置
     *
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("AI教学视频管理后台")
                .version("1.0.0")
                .contact(new Contact()
                        .name("***")
                        .url("http://localhost:8080")
                        .email("localhost@qq.com")));
    }


}