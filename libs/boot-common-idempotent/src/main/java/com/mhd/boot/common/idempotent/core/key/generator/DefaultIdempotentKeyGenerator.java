package com.mhd.boot.common.idempotent.core.key.generator;

import com.mhd.boot.common.idempotent.core.annotation.Idempotent;
import com.mhd.boot.common.idempotent.core.util.SpelUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * @author zhao-hao-dong

 */
public class DefaultIdempotentKeyGenerator implements IdempotentKeyGenerator {
    @NonNull
    @Override
    public String generate(Idempotent idempotent, JoinPoint point) {
        String prefix = idempotent.prefix();
        // 从幂等注解中获取配置的"唯一标识表达式"（SpEL格式）
        String uniqueExpression = idempotent.uniqueExpression();
        if (!StringUtils.hasText(uniqueExpression)) {
            return idempotent.prefix();
        } else {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Object[] args = point.getArgs();
            StandardEvaluationContext spelContext = SpelUtils.getSpelContext(point.getTarget(), method, args);

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                //把HttpServletRequest对象放入SpEL上下文，命名为"request" → 表达式中可通过#request获取请求参数
                spelContext.setVariable("request", requestAttributes.getRequest());
            }
            // 解析SpEL表达式，得到字符串类型的唯一值（比如解析"#userId"得到"123"）
            String uniqueStr = SpelUtils.parseValueToString(spelContext, uniqueExpression);
            // 拼接前缀和解析后的唯一值
            return prefix + ":" + uniqueStr;
        }
    }
}
