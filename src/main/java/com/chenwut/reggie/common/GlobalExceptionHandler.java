package com.chenwut.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //处理异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class) //指定能够处理的异常类型
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException e) {
//        e.printStackTrace();//打印堆栈中的异常信息
        log.error(e.getMessage());
        //捕获到异常之后，响应一个标准的Result

        /**
         * 细化处理异常
         */

        //1.如果包含"Duplicate entry",代表出现了条目重复问题
        if (e.getMessage().contains("Duplicate entry")) {
            /**
             *  切分字符串 Duplicate entry 'Jerry' for key 'employee.idx_username'
             *  获取用户名username
             */
            String[] split = e.getMessage().split(" ");

            return Result.error("“ " + split[2] + " ”已存在，不可使用，请更换");
        }

        return Result.error("未知错误，请联系管理员xxx-xxxxx");
    }

    //处理业务异常
    @ExceptionHandler(CustomException.class) //指定能够处理的异常类型
    public Result<String> customExceptionHandler(CustomException e) {
//        e.printStackTrace();//打印堆栈中的异常信息
        log.error(e.getMessage());

        //捕获到异常之后，响应一个标准的Result
        return Result.error(e.getMessage());
    }


}
