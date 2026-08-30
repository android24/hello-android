# 24 附录：从 Perfetto 到 CausalPerf / SmartPerfetto

第 24 章前面讲的是系统原生观测工具。

这一篇附录要往前走一步：

```text
当 logcat、dumpsys、bugreport 和 Perfetto 都拿到了，
工程师如何更快地从海量证据里找到因果链？
```

这正好可以引出两个延伸项目：

```text
CausalPerf
SmartPerfetto
```

在课程里，它们不应该被写成普通产品介绍。

更好的定位是：

```text
原生工具给证据。
工程师建立判断。
CausalPerf / SmartPerfetto 帮助工程师组织、解释和复盘证据。
```

## 项目地址与参与方式

- SmartPerfetto：[https://github.com/Gracker/SmartPerfetto](https://github.com/Gracker/SmartPerfetto)
- CausalPerf：[https://github.com/android24/CausalPerf](https://github.com/android24/CausalPerf)

其中，`CausalPerf` 目前仍处于开发中。

如果你对 Android 性能分析、Perfetto trace 解析、因果链建模、自动诊断报告或工程化工具建设感兴趣，非常欢迎参与进来：

```text
可以从阅读设计文档开始。
可以从补充性能案例开始。
可以从完善 trace 解析规则开始。
也可以从提交 issue、讨论场景和复现实验开始。
```

这类工具最需要的不是一个人把所有答案写完，而是一群人在真实性能问题里不断校准证据、规则和判断。

更具体地说，可以从这些方向切入：

| 参与方向 | 适合做什么 |
| --- | --- |
| 性能案例 | 补充真实或可复现的卡顿、ANR、启动、内存、Binder 等案例 |
| Trace 解析 | 完善 Perfetto trace 中线程、slice、FrameTimeline、Binder 事件的解析规则 |
| 因果链建模 | 设计现象、证据、候选根因、置信度和回归结果之间的关系 |
| 报告生成 | 把分析结果整理成更适合团队复盘的 Markdown / HTML 报告 |
| Demo 实验 | 为课程和工具准备可重复触发、可观测、可对比的 Android 实验场景 |
| 文档建设 | 补充使用说明、案例教程、术语解释和贡献指南 |

## 本附录先记住三句话

```text
Perfetto 强在时间线，但不会自动告诉你根因。
性能事故难在因果关系，而不是难在缺少图表。
智能化工具的价值不是替代工程师，而是减少重复寻找线索的成本。
```

## 一、Perfetto 的能力边界

Perfetto 能告诉你很多事实：

```text
哪个线程在运行
哪个线程在等待
哪一帧超时
Binder 调用发生在哪个时间段
system_server 是否参与
CPU 频率和调度状态如何变化
```

但 Perfetto 默认不会直接告诉你：

```text
根因是哪一段业务逻辑？
哪个事件是因，哪个事件是果？
同类问题在多个 trace 里是否重复出现？
哪些 slice 应该被优先关注？
这次修复是否真的让关键链路变短？
```

所以很多团队会遇到一个尴尬局面：

```text
trace 文件拿到了。
图也打开了。
每个人都在时间线上拖来拖去。
最后报告里还是一句：疑似主线程耗时。
```

这就是智能化诊断工具可以发挥作用的地方。

## 二、CausalPerf：从现象走向因果链

`CausalPerf` 适合放在“因果链分析”这一层讲。

课程里可以这样介绍它的目标：

```text
把一次性能现象拆成：
现象
  -> 直接证据
      -> 上游事件
          -> 影响路径
              -> 候选根因
                  -> 回归验证
```

比如一个课程详情页白屏 2 秒的问题，人工分析时可能会看到：

```text
Frame 超时
main thread 长任务
Binder wait
数据库查询
图片解码
GC
CPU 降频
```

真正困难的是判断：

```text
谁只是同时出现？
谁发生在关键路径上？
谁阻塞了用户可见结果？
谁修了之后体验会变好？
```

`CausalPerf` 可以被设计成一套因果推理工作台：

| 层级 | 要回答的问题 |
| --- | --- |
| 现象层 | 用户到底感受到什么 |
| 证据层 | 哪些 trace / log / dumpsys 片段能证明现象 |
| 链路层 | 事件之间的先后和等待关系是什么 |
| 候选层 | 哪些节点可能是真正根因 |
| 置信层 | 这些根因候选的证据强弱如何 |
| 回归层 | 修复后同一路径是否改善 |

注意这里的措辞是“候选根因”，不是“绝对根因”。

因为性能分析很少只靠一份 trace 就能宣判。更稳的做法是让工具给出线索排序，工程师再结合代码、业务和多次复现做判断。

## 三、SmartPerfetto：让 trace 更容易被读懂

`SmartPerfetto` 更适合放在“Perfetto 分析增强”这一层讲。

Perfetto 原始时间线很强，但对学习者和团队新人并不友好：

```text
不知道该看哪个进程
不知道该展开哪个线程
不知道哪些 category 有用
不知道一个长 slice 是否真的异常
不知道报告应该怎么写
```

`SmartPerfetto` 可以被课程化地理解为：

```text
Perfetto trace
  -> 自动识别关键窗口
      -> 标注主线程、RenderThread、Binder、FrameTimeline
          -> 聚合异常片段
              -> 输出证据链草稿
                  -> 生成可复盘报告
```

它的价值不是把 Perfetto 包起来，而是降低读 trace 的认知成本。

比如它可以围绕这些问题工作：

| 问题 | 智能化辅助方向 |
| --- | --- |
| trace 太大 | 自动裁剪到用户操作窗口 |
| 线程太多 | 标出主线程、RenderThread、Binder 关键线程 |
| slice 太碎 | 聚合长任务、等待、帧超时和异常片段 |
| 初学者不会读 | 给出“先看这里”的阅读路径 |
| 团队报告不统一 | 自动生成统一证据链模板 |

## 四、二者和 Perfetto 的关系

它们不应该被描述成“替代 Perfetto”。

更准确的关系是：

```text
Perfetto
  -> 提供系统时间线和底层证据

SmartPerfetto
  -> 帮你更快读懂 trace

CausalPerf
  -> 帮你把多个证据组织成因果链
```

如果用事故调查来类比：

```text
Perfetto 是监控录像。
SmartPerfetto 是录像里的自动标注和重点片段。
CausalPerf 是把片段、证词和现场状态串成案情时间线。
```

## 五、一个完整案例应该怎么讲

课程里介绍这两个工具时，最好用同一个事故贯穿：

```text
现象：课程详情页白屏 2 秒
时间点：21:16:02 点击课程卡片
logcat：记录 click traceId=course-42
dumpsys activity：CourseDetailActivity 已经 RESUMED
Perfetto：main thread 有 84ms 长任务，随后 Binder wait 320ms
gfxinfo：Janky frames 15.9%
bugreport：没有系统级 crash，后台状态正常
```

然后让工具辅助生成：

```text
直接现象：
  -> 首帧延迟和掉帧

关键证据：
  -> 主线程长任务
  -> Binder wait
  -> Activity 已进入 RESUMED

候选根因：
  -> 详情页首帧前做了同步数据准备
  -> 某个系统服务调用阻塞了主线程

建议验证：
  -> 把数据准备移出首帧关键路径
  -> 为系统调用增加异步化或缓存
  -> 重采同一路径 trace 对比长任务和帧耗时
```

这比单纯展示工具界面更能打动读者。

## 六、放进团队流程

如果后续要把 `CausalPerf` 和 `SmartPerfetto` 做成课程亮点，可以把它们放进这样的工程流程：

```text
本地复现
  -> 一键采集 trace / logcat / dumpsys
      -> 自动识别关键时间窗口
          -> 生成证据链草稿
              -> 工程师确认根因
                  -> 修复代码
                      -> 再次采集
                          -> 自动对比优化前后
                              -> 输出回归报告
```

这个流程非常适合和第 24 章 Demo 结合。

Demo 不只制造卡顿，还可以制造“可被工具分析的事故”：

```text
主线程同步 I/O
首帧前 JSON 解析
Binder wait
后台任务延迟
图片解码过重
内存持续上涨
```

这样读者会明白：

```text
系统工具负责打开现场。
智能工具负责整理现场。
工程师负责判断和修复现场。
```

## 七、写作边界

作为课程延伸项目，介绍 `CausalPerf` 和 `SmartPerfetto` 时建议保持三个边界：

```text
不要把它写成广告。
不要在读者还不懂 Perfetto 前直接讲智能诊断。
不要承诺工具能自动给出唯一根因。
```

更好的表达是：

```text
它们是在理解系统观测工具之后，
进一步把诊断流程工程化、自动化、团队化的尝试。
```

这会让工具介绍显得自然，也更有技术含量。

## 本附录自测

- 为什么 Perfetto 很强，但仍然需要智能化分析工具？
- CausalPerf 更适合解决“证据不足”还是“因果关系难判断”？
- SmartPerfetto 应该优先降低哪几类 trace 阅读成本？
- 为什么自动诊断工具最好输出候选根因，而不是唯一结论？

## 本附录总结

第 24 章的主体让读者学会使用系统观测工具。

本附录则告诉读者：

```text
当工具越来越多，真正高级的能力不是再背一个命令，
而是把证据组织成因果链，
再把因果链沉淀成团队可以复用的诊断流程。
```

`CausalPerf` 和 `SmartPerfetto` 可以成为这门课程非常有辨识度的亮点。

它们让第 24 章不只停留在“会用工具”，而是继续走向：

```text
会取证
会分析
会归因
会复盘
会把诊断能力产品化
```
