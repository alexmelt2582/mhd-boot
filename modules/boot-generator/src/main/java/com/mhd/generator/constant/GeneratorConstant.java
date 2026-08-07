package com.mhd.generator.constant;

/**
 * @author zhao-hao-dong
 * @since 2025-03-11
 **/
public interface GeneratorConstant {
    String LOG_PREFIX = " [CodeGenerator] ";
    String PACKAGE_CONNECT = ".";
    String DEFAULT_PREFIX = "classpath:";

    String DEFAULT_CONTROLLER_TEMPLATE    = DEFAULT_PREFIX + "template/java/controller_template.ftl";
    String DEFAULT_SERVICE_TEMPLATE       = DEFAULT_PREFIX + "template/java/service_template.ftl";
    String DEFAULT_SERVICE_IMPL_TEMPLATE  = DEFAULT_PREFIX + "template/java/service_impl_template.ftl";
    String DEFAULT_MAPPER_TEMPLATE        = DEFAULT_PREFIX + "template/java/mapper_template.ftl";
    String DEFAULT_VO_TEMPLATE            = DEFAULT_PREFIX + "template/java/vo_template.ftl";
    String DEFAULT_QUERY_REQ_DTO_TEMPLATE = DEFAULT_PREFIX + "template/java/query_dto_template.ftl";
    String DEFAULT_SAVE_REQ_DTO_TEMPLATE  = DEFAULT_PREFIX + "template/java/save_dto_template.ftl";

    String DEFAULT_VUE2_INDEX_TEMPLATE    = DEFAULT_PREFIX + "template/vue2/default_vue2_index_template.ftl";
    String DEFAULT_VUE2_JS_TEMPLATE       = DEFAULT_PREFIX + "template/vue2/default_vue2_js_template.ftl";
}
