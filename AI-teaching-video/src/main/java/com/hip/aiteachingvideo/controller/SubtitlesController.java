package com.hip.aiteachingvideo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hip.aiteachingvideo.model.po.Subtitles;
import com.hip.aiteachingvideo.service.SubtitlesService;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "字幕管理")
@RequestMapping("/subtitles")
public class SubtitlesController {
    @Autowired
    private SubtitlesService subtitlesService;

    /**
     * 根据视频ID获取字幕列表
     *
     * @return
     */
    @GetMapping("/{id}")
    public Result getSubtitlesList(@PathVariable Integer id) {
        return Result.ok(subtitlesService.list(new LambdaQueryWrapper<>(Subtitles.class).eq(Subtitles::getVideoId, id)));
    }

    /**
     * 修改字幕信息
     */
    @PutMapping("/{id}")
    public Result updateSubtitles(@PathVariable Integer id, @RequestBody Subtitles subtitles) {
        subtitles.setId(id);
        return Result.ok( subtitlesService.updateById(subtitles));
    }

    /**
     * 删除字幕信息
     */
    @DeleteMapping("/{id}")
    public Result deleteSubtitles(@PathVariable Integer id) {
        return Result.ok(subtitlesService.removeById(id));
    }

    /**
     * 新增字幕信息
     * @param subtitles
     * @return
     */
    @PostMapping
    public Result addSubtitles(@RequestBody Subtitles subtitles) {
        return Result.ok(subtitlesService.save(subtitles));
    }

    /**
     * 字幕分页查询
     */
    @GetMapping("/page/list")
    public Result<Page<Subtitles>> pageSubtitles(
            @RequestParam(required = false) Integer videoId,
            @RequestParam(required = false) String subtitleType,
            @RequestParam(required = false) String languageType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<Subtitles> qw = new LambdaQueryWrapper<>();
        qw.eq(videoId != null, Subtitles::getVideoId, videoId)
                .eq(StringUtils.isNotBlank(subtitleType), Subtitles::getSubtitleType, subtitleType)
                .eq(StringUtils.isNotBlank(languageType), Subtitles::getLanguagType, languageType).
                like(StringUtils.isNotBlank(description), Subtitles::getDesignation, description)
                .orderByDesc(Subtitles::getCreateTime);

        Page<Subtitles> page = new Page<>(pageNum, pageSize);
        subtitlesService.page(page, qw);
        return Result.ok(page);
    }



}
