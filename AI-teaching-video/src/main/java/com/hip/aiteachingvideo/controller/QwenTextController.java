package com.hip.aiteachingvideo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hip.aiteachingvideo.model.dto.QwenMessage;
import com.hip.aiteachingvideo.model.po.VideosInfo;
import com.hip.aiteachingvideo.service.QwenTextService;
import com.hip.aiteachingvideo.service.VideosInfoService;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@Tag(name = "ai请求服务")
@RequestMapping("/qwenTextAI")
public class QwenTextController {

    @Value("${hip.url}")
    private String url;
    @Autowired
    private QwenTextService qwenTextService;
    @Autowired
    private VideosInfoService videosInfoService;

    /**
     * 流式输出
     *
     * @param subtitlesUrl 信息
     * @return 流式
     */
    @Operation(summary = "AI流式输出")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<byte[]> streamChat(String subtitlesUrl, String id, String next) {
        log.info("streamChat subtitlesUrl:{},id:{},next:{}", subtitlesUrl, id, next);
        List<QwenMessage> messages = new ArrayList<>();
        messages.add(QwenMessage.builder()
                .role("system")
                .content("你是一个专业的视频专家，可以根据VTT文件和JSON文件，分析视频的结构和内容。")
                .build());
        VideosInfo byId = videosInfoService.getOne(new LambdaQueryWrapper<>(VideosInfo.class).eq(VideosInfo::getVideoId, Integer.parseInt(id)));
        File file = new File(url + subtitlesUrl);
        String contentnext = "";
        //读取url参数
        try {
            // 读取文件内容为字符串
            contentnext = new String(Files.readAllBytes(Paths.get(file.toURI())));
            System.out.println(contentnext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 调用服务
        contentnext += "这上面是一个VTT文件,请根据这上面的内容结合" + byId.getJsonText() + "进行分析" + "输出" + next + "的结果，分析vtt文件和JSON文件。" + """
                建议：
                    1.请先阅读VTT文件，了解视频的结构和内容。
                    2.结合JSON文件，分析视频的结构和内容，分析里面的内容。
                    3.结合VTT文件和JSON文件，分析视频的结构和内容。
                    3.不要超出VTT文件的时间范围，输出的时间严格按照VTT文件来，不同步JSON文件的时间。
                    4.不要输出JSON和Markdown表格的内容，直接生成可以展示的输出。
                """;
        messages.add(QwenMessage.builder()
                .role("user")
                .content(contentnext)
                .build());
        return qwenTextService.streamChat(messages)
                .map(content -> content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 普通输出
     * @param messages 信息
     * @return 字符串
     */
    @Operation(summary = "AI普通输出")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Result chat(@RequestBody List<QwenMessage> messages) {
        log.info("chat messages:{}", messages);
        return Result.ok(qwenTextService.sendChat(messages));
    }


    /**
     * 流式输出
     *
     * @param messages 信息
     * @return 流式
     */
    @Operation(summary = "AI流式输出")
    @PostMapping(value = "/stream/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<byte[]> streamChatAi(@RequestBody List<QwenMessage> messages) {
        return qwenTextService.streamChatAi(messages)
                .map(content -> content.getBytes(StandardCharsets.UTF_8));
    }
}