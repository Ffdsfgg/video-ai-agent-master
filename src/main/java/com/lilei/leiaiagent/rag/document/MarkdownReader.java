package com.lilei.leiaiagent.rag.document;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 读取 Markdown 文档的组件
 */
@Component
public class MarkdownReader {

    private final Resource[] resources;

    MarkdownReader(@Value("classpath:document/*.md") Resource[] resources) {
        this.resources = resources;
    }

    // 读取多篇 Markdown 文档
    public List<Document> loadMarkdowns() {
        // MarkdownDocumentReader 是一个文档读取器，用于读取 Markdown 格式的文档
        MarkdownDocumentReader reader;
        List<Document> allDocuments = new ArrayList<>();
        for (Resource resource : resources) {
            // MarkdownDocumentReaderConfig 是一个配置类，用于配置 Markdown 文档读取器的行为
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true) // 是否将水平线转换为文档
                    .withIncludeCodeBlock(false) // 是否包含代码块
                    .withIncludeBlockquote(false) // 是否包含引用块
                    .withAdditionalMetadata("filename", Objects.requireNonNull(resource.getFilename())) // 自主添加额外的元数据
                    .build();
            reader = new MarkdownDocumentReader(resource, config);
            allDocuments.addAll(reader.read());
        }
        return allDocuments;
    }
}
