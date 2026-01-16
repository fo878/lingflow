package com.lingflow.dto;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> error(int code, String message) {
        Response<T> response = new Response<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> Response<T> error(String message) {
        return error(500, message);
    }
}
