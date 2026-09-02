# Hello Android Capstone 真实观察指南

这份指南用于把第 26 章 Demo 从“页面可玩”推进到“证据可查”。

终章项目里，页面变化只是第一层。真正值得训练的是：你能否把一次操作对应到日志、Trace、时间线、任务状态和发布结论。

## 观察前检查

运行 Demo 后，先确认页面里的工程快照：

```text
applicationId：
versionName / versionCode：
BUILD_PROFILE：
debug：
sessionStartMs：
```

这些信息用于回答一个基础问题：

```text
我现在观察的到底是哪一个 App、哪一个版本、哪一种构建配置？
```

真实排障里，如果这个问题没有确认，后面的日志和指标都可能看错现场。

## Logcat 观察

在 Android Studio Logcat 中过滤：

```text
HelloCapstone
```

然后在 Demo 中执行两类操作：

```text
点击“重新追踪一次点击链路”
点击“注入当前事故证据”
```

预期你能看到两类日志：

```text
runTrace traceId=..., chapter=...
simulateIncident id=..., symptom=...
```

如果页面时间线变化了，但 Logcat 没有对应记录，就说明这次操作还没有形成真实证据。

## Trace 观察

Demo 内置两个 Trace section：

```text
CapstoneRunTrace
CapstoneIncident:<scenarioId>
```

推荐观察路径：

```text
1. 打开 Android Studio Profiler。
2. 启动 CPU trace 或 System Trace。
3. 在 Demo 中点击“重新追踪一次点击链路”。
4. 再选择一个事故剧本并注入证据。
5. 在 trace 结果中查找 CapstoneRunTrace 或 CapstoneIncident。
```

这一步训练的是“把用户动作映射到系统观察点”。

## 时间线对照

页面里的证据时间线会展示类似内容：

```text
[course-open-20] click course card
[gfxinfo] longest frame = 96ms
[perfetto] main thread busy, no Binder wait
[fix] move parse to Dispatchers.Default
```

请把它和 Logcat / Trace 对照：

| 页面线索 | 应该如何验证 |
| --- | --- |
| traceId | Logcat 中是否出现同一个 traceId |
| 主线程忙碌 | Perfetto / Profiler 中主线程是否有连续执行片段 |
| 帧耗时 | gfxinfo 或帧时间线是否支持这个判断 |
| 后台约束 | dumpsys jobscheduler / WorkManager 状态是否吻合 |
| 资源错乱 | 配置、主题、资源来源是否能解释 UI 现象 |
| 签名异常 | apksigner / 归档证书是否能证明差异 |

## 事故观察顺序

建议按这个顺序排查：

```text
1. 先看用户现象。
2. 再看页面证据时间线。
3. 再看 Logcat 是否有同一条操作记录。
4. 再看 Trace section 是否出现。
5. 再判断相关任务是否被推进到需要补证据。
6. 最后看发布门禁是否应该阻塞。
```

这个顺序看起来有点慢，但它能避免一个常见问题：

```text
还没有证据，就急着宣布根因。
```

## 答辩时怎么讲

答辩时可以这样说：

```text
我先确认当前运行的是 com.helloandroid.capstone。
这次构建的 BUILD_PROFILE 是 debug-capstone。
我点击课程详情后，页面生成了 traceId。
同一时间，Logcat 中出现 HelloCapstone 的 runTrace 日志。
我注入事故后，证据时间线、相关任务状态和 Trace section 同时变化。
所以这个 Demo 不是静态说明，而是能把操作、证据和治理结论串起来。
```

这段话不长，但能体现一种成熟工程意识：先确认现场，再建立证据链，最后给结论。
