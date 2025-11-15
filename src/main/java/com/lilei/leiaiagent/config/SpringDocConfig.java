package com.lilei.leiaiagent.config;

import com.lilei.leiaiagent.pojo.vo.BaseResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpringDoc配置类
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VideoAnalysis AI Agent API")
                        .description("视频解析系统API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Li Lei")
                                .email("lilei@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("/").description("本地环境")))
                .components(new Components()
                        .schemas(buildSchemas()));
    }

    /**
     * 构建通用响应模型
     */
    private Map<String, Schema> buildSchemas() {
        Map<String, Schema> schemaMap = new HashMap<>();
        
        // 注册BaseResponse<Object>为通用响应模型
        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(
                        new AnnotatedType(BaseResponse.class).resolveAsRef(false));
        
        if (resolvedSchema.schema != null) {
            schemaMap.put("BaseResponse", resolvedSchema.schema);
        }
        
        return schemaMap;
    }
    
    /**
     * 自定义OpenAPI全局配置，为所有接口添加通用响应
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    // 为所有操作添加统一的响应定义
                    if (pathItem.readOperations() != null) {
                        pathItem.readOperations().forEach(operation -> {
                            // 初始化响应对象
                            if (operation.getResponses() == null) {
                                operation.setResponses(new ApiResponses());
                            }
                            
                            // 添加200响应
                            ApiResponse okResponse = operation.getResponses().get("200");
                            if (okResponse == null) {
                                okResponse = new ApiResponse()
                                        .description("操作成功")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new Schema<>().$ref("#/components/schemas/BaseResponse"))));
                                operation.getResponses().addApiResponse("200", okResponse);
                            }
                            
                            // 添加400响应
                            operation.getResponses().addApiResponse("400", new ApiResponse()
                                    .description("请求参数错误")
                                    .content(new Content()
                                            .addMediaType("application/json", new MediaType()
                                                    .schema(new Schema<>().$ref("#/components/schemas/BaseResponse")))));
                            
                            // 添加500响应
                            operation.getResponses().addApiResponse("500", new ApiResponse()
                                    .description("系统内部错误")
                                    .content(new Content()
                                            .addMediaType("application/json", new MediaType()
                                                    .schema(new Schema<>().$ref("#/components/schemas/BaseResponse")))));
                        });
                    }
                });
            }
        };
    }
} 