package com.opspilot.common;

import com.opspilot.common.result.ErrorCode;
import com.opspilot.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDenied(AccessDeniedException e) {
        return Result.fail(ErrorCode.FORBIDDEN, "当前账号没有执行该操作的权限");
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public Result<String> handleUnauthenticated(AuthenticationCredentialsNotFoundException e) {
        return Result.fail(ErrorCode.UNAUTHORIZED, "请先登录");
    }

    @ExceptionHandler(Exception.class)
    public Result<String>handleException(Exception e){
        log.error("Unhandled request error", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "请求处理失败");

    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() == null
                ? "请求参数不合法"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(ErrorCode.VALIDATION_ERROR, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        return Result.fail(ErrorCode.BAD_REQUEST, e.getMessage());
    }
}
