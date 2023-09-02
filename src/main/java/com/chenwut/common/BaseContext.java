package com.chenwut.common;

/**
 * 基于ThreadLocal的封装工具类，用于保护和获取当前用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //获取值
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    //设置值
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
}