package com.mhd.generator.core;


import com.mhd.generator.handler.BackendTemplateHandler;
import com.mhd.generator.handler.Vue2TemplateHandler;
import com.mhd.generator.template.DefaultBackendTemplate;
import com.mhd.generator.template.DefaultVue2Template;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
public class TemplateHandlerManager {
    private final Map<Class<? extends ITemplate>, TemplateHandler<? extends ITemplate>> handlerMap = new HashMap<>();
    private static final TemplateHandlerManager INSTANCE = new TemplateHandlerManager();

    // 私有构造方法，防止外部直接实例化
    private TemplateHandlerManager() {
    }

    static {
        registerDefaultHandlers();
    }

    // 默认注册器
    private static void registerDefaultHandlers() {
        // 注册默认的处理类
        registerHandler(DefaultVue2Template.class, new Vue2TemplateHandler());
        registerHandler(DefaultBackendTemplate.class, new BackendTemplateHandler());
        // 如果有更多默认处理类，继续在这里注册
    }

    public static <T extends ITemplate> void registerHandler(Class<T> configClass, TemplateHandler<T> handler) {
        INSTANCE.doRegisterHandler(configClass, handler);
    }

    public static <T extends ITemplate> TemplateHandler<T> getTemplateHandler(T templateConfig) {
        return INSTANCE.doGetTemplateHandler(templateConfig);
    }

    private <T extends ITemplate> void doRegisterHandler(Class<T> configClass, TemplateHandler<T> handler) {
        // 校验是否已经注册
        if (handlerMap.containsKey(configClass)) {
            throw new IllegalArgumentException("Handler for class " + configClass.getName() + " is already registered.");
        }
        // 校验 handler 是否为空
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null.");
        }
        handlerMap.put(configClass, handler);
    }

    public <T extends ITemplate> TemplateHandler<T> doGetTemplateHandler(T templateConfig) {
        TemplateHandler<?> handler = handlerMap.get(templateConfig.getClass());
        if (handler == null) {
            //throw new IllegalArgumentException("No handler registered for class: " + templateConfig.getClass().getName());
            throw new IllegalArgumentException("没有为类注册处理程序：" + templateConfig.getClass().getName());
        }
        // 显式类型转换
        @SuppressWarnings("unchecked")
        TemplateHandler<T> typedHandler = (TemplateHandler<T>) handler;
        return typedHandler;
    }
}
