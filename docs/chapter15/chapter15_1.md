# 15.1 为什么要学习 View 绘制与渲染链路

第 14 章我们追踪了一次触摸事件：它如何从系统进入 App，如何经过 Activity、ViewGroup、View 和 Compose 手势，最后找到处理者。

第 15 章继续追问：

```text
事件被处理之后，屏幕为什么真的变了？
```

按钮状态改变、列表滚动、动画前进、页面首帧出现，这些都不只是“调用了 setState”或者“执行了 onDraw”。它们背后有一整条渲染链路：View 树刷新、Choreographer 调度、RenderThread 执行、Surface 接收 Buffer、SurfaceFlinger 合成，最后才变成用户看到的一帧。

## 本章通关画面

完成第 15 章后，你应该能把一次画面变化讲成这样：

```text
业务状态变化
  -> requestLayout / invalidate / Compose recomposition
      -> Choreographer 等待 VSYNC
          -> ViewRootImpl 执行 traversal
              -> measure / layout / draw
                  -> HardwareRenderer 生成渲染命令
                      -> RenderThread 与 GPU 协作
                          -> Buffer 进入 BufferQueue
                              -> SurfaceFlinger 合成到屏幕
```

这条链路会把前面几章连接起来：

- 第 13 章解释窗口如何出现。
- 第 14 章解释输入如何进入。
- 第 15 章解释画面如何刷新。

## 本章剧情线

如果第 13 章是“舞台搭建”，第 14 章是“观众按下按钮”，那第 15 章就是灯光、布景和大屏幕同步换场。

你点击一个按钮，业务代码只改了一个状态。

但系统要回答更多问题：

- 哪些 View 需要重新测量？
- 哪些区域只需要重画？
- 这一帧什么时候开始？
- 主线程来不来得及？
- 渲染命令交给谁？
- Buffer 怎么送到 SurfaceFlinger？
- 为什么偶尔会掉帧、闪烁、白屏或黑屏？

本章要做的，就是把这些问题串成一条“屏幕更新路线”。

## 本章探索任务

```text
理解一帧为什么被调度
  -> 区分 requestLayout 与 invalidate
      -> 看懂 measure / layout / draw
          -> 认识 DisplayList、HardwareRenderer 与 RenderThread
              -> 理解 Surface、BufferQueue 与 SurfaceFlinger
                  -> 排查掉帧、闪烁、过度绘制和首帧问题
                      -> 完成一帧渲染链路观察实验
```

## 本节定位

本节是第 15 章入口。

我们先回答：

- 为什么 UI 刷新不是“立刻画”？
- View 绘制和 SurfaceFlinger 是什么关系？
- 为什么卡顿、掉帧、闪烁、白屏不能只看业务代码？
- 第 15 章 demo 应该如何观察一帧渲染？

## 学习目标

学完本节后，你应该能够：

- 理解渲染链路是 Android Framework 的核心体验链路。
- 知道一帧画面要经过 App、RenderThread、BufferQueue 和 SurfaceFlinger。
- 初步区分 View 绘制、硬件加速、屏幕合成和性能问题。
- 知道第 15 章要解决哪些真实显示体验问题。

## 第一部分：画面变化不是一次函数调用

在业务代码里，我们经常写：

```kotlin
count++
```

或者：

```kotlin
state = state.copy(loading = false)
```

看起来只是状态变化。

但屏幕真正更新，需要很多步骤：

```text
状态变化
  -> UI 失效
      -> 等待下一帧
          -> 重新计算布局或绘制内容
              -> 提交渲染结果
                  -> 系统合成显示
```

所以，显示问题往往不是“一个按钮没刷新”那么简单。

## 第二部分：为什么要等下一帧

Android 不希望每次状态改变都马上画一次。

如果一段代码连续改了 10 次 UI，系统更希望把这些变化合并到下一帧里统一处理。

这就是 Choreographer 的价值。

它像一位节拍老师：

```text
先别急着画。
等屏幕刷新节拍来了，
大家按顺序处理 input、animation、traversal、draw。
```

这能减少无意义的重复绘制，也让动画和滚动更稳定。

## 第三部分：View 绘制只是渲染链路的一段

很多人学到 `onDraw()` 就以为“绘制结束了”。

但 `onDraw()` 更像 App 侧的绘制描述。

后面还有：

- 渲染命令记录。
- RenderThread 执行。
- GPU 绘制。
- Buffer 交换。
- SurfaceFlinger 合成。
- 屏幕显示。

所以性能分析不能只停留在 View 层。

## 第四部分：本章要解决的问题

第 15 章重点解决：

- `requestLayout()` 和 `invalidate()` 有什么区别。
- 一帧刷新为什么由 Choreographer 调度。
- `measure / layout / draw` 分别做什么。
- 硬件加速和 DisplayList 大致解决什么问题。
- RenderThread 和主线程是什么关系。
- Surface、BufferQueue、SurfaceFlinger 如何连接 App 和屏幕。
- 掉帧、过度绘制、白屏、闪烁、黑屏应该如何排查。

## 本节小挑战

### 一帧侦探开场题

请判断下面现象更可能先从哪里查：

- 点击按钮后文字变了，但动画卡了一下。
- 页面第一次打开白屏很久。
- 列表滚动时偶尔掉帧。
- Dialog 关闭后底层画面闪一下。
- 自定义 View 更新数据后没有重画。

不要急着给结论，先写下你认为应该看的第一条线索：主线程、View 绘制、Buffer、窗口，还是系统合成。

## 本节实践任务

### 基础任务

- 找一个会更新 UI 的页面。
- 记录一次点击前后的画面变化。
- 思考它是否需要重新布局，还是只需要重画。

### 进阶任务

- 打开 Android Studio Profiler 或 Layout Inspector。
- 观察一次 UI 更新前后界面是否重新布局。
- 写下你理解的一帧刷新路线。

## 本节小结

第 15 章的核心不是背诵 `onDraw()`，而是理解：一次画面变化要经过状态更新、帧调度、View 树刷新、渲染命令、Buffer 提交和系统合成。只有把这条链路连起来，掉帧、白屏、闪烁、黑屏和首帧慢才会有清晰的排查入口。
