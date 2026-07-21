package com.mhd.boot.common.web.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import com.mhd.boot.common.enums.ErrorCodeEnum;
import com.mhd.boot.common.exception.BusinessException;
import com.mhd.boot.common.respnsedata.BaseResponse;
import com.mhd.boot.common.respnsedata.BaseResultUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器
 *
 * @author zhao-hao-dong
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 构建请求上下文信息，便于快速定位问题
     *
     * @param request HTTP请求对象
     * @return 格式化的请求上下文字符串
     */
    private String buildRequestContext(HttpServletRequest request) {
        StringBuilder context = new StringBuilder();
        //// 获取客户端真实IP
        //String clientIp = IpUtils.getClientIp(request);
        //context.append("[ClientIP:").append(clientIp).append("]");
        context.append("[URI:").append(request.getRequestURI()).append("]");
        context.append("[Method:").append(request.getMethod()).append("]");
        return context.toString();
    }

    //
    ///**
    // * 处理 Spring Security 权限不足的异常
    // * 来源是，使用 @PreAuthorize 注解，AOP 进行权限拦截
    // */
    //@ExceptionHandler(value = AccessDeniedException.class)
    //public BaseResponse<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
    //    throw ex; // 直接抛出异常，交给 Spring Security 处理
    //}

    /**
     * 业务异常处理
     *
     * @param request HTTP请求对象
     * @param e       业务异常
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handlerBusinessException(HttpServletRequest request, BusinessException e) {
        String requestContext = buildRequestContext(request);
        log.error("[BusinessException] {} ", requestContext, e);
        return BaseResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * Validation参数校验异常处理
     *
     * @param request HTTP请求对象
     * @param e       参数校验异常
     * @return 统一响应结果
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public BaseResponse<?> handleMethodArgumentNotValidException(HttpServletRequest request, Exception e) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        StringBuilder errorInfo = new StringBuilder();
        BindingResult bindingResult = null;
        // 获取校验结果
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        }
        if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        }
        // 构建详细的校验错误信息
        if (bindingResult != null && CollectionUtil.isNotEmpty(bindingResult.getFieldErrors())) {
            for (int i = 0; i < bindingResult.getFieldErrors().size(); i++) {
                if (i > 0) {
                    errorInfo.append(",");
                }
                FieldError fieldError = bindingResult.getFieldErrors().get(i);
                errorInfo.append(fieldError.getField()).append(" :").append(fieldError.getDefaultMessage());
            }
        }
        log.error("请求地址'[{}] {}',参数校验异常: {}", method, requestURI, errorInfo, e);
        return BaseResultUtils.error(ErrorCodeEnum.VALID_FAILED, errorInfo.toString());
    }

    ///**
    // * Validation自定义校验异常处理
    // *
    // * @param request HTTP请求对象
    // * @param ex      约束违反异常
    // * @return 统一响应结果
    // */
    //@ExceptionHandler(ConstraintViolationException.class)
    //public BaseResponse<?> handleConstraintViolationException(HttpServletRequest request,
    //                                                          ConstraintViolationException ex) {
    //    String requestContext = buildRequestContext(request);
    //    String exceptionInfo = buildExceptionStackTrace(ex);
    //    String errorInfo = ex.getConstraintViolations().stream()
    //            .map(violation -> violation.getPropertyPath() + " :" + violation.getMessage())
    //            .collect(Collectors.joining(","));
    //    log.warn("[ValidationException] {} {} [ViolatedConstraints:{}] [ViolationCount:{}]",
    //            requestContext, exceptionInfo, errorInfo, ex.getConstraintViolations().size(), ex);
    //    return BaseResultUtils.error(ErrorCodeEnum.PARAM_ERROR, errorInfo);
    //}

    /**
     * 请求方式不支持，返回 405
     *
     * @param ex HTTP请求方法不支持异常
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public BaseResponse<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, ex.getMethod());
        return BaseResultUtils.error(HttpStatus.HTTP_BAD_METHOD, ex.getMessage());
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponse<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.error("请求地址'[{}] {}',请求参数类型不匹配,发生系统异常", method, requestURI);
        return BaseResultUtils.error(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), e.getValue()));
    }

    /**
     * 找不到路由
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public BaseResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.error("请求地址'[{}] {}',地址不存在,发生系统异常", method, requestURI);
        return BaseResultUtils.error(HttpStatus.HTTP_NOT_FOUND, e.getMessage());
    }

    /**
     * 处理SpringMVC请求参数缺失异常
     *
     * @param ex 请求参数缺失异常
     * @return 统一响应结果
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public BaseResponse<?> missingServletRequestParameterExceptionHandler(HttpServletRequest request, MissingServletRequestParameterException ex) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.error("请求地址'[{}] {}',请求参数缺失,发生系统异常", method, requestURI);
        return BaseResultUtils.error(String.format("请求参数缺失:%s", ex.getParameterName()));
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public BaseResponse<Void> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.error("请求地址'[{}] {}',请求路径中缺少必需的路径变量,发生系统异常", method, requestURI);
        return BaseResultUtils.error(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }


    /**
     * 处理请求体读取异常（JSON格式错误、类型转换失败、请求参数格式非法、字段类型不匹配等）
     *
     * @param e 请求体读取异常
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<Void> methodArgumentTypeInvalidFormatExceptionHandler(HttpServletRequest request, HttpMessageNotReadableException e) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        //if (e.getCause() instanceof InvalidFormatException) {
        //    InvalidFormatException invalidFormatException = (InvalidFormatException) e.getCause();
        //    log.warn("[JsonFormatException] {} {} [InvalidValue:{}] [TargetType:{}]",
        //            requestContext, exceptionInfo, invalidFormatException.getValue(),
        //            invalidFormatException.getTargetType().getSimpleName(), ex);
        //}
        log.error("请求地址'[{}] {}',参数解析失败,发生系统异常", method, requestURI);
        return BaseResultUtils.error(HttpStatus.HTTP_BAD_REQUEST, "请求参数格式错误：" + e.getMostSpecificCause().getMessage());
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     *
     * @param request HTTP请求对象
     * @param ex      系统异常
     * @return 统一响应结果
     */
    @ExceptionHandler(value = Exception.class)
    public BaseResponse<Void> defaultExceptionHandler(HttpServletRequest request, Throwable ex) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.error("请求地址'[{}] {}',发生系统未知异常", method, requestURI, ex);
        return BaseResultUtils.error(ErrorCodeEnum.FAIL, ex.getMessage());
    }
}
