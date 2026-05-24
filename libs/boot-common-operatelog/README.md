## 操作日志模块说明

### 1、模块简介

- 描述：当前模块用于记录操作日志。
- 功能点：
    - 全局判断是否开启日志记录(配置文件中配置)
    - 局部判断是否开启日志记录(OperateLog 注解中配置)
    - 局部判断是否记录方法参数(OperateLog 注解中配置)
    - 局部判断是否记录方法返回值(OperateLog 注解中配置)
    - 提供了默认收集器，包含：操作模块、操作描述、操作类型、操作结果、操作异常信息、请求方法、请求参数、请求IP、请求地址、请求浏览器、请求结果、耗时
    - 提供了默认处理器，当卡其日志记录的时候，默认打印 收集器信息，打印类型为 warn。打印方法默认存在注解 @Async，如需异步打印请手动开启
    - 提供了自定义收集器(实现方式需要注册为组件，被 Spring 管理)：
        - 方式一：继承类 AbstractOperateLogDefaultCollectorService，重写 extractCollectLog
          方法，在默认收集器的基础之上，对收集信息自定义，如果需要添加额外的参数，OperateLogVO 对象提供了 userId 和
          otherParams 字段方便扩展
        - 方式二：实现接口 OperateLogCollectorService，支持用户完全自定义。
    - 提供了自定义处理器(实现方式需要注册为组件，被 Spring 管理)：实现 OperateLogHandlerService。

### 4、模块使用

- 添加依赖

```xml
<dependency>
    <groupId>com.mhd</groupId>
    <artifactId>mhd-operatelog-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

- 模块引入
- 在配置文件中配置 mhd.operate-log.enable 的值为 true。默认为true
- 在 controller 方法上添加 OperateLog
  注解。示例：`@OperateLog(module = "测试类", description = "hello", type = OperateTypeEnum.GET)`