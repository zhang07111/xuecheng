package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableBiMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一的异常捕获类
 */

@ControllerAdvice //控制器增强
public class ExceptionCatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    //定义map
    private static ImmutableBiMap<Class<? extends Throwable>, ResultCode> EXCEPTION;
    //定义map的builder对象,去构建ImmutableBiMap
    public static ImmutableBiMap.Builder<Class<? extends Throwable>, ResultCode> builder = ImmutableBiMap.builder();


    //捕获CustomException 的异常
    @ExceptionHandler(CustomException.class)
    @ResponseBody

    public ResponseResult customException(CustomException c) {
        //记录日志
        LOGGER.error("catch exception:{}", c.getMessage());

        ResultCode resultCode = c.getResultCode();
        return new ResponseResult(resultCode);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception e) {
        //记录日志
        LOGGER.error("catch exception:{}", e.getMessage());
        if (EXCEPTION == null) {
            EXCEPTION = builder.build();//构建成功
        }
        //从EXCEPTION中找异常类型对应的异常代码,找到了将错误代码响应给用户
        ResultCode resultCode = EXCEPTION.get(e.getClass());
        if (resultCode != null) {
            return new ResponseResult(resultCode);
        } else {
            //返回99999 的错误信息
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }

    }


    static {
        builder.put(HttpMediaTypeNotSupportedException.class, CommonCode.INVALD_PARAM);
    }

}
