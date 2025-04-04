package com.ticketing.common.response;

import lombok.Data;

/**
 * 通用响应对象
 */
@Data
public class Result<T> {
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    /**
     * 失败
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 系统错误
     */
    public static <T> Result<T> systemError() {
        return error(500, "系统错误");
    }
    
    /**
     * 参数错误
     */
    public static <T> Result<T> paramError(String message) {
        return error(400, message);
    }
    
    /**
     * 业务错误
     */
    public static <T> Result<T> businessError(String message) {
        return error(600, message);
    }
} 