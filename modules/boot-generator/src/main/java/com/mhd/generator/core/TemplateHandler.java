package com.mhd.generator.core;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
public interface TemplateHandler<T extends ITemplate> {
    /**
     * 处理模板配置
     */
    void handleTemplateConfig(T template);
}
