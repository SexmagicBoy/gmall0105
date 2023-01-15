package com.atguigu.gmall.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 使用范围 - 方法上
@Target(ElementType.METHOD)
// 生效范围 - 程序运行时
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    // 用户是否必须登录成功才能使用这个方法
    boolean mustLogin() default true;
}
