# 15.8 综合实践：一帧渲染链路观察实验

第 15 章最后一节，我们把 View 绘制、Choreographer、RenderThread、Surface、BufferQueue、SurfaceFlinger 和渲染体验问题放进一个观察实验。

目标是：让你能从一次 UI 状态变化，解释它如何被安排到下一帧，又如何一步步变成屏幕上的画面。

## 本节剧情钩子

现在你已经从输入事件侦查室走到了渲染控制台。

你不再只问：

```text
点击有没有响应？
```

而是继续追问：

```text
状态变化后谁请求刷新？
这一帧什么时候开始？
有没有重新 measure / layout？
draw 花了多久？
主线程是否错过帧预算？
Buffer 是否顺利提交？
SurfaceFlinger 为什么能合成最终画面？
```

本节要做的，就是把这些问题串成一条一帧渲染路线。

你可以把自己当成一帧侦探。

第 14 章负责追踪“事件交给谁”，第 15 章负责追踪“画面如何变”。一次点击之后，真正的故事还没结束：业务状态改变只是开场，下一帧能否准时到达屏幕，才是用户体验的终点。

## 本节定位

本节是第 15 章综合实践。

本节会使用配套工程：

```text
examples/15-rendering-frame-lab/
```

这个工程围绕一帧调度、布局重算、重绘、滚动掉帧、主线程忙碌和渲染问题诊断做成一个可观察实验室。

## 学习目标

学完本节后，你应该能够：

- 观察一次 UI 更新是否触发 layout 或 draw。
- 用 Choreographer 记录帧回调。
- 模拟主线程忙碌并观察掉帧风险。
- 区分重布局、重绘、滚动和动画的成本。
- 写一份一帧渲染链路报告。

## 第一部分：实践工程规划

第 15 章 demo 已经拆成这些可观察区域：

- `渲染观察分数`：提示一帧实验完成度。
- `预期 vs 实际`：对照你对 layout / draw / frame 的判断。
- `一帧链路卡片`：展示刷新请求、View 树、帧调度、渲染提交和系统合成边界。
- `刷新请求实验区`：分别触发颜色变化、文本变长、尺寸变化。
- `Choreographer 帧节拍器`：记录 frameTime 和帧间隔。
- `滚动与动画实验区`：观察滚动、位移、透明度和尺寸动画的成本。
- `主线程忙碌按钮`：模拟一帧错过预算。
- `渲染问题诊断卡`：整理白屏、闪烁、过度绘制、黑屏和掉帧。
- `一帧事件轨迹`：记录刷新请求、帧回调、布局、绘制和诊断结论。

它的目标不是直接模拟完整 SurfaceFlinger，而是把 App 侧可观察证据和系统渲染链路连接起来。

## 第二部分：一帧侦探通关路线

建议按三段路线完成：

```text
初级侦探：触发刷新请求
  -> 改变颜色
      -> 改变文本长度
          -> 改变 View 尺寸
              -> 区分 invalidate 与 requestLayout

中级侦探：追踪帧节拍
  -> 记录 Choreographer frame
      -> 模拟主线程忙碌
          -> 对比帧间隔
              -> 判断是否有掉帧风险

高级侦探：解释屏幕合成
  -> 观察滚动和动画
      -> 写出 ViewRootImpl / RenderThread / Surface / SurfaceFlinger 路线
          -> 完成一帧渲染报告
```

读者不是“点按钮看效果”，而是在复原一次画面变化的现场。

## 第三部分：如何迁移到自己的页面

跑完配套工程后，也可以把同样的观察方法迁移到任意一个现有 Android 页面。

准备一个页面，最好包含：

- 一个会改变颜色的区域。
- 一个会改变文案长度的 Text。
- 一个可以滚动的列表。
- 一个简单动画。
- 一个能模拟主线程忙碌的按钮。

然后按下面路线观察：

```text
改变颜色
  -> 判断是否只需要重绘
      -> 改变文案长度
          -> 判断是否触发布局
              -> 快速滚动列表
                  -> 观察是否掉帧
                      -> 模拟主线程忙碌
                          -> 写一份一帧报告
```

如果暂时没有完整性能工具，也可以先做“纸面观察”：

| 操作 | 你的预期 | 可能触发的阶段 | 需要收集的证据 |
| --- | --- | --- | --- |
| 改变颜色 | 只重画 | invalidate / draw | 自定义 View 日志或肉眼观察 |
| 改变长文本 | 可能重布局 | requestLayout / measure / layout / draw | 控件尺寸是否变化 |
| 改变宽高 | 大概率重布局 | measure / layout / draw | 父子 View 是否重新摆放 |
| 滚动列表 | 连续多帧刷新 | input / animation / traversal / draw | 是否出现卡顿 |
| 模拟主线程忙碌 | 帧间隔变大 | 主线程阻塞 / traversal 延迟 | 点击反馈和帧日志 |

这样做的意义是：即使没有专门 demo，你也能先建立“操作 -> 预期 -> 证据 -> 结论”的思维方式。

## 第四部分：刷新请求实验

设计三个按钮：

```text
改变颜色
改变文本长度
改变尺寸
```

观察：

- 改变颜色更像只需要重绘吗？
- 改变文本长度是否可能触发布局？
- 改变尺寸是否一定需要 requestLayout？
- 哪些操作会影响更多 View？

这组实验帮助读者把 `invalidate()` 和 `requestLayout()` 从概念变成证据。

## 第五部分：Choreographer 帧观察

记录帧回调：

```text
frameTimeNanos
上一帧时间
帧间隔
是否超过预算
```

观察：

- 正常情况下帧间隔是否稳定。
- 主线程忙碌后帧间隔是否变大。
- 滚动或动画时是否更容易出现慢帧。

这一部分要让读者看到：卡顿不是感觉，它可以被记录。

## 第六部分：滚动与动画实验

设计两类动画：

- 改变 `translationX / alpha / scale`。
- 改变宽高或触发布局。

再设计一个列表滚动区域。

观察：

- transform 动画是否更稳定。
- 尺寸变化是否更容易触发布局。
- 列表 item 复杂度是否影响滚动。
- 图片、阴影、圆角、透明叠加是否增加绘制压力。

这部分连接第 9 章性能优化和第 15 章渲染链路。

## 第七部分：SurfaceFlinger 观察边界

普通 App demo 很难直接完整模拟 SurfaceFlinger 内部。

但可以让读者建立边界意识：

```text
App 能观察：
  -> 刷新请求
  -> 主线程任务
  -> Choreographer frame
  -> layout / draw 现象
  -> 滚动和动画表现

系统侧继续追：
  -> Surface
  -> BufferQueue
  -> SurfaceFlinger
  -> Display
```

这能避免一个误区：

```text
看不到 SurfaceFlinger，不代表它不在链路里。
```

## 第八部分：渲染问题诊断卡

demo 可以把常见问题做成诊断卡：

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 首帧白屏 | 启动耗时、首帧时间 | 初始化重、首屏布局复杂 | 延迟任务，优化首屏 |
| 点击后画面慢半拍 | 主线程任务和帧间隔 | 事件后同步任务过重 | 拆分任务，避免阻塞帧 |
| 列表滚动掉帧 | 帧间隔、item 复杂度 | 图片、布局、重组过重 | 简化 item，优化图片和状态 |
| 动画卡顿 | 动画属性和帧数据 | 触发频繁 layout | 优先使用 transform |
| 黑屏 | Surface / Buffer 线索 | Surface 未准备或 Buffer 未提交 | 检查 Surface 生命周期 |
| 自定义 View 不刷新 | invalidate / requestLayout | 数据变了但未请求刷新 | 根据变化类型触发刷新 |

## 第九部分：一帧渲染报告

建议报告格式：

```text
操作：
刷新请求：
是否触发布局：
是否触发绘制：
帧回调记录：
主线程是否忙碌：
可能的渲染链路：
可能的 SurfaceFlinger 入口：
我的结论：
仍不确定：
```

推荐 AOSP 入口：

```text
frameworks/base/core/java/android/view/View.java
frameworks/base/core/java/android/view/ViewGroup.java
frameworks/base/core/java/android/view/ViewRootImpl.java
frameworks/base/core/java/android/view/Choreographer.java
frameworks/base/graphics/java/android/graphics/HardwareRenderer.java
frameworks/base/libs/hwui/
frameworks/native/libs/gui/BufferQueue.cpp
frameworks/native/services/surfaceflinger/
```

## 第十部分：本章通关检查

完成第 15 章后，请确认自己能回答：

- UI 状态变化后为什么不是立刻绘制到屏幕？
- `invalidate()` 和 `requestLayout()` 有什么区别？
- measure、layout、draw 分别解决什么问题？
- Choreographer 和 VSYNC 有什么关系？
- HardwareRenderer、RenderThread 大致负责什么？
- Surface、BufferQueue、SurfaceFlinger 如何连接 App 和屏幕？
- 掉帧和 Jank 应该如何收集证据？
- 白屏、闪烁、黑屏、过度绘制应该如何分类排查？

## 本节小挑战

### 一帧侦探终局题

请为下面路径写一份一帧渲染报告：

```text
打开页面
  -> 点击按钮
      -> 文本变长
          -> 列表滚动
              -> 主线程忙碌 120ms
                  -> 动画卡顿
                      -> 画面恢复
```

要求写出刷新请求、可能触发的阶段、慢帧位置和优化方向。

## 本节实践任务

### 基础任务

- 设计颜色变化、文本变化、尺寸变化三个操作。
- 记录它们更像 invalidate 还是 requestLayout。
- 写一份 10 行以内的一帧报告。

### 进阶任务

- 增加 Choreographer 帧记录。
- 增加主线程忙碌按钮。
- 增加一个滚动列表和一个动画区域。
- 对照源码搜索 `ViewRootImpl`、`Choreographer`、`HardwareRenderer`、`SurfaceFlinger`。

## 本节小结

第 15 章把“画面变化”从业务状态推进到渲染系统。你不需要一次读完 SurfaceFlinger，但应该已经能把 ViewRootImpl、Choreographer、measure / layout / draw、HardwareRenderer、RenderThread、Surface、BufferQueue 和 SurfaceFlinger 放在同一张地图上。到这里，Framework 入门链路已经从启动、窗口、输入继续延伸到了显示结果本身。
