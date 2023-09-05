package com.sky.handler;

import com.graphbuilder.math.func.LgFunction;
import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理sql异常
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result sqlExceptionHandler(SQLIntegrityConstraintViolationException exception){
        String message = exception.getMessage();
        //Duplicate entry 'lisi' for key 'employee.idx_username'
        log.info(message);
        // 判断异常是否为 插入唯一时 报的异常!
        if (message.contains("Duplicate entry")){

            String[] s = message.split(" ");

            String username = s[2];
            log.info("已存在的用户名:{}",username);
            return Result.error(username + MessageConstant.ACCOUNT_ALREADY_EXISTS);

        }else{
            return Result.error("未知错误");
        }



    }
}
