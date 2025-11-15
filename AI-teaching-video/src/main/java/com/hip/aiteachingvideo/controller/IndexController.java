package com.hip.aiteachingvideo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("index")
@Tag(name = "主页")
public class IndexController {
    /**
     * 主页数据
     * @return
     */
    @GetMapping
    @Operation(summary = "获取主页")
    public String body() {
        return "Hello, AiTeachingVideo!";
    }
}
