package com.musicrank.common;

import lombok.Data;

/**
 * 统一 API 响应包装类
 * 所有接口返回统一格式，方便前端统一处理
 */
@Data
public class Result<T> {

    /** 状态码：200 成功，其他为错误码 */
    private int code;

    /** 返回消息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 成功响应 ====================

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = message;
        r.data = data;
        return r;
    }

    public static Result<Void> success() {
        Result<Void> r = new Result<>();
        r.code = 200;
        r.message = "success";
        return r;
    }

    // ==================== 失败响应 ====================

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(int code, String message, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = data;
        return r;
    }

    // ==================== 常用错误快捷方法 ====================

    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> Result<T> conflict(String message) {
        return error(409, message);
    }

    public static <T> Result<T> serverError(String message) {
        return error(500, message);
    }
}
