package com.mhd.generator.template;

import cn.hutool.core.map.MapUtil;
import com.mhd.generator.constant.GlobalConstant;
import com.mhd.generator.core.BaseTemplate;
import com.mhd.generator.core.ITemplate;
import com.mhd.generator.util.GeneratorUtils;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.function.Function;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
@EqualsAndHashCode(callSuper = true)
@ToString
@Data
public class DefaultVue2Template extends ITemplate {
    @Valid
    private BaseTemplate vue2IndexTemplate;
    @Valid
    private BaseTemplate vue2JsTemplate;

    private DefaultVue2Template(BaseTemplate vue2IndexTemplate, BaseTemplate vue2JsTemplate) {
        this.vue2IndexTemplate = vue2IndexTemplate;
        this.vue2JsTemplate = vue2JsTemplate;
    }

    // 提供默认配置的静态工厂方法
    public static DefaultVue2Template createDefault() {
        return new DefaultVue2Template(defaultVue2IndexTemplate(), defaultVue2JsTemplate());
    }

    // 初始化Index模板的默认配置
    private static BaseTemplate defaultVue2IndexTemplate() {
        return BaseTemplate.builder()
                .enable(true)
                .logPrefix("Vue2 Index")
                .outputDir(GlobalConstant.DEFAULT_PREFIX + "/vue2")
                .outputName("index")
                .outputFileSuffix(".vue")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_VUE2_INDEX_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    // 初始化JS模板的默认配置
    private static BaseTemplate defaultVue2JsTemplate() {
        return BaseTemplate.builder()
                .enable(true)
                .logPrefix("Vue2 JS")
                .outputDir(GlobalConstant.DEFAULT_PREFIX + "/js")
                .outputName("index")
                .outputFileSuffix(".js")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_VUE2_JS_TEMPLATE)
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    // 允许自定义Index模板
    public DefaultVue2Template vue2IndexBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.vue2IndexTemplate = GeneratorUtils.applyCustomization(vue2IndexTemplate, customizer);
        return this;
    }

    // 允许自定义JS模板
    public DefaultVue2Template vue2JsBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.vue2JsTemplate = GeneratorUtils.applyCustomization(vue2JsTemplate, customizer);
        return this;
    }
}
