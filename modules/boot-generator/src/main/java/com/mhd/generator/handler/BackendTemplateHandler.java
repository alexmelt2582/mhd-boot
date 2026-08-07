package com.mhd.generator.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.mhd.generator.constant.GlobalConstant;
import com.mhd.generator.core.BaseTemplate;
import com.mhd.generator.core.TemplateHandler;
import com.mhd.generator.exception.CodeGeneratorException;
import com.mhd.generator.template.DefaultBackendTemplate;
import com.mhd.generator.util.GeneratorUtils;
import com.mhd.generator.util.StringUtils;
import com.mhd.generator.util.ValidationUtils;
import com.mhd.generator.validator.GroupMybatis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author zhao-hao-dong
 * @since 2025-03-17
 **/
public class BackendTemplateHandler implements TemplateHandler<DefaultBackendTemplate> {
    private static final Logger log = LoggerFactory.getLogger(BackendTemplateHandler.class + GlobalConstant.LOG_PREFIX);

    /**
     * 用 MP 查询数据库元数据，不生成文件，只取 TableInfo
     */
    private List<TableInfo> extractTableMetadata(DefaultBackendTemplate template) {
        DataSourceConfig dsc = new DataSourceConfig.Builder(
                template.getDbConfig().getDbUrl(),
                template.getDbConfig().getDbUsername(),
                template.getDbConfig().getDbPassword()
        ).build();

        // 策略配置：告诉 MP 表名、前缀、命名规则
        StrategyConfig strategy = new StrategyConfig.Builder()
                .addInclude(template.getDbConfig().getDbTableList())
                .addTablePrefix(template.getDbConfig().getDbTablePrefix())
                .entityBuilder()
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel) // 转驼峰
                .enableLombok()
                .idType(IdType.AUTO) // 指定ID类型
                .enableTableFieldAnnotation()
                .build();

        PackageConfig pkg = new PackageConfig.Builder()
                .parent(template.getPackageConfig().getParent())
                .entity(template.getPackageConfig().getEntity())
                .mapper(template.getPackageConfig().getMapper())
                .service(template.getPackageConfig().getService())
                .serviceImpl(template.getPackageConfig().getServiceImpl())
                .controller(template.getPackageConfig().getController())
                .build();

        GlobalConfig global = new GlobalConfig.Builder()
                .author(template.getGlobalConfig().getAuthor())
                .build();

        // 关键：ConfigBuilder 会帮你查数据库、算好所有命名
        ConfigBuilder configBuilder = new ConfigBuilder(pkg, dsc, strategy, null, global, null);
        return configBuilder.getTableInfoList();
    }

    /**
     * 基于 MP 的 TableInfo 构建模板上下文
     */
    private Map<String, Object> buildContext(TableInfo tableInfo, DefaultBackendTemplate template) {
        Map<String, Object> ctx = new HashMap<>();

        // ========== 1. 基础信息 ==========
        ctx.put("author", StrUtil.blankToDefault(template.getGlobalConfig().getAuthor(), "alexmelt"));
        ctx.put("date", DateTimeFormatter.ofPattern(
                StrUtil.blankToDefault(template.getGlobalConfig().getDatePattern(), "yyyy-MM-dd")
        ).format(LocalDateTimeUtil.now()));

        // ========== 2. 表元数据 ==========
        ctx.put("tableName", tableInfo.getName());           // sys_user
        ctx.put("tableComment", tableInfo.getComment());     // 用户表

        // ---------- 3. naming（命名相关） ----------
        Map<String, Object> naming = new HashMap<>();
        String entityName = tableInfo.getEntityName();  // User
        naming.put("entityName", entityName);
        naming.put("entityNameLower", StringUtils.toFirstLower(entityName));
        String controllerName = tableInfo.getControllerName();
        naming.put("controllerName", controllerName);
        naming.put("controllerNameLower", StringUtils.toFirstLower(controllerName));
        String serviceName = tableInfo.getServiceName();
        naming.put("serviceName", serviceName);
        naming.put("serviceNameLower", StringUtils.toFirstLower(serviceName));
        String serviceImplName = tableInfo.getServiceImplName();
        naming.put("serviceImplName", serviceImplName);
        naming.put("serviceImplNameLower", StringUtils.toFirstLower(serviceImplName));
        String mapperName = tableInfo.getMapperName();
        naming.put("mapperName", mapperName);
        naming.put("mapperNameLower", StringUtils.toFirstLower(mapperName));

        String saveDTOName = entityName + "SaveDTO";
        naming.put("saveDTOName", saveDTOName);
        naming.put("saveDTONameLower", StringUtils.toFirstLower(saveDTOName));
        String queryDTOName = entityName + "QueryDTO";
        naming.put("queryDTOName", queryDTOName);
        naming.put("queryDTONameLower", StringUtils.toFirstLower(queryDTOName));
        String voName = entityName + "Vo";
        naming.put("voName", voName);
        naming.put("voNameLower", StringUtils.toFirstLower(voName));
        ctx.put("naming", naming);

        // ---------- 4. packages（包路径） ----------
        Map<String, Object> packages = new HashMap<>();
        String parent = template.getPackageConfig().getParent();

        packages.put("parent", parent);
        packages.put("entityPackage", resolvePackage(parent, template.getPackageConfig().getEntity()));
        packages.put("mapperPackage", resolvePackage(parent, template.getPackageConfig().getMapper()));
        packages.put("servicePackage", resolvePackage(parent, template.getPackageConfig().getService()));
        packages.put("serviceImplPackage", resolvePackage(parent, template.getPackageConfig().getServiceImpl()));
        packages.put("controllerPackage", resolvePackage(parent, template.getPackageConfig().getController()));

        packages.put("queryDTOPackage", resolvePackage(parent, template.getPackageConfig().getQueryReqDTO()));
        packages.put("saveDTOPackage", resolvePackage(parent, template.getPackageConfig().getSaveReqDTO()));
        packages.put("voPackage", resolvePackage(parent, template.getPackageConfig().getVo()));
        return ctx;
    }

    private String resolvePackage(String... classPaths) {
        return StringUtils.resolveClassPath(classPaths);
    }

    @Override
    public void handleTemplateConfig(DefaultBackendTemplate template) {
        // 1. 一次性提取所有表的元数据（不用生成 Entity，不用编译，不用反射）
        List<TableInfo> tableInfos = extractTableMetadata(template);

        // 2. 先一次生成需要的 Entity，如果需要自己渲染，则放入在 循环中
        // 方案 A：继续用 MP 生成 Entity（稳定）
        if (template.getEntityTemplate().isEnable()) {
            generateMybatisOfEntity(template);
        }
        for (TableInfo tableInfo : tableInfos) {
            // 2. 构建统一上下文（所有文件共用一份）
            Map<String, Object> baseParamMap = buildContext(tableInfo, template);

            // 3. 生成 Entity（可选：如果是自己渲染则采用这个，并注释掉方案A）
            //if (template.getEntityTemplate().isEnable()) {
            //    // 方案 B：自己渲染 Entity（完全控制，推荐）
            //    generateMeOfEntity(template.getEntityTemplate(), baseParamMap);
            //}

            // 4. 生成 XML
            if (template.getXmlTemplate().isEnable()) {
                generateMybatisOfXml(template);
            }

            // 5. 生成 Mapper
            if (template.getMapperTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getMapperTemplate());
            }

            // 6. 生成 Service
            if (template.getServiceTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getServiceTemplate());
            }

            if (template.getServiceImplTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getServiceImplTemplate());
            }

            if (template.getControllerTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getControllerTemplate());
            }

            if (template.getQueryReqDTOTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getQueryReqDTOTemplate());
            }

            if (template.getSaveReqDTOTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getSaveReqDTOTemplate());
            }

            if (template.getVoTemplate().isEnable()) {
                GeneratorUtils.handleTemplateToFile(template.getVoTemplate());
            }
        }
    }

    private void generateMybatisOfEntity(DefaultBackendTemplate template) {
        Consumer<StrategyConfig.Builder> consumer;
        if (template.getEntityTemplate().isOverwriteExisting()) {
            consumer = builder -> {
                builder.addInclude(template.getDbConfig().getDbTableList()) // 指定表名
                        .addTablePrefix(Arrays.asList(template.getDbConfig().getDbTablePrefix()))         // 过滤表前缀
                        // 实体类策略：启用Lombok和文件覆盖
                        .entityBuilder()
                        .enableLombok()// 启用Lombok
                        .enableFileOverride() // 覆盖实体类文件
                        .idType(IdType.AUTO)             // 指定ID类型
                        .naming(NamingStrategy.underline_to_camel)  // 转驼峰
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .enableTableFieldAnnotation()
                        .serviceBuilder()
                        .disable()
                        .controllerBuilder()
                        .disable()
                        .mapperBuilder()
                        .disable();
            };
        } else {
            consumer = builder -> {
                builder.addInclude(template.getDbConfig().getDbTableList()) // 指定表名
                        .addTablePrefix(Arrays.asList(template.getDbConfig().getDbTablePrefix()))         // 过滤表前缀
                        // 实体类策略：启用Lombok和文件覆盖
                        .entityBuilder()
                        .enableLombok()// 启用Lombok
                        .idType(IdType.AUTO)             // 指定ID类型
                        .naming(NamingStrategy.underline_to_camel)  // 转驼峰
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .enableTableFieldAnnotation()
                        .serviceBuilder()
                        .disable()
                        .controllerBuilder()
                        .disable()
                        .mapperBuilder()
                        .disable();
            };
        }
        generateMybatis(template, consumer);
    }

    private List<String> generateMybatisOfXml(DefaultBackendTemplate template) {
        Consumer<StrategyConfig.Builder> consumer;
        if (template.getXmlTemplate().isOverwriteExisting()) {
            consumer = builder -> {
                builder.addInclude(template.getDbConfig().getDbTableList()) // 指定表名
                        .addTablePrefix(Arrays.asList(template.getDbConfig().getDbTablePrefix()))         // 过滤表前缀
                        // 实体类策略：启用Lombok和文件覆盖
                        .entityBuilder()
                        .disable()
                        .serviceBuilder()
                        .disable()
                        .controllerBuilder()
                        .disable()
                        .mapperBuilder()
                        .disableMapper()
                        .enableBaseResultMap()
                        .enableBaseColumnList()
                        .enableFileOverride();
            };
        } else {
            consumer = builder -> {
                builder.addInclude(template.getDbConfig().getDbTableList()) // 指定表名
                        .addTablePrefix(Arrays.asList(template.getDbConfig().getDbTablePrefix()))         // 过滤表前缀
                        // 实体类策略：启用Lombok和文件覆盖
                        .entityBuilder()
                        .disable()
                        .serviceBuilder()
                        .disable()
                        .controllerBuilder()
                        .disable()
                        .mapperBuilder()
                        .disableMapper()
                        .enableBaseResultMap()
                        .enableBaseColumnList();
            };
        }
        generateMybatis(template, consumer);
        List<String> list = new ArrayList<>();
        for (String table : template.getDbConfig().getDbTableList()) {
            if (ArrayUtil.isNotEmpty(template.getDbConfig().getDbTablePrefix())) {
                for (String prefix : template.getDbConfig().getDbTablePrefix()) {
                    if (table.startsWith(prefix)) {
                        list.add(StringUtils.toUpperCamelCase(table.substring(prefix.length())));
                        break;
                    }
                }
            } else {
                list.add(table);
            }
        }
        return list;
    }

    private void generateMybatis(DefaultBackendTemplate template, Consumer<StrategyConfig.Builder> consumer) {
        ValidationUtils.validateByGroup(template, GroupMybatis.class);
        // 判断生成 xml 文件的路径
        String filePath;
        if (template.getPackageConfig().getXml().startsWith(GlobalConstant.DEFAULT_PREFIX)) {
            String newXmlPath = template.getPackageConfig().getXml().substring(GlobalConstant.DEFAULT_PREFIX.length());
            filePath = "src/main/resources" + newXmlPath;
        } else {
            filePath = template.getPackageConfig().getXml();
        }
        // 生成 entity
        // 使用元数据查询的方式生成代码,默认已经根据jdbcType来适配java类型,支持使用typeConvertHandler来转换需要映射的类型映射
        DataSourceConfig.Builder dataSourceBuilder = new DataSourceConfig.Builder(template.getDbConfig().getDbUrl(), template.getDbConfig().getDbUsername(), template.getDbConfig().getDbPassword())
                .typeConvertHandler(new CustomMysqlTypeConvert());
        FastAutoGenerator.create(dataSourceBuilder)
                //FastAutoGenerator.create(template.getDbConfig().getDbUrl(), template.getDbConfig().getDbUsername(), template.getDbConfig().getDbPassword())
                // 全局配置
                .globalConfig(builder -> {
                    builder.author(template.getGlobalConfig().getAuthor())      // 作者
                            .outputDir(template.getGlobalConfig().getOutputDir())   // 输出路径
                            .disableOpenDir()       // 生成后不打开文件夹
                            .commentDate(template.getGlobalConfig().getDatePattern());
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(template.getPackageConfig().getParent()) // 父包名
                            .entity(template.getPackageConfig().getEntity())         // 实体类包名
                            .service(template.getPackageConfig().getService())      // Service包名
                            .serviceImpl(template.getPackageConfig().getServiceImpl())
                            .mapper(template.getPackageConfig().getMapper())
                            .controller(template.getPackageConfig().getController())
                            .pathInfo(Collections.singletonMap(OutputFile.xml, filePath)); // XML文件路径
                })
                // 策略配置
                .strategyConfig(consumer)
                // 模板引擎（默认Velocity）
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

    private Map<String, Object> generateBaseParamMap(String entity, DefaultBackendTemplate.GlobalConfig globalConfig, DefaultBackendTemplate.PackageConfig packageconfig) {
        String tmpMapper = entity + "Mapper";
        String tmpService = entity + "Service";
        String tmpServiceImpl = entity + "ServiceImpl";
        String tmpController = entity + "Controller";
        String tmpQueryReqDTO = entity + "QueryReqDTO";
        String tmpSaveReqDTO = entity + "SaveReqDTO";
        String tmpVO = entity + "VO";
        Map<String, Object> map = new HashMap<>();
        map.put("author", StrUtil.isNotBlank(globalConfig.getAuthor()) ? globalConfig.getAuthor() : "alexmelt");
        map.put("date", DateTimeFormatter.ofPattern(StrUtil.isNotBlank(globalConfig.getDatePattern()) ? globalConfig.getDatePattern() : "yyyy-MM-dd").format(LocalDateTimeUtil.now()));
        map.put("packageMapper", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getMapper(), tmpMapper));
        map.put("packageEntity", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getEntity(), entity));
        map.put("packageService", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getService(), tmpService));
        map.put("packageServiceImpl", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getServiceImpl(), tmpServiceImpl));
        map.put("packageController", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getController(), tmpController));
        map.put("packageQueryReqDTO", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getQueryReqDTO(), tmpQueryReqDTO));
        map.put("packageSaveReqDTO", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getSaveReqDTO(), tmpSaveReqDTO));
        map.put("packageVO", StringUtils.resolveClassPath(packageconfig.getParent(), packageconfig.getVo(), tmpVO));
        map.put("mapper", tmpMapper);
        map.put("entity", entity);
        map.put("entityToLower", StringUtils.toFirstLower(entity));
        map.put("service", tmpService);
        map.put("serviceToLower", StringUtils.toFirstLower(tmpService));
        map.put("serviceImpl", tmpServiceImpl);
        map.put("controller", tmpController);
        map.put("queryReqDTO", tmpQueryReqDTO);
        map.put("queryReqDTOToLower", StringUtils.toFirstLower(tmpQueryReqDTO));
        map.put("saveReqDTO", tmpSaveReqDTO);
        map.put("saveReqDTOToLower", StringUtils.toFirstLower(tmpSaveReqDTO));
        map.put("vo", tmpVO);
        map.put("voToLower", StringUtils.toFirstLower(tmpVO));
        return map;
    }

    private void generateMeOfMapper(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 1. 处理文件输出名称
        // 2. 处理模板参数
        String tmpPackageEntity = baseparamMap.get("packageMapper").toString();
        String tmpPackage = tmpPackageEntity.substring(0, tmpPackageEntity.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
        paramMap.put("package", tmpPackage);
        if (MapUtil.isNotEmpty(template.getParamMap())) {
            paramMap.putAll(template.getParamMap());
        }
        template.setParamMap(paramMap);
        GeneratorUtils.handleTemplateToFile(template);

    }

    private void generateMeOfService(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 2. 处理模板参数
        String tmpPackageService = baseparamMap.get("packageService").toString();
        String tmpPackage = tmpPackageService.substring(0, tmpPackageService.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
        paramMap.put("package", tmpPackage);
        if (MapUtil.isNotEmpty(template.getParamMap())) {
            paramMap.putAll(template.getParamMap());
        }
        template.setParamMap(paramMap);
        GeneratorUtils.handleTemplateToFile(template);

    }

    private void generateMeOfServiceImpl(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 2. 处理模板参数
        String tmpPackageService = baseparamMap.get("packageServiceImpl").toString();
        String tmpPackage = tmpPackageService.substring(0, tmpPackageService.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
        paramMap.put("package", tmpPackage);
        if (MapUtil.isNotEmpty(template.getParamMap())) {
            paramMap.putAll(template.getParamMap());
        }
        template.setParamMap(paramMap);
        GeneratorUtils.handleTemplateToFile(template);

    }

    private void generateMeOfController(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 2. 处理模板参数
        String tmpPackageService = baseparamMap.get("packageController").toString();
        String tmpPackage = tmpPackageService.substring(0, tmpPackageService.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
        paramMap.put("package", tmpPackage);
        if (MapUtil.isNotEmpty(template.getParamMap())) {
            paramMap.putAll(template.getParamMap());
        }
        template.setParamMap(paramMap);
        GeneratorUtils.handleTemplateToFile(template);
    }

    private void generateMeOfQueryReqDTO(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 2. 处理模板参数
        try {
            Class<?> entityClass = Class.forName(baseparamMap.get("packageEntity").toString());
            Field[] fields = entityClass.getDeclaredFields();
            handleField(paramMap, fields);
            String tmpPackageQueryReqDTO = baseparamMap.get("packageQueryReqDTO").toString();
            String tmpPackage = tmpPackageQueryReqDTO.substring(0, tmpPackageQueryReqDTO.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
            paramMap.put("package", tmpPackage);
            if (MapUtil.isNotEmpty(template.getParamMap())) {
                paramMap.putAll(template.getParamMap());
            }
            template.setParamMap(paramMap);
        } catch (ClassNotFoundException e) {
            log.error("类 " + baseparamMap.get("packageEntity").toString() + " 不存在");
        }
        GeneratorUtils.handleTemplateToFile(template);

    }

    private void generateMeOfSaveReqDTO(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 2. 处理模板参数
        try {
            Class<?> entityClass = Class.forName(baseparamMap.get("packageEntity").toString());
            Field[] fields = entityClass.getDeclaredFields();
            handleField(paramMap, fields);
            String tmpPackageSaveReqDTO = baseparamMap.get("packageSaveReqDTO").toString();
            String tmpPackage = tmpPackageSaveReqDTO.substring(0, tmpPackageSaveReqDTO.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
            paramMap.put("package", tmpPackage);
            if (MapUtil.isNotEmpty(template.getParamMap())) {
                paramMap.putAll(template.getParamMap());
            }
            template.setParamMap(paramMap);
        } catch (ClassNotFoundException e) {
            log.error("类 " + baseparamMap.get("packageEntity").toString() + " 不存在");
        }
        GeneratorUtils.handleTemplateToFile(template);

    }

    private void generateMeOfVO(BaseTemplate template, Map<String, Object> baseparamMap) {
        Map<String, Object> paramMap = new HashMap<>(baseparamMap);
        // 2. 处理模板参数
        try {
            Class<?> entityClass = Class.forName(baseparamMap.get("packageEntity").toString());
            Field[] fields = entityClass.getDeclaredFields();
            handleField(paramMap, fields);
            String tmpPackageVO = baseparamMap.get("packageVO").toString();
            String tmpPackage = tmpPackageVO.substring(0, tmpPackageVO.lastIndexOf(GlobalConstant.PACKAGE_CONNECT));
            paramMap.put("package", tmpPackage);
            if (MapUtil.isNotEmpty(template.getParamMap())) {
                paramMap.putAll(template.getParamMap());
            }
            template.setParamMap(paramMap);
        } catch (ClassNotFoundException e) {
            log.error("类 " + baseparamMap.get("packageEntity").toString() + " 不存在");
        }
        GeneratorUtils.handleTemplateToFile(template);
    }

    public static void handleField(Map<String, Object> paramMap, Field[] fields) {
        List<Map<String, String>> fieldList = new ArrayList<>();
        Set<String> importSet = new HashSet<>();
        for (Field field : fields) {
            // 排除序列化 serialVersionUID
            if (field.getName().contains("serialVersionUID")) {
                continue;
            }
            Map<String, String> fieldMap = new HashMap<>();
            fieldMap.put("name", field.getName());
            fieldMap.put("type", field.getType().getSimpleName());
            // 获取字段的完整类型信息
            String fieldType = field.getType().getName();
            if (!fieldType.startsWith("java.lang")) { // 排除java.lang包下的类
                importSet.add(fieldType);
            }
            fieldList.add(fieldMap);
        }
        paramMap.put("fields", fieldList);
        paramMap.put("imports", importSet);
    }
}
