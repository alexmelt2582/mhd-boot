package com.mhd.boot.common.idempotent.core.util;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * @author zhao-hao-dong

 */
public class SpelUtils {
    public static final ExpressionParser PARSER = new SpelExpressionParser();
    public static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private SpelUtils() {
    }

    public static String parseValueToString(Object rootObject, Method method, Object[] args, String spelExpression) {
        StandardEvaluationContext context = getSpelContext(rootObject, method, args);
        return parseValueToString(context, spelExpression);
    }

    public static StandardEvaluationContext getSpelContext(Object rootObject, Method method, Object[] args) {
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args, PARAMETER_NAME_DISCOVERER);
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; ++i) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        return context;
    }

    public static String parseValueToString(StandardEvaluationContext context, String spelExpression) {
        return PARSER.parseExpression(spelExpression).getValue(context, String.class);
    }
}
