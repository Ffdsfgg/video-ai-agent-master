package com.lilei.leiaiagent.config;

import com.lilei.leiaiagent.constant.ErrorCode;
import com.lilei.leiaiagent.pojo.vo.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 响应工具类 - 用于统一创建响应对象
 */
@Schema(description = "响应工具类")
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 成功（自定义消息）
     *
     * @param data 数据
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(0, data, message);
    }

    /**
     * 失败（使用错误枚举）
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
    }

    /**
     * 失败（使用自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误信息
     * @return 响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败（使用错误枚举和自定义消息）
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @return 响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
    
    /**
     * 失败（使用错误码和数据）
     *
     * @param code 错误码
     * @param data 数据
     * @param message 错误信息
     * @return 响应
     */
    public static <T> BaseResponse<T> error(int code, T data, String message) {
        return new BaseResponse<>(code, data, message);
    }
}
