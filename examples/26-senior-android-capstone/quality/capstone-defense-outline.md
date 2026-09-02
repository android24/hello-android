# Hello Android Capstone 终章答辩大纲

这份大纲用于第 26 章毕业项目答辩。

建议控制在 10 到 15 分钟。

## 1. 项目定位

```text
Hello Android Capstone 是一个课程学习 App。
它同时也是一个 Android 工程能力展示台。
目标是把主路径、架构、Framework、诊断、治理和答辩串成闭环。
```

## 2. 主路径演示

```text
首页
  -> 选择章节
      -> 打开详情
          -> 查看学习进度
              -> 收藏 / 笔记 / 离线任务
                  -> 观察 traceId 和诊断事件
```

演示前先说明当前工程现场：

```text
applicationId：
versionName / versionCode：
BUILD_PROFILE：
Logcat Tag：HelloCapstone
Trace section：CapstoneRunTrace
```

这一步能避免答辩一开始就变成“我点给你看”。你是在说明：我知道自己正在验证哪个版本，也知道证据会从哪里出现。

答辩时不要只说“这个页面能点”。

要说明：

```text
这个操作属于哪一层。
它的数据从哪里来。
失败时如何反馈。
它留下了什么证据。
```

## 3. 架构设计

推荐讲清：

```text
app
feature-course
feature-diagnosis
feature-governance
domain-course
data-course
core-ui
core-observability
core-governance
```

重点不是模块数量，而是依赖方向。

## 4. Framework 因果链

请选择一次点击讲清：

```text
InputDispatcher 为什么要找目标窗口
ViewRootImpl 为什么是 App 和窗口之间的桥
ActivityThread 为什么承载主线程消息
Choreographer 为什么决定下一帧
RenderThread 和 SurfaceFlinger 为什么参与最终显示
```

不要背类名，要讲因果。

## 5. 事故诊断

至少展示一次完整诊断：

```text
现象
时间线
证据
假设
反证
根因
修复
回归
治理动作
```

建议现场选择一个事故剧本，点击注入证据，然后同时展示三件事：

```text
页面证据时间线是否变化
Logcat 中是否出现 HelloCapstone 日志
相关任务或动手实验状态是否被推进
```

如果这三件事能对上，说明你的 Demo 已经不是静态演示，而是能把事故、证据和工程状态串起来。

## 6. 发版治理

说明这次版本能不能发：

```text
P0 是否存在
P1 是否允许灰度
mapping / symbols 是否归档
权限和组件是否安全
性能基线是否退化
回滚策略是否明确
```

## 7. 后续演进

只讲最重要的 3 件事。

例如：

```text
接入真实 Perfetto trace 导入
把发布门禁接入 CI
补充 Baseline Profile 和启动专项
```

## 8. 答辩 Rubric 自评

最后用 1 分钟对照 Rubric 自评：

| 分数 | 当前是否具备 | 需要展示的证据 |
| --- | --- | --- |
| 60 分 |  | 主路径能运行 |
| 70 分 |  | 架构边界能解释 |
| 80 分 |  | Framework 因果链能讲清 |
| 90 分 |  | 事故诊断和发布门禁形成闭环 |
| 95 分以上 |  | 能提出真实工程演进方案 |

不要怕指出短板。

能清楚说出“我现在差在哪里，下一步为什么先补这里”，本身就是工程判断力的一部分。
