package com.liu.picturebackend.common;

import com.liu.picturebackend.exception.ErrorCode;
import lombok.Data;

/**
 * 全局相应封装类
 *
 * @param <T>
 */
@Data
public class BaseResponse<T> {

    public int code;

    public T date;

    public String message;

    public BaseResponse(int code, T date, String message) {
        this.code = code;
        this.date = date;
        this.message = message;
    }

    public BaseResponse(int code, T date) {
        this(code, date, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
