package com.hip.aiteachingvideo.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import com.hip.aiteachingvideo.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@Tag(name = "工具类管理")
@RequestMapping("/utils")
public class UtilsController {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 生成图片验证码
     *
     * @return
     */
    @Operation(summary = "生成图片验证码")
    @GetMapping("/captcha")
    public Result generateCaptcha() {
        // 生成唯一令牌
        String token = IdUtil.randomUUID();

        // 创建验证码
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(90, 38, 4, 15);
        String code = captcha.getCode();

        // 将验证码图片转换为 Base64
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        captcha.write(os);
        String imageBase64 = Base64.encode(os.toByteArray());

        // 拼接 Data URL
        String imageData = "data:image/png;base64," + imageBase64;

        // 返回结果集
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("imageData", imageData);
        log.info("放回结果");
        redisTemplate.opsForValue().set("captcha:" + token, code, 2, TimeUnit.MINUTES);

        return Result.ok(result);
    }

    /**
     * 验证图片验证码
     *
     * @param token 令牌
     **/
    @Operation(summary = "验证图片验证码")
    @GetMapping("/captcha/validate")
    public Result validateCaptcha(@RequestParam String token, @RequestParam String code) {
        log.info("token:{}, code:{}", token, code);
        //将验证码从redis中取出
        String captcha = (String) redisTemplate.opsForValue().get("captcha:" + token);
        if (captcha == null) return Result.error("验证码已过期");
        redisTemplate.delete("captcha:" + token);
        //错误
        if (!StringUtils.equalsIgnoreCase(captcha, code)) return Result.error("验证码错误");
        System.out.println("验证码通过");
        return Result.ok("验证码通过");
    }



}
