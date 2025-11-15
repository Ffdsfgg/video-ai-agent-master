package com.hip.aiteachingvideo.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hip.aiteachingvideo.model.po.VideosInfo;
import com.hip.aiteachingvideo.service.VideosService;
import com.hip.aiteachingvideo.service.impl.VideosInfoServiceImpl;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Slf4j
@RestController
@Tag(name = "视频基本信息管理")
@RequestMapping("/videoInfo")
public class videosInfoController {
    @Autowired
    private VideosInfoServiceImpl videosInfoService;

    @Autowired
    private VideosService videosService;
    /**
     * 根据视频ID获取视频基本信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getVideosInfo(@PathVariable("id") Integer id) {
        return Result.ok(videosInfoService.getOne(new LambdaQueryWrapper<>(VideosInfo.class).eq(VideosInfo::getVideoId, id)));
    }

    /**
     * 生成教学大纲，内容概要，如时间段对应的内容
     */
    @PostMapping("/generate")
    @Operation(summary = "生成教学大纲，内容概要")
    @Transactional
    public Result generateVideoInfo(@RequestBody VideosInfo videosInfo) {
        log.info("生成教学大纲，内容概要:{}", videosInfo);
        LambdaUpdateWrapper<VideosInfo> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();

        if (isValid(videosInfo.getTeachingOutline())) {
            lambdaUpdateWrapper.set(VideosInfo::getTeachingOutline, videosInfo.getTeachingOutline());
        }
        if (isValid(videosInfo.getContentSummary())) {
            lambdaUpdateWrapper.set(VideosInfo::getContentSummary, videosInfo.getContentSummary());
        }
        if (isValid(videosInfo.getStructuredOutput())) {
            lambdaUpdateWrapper.set(VideosInfo::getStructuredOutput, videosInfo.getStructuredOutput());
        }

        lambdaUpdateWrapper.eq(VideosInfo::getVideoId, videosInfo.getVideoId());
        videosInfoService.update(lambdaUpdateWrapper);
        return Result.ok("生成成功");
    }

    private boolean isValid(String s) {
        return StrUtil.isNotBlank(s) && !"\"\"".equals(s);
    }

    /**
     * 修改视频基本信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改视频基本信息")
    @Transactional
    public Result updateVideosInfo(@PathVariable("id") Integer id,
                                   @RequestBody VideosInfo videosInfo) {
        log.info("修改视频基本信息:{}", videosInfo);
        // 处理已存在的记录
        if (!videosInfoService.lambdaQuery()
                .eq(VideosInfo::getId, id)
                .exists()) {
            return Result.error("记录不存在，无法修改");
        }
        // 只更新业务字段，不更新时间戳
        videosInfo.setId(id);
        videosInfoService.updateById(videosInfo);
        return Result.ok("修改成功");
    }

    /**
     * 视频信息分页 复合查询
     */
    @GetMapping("/page/list")
    @Operation(summary = "视频信息分页查询")
    public Result<Page<VideosInfo>> pageVideosInfo(
            @RequestParam(required = false) Integer videoId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        LambdaQueryWrapper<VideosInfo> qw = new LambdaQueryWrapper<>();


        qw.eq(videoId != null, VideosInfo::getVideoId, videoId); //视频 ID 精确匹配
        // 创建时间区间
        qw.between(startTime != null && endTime != null,
                VideosInfo::getCreateTime, startTime, endTime);
        // 关键词多字段模糊匹配
        if (StrUtil.isNotBlank(keyword)) {
            qw.and(w -> w
                    .like(VideosInfo::getTeachingOutline, keyword)
                    .or()
                    .like(VideosInfo::getContentSummary, keyword)
                    .or()
                    .like(VideosInfo::getStructuredOutput, keyword));
        }
        //倒序最新
        qw.orderByDesc(VideosInfo::getCreateTime);

        Page<VideosInfo> page = new Page<>(pageNum, pageSize);
        videosInfoService.page(page, qw);
        return Result.ok(page);
    }
}
