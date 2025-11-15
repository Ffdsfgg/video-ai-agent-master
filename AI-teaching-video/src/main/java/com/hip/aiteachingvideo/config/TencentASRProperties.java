package com.hip.aiteachingvideo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 语音识别配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "hip.tencent.asr")
public class TencentASRProperties {

    private static String secretId;
    private static String secretKey;

    // 非静态的setter方法用于Spring注入
    public void setSecretId(String secretId) {
        TencentASRProperties.secretId = secretId;
    }

    public void setSecretKey(String secretKey) {
        TencentASRProperties.secretKey = secretKey;
    }

    // 提供获取静态字段的方法
    public static String getSecretId() {
        return secretId;
    }

    public static String getSecretKey() {
        return secretKey;
    }
}