package com.hip.aiteachingvideo.utils;

/**
 * 统一返回结果状态信息类
 */
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    ERROR(500, "系统异常"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(427, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    TOKEN_NOTLOGIN(401, "找不到token");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}