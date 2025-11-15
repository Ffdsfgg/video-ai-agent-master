package com.hip.aiteachingvideo.controller;

import cn.hutool.core.date.DateUtil;
import com.hip.aiteachingvideo.context.BaseContext;
import com.hip.aiteachingvideo.model.po.Knowledge;
import com.hip.aiteachingvideo.service.KnowledgeService;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "本地知识库")
@RequestMapping("/knowledge")
public class KnowledgeController {
    @Autowired
    private KnowledgeService knowledgeService;
    /**
     * 增加本地知识库
     *
     * @param knowledge 增加知识库内容
     * @return 成功或者失败
     */
    @PostMapping
    @Operation(summary = "增加知识库内容")
    public Result addKnowledge(@RequestBody Knowledge knowledge) {
        knowledge.setCreateTime(DateUtil.date());
        knowledge.setUpdateTime(DateUtil.date());
        knowledge.setUserId(Math.toIntExact(BaseContext.getCurrentId()));
        return Result.ok(knowledgeService.save(knowledge));
    }

    /**
     * 删除本地知识库
     *
     * @param id 要删除的知识库id
     * @return 成功或者失败
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除知识库内容")
    public Result deleteKnowledge(@PathVariable("id") Long id) {
        return Result.ok(knowledgeService.removeById(id));
    }

    @GetMapping
    @Operation(summary = "查询知识库内容")
    public Result queryKnowledge() {
        return Result.ok(knowledgeService.list());
    }
}