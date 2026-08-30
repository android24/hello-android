# 第24章示例工程：系统证据链分析实验室

这个工程对应课程第 24 章：系统观测工具：Perfetto、dumpsys、bugreport 与证据链分析。

它不是一个普通性能 demo，而是一张“事故现场取证台”。你可以在页面里选择事故剧本，制造带 `traceId` 的轻量卡顿或内存增长，查看 logcat、dumpsys、Perfetto、gfxinfo、meminfo、procstats、simpleperf 和 bugreport 应该如何组合，完成证据链答题，最后生成一份可复制、可分享的证据链报告。

## 学习目标

运行本工程后，你应该能回答：

- 为什么排查复杂问题不能只看 logcat？
- logcat、dumpsys、Perfetto、bugreport 分别回答什么问题？
- 为什么第一步一定要锁定复现时间点？
- 为什么 dumpsys 是系统状态快照，而不是完整时间线？
- 为什么 Perfetto 适合分析线程、Binder、调度和帧？
- 为什么 gfxinfo、meminfo、procstats、simpleperf 要和 Perfetto 一起读？
- 为什么 bugreport 分享前必须脱敏？
- 为什么 traceId 和 trace section 能降低跨工具对齐成本？
- 如何判断第一证据、状态证据、时间线证据和根因候选是否匹配？
- 一份证据链报告应该如何写，才能支撑根因判断和回归验证？

## 工程结构

```text
24-observability-evidence-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/java/com/helloandroid/observability/
      MainActivity.kt
      ObservabilityLabScreen.kt
      ObservabilityLabState.kt
      ObservabilityLabStore.kt
    src/main/res/values/
      colors.xml
      strings.xml
      themes.xml
  quality/
    evidence-command-cookbook.md
    evidence-chain-report-template.md
    perfetto_config.textproto
```

## 运行方式

用 Android Studio 打开本目录：

```text
examples/24-observability-evidence-lab/
```

等待 Gradle Sync 完成后，运行 `app`。

如果你习惯命令行，并且本机有 Gradle Wrapper 或全局 Gradle，也可以执行：

```bash
./gradlew :app:assembleDebug
```

当前仓库环境没有统一的根 Gradle Wrapper，建议优先用 Android Studio 打开示例工程。

## 实验区域

### 现场触发器

页面提供两个轻量触发按钮：

```text
制造主线程忙碌
制造内存增长
```

它们不会故意制造危险 ANR，而是生成适合练手的观测信号。你可以先点击按钮，再用 logcat、Perfetto、gfxinfo 或 meminfo 观察变化。

每次触发都会产生一个 `traceId`，并写入：

```text
logcat tag：Chapter24Lab
trace section：ch24_main_thread_work_<traceId>
trace section：ch24_memory_growth_<traceId>
```

这能让你在 logcat 里先找到时间点，再回到 Perfetto 里搜索对应 section，把“日志里的事件”和“时间线里的耗时”接起来。

### 任务板

页面把本章拆成 8 个观察点：

```text
制造或选择现场
锁定时间点
确认系统状态
阅读时间线
补充性能指标
保存完整现场
脱敏后协作
输出证据链报告
```

建议按下面节奏练：

```text
选剧本
  -> 找 logcat 时间点
      -> 查 dumpsys 状态
          -> 读 Perfetto 时间线
              -> 补 gfxinfo / meminfo / simpleperf
                  -> 用 bugreport 保存完整现场
                      -> 写报告
```

### 事故剧本

工程内置六个剧本：

| 剧本 | 训练重点 |
| --- | --- |
| 详情页点击后卡顿 | logcat 时间点、主线程长任务、首帧延迟 |
| 列表滑动掉帧 | gfxinfo、FrameTimeline、RenderThread、CPU 热点 |
| 后台同步没有执行 | dumpsys jobscheduler、device idle、bugreport |
| 内存压力后进程被回收 | meminfo、procstats、LMKD、状态恢复 |
| 偶发 ANR 等待 | ANR trace、Binder wait、Perfetto 对齐 |
| 某机型 Native 崩溃 | tombstone、signal、symbols、native backtrace |

每个剧本都会激活不同证据卡片，页面会自动计算诊断可信度。

### 证据链卡片

证据不是越多越好，而是要能回答不同问题：

| 工具 | 更适合回答 |
| --- | --- |
| logcat | 什么时候发生、发生了什么事件 |
| dumpsys | 系统当前认为状态是什么 |
| Perfetto | 时间线上谁在运行、谁在等待 |
| gfxinfo | 掉帧是否真实存在 |
| meminfo | 内存是否异常增长 |
| procstats | 进程状态历史如何变化 |
| simpleperf | CPU 热点在哪里 |
| bugreport | 事故完整现场如何保存 |

### 命令取证区

选中证据卡片后，页面会给出对应命令，例如：

```bash
adb logcat -v threadtime | grep Chapter24Lab
adb shell dumpsys activity activities
adb shell dumpsys jobscheduler
adb shell dumpsys gfxinfo com.helloandroid.observability framestats
adb shell dumpsys meminfo com.helloandroid.observability
adb shell dumpsys procstats --hours 3
adb bugreport bugreports/
```

更多命令放在：

```text
quality/evidence-command-cookbook.md
```

### Perfetto 阅读路线

Demo 给出一条固定路线：

```text
找到复现时间点
  -> 展开 app main thread
      -> 对齐 RenderThread / Binder / system_server
          -> 看慢帧附近谁在运行、谁在等待
              -> 回到 logcat 和 dumpsys 验证推论
```

配套 Perfetto 配置放在：

```text
quality/perfetto_config.textproto
```

### 脱敏与协作检查

证据链不是越完整越能随便发。

分享 bugreport、trace 或日志前，要先检查：

```text
账号
手机号
token
定位
文件路径
设备序列号
业务订单号
应用私有数据
```

这部分承接第 23 章的安全意识，也为第 25 章的团队治理做准备。

### 证据链答题区

Demo 里有一组小题，用来训练判断顺序：

```text
复杂事故第一步看什么？
Job 调度状态应该问谁？
主线程运行还是等待应该看什么？
bugreport / trace 分享前要做什么？
```

提交后会显示得分和解释。它的目的不是考试，而是让你把“工具名字”变成“证据选择能力”。

### 证据链报告

页面底部会生成一份证据链报告，包含：

```text
包名
事故剧本
表面现象
第一问题
根因候选
诊断可信度
traceId
答题结果
证据链
报告结论
```

你可以复制报告，也可以通过系统分享面板发给同学或团队成员，模拟一次真实事故复盘。

## 推荐练习

- 新增一个事故剧本，例如“通知不弹”或“前台服务被系统停止”。
- 给证据卡片增加“证据可信度”和“反证信息”。
- 把主线程忙碌时间从 120ms 调整到 300ms，对比 gfxinfo 和 Perfetto。
- 把内存增长按钮改成分批分配，观察 meminfo 是否更容易看出趋势。
- 新增一道证据链题，例如“Native Crash 应该先看 logcat 还是 tombstone？”。
- 用 `quality/perfetto_config.textproto` 采集一次真实 trace。
- 用 `quality/evidence-chain-report-template.md` 写一份自己的事故报告。

## 对应课程内容

- [24.1 为什么要学习系统观测工具、证据链与事故分析](../../docs/chapter24/chapter24_1.md)
- [24.2 logcat、结构化日志与时间点定位](../../docs/chapter24/chapter24_2.md)
- [24.3 dumpsys：系统服务状态快照与问题定位](../../docs/chapter24/chapter24_3.md)
- [24.4 bugreport、DropBox、ANR trace 与 tombstone：完整事故包怎么读](../../docs/chapter24/chapter24_4.md)
- [24.5 Perfetto：从系统时间线看线程、Binder、调度与帧](../../docs/chapter24/chapter24_5.md)
- [24.6 gfxinfo、meminfo、procstats 与 simpleperf：性能现场证据](../../docs/chapter24/chapter24_6.md)
- [24.7 观测体验问题：误读、隐私、复现与团队协作](../../docs/chapter24/chapter24_7.md)
- [24.8 综合实践：系统证据链分析实验室](../../docs/chapter24/chapter24_8.md)
- [24 附录：从 Perfetto 到 CausalPerf / SmartPerfetto](../../docs/chapter24/appendix_smart_perfetto.md)
