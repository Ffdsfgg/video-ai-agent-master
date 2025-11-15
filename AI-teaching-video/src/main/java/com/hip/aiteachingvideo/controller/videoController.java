package com.hip.aiteachingvideo.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hip.aiteachingvideo.context.BaseContext;
import com.hip.aiteachingvideo.model.po.Videos;
import com.hip.aiteachingvideo.model.po.VideosInfo;
import com.hip.aiteachingvideo.service.VideosInfoService;
import com.hip.aiteachingvideo.service.VideosService;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@Slf4j
@RestController
@Tag(name = "视频管理")
@RequestMapping("/video")
public class videoController {
    @Autowired
    private VideosService videosService;
    @Autowired
    private VideosInfoService videosInfoService;

    @Value("${hip.url}")
    private String url;

    /**
     * 增加视频
     */
    @SneakyThrows
    @PostMapping
    @Operation(summary = "增加视频")
    @Transactional
    public Result addVideo(@RequestBody Videos videos) {
        log.info("增加视频:{}", videos);
        String url = videos.getUrl();
        System.out.println(videos);
        String type = "";
        if (url != null && !url.isEmpty()) {
            String[] parts = url.split("\\.");
            if (parts.length > 1) {
                type = parts[parts.length - 1];
            }
        }
        videos.setType(type);
        DateTime date = DateUtil.date();
        videos.setUserId(1);
        videos.setCreateTime(date);
        videos.setUpdateTime(date);
        videos.setStatus(0);
        videos.setUserId(Math.toIntExact(BaseContext.getCurrentId()));
        boolean save = videosService.save(videos);
        if (!save) return Result.error("增加失败");
        // 生成字幕
        videosService.generateSubtitles(videos);
        //请求ai测试视频
        videosService.testVideo(videos);
        // 视频请求ai生成基本信息
        String s = videosService.clipAndUpload(videos);
        //生成echonest信息和课程信息
        videosService.generateEchonest(videos, s);

        return Result.ok("增加成功");
    }

    /**
     * 获取视频列表,根据用户id
     */
    @GetMapping
    @Operation(summary = "获取视频列表")
    public Result getVideoList() {
        log.info("获取视频列表");
        return Result.ok(videosService.list(new LambdaQueryWrapper<>(Videos.class).eq(Videos::getUserId, BaseContext.getCurrentId()).eq(Videos::getStatus, 0)));
    }

    /**
     * 根据id获取视频信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id获取视频信息")
    public Result getVideoById(@PathVariable("id") Integer id) {
        log.info("根据id获取视频信息:{}", id);
        return Result.ok(videosService.getById(id));
    }

    /**
     * 放回一个公开视频
     */
    @GetMapping("/public")
    @Operation(summary = "公开视频")
    public Result getPublicVideo() {
        log.info("开视频");
        return Result.ok(videosService.list(new LambdaQueryWrapper<>(Videos.class).eq(Videos::getIsPublic, 0).eq(Videos::getStatus, 0)));
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除视频")
    @Transactional
    public Result deleteVideo(@PathVariable("id") Integer id) {
        log.info("删除视频:{}", id);
        //先判断当前用户是否有权限删除该视频
        Videos videos = videosService.getById(id);
        log.info("当前用户:{},{}", BaseContext.getCurrentId(), videos.getUserId());
        if (videos.getUserId().equals(BaseContext.getCurrentId())) return Result.error("你没有权限删除该视频");
        videosService.removeById(id);
        videosInfoService.remove(new LambdaQueryWrapper<>(VideosInfo.class).eq(VideosInfo::getVideoId, videos.getId()));
        // 删除视频文件
        new File(url + videos.getUrl()).delete();
        return Result.ok("删除成功");
    }

    /**
     * 修改视频信息
     */
    @PutMapping("/update")
    @Operation(summary = "修改视频信息")
    @Transactional
    public Result updateVideo(@RequestBody Videos videos) {
        log.info("修改视频信息:{}", videos);
        //先判断当前用户是否有权限修改该视频
        Videos oldVideos = videosService.getById(videos.getId());
        log.info("当前用户:{},{}", BaseContext.getCurrentId(), oldVideos.getUserId());
        if (oldVideos.getUserId().equals(BaseContext.getCurrentId())) return Result.error("你没有权限修改该视频");
        videos.setUpdateTime(DateUtil.date());
        boolean update = videosService.updateById(videos);
        if (!update) return Result.error("修改失败");
        return Result.ok("修改成功");
    }

    @GetMapping("/open")
    @Operation(summary = "可以使用的视频")
    public Result getVideoOpen() {
        return Result.ok(videosService.list(
                new LambdaQueryWrapper<Videos>()
                        .eq(Videos::getIsPublic, 0)
                        .or()
                        .eq(Videos::getUserId, BaseContext.getCurrentId())
                        .eq(Videos::getStatus, 0)
        ));
    }

    /**
     * 视频分页 模糊搜索
     */
    @GetMapping("/page")
    @Operation(summary = "视频分页查询")
    public Result<Page<Videos>> pageVideo(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String videoType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isPublic,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        LambdaQueryWrapper<Videos> qw = new LambdaQueryWrapper<>();


        qw.like(StringUtils.isNotBlank(name), Videos::getName, name); // 模糊搜索：文件名称
        qw.eq(StringUtils.isNotBlank(videoType), Videos::getVideoType, videoType);// 视频类别
        qw.eq(status != null, Videos::getStatus, status); // 状态
        qw.eq(isPublic != null, Videos::getIsPublic, isPublic);//公开课
        qw.orderByDesc(Videos::getCreateTime);// 倒序最新

        Page<Videos> page = new Page<>(pageNum, pageSize);
        videosService.page(page, qw);

        return Result.ok(page);
    }
}