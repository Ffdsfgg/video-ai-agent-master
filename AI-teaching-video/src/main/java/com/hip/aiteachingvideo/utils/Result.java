package com.hip.aiteachingvideo.utils;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 统一 API 响应结果封装类，适合 RESTful 接口统一返回格式
 */
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    // 构造带数据的 Result 实例
    private Result(T data) {
        this.data = data;
    }

    // 全参构造器（供内部使用）
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 静态构建方法 ============

    /**
     * 构建一个空的 Result 对象（无 data）
     */
    public static <T> Result<T> build() {
        return new Result<>(null);
    }

    /**
     * 构建一个包含 data 的 Result
     */
    public static <T> Result<T> build(T data) {
        return new Result<>(data);
    }

    /**
     * 构建完整 Result 对象
     */
    public static <T> Result<T> build(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 构建带 ResultCodeEnum 的 Result
     */
    public static <T> Result<T> build(ResultCodeEnum resultCodeEnum, T data) {
        return build(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), data);
    }

    // ========== 快捷成功/失败方法 ============

    /**
     * 操作成功并携带数据
     */
    public static <T> Result<T> ok(T data) {
        return build(ResultCodeEnum.SUCCESS, data);
    }


    /**
     * 操作失败，自定义消息
     */
    public static <T> Result<T> error(String message) {
        return build(ResultCodeEnum.ERROR.getCode(), message, null);
    }

    /**
     * 操作失败，指定错误码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return build(code, message, null);
    }

    /**
     * 参数校验失败
     */
    public static <T> Result<T> badRequest(String message) {
        return build(ResultCodeEnum.BAD_REQUEST.getCode(), message, null);
    }

    /**
     * 未授权访问
     */
    public static <T> Result<T> unauthorized(String message) {
        return build(ResultCodeEnum.UNAUTHORIZED.getCode(), message, null);
    }

    /**
     * 权限不足
     */
    public static <T> Result<T> forbidden(String message) {
        return build(ResultCodeEnum.FORBIDDEN.getCode(), message, null);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String message) {
        return build(ResultCodeEnum.NOT_FOUND.getCode(), message, null);
    }

    // ========== 链式调用方法 ============

    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }

    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    // ========== Getter & Setter ============

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }



    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}