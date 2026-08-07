## 代码生成器模块说明

### 1、模块介绍

- 描述：用于生成代码。不局限于 Java
- 功能点：
    - 使用 FreeMarker 模板引擎
    - 默认提供了 Vue2 页面 index 和 js 文件代码生成
    - 默认提供了 Controller、Service、ServiceImpl、Mapper、Entity、Xml、VO、QueryReqDTO、SaveReqDTO 代码生成
    - 提供可扩展性，支持完全自定义
    - 代码生成支持：是否启用、日志前缀、是否覆盖文件、模板路径、以及额外的参数
    - 模板路径支持 绝对路径 和 classpath 路径（必须以classpath:为前缀）。classpath 路径是指 resources 目录下的路径
    - 如果提供的参数和模板内的参数不一致，代码生提示用户，支持添加过滤排除

### 2、模块涉及

### 3、模块使用

### 4、自定义模块

- 创建模板文件：
    - 可以放在 resources/template 目录下，使用时提供相对路径，前缀是：classpath:
    - 也可以自定义路径。使用时提供绝对路径
- 编写实体类：
    - 需要继承 ITemplate 类
    - 添加模板属性：BaseTemplate
- 编写处理类：
    - 根据实体类，需要实现 TemplateHandler 接口
    - 使用 GeneratorUtils.handleTemplateToFile() 方法生成文件，内置数据校验和模板参数校验。
- 注册处理器：
    - 调用 TemplateHandlerManager.registerHandler 注册当前处理器
- 使用代码生成器：
    - 通过 CodeGenerator.create().execute() 传递当前实体类，调用模板。

> 示例

- 创建 test_template.ftl文件，添加如下内容：

```ftl
public static void main(String[] args) {
    System.out.println("${message}");
}
```

- 创建 TestTemplate.java 文件，添加如下内容：

```java

@ToString
@Getter
public class TestTemplate extends ITemplate {
    @Valid
    private BaseTemplate testTemplate;

    private TestTemplate(BaseTemplate testTemplate) {
        this.testTemplate = testTemplate;
    }

    // 提供默认配置的静态工厂方法
    public static TestTemplate create() {
        return new TestTemplate(defaultTestTemplate());
    }

    // 初始化Index模板的默认配置
    private static BaseTemplate defaultTestTemplate() {
        return BaseTemplate.builder()
                .enable(true)
                .logPrefix("test")
                .outputDir(GlobalConstant.DEFAULT_PREFIX + "/test")
                .outputName("test")
                .outputFileSuffix(".java")
                .overwriteExisting(false)
                .templatePath(GlobalConstant.DEFAULT_PREFIX + "/template/test_template.ftl")
                .paramMap(MapUtil.newHashMap())
                .build();
    }

    public TestTemplate testBuilder(Function<BaseTemplate.BaseTemplateBuilder, BaseTemplate.BaseTemplateBuilder> customizer) {
        this.testTemplate = GeneratorUtils.applyCustomization(testTemplate, customizer);
        return this;
    }
}
```

- 创建 TestHandler.java 文件，添加如下内容：

```java
public class TestHandler implements TemplateHandler<TestTemplate> {
    @Override
    public void handleTemplateConfig(TestTemplate template) {
        GeneratorUtils.handleTemplateToFile(template.getTestTemplate());
    }
}
```

- 创建 TestMain.java 文件，添加如下内容：

```java
public class TestMain {
    public static void main(String[] args) {
        TestTemplate testTemplate = TestTemplate.create()
                .testBuilder(builder -> builder.paramMap(Map.of(
                        "message", "Hello World"
                )));
        TemplateHandlerManager.registerHandler(TestTemplate.class, new TestHandler());
        CodeGenerator.create()
                .execute(
                        testTemplate
                );
    }
}
```

### 5、注意事项

- 当前模块中校验使用的是 Hibernate Validator 进行校验
- 校验 API 使用的是 jakarta.validation.*