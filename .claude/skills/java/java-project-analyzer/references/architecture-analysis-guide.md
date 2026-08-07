# 架构分析指南

## 一、项目结构快速识别

### 1.1 构建系统识别

| 构建工具 | 标志文件 | 典型特征 |
|---------|---------|---------|
| Maven | `pom.xml` | 依赖声明、module 多模块 |
| Gradle | `build.gradle` / `build.gradle.kts` | 更灵活的构建脚本 |
| Ant + Ivy | `build.xml` + `ivy.xml` | 老项目，较少见 |

**分析要点**：
- 读取 parent pom 看继承关系
- 看 modules 列表了解子模块划分
- 看 dependencies/dependencyManagement 了解技术栈版本

### 1.2 项目类型判断

**Spring Boot 项目**：
- 有 `spring-boot-starter-parent` 父依赖
- 有 `@SpringBootApplication` 注解的启动类
- 有 `application.yml` / `application.properties`

**Spring Cloud 微服务**：
- 多模块结构，每个模块是一个服务
- 有 `spring-cloud-dependencies` 依赖
- 包含服务注册发现（Nacos/Eureka/Consul）
- 包含配置中心、网关、熔断器等组件

**单体应用**：
- 单模块或少量模块
- 所有功能在一个应用内
- 通常按业务分包

## 二、分层架构识别

### 2.1 经典三层架构

```
src/main/java/com/xxx/
├── controller/    # 控制层，接收请求
├── service/       # 业务逻辑层
│   └── impl/      # 实现类
├── dao/           # 数据访问层
│   └── mapper/    # MyBatis Mapper
├── entity/        # 数据库实体
├── dto/           # 数据传输对象
├── vo/            # 视图对象
└── config/        # 配置类
```

**识别特征**：
- 包名明确分为 controller/service/dao/mapper
- 每层之间单向依赖
- 实体类和 DTO 分离

### 2.2 DDD 分层架构

```
src/main/java/com/xxx/
├── interfaces/       # 接口层（适配外部请求）
│   ├── controller/
│   └── facade/
├── application/      # 应用层（编排业务流程）
│   ├── service/
│   └── command/
├── domain/           # 领域层（核心业务逻辑）
│   ├── model/        # 领域模型
│   ├── repository/   # 仓储接口
│   ├── service/      # 领域服务
│   └── event/        # 领域事件
└── infrastructure/   # 基础设施层
    ├── persistence/  # 持久化实现
    ├── rpc/          # 远程调用
    └── config/
```

**识别特征**：
- 有明确的 domain 层，包含领域模型和领域服务
- repository 是接口，实现在 infrastructure 层
- 依赖方向：外层依赖内层，内层不依赖外层

### 2.3 按业务模块分包

```
src/main/java/com/xxx/
├── user/           # 用户模块
│   ├── controller/
│   ├── service/
│   ├── dao/
│   └── entity/
├── order/          # 订单模块
│   ├── controller/
│   ├── service/
│   ├── dao/
│   └── entity/
└── common/         # 公共模块
    ├── utils/
    ├── exception/
    └── config/
```

**识别特征**：
- 顶层按业务领域分包
- 每个业务包内有完整的 controller/service/dao 结构
- 有 common 或 shared 模块放公共代码

## 三、核心设计模式识别

### 3.1 常见设计模式速查

| 模式 | 识别特征 | 典型场景 |
|-----|---------|---------|
| 策略模式 | Strategy 接口 + 多个实现类 + 策略选择器 | 支付方式、优惠计算、路由选择 |
| 工厂模式 | Factory 类/方法，根据参数创建对象 | Bean 创建、对象构造 |
| 模板方法 | 抽象类定义骨架，子类实现步骤 | 流程处理、算法骨架 |
| 观察者/事件 | Event + Listener + EventPublisher | 解耦异步处理、领域事件 |
| 责任链 | Handler 链，依次处理 | 过滤器、拦截器、校验链 |
| 装饰器 | 包装原有对象，增强功能 | 缓存装饰、日志装饰 |
| 适配器 | Adapter 类，转换接口 | 第三方接口适配、旧系统兼容 |
| 单例 | 私有构造 + 静态实例 | 配置类、工具类 |
| 建造者 | Builder 类，链式调用 | 复杂对象构建 |
| 状态模式 | State 接口 + 状态实现类 | 订单状态、流程状态 |

### 3.2 框架内置模式

**Spring 中常见的模式**：
- **代理模式**：AOP、事务管理（@Transactional）
- **模板方法**：JdbcTemplate、RedisTemplate、RestTemplate
- **策略模式**：HandlerMapping、ViewResolver
- **观察者模式**：ApplicationEvent、ApplicationListener
- **工厂模式**：BeanFactory、ApplicationContext

**MyBatis 中常见的模式**：
- **动态代理**：Mapper 接口的实现
- **模板方法**：SqlSessionTemplate
- **建造者模式**：XMLConfigBuilder、XMLMapperBuilder

## 四、模块依赖关系分析

### 4.1 分析方法

1. **读 pom.xml 的 dependency 声明**
   - 看模块间的直接依赖
   - 看依赖传递带来的间接依赖

2. **grep import 语句**
   - 搜索 `import com.xxx.moduleA.*` 看哪些模块引用了 moduleA
   - 统计引用频次判断模块重要性

3. **看包结构和命名**
   - `common`、`core`、`foundation` 通常是底层公共模块
   - `api`、`client` 通常是对外接口定义
   - `service`、`biz` 通常是业务实现

### 4.2 依赖关系可视化

分析完成后，用文字描述模块依赖关系，例如：

```
依赖关系（从上到下）：
┌─────────────────────────────────────┐
│  web 层 (controller、对外 API)       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  service 层 (业务逻辑)               │
│  ├─ user-service                    │
│  ├─ order-service                   │
│  └─ payment-service                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  dao 层 (数据访问)                   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  common 层 (公共工具、实体、DTO)      │
└─────────────────────────────────────┘
```

## 五、技术栈深度解读

### 5.1 Web 框架

**Spring MVC**：
- 识别：`spring-boot-starter-web` 依赖
- 关键点：DispatcherServlet、HandlerMapping、ViewResolver

**Spring WebFlux**（响应式）：
- 识别：`spring-boot-starter-webflux` 依赖
- 关键点：Flux/Mono、Reactor、非阻塞

### 5.2 持久层框架

**MyBatis / MyBatis-Plus**：
- 识别：`mybatis-spring-boot-starter`、`mybatis-plus-boot-starter`
- 关键点：Mapper 接口、XML 映射、分页插件、条件构造器

**Spring Data JPA**：
- 识别：`spring-boot-starter-data-jpa`
- 关键点：Repository 接口、实体注解、查询方法命名约定

### 5.3 缓存

**Redis**：
- 识别：`spring-boot-starter-data-redis`、`jedis`、`lettuce`
- 常见用途：缓存、分布式锁、计数器、排行榜、消息队列

**Caffeine / Guava Cache**（本地缓存）：
- 识别：`caffeine`、`guava` 依赖
- 常见用途：一级缓存、热点数据

### 5.4 消息队列

**Kafka**：
- 识别：`spring-kafka` 依赖
- 特点：高吞吐、分区、适合日志/大数据场景

**RocketMQ**：
- 识别：`rocketmq-spring-boot-starter`
- 特点：事务消息、延时消息、顺序消息

**RabbitMQ**：
- 识别：`spring-boot-starter-amqp`
- 特点：灵活路由、死信队列、优先级队列

### 5.5 分布式相关

**服务注册发现**：
- Nacos：`spring-cloud-starter-alibaba-nacos-discovery`
- Eureka：`spring-cloud-starter-netflix-eureka-client`
- Consul：`spring-cloud-starter-consul-discovery`

**配置中心**：
- Nacos Config：`spring-cloud-starter-alibaba-nacos-config`
- Spring Cloud Config：`spring-cloud-config-client`
- Apollo：`apollo-client`

**网关**：
- Spring Cloud Gateway：`spring-cloud-starter-gateway`
- Zuul：`spring-cloud-starter-netflix-zuul`

**远程调用**：
- OpenFeign：`spring-cloud-starter-openfeign`
- Dubbo：`dubbo-spring-boot-starter`

**分布式事务**：
- Seata：`seata-spring-boot-starter`
- RocketMQ 事务消息

## 六、代码质量与工程实践识别

### 6.1 好的迹象

- 统一的异常处理（`@ControllerAdvice` + 自定义异常）
- 统一的返回结果封装（Result/Response 类）
- 参数校验（`@Valid` + `@NotNull` 等注解）
- 接口版本控制
- 有单元测试（`src/test/java` 下有测试类）
- 有集成测试
- 代码分层清晰，职责单一
- 有详细的注释和文档
- 使用了代码规范检查（Checkstyle、SpotBugs）

### 6.2 潜在问题

- 超大 Service 类（几千行的上帝类）
- 大量重复代码
- 魔法值（硬编码的数字/字符串）
- 深层嵌套的 if-else
- 没有异常处理或异常吞没
- SQL 拼接，可能有注入风险
- 循环依赖
- 没有日志或日志不规范

## 七、分析输出模板

分析一个项目后，按以下结构组织输出：

```
## 一、项目概览
- 项目名称：
- 业务领域：
- 项目规模（代码量/模块数）：
- 构建工具：
- JDK 版本：

## 二、技术栈
- 后端框架：
- 数据库：
- 缓存：
- 消息队列：
- 其他中间件：
- 工具库：

## 三、架构风格
- 架构类型：单体/微服务/...
- 分层方式：三层/DDD/...
- 模块划分：

## 四、核心模块说明
| 模块 | 职责 | 关键类 | 依赖 |
|-----|-----|-------|-----|
| xxx | xxx | xxx | xxx |

## 五、设计亮点
1. ...
2. ...

## 六、关键文档索引
- README.md：项目介绍
- docs/architecture.md：架构设计
- ...
```
