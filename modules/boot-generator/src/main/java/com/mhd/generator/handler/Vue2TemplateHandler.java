package com.mhd.generator.handler;


import com.mhd.generator.core.TemplateHandler;
import com.mhd.generator.template.DefaultVue2Template;
import com.mhd.generator.util.GeneratorUtils;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
public class Vue2TemplateHandler implements TemplateHandler<DefaultVue2Template> {
    @Override
    public void handleTemplateConfig(DefaultVue2Template template) {
        GeneratorUtils.handleTemplateToFile(template.getVue2IndexTemplate());
        GeneratorUtils.handleTemplateToFile(template.getVue2JsTemplate());
    }
}
