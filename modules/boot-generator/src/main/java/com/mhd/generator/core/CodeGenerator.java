package com.mhd.generator.core;

import com.mhd.generator.constant.GlobalConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
public class CodeGenerator {
    private static final Logger log = LoggerFactory.getLogger(CodeGenerator.class + GlobalConstant.LOG_PREFIX);

    private static final CodeGenerator INSTANCE = new CodeGenerator();

    private CodeGenerator() {
    }

    static {
        // 移除JUL默认的处理器
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        // 添加SLF4J桥接处理器
        SLF4JBridgeHandler.install();
    }

    public static CodeGenerator create() {
        return INSTANCE;
    }

    @SafeVarargs
    public final <T extends ITemplate> void execute(T... templates) {
        if (templates == null || templates.length == 0) {
            throw new IllegalArgumentException("At least one template must be provided.");
        }
        for (T template : templates) {
            if (template == null) {
                throw new IllegalArgumentException("template cannot be null.");
            }
            TemplateHandler<T> handler = TemplateHandlerManager.getTemplateHandler(template);
            handler.handleTemplateConfig(template);
        }
    }
}
