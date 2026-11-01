package com.liu.picturebackend.exception;

/**
 * 异常处理工具类
 */
public class ThrowUtils {

    /**
     * 条件成立抛出异常
     *
     * @param condition 异常条件
     * @param exception 错误信息
     */
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition 异常条件
     * @param errorCode 自定义错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition 异常条件
     * @param errorCode 自定义错误信息
     * @param message   自定义消息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }

}
