## Quartz模块说明

### 1、模块简介

- 描述：当前模块用于启动 Quartz 定时任务。
- 功能点：
    - 全局判断是否开启 Quartz 定时任务(配置文件中配置)
    - 提供了 Quartz 任务管理类 SchedulerManager，方便用户进行：创建、修改、删除、暂停、启动、立即执行任务。
    - 提供了默认的日志记录类 JobLogDefaultHandlerServiceImpl
    - 提供了自定义日志记录(实现方式需要注册为组件，被 Spring 管理)，实现接口 JobLogHandlerService 即可。
    - 提供了任务接口 JobHandler，用户新增任务实现当前接口即可。默认使用组件名称寻找对应的任务。

### 2、模块涉及

- 描述：如果要使用当前模块，涉及的包有：
    - `com.mhd.boot.common.job.quartz.config.MeQuartzAutoConfiguration`

### 3、模块引入

- 描述：当前模块默认自动注册，无需用户手动注册。

> 注册方式一(不推荐)

- 直接在 Configuration 中配置文件中注册。

> 注册方式二(推荐，自动)

- 在资源目录 resources 下创建 META-INF 文件夹
- 在 META-INF 文件夹内创建 spring 目录
- 在 spring 下创建 org.springframework.boot.autoconfigure.Autoconfiguration.imports 文件中写入如下内容

```markdown
com.mhd.boot.common.job.quartz.config.MeQuartzAutoConfiguration
```

### 4、模块使用

- 模块引入
- 在配置文件中配置如下信息：

```yml
--- ######################################### Quartz 相关 #########################################
# Quartz 配置项，对应 QuartzProperties 配置类
spring:
  quartz:
    auto-startup: false # 本地开发环境，尽量不要开启 Job
    scheduler-name: schedulerName # Scheduler 名字。默认为 schedulerName
    job-store-type: jdbc # Job 存储器类型。默认为 memory 表示内存，可选 jdbc 使用数据库。
    wait-for-jobs-to-complete-on-shutdown: true # 应用关闭时，是否等待定时任务执行完成。默认为 false ，建议设置为 true
    properties: # 添加 Quartz Scheduler 附加属性，更多可以看 http://www.quartz-scheduler.org/documentation/2.4.0-SNAPSHOT/configuration.html 文档
      org:
        quartz:
          # Scheduler 相关配置
          scheduler:
            instanceName: schedulerName
            instanceId: AUTO # 自动生成 instance ID
          # JobStore 相关配置
          jobStore:
            # JobStore 实现类。可见博客：https://blog.csdn.net/weixin_42458219/article/details/122247162
            class: org.springframework.scheduling.quartz.LocalDataSourceJobStore
            isClustered: false # 关闭集群模式
            clusterCheckinInterval: 15000 # 集群检查频率，单位：毫秒。默认为 15000，即 15 秒
            misfireThreshold: 60000 # misfire 阀值，单位：毫秒。
          #            driverDelegateClass: org.quartz.impl.jdbcjobstore.PostgreSQLDelegate # 如果是 postgresql 数据库需要设置
          # 线程池相关配置
          threadPool:
            threadCount: 25 # 线程池大小。默认为 10 。
            threadPriority: 5 # 线程优先级
            class: org.quartz.simpl.SimpleThreadPool # 线程池类型
    jdbc: # 使用 JDBC 的 JobStore 的时候，JDBC 的配置
      initialize-schema: never # 是否自动使用 SQL 初始化 Quartz 表结构。这里设置成 never ，我们手动创建表结构。

```