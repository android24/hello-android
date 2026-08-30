# 25.4 依赖、资源、配置与多环境治理

大型工程里，很多事故不是业务代码写错，而是工程资产失控。

例如：

```text
依赖版本冲突
资源被覆盖
debug 配置进了 release
测试环境地址打进线上包
多渠道开关不一致
权限声明被库偷偷带入
```

这些问题表面零散，背后都是治理问题。

## 本节先记住三句话

```text
依赖决定工程吃进了什么，资源决定最终包里长什么样，配置决定运行时走哪条路。
大型工程最怕隐式变化。
治理的核心是让变化可见、可控、可回滚。
```

## 贯穿案例：一次依赖升级引发连锁反应

课程 App 升级了一个图片库。

看起来只是改了一行版本号。

结果：

```text
引入了新的传递依赖。
和旧网络库版本冲突。
带入了新的资源名。
release 包体积增加 1.8MB。
某个低端机启动变慢。
R8 后线上出现 NoSuchMethodError。
```

这就是大型工程里最典型的问题：

```text
一次局部变化，穿过依赖、资源、构建、运行时和发布链路，最后变成线上事故。
```

## 现场侦探问题

如果发版前突然发现包体积增加，你会先删图片吗？

不一定。

你应该先问：

```text
是代码增加、资源增加，还是 native so 增加？
是直接依赖增加，还是传递依赖带入？
是 debug 资源进了 release，还是多语言资源没有裁剪？
是 R8 规则保留过宽，还是 keep 住了不该保留的类？
```

第 17、18、23、24 章的证据意识，在这里会非常有用。

## 学习目标

学完本节后，你应该能够：

- 理解依赖治理、资源治理和配置治理的关系。
- 使用版本目录、依赖约束和锁定降低版本漂移。
- 识别传递依赖、重复依赖和冲突依赖。
- 设计资源命名、资源前缀和多模块资源边界。
- 管理 debug、staging、release、多渠道和远程配置。
- 把依赖、资源、权限和包体积纳入发布检查。

## 第一部分：依赖治理

依赖治理先解决三个问题：

```text
我们用了哪些库？
这些库从哪里来？
它们现在是什么版本，为什么是这个版本？
```

常见手段：

```text
Version Catalog
dependency constraints
dependency lock
dependency analysis
版本升级记录
安全漏洞扫描
传递依赖排查
```

依赖不是越新越好，也不是越旧越稳。

合理的治理方式是：

```text
统一声明
统一升级
统一验证
统一回滚
```

## 第二部分：传递依赖和冲突

很多冲突不是你直接写出来的。

而是库带来的：

```text
app
  -> image-lib:2.0
      -> okio:3.6

app
  -> network-lib:1.5
      -> okio:2.10
```

如果最终解析出的版本和某个库编译时预期不一致，就可能出现：

```text
NoSuchMethodError
ClassNotFoundException
Duplicate class
运行时行为变化
```

排查时要看：

```bash
./gradlew :app:dependencies
./gradlew :app:dependencyInsight --dependency okio
```

一个简化的 `dependencyInsight` 输出可以这样读：

```text
okio:3.6.0
   Selection reasons:
      - By conflict resolution: between versions 3.6.0 and 2.10.0

okio:3.6.0
\--- image-lib:2.0
     \--- appDebugRuntimeClasspath

okio:2.10.0 -> 3.6.0
\--- network-lib:1.5
     \--- appDebugRuntimeClasspath
```

这段输出说明：

```text
工程里同时出现了 okio 3.6.0 和 2.10.0。
Gradle 最终选择了 3.6.0。
network-lib 原本依赖 2.10.0，但运行时会拿到 3.6.0。
如果 network-lib 使用了旧版本特有行为，就可能出现运行时风险。
```

处理依赖冲突不要只有一种手段。

可以按风险选择：

| 处理方式 | 适用场景 | 风险 |
| --- | --- | --- |
| 升级旧库 | 旧库兼容新依赖，维护状态良好 | 需要回归旧库行为 |
| 约束版本 | 团队确认统一版本并能完整验证 | 可能影响传递依赖方 |
| exclude 依赖 | 传递依赖确实不需要或重复引入 | 容易造成运行时缺类 |
| 降级新库 | 新库收益不大，冲突风险高 | 可能错过安全或性能修复 |
| 替换库 | 原库维护差或冲突频繁 | 改造成本较高 |

依赖治理的关键不是“让 Gradle 不报错”，而是让最终运行时 classpath 和团队预期一致。

这和第 18 章代码加载、第 23 章供应链风险都能接起来。

## 第三部分：组件化和插件化里的依赖治理

组件化工程里，依赖治理还要多问几句：

```text
组件能不能独立运行？
组件 debug 时使用 fake 实现还是真实实现？
组件之间是否只能通过 contract 通信？
路由参数是否有类型约束？
组件是否偷偷依赖了宿主 app 的实现？
```

一个常见结构是：

```text
feature:course
  -> course-api
  -> core

feature:payment
  -> payment-api
  -> core

app
  -> feature:course
  -> feature:payment
  -> 组装 course-api / payment-api 实现
```

这样业务组件可以通过 `api` 或 `contract` 暴露能力，而不是直接互相依赖实现。

插件化工程还要继续增加治理项：

```text
宿主和插件协议版本
插件依赖库版本
插件资源命名和隔离
插件签名和来源校验
插件崩溃归因
插件灰度和回滚
宿主兼容旧插件
插件兼容旧宿主
```

插件化最怕“能加载，但不可治理”。

能动态加载只是第一步，真正困难的是让动态能力可发布、可观测、可回滚。

## 第四部分：资源治理

第 17 章已经讲过资源系统。

第 25 章要把它变成团队规则：

```text
library 模块使用 resourcePrefix
公共资源进入 designsystem
业务资源留在 feature
图片按密度和场景管理
字符串按业务域命名
主题 token 统一收口
无用资源定期清理
资源冲突进入 CI 检查
```

资源问题不要等 UI 错了才发现。

它应该在构建期、review 和发布前就被看见。

组件化和插件化场景下，资源治理还要关注：

```text
组件资源是否使用统一前缀？
组件独立运行时主题是否完整？
插件资源是否可能和宿主资源撞名？
动态换肤是否覆盖插件页面？
多语言资源是否在组件和宿主中保持一致？
```

第 17 章讲过资源匹配规则。

第 25 章要把它变成治理规则：

```text
资源不只是 UI 文件。
资源也是跨模块、跨组件、跨插件的兼容协议。
```

## 第五部分：配置治理

大型工程常见环境：

```text
debug
staging
pre_release
release
internal
beta
production
```

常见配置：

```text
baseUrl
feature flag
analytics key
crash report key
payment environment
push channel
log level
network timeout
```

配置治理的底线：

```text
线上包不能连测试环境。
测试开关不能误入 release。
敏感 key 不能写死在源码里。
日志级别和脱敏策略要跟构建类型绑定。
```

## 第六部分：BuildConfig、Manifest placeholder 和远程配置

不同配置有不同位置：

| 配置类型 | 适合放哪里 |
| --- | --- |
| 编译期常量 | BuildConfig / generated source |
| Manifest 值 | manifestPlaceholders |
| 资源级配置 | resValue / string resource |
| 运行时开关 | remote config / server config |
| 敏感信息 | 安全存储或服务端下发，避免硬编码 |

不要把所有配置都塞进 `BuildConfig`。

也不要把所有变化都依赖远程配置。

编译期配置适合稳定差异，远程配置适合运行时策略。

## 第七部分：发布前检查

发布前应该检查：

```text
依赖版本是否锁定
是否有重复类或冲突依赖
包体积是否异常增长
资源 shrink 是否符合预期
权限声明是否新增
组件 exported 是否符合预期
debug 日志和调试开关是否关闭
mapping / symbols 是否保存
签名和渠道配置是否正确
```

这不是形式主义。

这些检查每一项都对应真实事故。

## 本节自测

- 为什么依赖升级可能导致运行时 `NoSuchMethodError`？
- resourcePrefix 解决的是什么问题？
- debug / staging / release 配置为什么不能只靠人工确认？
- 哪些配置适合编译期，哪些配置适合远程下发？
- 发布前为什么要检查权限、资源、mapping 和 symbols？

## 本节总结

依赖、资源和配置是大型工程的“隐形地基”。

它们平时不显眼，但一旦失控，问题会穿透构建、运行、发布和线上诊断。

治理它们的目标不是让工程更复杂，而是让每一次变化都可见、可控、可解释。
