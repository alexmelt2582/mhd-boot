package com.mhd.generator;

import com.mhd.generator.config.BackendConfig;
import com.mhd.generator.config.BackendGenerator;
import com.mhd.generator.constant.GeneratorConstant;

/**
 * @author zhao-hao-dong
 **/
public class JavaGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3306/mhd-boot?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&autoReconnect=true";
        String username = "root";
        String password = "root";
        String outputDir = System.getProperty("user.dir") + "/src/main/java";
        String[] tableNames = new String[]{"hzb_notice_rule"};
        String[] tablePrefixList = new String[]{"hzb_"};
        BackendConfig config = BackendConfig.createDefault()
                .globalConfigBuilder(globalConfigBuilder -> globalConfigBuilder
                        .author("zhao-hao-dong")
                        .outputDir(outputDir)
                        .datePattern("yyyy-MM-dd"))
                .packageConfigBuilder(packageConfigBuilder -> packageConfigBuilder
                        .parent("com.mhd.alert")
                        .entity("entity")
                        .xml(GeneratorConstant.DEFAULT_PREFIX + "/mapper")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .queryReqDTO("model.dto")
                        .saveReqDTO("model.dto")
                        .vo("model.vo"))
                .dbConfigBuilder(dbConfigBuilder -> dbConfigBuilder
                        .dbUrl(url)
                        .dbUsername(username)
                        .dbPassword(password)
                        .dbTablePrefix(tablePrefixList)
                        .dbTableList(tableNames))
                .controllerBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .serviceBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .serviceImplBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .mapperBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .entityBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .xmlBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .queryReqDTOBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .saveReqDTOBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .voBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                // 用户自定义上下文参数（合并时优先级高于系统值，moduleName 默认不自动推导）
                .extraParam("moduleName", "通知规则");
        new BackendGenerator(config).generate();
    }
}
