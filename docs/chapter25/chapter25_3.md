# 25.3 Gradle、构建效率、缓存与 CI 加速

大型 Android 工程里，构建速度就是开发体验。

如果改一行文案要等五分钟，团队会自然学会少跑测试、少切分支、少验证。

构建慢不是小问题。

它会慢慢侵蚀质量。

## 本节先记住三句话

```text
构建效率不是机器问题，而是工程反馈速度问题。
不要只问构建慢不慢，要问慢在哪里、为什么慢、是否可复现。
CI 慢会让质量门禁变成摆设。
```

## 贯穿案例：一次小改动等了很久

课程 App 变大后，开发者只改了一个按钮文案。

结果：

```text
本地 assembleDebug 3 分钟。
CI 跑完整检查 28 分钟。
PR 排队等机器 40 分钟。
合并后发现 release 构建才失败。
```

团队开始出现一些习惯：

```text
本地不跑测试。
PR 拆得越来越大。
能不升级依赖就不升级。
CI 红了先重跑。
构建失败靠群里问人。
```

这不是懒。

这是反馈系统太慢后，人类自然做出的适应。

## 现场侦探问题

如果 CI 很慢，你会先买更强机器吗？

有时候需要。

但更应该先问：

```text
哪些 task 最慢？
哪些 task 没有缓存？
哪些模块被不必要地重新编译？
annotation processing 是否拖慢构建？
资源处理和 Dex 是否重复执行？
测试是否可以分层执行？
```

构建优化也要有证据链。

## 学习目标

学完本节后，你应该能够：

- 理解 Android 构建链路的大致阶段。
- 区分本地构建、增量构建和 CI 构建的优化重点。
- 使用 build scan、profile、Gradle task 输出定位慢点。
- 解释缓存、增量编译、configuration cache 的价值。
- 设计分层 CI 流水线。
- 建立构建性能基线和回归检查意识。

## 第一部分：Android 构建大致在做什么

一次 Android 构建通常会经历：

```text
读取 Gradle 配置
解析依赖
生成 BuildConfig / R / Manifest
编译 Kotlin / Java
处理 KSP / KAPT / annotation processor
编译资源
Dex
R8 / shrink / obfuscate
打包 APK / AAB
签名
运行测试和静态检查
```

不同阶段慢，原因不一样。

例如：

| 慢点 | 可能原因 |
| --- | --- |
| Configuration 慢 | build script 逻辑重、插件多、跨 project 配置 |
| Kotlin 编译慢 | 模块过大、ABI 变化频繁、KAPT 成本高 |
| 资源编译慢 | 资源多、模块资源边界不清、频繁全量处理 |
| Dex 慢 | 依赖多、类数量大、增量失效 |
| R8 慢 | 规则复杂、release 构建缺少分层策略 |
| 测试慢 | 测试粒度混杂、环境初始化重 |

不要只说“Gradle 慢”。

要说清楚慢在哪一段。

## 第二部分：本地构建和 CI 构建不是一回事

本地构建追求：

```text
增量快
反馈短
IDE 同步稳定
开发者能频繁运行
```

CI 构建追求：

```text
可复现
可追踪
覆盖关键门禁
并行和缓存稳定
失败信息清楚
```

本地可以重视增量。

CI 更重视确定性。

这也是为什么 CI 里不要依赖某个人电脑上残留的状态。

## 第三部分：构建优化的证据

构建优化也需要前后对比：

```text
优化前 assembleDebug：180s
优化后 assembleDebug：95s
优化前 CI check：28min
优化后 CI check：16min
缓存命中率：42% -> 78%
最慢 task：:feature:course:kaptDebugKotlin -> 已迁移 KSP
```

常用观察方式：

```bash
./gradlew assembleDebug --profile
./gradlew assembleDebug --scan
./gradlew :app:assembleDebug --dry-run
./gradlew help --scan
```

如果不能联网使用 build scan，也可以先看 Gradle profile、CI task 耗时和日志。

一个简化的构建耗时报告可以这样读：

```text
Task                                  Duration
:feature:course:kaptDebugKotlin       62.4s
:app:mergeDebugResources              28.1s
:app:dexBuilderDebug                  21.7s
:core:database:kspDebugKotlin         12.6s
```

这份报告不能只看“谁最慢”。

还要继续问：

```text
这个 task 每次都慢，还是只有冷构建慢？
它是否支持增量？
它的输入是否经常变化？
它是否被不相关模块改动触发？
它是否可以通过模块拆分、KSP 迁移、缓存或 CI 并行改善？
```

例如 `:feature:course:kaptDebugKotlin` 很慢，可能不是课程模块代码写得差，而是：

```text
注解处理器不支持增量。
模块 API 变化太频繁。
feature 模块依赖过多。
Hilt 聚合任务影响范围太大。
```

构建分析要避免“看到慢 task 就怪某个模块”。真正有价值的结论应该指出触发条件、影响范围和改造方向。

## 第四部分：缓存和增量编译

构建优化里最重要的几个词：

```text
incremental build
build cache
configuration cache
remote cache
ABI
task input / output
```

可以这样理解：

```text
增量编译
  -> 只重新编译受影响的部分

build cache
  -> 输入一样时复用已有 task 输出

configuration cache
  -> 复用 Gradle 配置阶段结果

remote cache
  -> 团队共享可复用构建产物
```

缓存不是开关一开就结束。

如果 task 输入不稳定，缓存就很难命中。

常见缓存杀手：

```text
构建脚本读取当前时间
task 输入包含绝对路径
生成文件没有声明 input / output
插件在配置阶段做重活
版本号或环境变量无序变化
```

## 第五部分：KAPT、KSP 和注解处理

Android 工程里，注解处理常常是构建热点。

常见来源：

```text
Room
Hilt
Moshi
Glide
自定义 annotation processor
```

如果还在大量使用 KAPT，要特别关注：

```text
是否支持 KSP？
是否开启增量处理？
是否某个 processor 导致全量重跑？
是否可以减少跨模块注解影响？
```

第 7 章讲 Hilt 怎么用。

第 25 章要补一句：

```text
依赖注入提升工程结构，但它也会进入构建成本。
```

工程治理就是要同时看收益和代价。

## 第六部分：CI 分层

不要让每一次 PR 都跑全世界。

可以把 CI 分层：

| 层级 | 触发时机 | 检查内容 |
| --- | --- | --- |
| 快速检查 | 每个 PR | 编译、lint、关键单测、格式 |
| 模块检查 | 相关路径变化 | 对应 feature / core 测试 |
| 集成检查 | 合并前或主干 | assemble、集成测试、依赖分析 |
| Release 检查 | 发版前 | R8、签名、包体积、mapping、权限、渠道配置 |
| 夜间检查 | 定时 | 全量测试、性能基线、稳定性巡检 |

CI 的目标不是“每次跑最多”，而是“在正确时机给出正确反馈”。

## 第七部分：构建效率也要治理

建议团队记录构建指标：

```text
本地冷构建时间
本地增量构建时间
CI PR 平均耗时
CI p95 耗时
缓存命中率
最慢 task 排名
失败最多的 task
release 构建耗时
```

没有指标，构建优化很容易变成感觉。

有指标，团队才能知道一次改造到底有没有价值。

## 本节自测

- 为什么构建慢会影响代码质量？
- 本地构建和 CI 构建的优化目标有什么不同？
- build cache 和 configuration cache 分别解决什么问题？
- 为什么 KAPT / KSP 会成为大型工程构建治理重点？
- CI 为什么需要分层，而不是每次都跑全部？

## 本节总结

构建效率是工程反馈速度。

大型 Android 工程要把构建慢当成可观测、可分析、可治理的问题：

```text
先定位慢点。
再优化任务。
再提高缓存。
再分层 CI。
最后持续度量。
```

当反馈变快，团队才更愿意频繁验证，质量也会自然变好。
