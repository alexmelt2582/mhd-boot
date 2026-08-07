package com.mhd.generator;

import com.mhd.generator.config.BackendConfig;
import com.mhd.generator.constant.GeneratorConstant;

/**
 * @author zhao-hao-dong
 **/
public class JavaGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3306/mhd-boot?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&autoReconnect=true";
        String username = "root";
        String password = "Z4,d.3[j";
        String outputDir = System.getProperty("user.dir") + "/src/main/java";
        String[] tableNames = new String[]{"hzb_notice_rule"};
        String[] tablePrefixList = new String[]{"hzb_"};
        BackendConfig.createDefault()
                .globalConfigBuilder(globalConfigBuilder -> globalConfigBuilder
                        .author("zhao-hao-dong")
                        .outputDir(outputDir)
                        .datePattern("yyyy-MM-dd"))
                .packageConfigBuilder(packageConfigBuilder -> packageConfigBuilder
                        .parent("com.mhd.zz")
                        .entity("entity")
                        .xml(GeneratorConstant.DEFAULT_PREFIX + "/mapper")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .queryReqDTO("dto.query")
                        .saveReqDTO("dto.save")
                        .vo("vo"))
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
                .entityBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(false))
                .xmlBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .queryReqDTOBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .saveReqDTOBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .voBuilder(baseTemplateBuilder -> baseTemplateBuilder.enable(true))
                .generate();
    }
}
