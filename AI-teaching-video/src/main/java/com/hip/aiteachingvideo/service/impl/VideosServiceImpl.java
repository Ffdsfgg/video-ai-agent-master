package com.hip.aiteachingvideo.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hip.aiteachingvideo.model.dto.Message;
import com.hip.aiteachingvideo.model.dto.QwenMessage;
import com.hip.aiteachingvideo.model.po.Subtitles;
import com.hip.aiteachingvideo.model.po.Videos;
import com.hip.aiteachingvideo.model.po.VideosInfo;
import com.hip.aiteachingvideo.service.ChatService;
import com.hip.aiteachingvideo.service.QwenTextService;
import com.hip.aiteachingvideo.service.VideosService;
import com.hip.aiteachingvideo.mapper.VideosMapper;
import com.hip.aiteachingvideo.utils.OSSUtils;
import com.hip.aiteachingvideo.utils.VccUtils;
import com.hip.aiteachingvideo.utils.VideoProcessorUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author 87502
 * @description 针对表【videos】的数据库操作Service实现
 * @createDate 2025-07-05 19:33:07
 */
@Service
public class VideosServiceImpl extends ServiceImpl<VideosMapper, Videos> implements VideosService {

    private static final String OUTPUT_DIR = "output/"; // 视频临时文件路径

    @Autowired
    private OSSUtils ossUtils;
    @Autowired
    private ChatService chatService;
    @Value("${hip.url}")
    private String url;

    @Value("${hip.location}")
    private String location;
    @Autowired
    private VideosInfoServiceImpl videosInfoService;

    @Autowired
    private SubtitlesServiceImpl subtitlesService;

    @Autowired
    private VideoProcessorUtils videoProcessorUtils;

    @Autowired
    private QwenTextService qwenTextService;

    //    @Async
    @SneakyThrows
    @Override
    public String clipAndUpload(Videos videos) {
        //处理视频文件
        //jave
        File inputFile = new File(url + videos.getUrl()); // 输入视频文件路径
        File outputDir = new File(url + OUTPUT_DIR); // 输出目录路径
        System.out.println(url + videos.getUrl());
        List<File> resultFiles = videoProcessorUtils.processVideo(inputFile, outputDir);

        System.out.println("分割后的视频文件:");
        List<Message> messages = new ArrayList<>();
        for (File file : resultFiles) {
            // 构造 OSS 上的目标路径
            String datePath = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
            String objectName = "videos/" + datePath + file.getName();

            // 上传文件到 OSS
            String ossUrl = ossUtils.uploadFile(file, objectName);
            // 构造消息
            Message.ContentItem contentItem = Message.ContentItem.builder()
                    .type("video_url")
                    .video_url(new Message.ContentItem.VideoUrl(ossUrl))
                    .build();

            Message message = Message.builder()
                    .role("user")
                    .content(Collections.singletonList(contentItem))
                    .build();
            messages.add(message);

            System.out.println("上传成功：" + ossUrl);
        }
        Message userMessage = Message.builder()
                .role("user")
                .content(Collections.singletonList(Message.ContentItem.builder()
                        .type("text")
                        .text("""
                                请总结视频的核心内容和关键点并生成详细的教学大纲。每个章节需包括标题、时间段及核心内容简述，,ntxt是内容输出，title是题目输出不用时间段，使用中文只要放回json其他的不用例子,分析的可以详细一点
                                [{
                                "title":xxx,
                                "next":xxx
                                },...]
                                """)
                        .build()))
                .build();

        messages.add(userMessage);

        //发送请求到ai
        String aiSendMessage = chatService.sendChat(messages);
        //存入数据库
        videosInfoService.save(VideosInfo.builder()
                .videoId(videos.getId())
                .jsonText(aiSendMessage)
                .createTime(DateUtil.date())
                .updateTime(DateUtil.date())
                .build());


        System.out.println("发送完成" + aiSendMessage);
        return aiSendMessage;
    }

    @Async
    @Override
    public void generateSubtitles(Videos videos) {
        String audioUrl = location + videos.getUrl();
        // 根据当前日期构建目录结构，例如 "2025/07/10/"
        String format = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        File folder = new File(url + format);
        // 如果该目录不存在，则创建之
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        // 获取目标文件名，这里假设我们基于audioUrl或自定义规则生成唯一的文件名
        String fileName = UUID.randomUUID() + ".vtt"; // 使用UUID生成唯一文件名

        // 构建完整的文件保存路径
        String filePath = new File(folder, fileName).getPath();
        VccUtils.convertToVtt(audioUrl, filePath);

        subtitlesService.save(Subtitles.builder()
                .videoId(videos.getId())
                .subtitleType("VTT")
                .languagType("中文")
                .subtitleUrl("/" + format + fileName)
                .designation(videos.getDescription())
                .createTime(DateUtil.date())
                .updateTime(DateUtil.date())
                .build());
    }

    //    @Async
    @Override
    public void generateEchonest(Videos videos, String next) {
        List<QwenMessage> messages = new ArrayList<>();
        boolean add = messages.add(QwenMessage.builder()
                .role("user")
                .content("帮我按照这个数据" + next + """
                        生成以下数据,其中就是词云、图表、课程类型等信息其中xxx是字符串，xx是数字，xx不要重复，coreContent的xxx长度最好是2-4字符，格式如下：
                        {
                        wordCloud:[{ name: 'xx', value: xx }, { name: 'xx', value: xx },...],
                        coreContent:[{ yAxis: ['xxx',...], series: [xx,...] }],
                        courseType:"xxx",类别就好比是java，python，前端，后端，数据库，机器学习，网络，数据分析，爬虫等等
                        }
                        """)
                .build());
        String sendChat = qwenTextService.sendChat(messages);
        JSONObject response = JSONUtil.parseObj(sendChat);
        String content = response.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getStr("content");
        String jsonData = ReUtil.get("(?s)```json\\s*(.*?)\\s*```", content, 1).trim();
        // 将提取的 JSON 字符串转换为 JSONObject
        JSONObject jsonObject = JSONUtil.parseObj(jsonData);
        System.out.println(jsonData);

        String courseType = jsonObject.getStr("courseType");
        String wordCloudStr = jsonObject.getJSONArray("wordCloud").toString();
        String coreContentStr = jsonObject.getJSONArray("coreContent").toString();

        LambdaUpdateWrapper<VideosInfo> updateWrapper = Wrappers.<VideosInfo>lambdaUpdate()
                .eq(VideosInfo::getVideoId, videos.getId())
                .set(VideosInfo::getTableText, coreContentStr)
                .set(VideosInfo::getSumUp, wordCloudStr);
        // 执行更新
        videosInfoService.update(updateWrapper);
        this.update(new LambdaUpdateWrapper<Videos>().eq(Videos::getId, videos.getId()).set(Videos::getVideoType, courseType));
    }

    @Async
    @Override
    public void testVideo(Videos videos) {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}