package com.hip.aiteachingvideo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hip.aiteachingvideo.model.po.Videos;

import java.io.IOException;

/**
 * @author 87502
 * @description 针对表【videos】的数据库操作Service
 * @createDate 2025-07-05 19:33:07
 */
public interface VideosService extends IService<Videos> {

    /**
     * 视频请求ai生成基本信息
     *
     * @param videos 视频
     * @return 视频信息
     */
    String clipAndUpload(Videos videos) throws IOException, InterruptedException;

    /**
     * 请求ai生成字幕
     */
    void generateSubtitles(Videos videos);

    /**
     * 请求ai生成echonest
     */
    void generateEchonest(Videos videos, String next);


    void testVideo(Videos videos);
}
