package com.hip.aiteachingvideo.config;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hip.aiteachingvideo.context.BaseContext;
import com.hip.aiteachingvideo.utils.JwtUtil;
import com.hip.aiteachingvideo.utils.Result;
import com.hip.aiteachingvideo.utils.ResultCodeEnum;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
/**
 * 拦截器，校验jwt
 */
public class JwtTokenInterceptor implements HandlerInterceptor {
    @Value("${hip.jwt.admin-secret-key}")
    private String SecretKey;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //1、从请求头中获取令牌
        String token = request.getHeader("Authorization");
        //2、校验令牌
        log.info("token:{}", token);
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(SecretKey, token);
            Long empId = Long.valueOf(claims.get("userId").toString());
            BaseContext.setCurrentId(empId);
            System.out.println("empId:" + empId);
            return true;
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");

            Result result = Result.build(ResultCodeEnum.TOKEN_NOTLOGIN, "token 校验失败");

            response.getWriter().write(JSONUtil.toJsonStr(result));
            return false;
        }
    }
}