package com.hip.aiteachingvideo.utils;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class VideoProcessorUtils {

    private static final long MAX_VIDEO_DURATION_MS = 8 * 60 * 1000; // 8 分钟
    private static final int TARGET_PIXEL_COUNT = 600_000;           // 目标像素数

    @Autowired
    @Qualifier("taskExecutor1")
    private Executor taskExecutor1;

    @SneakyThrows
    public List<File> processVideo(File inputFile, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        MultimediaObject multimediaObject = new MultimediaObject(inputFile);
        MultimediaInfo info = multimediaObject.getInfo();
        long duration = info.getDuration();

        List<CompletableFuture<File>> futures = new ArrayList<>();
        long startTime = 0;

        while (startTime < duration) {
            long endTime = Math.min(startTime + MAX_VIDEO_DURATION_MS, duration);
            if (endTime - startTime < 2000) {
                break; // 至少保留 2 秒
            }

            long finalStartTime = startTime;
            CompletableFuture<File> future = CompletableFuture.supplyAsync(
                    () -> cutAndConvertPart(inputFile, outputDir, finalStartTime, endTime, info),
                    taskExecutor1
            );
            futures.add(future);
            startTime = endTime;
        }

        List<File> resultFiles = new ArrayList<>();
        for (CompletableFuture<File> future : futures) {
            try {
                resultFiles.add(future.get()); // 阻塞等待结果
            } catch (Exception e) {
                throw new RuntimeException("切片任务失败", e);
            }
        }
        return resultFiles;
    }

    private File cutAndConvertPart(File inputFile,
                                   File outputDir,
                                   long startTime,
                                   long endTime,
                                   MultimediaInfo info) {
        try {
            String uuid = UUID.randomUUID().toString();
            File outputFile = new File(outputDir, uuid + ".mp4");

            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("copy");

            VideoAttributes video = new VideoAttributes();
            video.setCodec("copy");

            // 计算缩放比例
            VideoSize originalSize = info.getVideo().getSize();
            double scaleFactor = Math.sqrt(
                    (double) TARGET_PIXEL_COUNT / (originalSize.getWidth() * originalSize.getHeight())
            );
            int targetWidth = (int) (originalSize.getWidth() * scaleFactor);
            int targetHeight = (int) (originalSize.getHeight() * scaleFactor);
            video.setSize(new VideoSize(targetWidth, targetHeight));

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOffset((float) (startTime / 1000.0));
            attrs.setDuration((float) ((endTime - startTime) / 1000.0));
            attrs.setAudioAttributes(audio);
            attrs.setVideoAttributes(video);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(inputFile), outputFile, attrs);
            return outputFile;
        } catch (Exception e) {
            throw new RuntimeException("单段切片失败", e);
        }
    }
}