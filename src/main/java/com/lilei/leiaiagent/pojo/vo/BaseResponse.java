package com.lilei.leiaiagent.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * 
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一响应结果")
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码", example = "0")
    private int code;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "响应消息", example = "ok")
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 成功响应（自定义数据）
     *
     * @param data 响应数据
     * @return 统一响应对象
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "操作成功");
    }

    /**
     * 成功响应（自定义数据和消息）
     *
     * @param data 响应数据
     * @param message 响应消息
     * @return 统一响应对象
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(0, data, message);
    }

    /**
     * 错误响应（自定义错误码和消息）
     *
     * @param code 错误码
     * @param message 错误消息
     * @return 统一响应对象
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }
}

