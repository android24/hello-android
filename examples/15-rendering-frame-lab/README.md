# 示例工程：一帧渲染链路实验室

## 对应章节

第15章 View 绘制、RenderThread、SurfaceFlinger 与渲染链路

## 工程目标

本工程用于配合第 15 章，把 `invalidate`、`requestLayout`、measure / layout / draw、Choreographer、滚动、动画、主线程忙碌和 SurfaceFlinger 边界放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕四个问题展开：

- UI 状态变化之后，为什么不是立刻画到屏幕？
- `invalidate()` 和 `requestLayout()` 的差异如何观察？
- Choreographer 帧间隔如何帮助判断掉帧？
- App 侧证据如何继续连接到 RenderThread、Surface 和 SurfaceFlinger？

## 当前效果

运行后你会看到一个“第 15 章 一帧渲染链路实验室”页面：

- `渲染观察分数` 用 100 分制提示当前实验进度。
- `预期 vs 实际` 会在每次实验后展示你的判断和观察结果。
- `一帧链路卡片` 展示刷新请求、View 树、帧调度、渲染提交、系统合成边界。
- `刷新请求实验区` 内嵌一个原生 `RenderProbeView`，用于观察 `invalidate`、`requestLayout`、`onMeasure`、`onLayout`、`onDraw`。
- `Choreographer 帧节拍器` 可以采样 18 帧，并记录最近帧、最大帧和慢帧数量。
- `模拟主线程忙碌` 用来观察主线程阻塞如何影响帧预算。
- `滚动与动画实验区` 对比 transform 动画、尺寸变化和列表滚动。
- `渲染问题诊断卡` 把 UI 不刷新、文本变长卡顿、滚动掉帧、动画卡顿、黑屏闪烁变成排查清单。
- `一帧事件轨迹` 记录刷新请求、View 回调、帧采样、滚动和动画日志。

这个 demo 不直接模拟完整 SurfaceFlinger，也不读取系统内部 BufferQueue 状态；它从 App 侧观察能拿到的证据，帮助你把这些证据连接到系统渲染链路。

## 探索玩法

建议把自己当成一帧侦探，按三段路线完成。

你不是在“随便点按钮”，而是在复原一次画面变化的现场：

```text
状态变化
  -> 刷新请求
      -> View 树遍历
          -> Choreographer 帧节拍
              -> RenderThread / GPU 边界
                  -> SurfaceFlinger 合成推断
```

每做完一段实验，都建议先写下自己的预期，再看 `预期 vs 实际` 和事件日志。读懂渲染链路，最怕直接背结论；真正有效的是先猜一次，再让证据修正你的判断。

### 初级侦探：区分刷新请求

先完成：

```text
点击改变颜色
  -> 观察 invalidate
      -> 点击切换文案
          -> 观察 requestLayout
              -> 点击切换 View 尺寸
                  -> 对比 onMeasure / onLayout / onDraw
```

通关判断：

```text
你能解释内容变化和尺寸变化为什么触发不同的刷新路径。
```

### 中级侦探：追踪帧节拍

继续完成：

```text
启动 18 帧采样
  -> 观察最近帧和最大帧
      -> 模拟主线程忙碌
          -> 再次采样
              -> 对比慢帧数量
```

通关判断：

```text
你能把“卡了一下”翻译成帧间隔、慢帧和主线程阻塞证据。
```

### 高级侦探：解释滚动、动画和合成边界

最后完成：

```text
执行 transform 动画
  -> 执行尺寸变化
      -> 快速滚动列表
          -> 阅读渲染问题诊断卡
              -> 写一份一帧渲染报告
```

通关判断：

```text
你能把 App 侧刷新证据继续连接到 RenderThread、GPU、Surface 和 SurfaceFlinger。
```

最小报告可以写成：

```text
操作：切换长文本并启动帧采样。
预期：文本变长可能触发 requestLayout，随后发生 measure / layout / draw。
实际：RenderProbeView 记录 onMeasure / onLayout / onDraw，FrameClock 记录最大帧间隔。
事件推断：这次变化影响了尺寸，因此不是单纯 invalidate。
源码入口：ViewRootImpl, Choreographer, View, HardwareRenderer
仍不确定：RenderThread 和 GPU 在这次帧里具体耗时多少？
```

## 渲染问题诊断表

遇到渲染问题时，不要第一时间只怀疑业务状态。先按下面这张表找证据。

| 现象 | 先看哪条证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| UI 不刷新 | `invalidate` 与 `onDraw` 是否出现 | 数据变了但没有请求重画 | 内容变化调用 `invalidate()` |
| 文案变长后卡顿 | `onMeasure` / `onLayout` 是否频繁出现 | 文本变化影响布局范围 | 控制布局影响范围，减少不必要 requestLayout |
| 列表滚动掉帧 | 帧间隔、item 复杂度 | 滚动中执行重任务或 item 过重 | 简化 item，异步加载，减少状态抖动 |
| 动画卡顿 | 动画属性和帧间隔 | 每帧触发布局或绘制太重 | 优先使用 translation、alpha、scale |
| 黑屏或闪烁 | 首帧、Surface、Buffer 边界 | Surface 未准备或窗口切换中间态 | 稳定首帧，检查 Surface 生命周期 |

推荐排查顺序：

```text
状态有没有变化？
  -> 有没有触发 invalidate / requestLayout？
      -> View 树有没有执行 measure / layout / draw？
          -> 帧间隔有没有变大？
              -> 是否需要继续看 RenderThread / GPU / SurfaceFlinger？
```

## 运行方式

1. 使用 Android Studio 打开 `examples/15-rendering-frame-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `RenderingFrameLab`。
5. 点击颜色、文案、尺寸、帧采样、滚动和动画实验区，观察事件轨迹。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
15-rendering-frame-lab/
  app/
    src/main/java/com/helloandroid/rendering/
      RenderingLabApplication.kt
      MainActivity.kt
      RenderProbeView.kt
      RenderingLabState.kt
      RenderingLabStore.kt
      RenderingLabScreen.kt
  quality/
    rendering-frame-report-template.md
    rendering-reading-notes.md
```

## 关键源码入口

- `MainActivity.kt`：接入 Compose 页面和主线程忙碌实验。
- `RenderProbeView.kt`：原生 View 探针，记录 `invalidate`、`requestLayout`、`onMeasure`、`onLayout`、`onDraw`。
- `RenderingLabState.kt`：定义渲染实验状态、分数、链路卡片、诊断卡和事件日志。
- `RenderingLabStore.kt`：记录刷新请求、View 回调、帧采样、滚动、动画和 Logcat 日志。
- `RenderingLabScreen.kt`：展示渲染观察页面、刷新请求实验区、帧节拍器、滚动动画区和事件轨迹。
- `quality/rendering-frame-report-template.md`：一帧渲染报告模板。
- `quality/rendering-reading-notes.md`：View / Choreographer / SurfaceFlinger 源码阅读建议。

## 推荐对照的 AOSP 入口

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

建议带着问题看：

```text
ViewRootImpl 什么时候 scheduleTraversals？
invalidate 和 requestLayout 如何向上汇报？
Choreographer 如何接收 VSYNC 并安排 traversal？
HardwareRenderer 如何连接 View 树和硬件渲染？
BufferQueue 为什么要连接生产者和消费者？
SurfaceFlinger 为什么负责最终合成？
```

## 练习任务

### 基础任务

- 点击 `改变颜色`，记录是否出现 `invalidate` 和 `onDraw`。
- 点击 `切换文案`，观察是否出现 `requestLayout`、`onMeasure`、`onLayout`。
- 点击 `切换 View 尺寸`，对比尺寸变化前后的日志。
- 启动 `采样 18 帧`，记录最大帧间隔。
- 快速滚动列表，观察 scroll 如何进入一帧链路卡片。
- 使用 `quality/rendering-frame-report-template.md` 写一份短报告。

### 进阶任务

- 给 `RenderProbeView` 增加一次复杂绘制，观察 draw 日志和帧间隔变化。
- 增加一个会频繁改变宽高的动画，对比 transform 动画。
- 给列表 item 增加图片或更多层级，观察滚动表现。
- 对照 AOSP 搜索 `ViewRootImpl`、`Choreographer`、`HardwareRenderer`、`SurfaceFlinger`。

## 通关目标

完成本工程后，你应该能说清楚：

- UI 状态变化不是立刻画到屏幕。
- `invalidate()` 和 `requestLayout()` 的区别。
- measure、layout、draw 如何协作。
- Choreographer 帧间隔如何帮助判断掉帧。
- 主线程忙碌为什么会影响下一帧。
- transform 动画为什么通常比尺寸动画更稳定。
- App 侧证据如何连接到 RenderThread、GPU、Surface 和 SurfaceFlinger。
- 白屏、闪烁、黑屏、掉帧和 UI 不刷新应该如何分类排查。
