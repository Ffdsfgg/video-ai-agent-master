package com.hip.aiteachingvideo.utils;

import com.hip.aiteachingvideo.config.TencentASRProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.asr.v20190614.AsrClient;
import com.tencentcloudapi.asr.v20190614.models.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 音频转VTT工具类
 */
@Component
public class VccUtils {

    private static TencentASRProperties tencentASRProperties;

    @Autowired
    public void setTencentASRProperties(TencentASRProperties properties) {
        tencentASRProperties = properties;
    }

    // 替换原来的常量
    private static final String DEFAULT_OUTPUT = "output.vtt";

    /**
     * 语音转VTT（使用默认输出路径）
     *
     * @param audioUrl 音频URL地址
     */
    public static void convertToVtt(String audioUrl) {
        convertToVtt(audioUrl, DEFAULT_OUTPUT);
    }

    /**
     * 语音转VTT（自定义输出路径）
     *
     * @param audioUrl   音频URL地址
     * @param outputPath VTT文件保存路径
     */
    public static void convertToVtt(String audioUrl, String outputPath) {
        try {
            // 1. 创建识别任务
            Long taskId = createRecTask("16k_zh", audioUrl);
            if (taskId == null) {
                System.out.println("[ERROR] 任务创建失败");
                return;
            }

            // 2. 查询识别结果
            SentenceDetail[] sentenceDetails = queryRecTask(taskId);
            if (sentenceDetails == null) {
                System.out.println("[ERROR] 识别失败");
                return;
            }

            // 3. 生成VTT内容
            String vttContent = toVtt(Arrays.asList(sentenceDetails));
            System.out.println("语音识别完成，生成VTT内容");

            // 4. 保存到文件
            saveSrtToFile(vttContent, outputPath);
            System.out.println("VTT文件已保存至: " + outputPath);

        } catch (TencentCloudSDKException | IOException e) {
            System.err.println("[EXCEPTION] 处理过程中发生错误");
            e.printStackTrace();
        }
    }

    //  以下为内部工具方法
    private static void saveSrtToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    private static Long createRecTask(String engineType, String audioUrl) throws TencentCloudSDKException {
        Credential cred = new Credential(
                tencentASRProperties.getSecretId(),
                tencentASRProperties.getSecretKey()
        );
        AsrClient client = new AsrClient(cred, "ap-guangzhou");
        CreateRecTaskRequest req = new CreateRecTaskRequest();
        req.setChannelNum(1L);
        req.setResTextFormat(2L);
        req.setSourceType(0L);
        req.setConvertNumMode(1L);
        req.setEngineModelType(engineType);
        req.setUrl(audioUrl);
        return client.CreateRecTask(req).getData().getTaskId();
    }

    private static SentenceDetail[] queryRecTask(Long taskId) throws TencentCloudSDKException {
        Credential cred = new Credential(
                tencentASRProperties.getSecretId(),
                tencentASRProperties.getSecretKey()
        );
        AsrClient client = new AsrClient(cred, "ap-guangzhou");
        DescribeTaskStatusRequest req = new DescribeTaskStatusRequest();
        req.setTaskId(taskId);

        while (true) {
            DescribeTaskStatusResponse resp = client.DescribeTaskStatus(req);
            TaskStatus data = resp.getData();

            if ("success".equals(data.getStatusStr())) return data.getResultDetail();
            if (data.getStatus().equals(3L)) return null;  // 失败状态

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    private static String toVtt(List<SentenceDetail> sentences) {
        if (sentences == null || sentences.isEmpty()) return "";

        StringBuilder vtt = new StringBuilder("WEBVTT\n\n");
        final int MAX_LINE_LENGTH = 30;  // 单行最大长度（字符数）
        final List<String> BREAK_PUNCTUATIONS = Arrays.asList("。", "？", "！", "，", "；", "：", "、", ".", "?", "!", ",");

        for (SentenceDetail sentence : sentences) {
            String text = sentence.getFinalSentence();
            long startMs = sentence.getStartMs();
            long endMs = sentence.getEndMs();
            SentenceWords[] words = sentence.getWords();

            if (text == null || text.isEmpty()) continue;

            // 短句直接输出（或没有词级信息）
            if (text.length() <= MAX_LINE_LENGTH || words == null || words.length == 0) {
                vtt.append(buildVttEntry(startMs, endMs, text));
                continue;
            }

            // 基于词级时间戳进行智能分割
            int currentWordIndex = 0;
            long segmentStart = startMs;

            while (currentWordIndex < words.length) {
                int wordsInSegment = 0;
                StringBuilder segmentText = new StringBuilder(32);
                long segmentEnd = -1;

                // 收集当前分段的词语
                while (currentWordIndex < words.length) {
                    SentenceWords currentWord = words[currentWordIndex];
                    String wordText = currentWord.getWord() != null ? currentWord.getWord() : "";

                    // 首次添加词语
                    if (wordsInSegment == 0) {
                        segmentText.append(wordText);
                        segmentEnd = currentWord.getOffsetEndMs() + startMs;
                    }
                    // 后续添加词语（带空格）
                    else {
                        // 中文不需要空格，特殊处理英文单词间空格
                        boolean needsSpace = wordText.matches("^[a-zA-Z].*");
                        segmentText.append(needsSpace ? " " : "").append(wordText);
                        segmentEnd = currentWord.getOffsetEndMs() + startMs;
                    }
                    wordsInSegment++;
                    currentWordIndex++;

                    // 分段条件检查：标点/长度/词数
                    boolean atPunctuation = BREAK_PUNCTUATIONS.contains(wordText);
                    boolean exceedLength = segmentText.length() >= MAX_LINE_LENGTH;
                    boolean minWordsReached = wordsInSegment >= 3;

                    if ((atPunctuation && minWordsReached) || exceedLength) {
                        break;
                    }
                }

                // 输出当前分段（确保有效时间范围）
                if (segmentEnd > segmentStart) {
                    vtt.append(buildVttEntry(segmentStart, segmentEnd, segmentText.toString()));
                    segmentStart = segmentEnd;  // 下一段从当前段结束开始
                }
            }
        }
        return vtt.toString();
    }

    private static String buildVttEntry(long startMs, long endMs, String text) {
        return String.format("%s --> %s%n%s%n%n", msToVttTime(startMs), msToVttTime(endMs), text);
    }

    private static String msToVttTime(long ms) {
        long hours = ms / 3600000;
        long minutes = (ms % 3600000) / 60000;
        long seconds = (ms % 60000) / 1000;
        long milliseconds = ms % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
    }

}