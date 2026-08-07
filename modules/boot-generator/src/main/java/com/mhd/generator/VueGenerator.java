package com.mhd.generator;

import com.mhd.generator.core.CodeGenerator;
import com.mhd.generator.template.DefaultVue2Template;

import java.util.Map;

/**
 * @author zhao-hao-dong
 **/
public class VueGenerator {
    public static void main(String[] args) {
        DefaultVue2Template defaultVue2Template = DefaultVue2Template.createDefault()
                .vue2IndexBuilder(baseTemplateBuilder -> baseTemplateBuilder
                        .enable(false)
                        .paramMap(
                                Map.of(
                                        "name", "test",
                                        "nameToUpper", "test",
                                        "urlPath", 18
                                )))
                .vue2JsBuilder(baseTemplateBuilder -> baseTemplateBuilder
                        .enable(true)
                        .paramMap(
                                Map.of(
                                        "name", "test",
                                        "nameToUpper", "test",
                                        "urlPath", 18
                                )));
        CodeGenerator.create()
                .execute(
                        defaultVue2Template
                );
    }
}
