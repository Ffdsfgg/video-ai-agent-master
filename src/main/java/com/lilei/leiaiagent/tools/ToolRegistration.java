package com.lilei.leiaiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中工具注册类
 * 开发好了这么多工具类后，结合我们自己的需求，可以给 A1一次性提供所有工具，让它自己决定何时调用。所以我们可以创建 工具注册类，方便统一管理和绑定所有工具。
 * 可别小瞧这段代码，其实它暗含了好几种设计模式:
 * 1.工厂模式: allTools() 方法作为一个工厂方法，负责创建和配置多个工具实例，然后将它们包装成统一的数组返回。这符合工厂模式的核心思想-集中创建对象并隐藏创建细节。
 * 2.依赖注入模式: 通过 @va1ue 注解注入配置值，以及将创建好的工具通过 Spring 容器注入到需要它们的组件中。
 * 3.注册模式: 该类作为一个中央注册点，集中管理和注册所有可用的工具，使它们能够被系统其他部分统一访问。
 * 4.适配器模式的应用: ToolCallbacks.from 方法可以看作是一种适配器，它将各种不同的工具类转换为统-的 ToolCallback 数组，使系统能够以一致的方式处理它们。
 */
@Configuration
public class ToolRegistration {

    @Value("${search.api.key}")
    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();

        // 底层是使用的反射原理，过滤出所有带有 @Tool 注解的方法，并将它们转换为 ToolCallback 实例。
        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
        );
    }
}