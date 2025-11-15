package com.hip.aiteachingvideo.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云 OSS 工具类
 */
@Component
@Data
@ConfigurationProperties(prefix = "hip.aliyun.oss")
public class OSSUtils {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String baseUrl;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            createBucketIfNotExists();
        }
    }

    /**
     * 创建 Bucket（如果不存在）
     */
    private void createBucketIfNotExists() {
        if (!ossClient.doesBucketExist(bucketName)) {
            ossClient.createBucket(bucketName);
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        }
    }

    /**
     * 上传本地文件到 OSS
     *
     * @param file       本地文件
     * @param objectName OSS 上的文件路径（如："videos/2025/07/05/test.mp4"）
     * @return 文件访问地址
     */
    public String uploadFile(File file, String objectName) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);
        ossClient.putObject(request);

        return generatePublicUrl(objectName);
    }

    /**
     * 上传 InputStream 到 OSS
     *
     * @param inputStream 文件输入流
     * @param objectName  OSS 上的文件路径
     * @param contentType 文件类型（如 "image/jpeg", "video/mp4"）
     * @return 文件访问地址
     */
    public String uploadInputStream(InputStream inputStream, String objectName, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        if (StringUtils.hasText(contentType)) {
            metadata.setContentType(contentType);
        }

        ossClient.putObject(bucketName, objectName, inputStream, metadata);
        return generatePublicUrl(objectName);
    }

    /**
     * 生成带签名的 URL（用于私有读权限时临时访问）
     *
     * @param objectName 文件路径
     * @param expires    过期时间（单位：秒）
     * @return 签名 URL
     */
    public String generateSignedUrl(String objectName, int expires) {
        Date expiration = new Date(System.currentTimeMillis() + expires * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
        return url.toString();
    }

    /**
     * 生成公开访问的 URL（需要 Bucket 是 PublicRead）
     *
     * @param objectName 文件路径
     * @return 公共 URL
     */
    public String generatePublicUrl(String objectName) {
        return baseUrl.endsWith("/") ? baseUrl + objectName : baseUrl + "/" + objectName;
    }

    /**
     * 删除文件
     *
     * @param objectName 文件路径
     */
    public void deleteFile(String objectName) {
        ossClient.deleteObject(bucketName, objectName);
    }

    /**
     * 关闭客户端连接（Spring Boot 停止时建议调用）
     */
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}