# 第25章示例工程：大型工程治理工作台

这个工程对应课程第 25 章：大型 Android 工程治理：模块边界、构建效率、发布、监控与团队协作。

它不是一个普通的列表页面，而是一张“发版前工程体检台”。你可以在页面里选择事故剧本，观察模块依赖、组件契约、插件协议、构建耗时、依赖冲突、发布产物、发布门禁、监控指标和技术债，再根据风险等级生成一份发布治理报告。

## 学习目标

运行本工程后，你应该能回答：

- 为什么大型工程治理不是写流程，而是让风险可见、可控、可回归？
- 模块化、组件化和插件化分别解决什么问题？
- 为什么 `core` 反向依赖 `feature` 是架构底线风险？
- 为什么组件之间应该通过 contract / api 通信？
- 为什么插件化不只是“能加载”，还要治理宿主协议、资源、签名、崩溃和回滚？
- 为什么构建慢要看最慢 task、缓存命中和触发条件？
- 为什么依赖冲突不能只看 Gradle 是否报错，还要看运行时 classpath？
- 为什么总分很高但 mapping 缺失时仍然不能发布？
- 一份大型工程治理报告应该包含哪些证据、决策和后续动作？

## 工程结构

```text
25-engineering-governance-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/java/com/helloandroid/governance/
      MainActivity.kt
      GovernanceLabScreen.kt
      GovernanceLabState.kt
      GovernanceLabStore.kt
    src/main/res/values/
      colors.xml
      strings.xml
      themes.xml
  quality/
    governance-rules.json
    governance-release-report-template.md
```

## 运行方式

用 Android Studio 打开本目录：

```text
examples/25-engineering-governance-lab/
```

等待 Gradle Sync 完成后，运行 `app`。

如果你习惯命令行，并且本机有 Gradle Wrapper 或全局 Gradle，也可以执行：

```bash
./gradlew :app:assembleDebug
```

当前仓库环境没有统一的根 Gradle Wrapper，建议优先用 Android Studio 打开示例工程。

## 实验区域

### 治理规则模式

页面提供三种治理策略：

| 模式 | 适合场景 | 决策倾向 |
| --- | --- | --- |
| 保守门禁 | 支付、账号、安全和大版本发布 | P1 也倾向先修复 |
| 平衡灰度 | 普通业务迭代 | P1 可以小流量灰度 |
| 快速实验 | 内部实验和低风险功能 | P1 可以 1% 实验灰度 |

你可以先切换不同模式，再观察同一个事故剧本下的健康分和发布结论。

注意，规则可以宽松，但底线不能消失。只要出现 P0，Demo 都会给出“暂停发布”。

配套规则示例放在：

```text
quality/governance-rules.json
```

真实项目里，这类规则可以继续沉淀到 Gradle convention plugin、CI 门禁、发布平台和监控平台中。

### 任务板

页面顶部把本章拆成 10 个观察点：

```text
选择事故剧本
检查模块边界
检查组件契约
检查插件协议
检查构建耗时
检查依赖冲突
检查发布产物
检查监控告警
排序技术债
输出治理报告
```

建议不要一上来直接看报告。

更好的顺序是：

```text
选择剧本
  -> 看风险证据
      -> 判断 P0 / P1 / P2 / P3
          -> 检查发布决策
              -> 输出治理报告
```

### 事故剧本

工程内置八个剧本：

| 剧本 | 训练重点 |
| --- | --- |
| 2.5.0 发版前体检 | mapping、依赖冲突、构建耗时和发布阻塞 |
| 组件契约失控 | 组件化边界、contract、公共模块膨胀 |
| 插件协议不兼容 | 宿主插件协议、签名、崩溃归因和回滚 |
| 模块依赖倒流 | core 反向依赖 feature、模块边界底线 |
| 权限新增未评审 | 权限申请、拒绝路径、商店审核和灰度监控 |
| 资源前缀开始混乱 | 资源命名、包体积变化和后续主题替换风险 |
| 签名证书指纹异常 | release 签名、构建档案和供应链可信度 |
| release 配置串环境 | flavor、BuildConfig、远程配置和生产环境保护 |

每个剧本都会激活不同风险，页面会自动计算健康分和发布结论。

### 模块 / 组件 / 插件治理

这一块会展示一张简化依赖图：

```text
app
  -> feature:course
      -> feature:payment.impl
          -> feature:profile

core:common
  -> feature:profile

plugin:download
  -> minHostProtocol = 3
  -> currentHostProtocol = 2
```

你需要判断：

```text
哪些是模块化风险？
哪些是组件化风险？
哪些是插件化风险？
哪些风险会阻塞发布？
```

### 构建效率面板

页面会模拟几条构建耗时：

```text
:feature:course:kaptDebugKotlin       62.4s
:app:mergeDebugResources              28.1s
:app:dexBuilderDebug                  21.7s
:core:database:kspDebugKotlin         12.6s
```

目标不是记住 task 名，而是学会继续追问：

```text
它为什么慢？
是否每次都慢？
是否支持增量？
是否可以缓存？
是否被不相关模块触发？
```

### 依赖 / 资源 / 配置变化

工程会展示一段简化的 `dependencyInsight` 输出：

```text
okio:2.10.0 -> 3.6.0
\--- network-lib:1.5
     \--- appDebugRuntimeClasspath
```

这说明 Gradle 最终选择了 `okio:3.6.0`，但 `network-lib` 原本依赖 `2.10.0`。

你要判断：

```text
升级旧库？
约束版本？
exclude？
降级新库？
替换库？
```

同时，Demo 也会列出新增权限、exported 组件、资源前缀、包体积变化和 release 环境配置。这里要训练的是第 25 章的工程治理视角：一个改动能不能发布，不只看代码能不能编译，还要看它是否改变了权限、入口、资源、包体积和线上配置。

### 发布决策

Demo 会故意设置：

```text
mapping = missing
```

所以即使健康分不低，也应该得到：

```text
暂停发布
```

这对应第 25 章里的底线判断：

```text
总分不能掩盖 P0 阻塞项。
```

### 发布门禁清单

Demo 会把发版前检查拆成几道门：

```text
release mapping 已归档
组件 contract 无越界调用
插件协议兼容当前宿主
核心链路灰度监控已配置
回滚路径已演练
```

这里要练的不是“把 checklist 勾完”，而是判断每个失败项背后有没有负责人、证据、截止时间和回滚路径。大型工程里，很多事故并不是没人发现，而是发现之后没有进入决策。

### 监控告警面板

页面会展示：

```text
crash-free
ANR rate
冷启动 p95
慢帧率
登录成功率
下载成功率
```

你要练习把指标翻译成决策：

```text
继续灰度
降低灰度
关闭远程开关
暂停发布
进入根因分析
```

### 技术债排序

Demo 会列出几条技术债：

```text
拆分 core:common 中的业务状态
Hilt kapt 迁移到 KSP
下载插件补齐协议兼容测试
```

你需要判断它们谁影响本次发布，谁可以进入后续排期，谁必须在灰度前处理。技术债如果没有排序，就会从“以后再说”慢慢变成“线上事故已经发生”。

### 治理报告

页面底部会生成一份治理报告，包含：

```text
版本
构建号
Git commit
mapping / symbols
remote config
发布结论
工程健康分
当前剧本
发布门禁
风险清单
回归计划
```

这份报告不是最终答案，而是练习模板。

你可以直接复制报告，也可以通过系统分享面板发给同学或团队成员，模拟一次真实发版评审。更推荐的玩法是：先选择事故剧本，再切换治理规则，最后复制报告，对比不同规则下发布结论为什么变化。

## 推荐练习

- 新增一个事故剧本，例如“资源前缀冲突导致 release UI 错乱”。
- 新增一个 P0 风险，例如“release 签名证书不匹配”。
- 修改 `quality/governance-rules.json` 里的阈值，再同步到 `GovernanceLabStore.kt`，观察发布结论变化。
- 修改健康分算法，让 P0 风险直接把分数压到 60 以下。
- 给每个风险增加负责人和截止时间。
- 把治理报告改成更正式的团队发版评审格式。
- 把组件契约检查改成真实的模块依赖图解析。

## 对应课程内容

- [25.1 为什么要学习大型 Android 工程治理](../../docs/chapter25/chapter25_1.md)
- [25.2 模块边界、依赖方向与架构防腐](../../docs/chapter25/chapter25_2.md)
- [25.3 Gradle、构建效率、缓存与 CI 加速](../../docs/chapter25/chapter25_3.md)
- [25.4 依赖、资源、配置与多环境治理](../../docs/chapter25/chapter25_4.md)
- [25.5 发布、签名、灰度、回滚与版本治理](../../docs/chapter25/chapter25_5.md)
- [25.6 监控、告警、性能基线与事故复盘](../../docs/chapter25/chapter25_6.md)
- [25.7 团队协作、代码评审、技术债与治理节奏](../../docs/chapter25/chapter25_7.md)
- [25.8 综合实践：大型工程治理工作台](../../docs/chapter25/chapter25_8.md)
