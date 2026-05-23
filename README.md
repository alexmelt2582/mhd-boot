# mhd-boot
> 通用 Spring Boot 基础脚手架项目，提供可复用的公共模块与基础组件，支持持续扩展。

## 📦 项目介绍
`mhd-boot` 是基于 Spring Boot 3.x 构建的通用基础脚手架，旨在沉淀可复用的公共模块，减少新项目的重复配置。
目前已集成：
- 通用工具类、常量、枚举、异常处理
- SpringDoc API 文档
- MyBatis-Plus 通用 CRUD 与分页
- Web 层通用配置（全局异常、跨域、响应封装）
- Spring AI 基础集成（OpenAI/DashScope 兼容）
  后续可继续扩展：缓存、消息队列、分布式锁、监控等通用模块。

## 🗂️ 目录结构

mhd-boot
├── apps/ # 业务应用入口模块
│ └── interview-business/ # 示例业务模块（可扩展多个业务）
├── libs/ # 公共基础模块
│ ├── boot-common/ # 通用核心模块
│ ├── boot-common-doc/ # SpringDoc API 文档模块
│ ├── boot-common-mybatis/ # MyBatis-Plus 集成模块
│ ├── boot-common-web/ # Web 层通用配置模块
│ └── boot-ai/ # Spring AI 集成模块
├── deploy/ # 部署相关配置（SQL、Docker、RocketMQ 等）
├── doc/ # 项目文档
├── logs/ # 日志文件目录
└── pom.xml # 父工程 POM

## 🚀 快速开始

### 1. 环境要求
- JDK 17+
- Spring Boot 3.5.x+
- Maven 3.8+

### 2. 引入依赖
在你的业务项目中引入需要的模块：
```xml
<!-- 父工程 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.mhd</groupId>
            <artifactId>mhd-boot</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- 示例：引入 Web 通用模块 -->
<dependency>
    <groupId>com.mhd</groupId>
    <artifactId>boot-common-web</artifactId>
</dependency>
```

## 📦 模块说明

| 模块 | 功能 | 依赖说明 |
|------|------|----------|
| boot-common | 通用工具类、常量、枚举、异常 | 无额外依赖 |
| boot-common-doc | SpringDoc API 文档 | 需配合 Spring Boot Web 使用 |
| boot-common-mybatis | MyBatis-Plus 通用 CRUD、分页 | 需数据库驱动 |
| boot-common-web | 全局异常、跨域、响应封装 | 需 Spring Boot Web |
| boot-ai | Spring AI 基础集成 | 需配置 AI 模型 Key |