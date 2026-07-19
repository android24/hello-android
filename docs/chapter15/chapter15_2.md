# 15.2 从 invalidate 到 Choreographer：一帧如何被调度

上一节我们建立了第 15 章主线：画面变化不是立刻发生，而是被安排到一帧里统一处理。

这一节我们先看一帧的入口：

```text
谁告诉系统：我需要刷新？
谁决定什么时候刷新？
```

答案会遇到 `invalidate()`、`requestLayout()`、ViewRootImpl 和 Choreographer。

## 本节剧情钩子

你写了一个自定义 View。

点击按钮后，内部进度值从 40 变成 60。

但屏幕没变。

你突然意识到：改变数据不等于改变画面。你还要告诉系统：

```text
这块区域脏了，请下一帧帮我重画。
```

这句话，在 View 体系里通常就是 `invalidate()`。

## 本节定位

本节讲清楚 UI 刷新的调度入口，为后续 measure、layout、draw 和掉帧分析打基础。

## 学习目标

学完本节后，你应该能够：

- 区分 `invalidate()` 和 `requestLayout()`。
- 理解 ViewRootImpl 为什么要调度 traversal。
- 知道 Choreographer 如何把刷新安排到 VSYNC。
- 能解释“为什么 UI 改变不是马上画出来”。

## 第一部分：invalidate 是重画请求

`invalidate()` 的意思是：

```text
这个 View 的显示内容失效了，需要重新绘制。
```

常见场景：

- 自定义 View 的颜色变化。
- 进度条进度变化。
- 图形内容变化。
- 某个区域需要重新 draw。

它更关注“画的内容变了”。

简化理解：

```text
invalidate()
  -> 标记脏区域
      -> 请求下一帧绘制
```

## 第二部分：requestLayout 是布局请求

`requestLayout()` 的意思是：

```text
尺寸或位置可能变了，需要重新测量和布局。
```

常见场景：

- 文本变长，控件宽高可能变化。
- View 的 LayoutParams 改了。
- 列表 item 高度变了。
- 自定义容器子 View 数量变化。

它更关注“放在哪里、占多大”。

简化理解：

```text
requestLayout()
  -> 标记需要布局
      -> 下一帧执行 measure / layout
          -> 通常也会触发 draw
```

### invalidate 与 requestLayout 快速对照

| 对比项 | invalidate | requestLayout |
| --- | --- | --- |
| 主要含义 | 内容脏了，需要重画 | 尺寸或位置可能变了，需要重新测量布局 |
| 关注重点 | 画什么 | 多大、放哪里 |
| 常见触发 | 颜色、进度、图形内容变化 | 文案长度、宽高、LayoutParams、子 View 数量变化 |
| 典型阶段 | draw | measure / layout / draw |
| 成本倾向 | 通常比重新布局轻 | 可能影响父子 View 的布局协商，成本更高 |
| 常见误用 | 数据变了但忘记调用，导致画面不更新 | 只改颜色却频繁 requestLayout，增加布局成本 |

可以用一句话记住：

```text
内容变，先想 invalidate。
位置和尺寸变，再想 requestLayout。
```

但真实项目里也要看 View 的具体实现。比如 TextView 文案变化，看起来只是内容变化，但如果文案长度影响宽高，就可能需要重新测量和布局。

## 第三部分：ViewRootImpl 是刷新调度入口

View 自己不会直接把内容画到屏幕上。

当 View 请求刷新时，最终会向上走到 ViewRootImpl。

简化链路：

```text
View.invalidate / requestLayout
  -> 父 View 继续向上汇报
      -> ViewRootImpl.scheduleTraversals()
          -> Choreographer.postCallback()
```

ViewRootImpl 像 App 侧窗口和 View 树的总控台。

它知道：

- 当前窗口的根 View 是谁。
- 是否需要重新测量布局。
- 是否需要重新绘制。
- 什么时候执行下一次 View 树遍历。

## 第四部分：Choreographer 等待 VSYNC

Choreographer 不会随意挑一个时间刷新。

它会围绕 VSYNC 节拍安排一帧工作。

一帧里常见阶段可以简化为：

```text
VSYNC
  -> input
      -> animation
          -> traversal
              -> commit
```

可以这样理解：

- input：处理输入事件。
- animation：推进动画。
- traversal：执行 View 树测量、布局、绘制。
- commit：提交绘制结果。

这也是为什么输入、动画、绘制会互相影响。

如果 input 阶段太慢，后面的绘制就被挤压。

如果 traversal 太重，这一帧也可能错过显示时机。

## 第五部分：为什么主线程忙会掉帧

主线程不仅处理业务回调，也参与 UI 调度。

如果主线程在一帧到来时还在忙：

```text
VSYNC 到达
  -> 主线程还在执行耗时任务
      -> traversal 延迟
          -> 本帧错过
              -> 用户看到卡顿
```

这就是很多卡顿问题的基本形态。

它不一定马上导致 ANR，但会让动画、滚动和点击反馈变差。

## 第六部分：Compose 是否也需要帧调度

需要。

Compose 写法不同，但它仍然运行在 Android 的帧调度体系之上。

当 Compose 状态变化时，会经历：

```text
State 改变
  -> Recomposition
      -> Layout / Draw
          -> Android 帧调度
```

所以 Compose 不是绕过 Choreographer，而是在更高层用声明式方式组织 UI 更新。

## 本节小挑战

### 刷新请求分类题

请判断下面场景更像 `invalidate()` 还是 `requestLayout()`：

- 自定义 View 进度值从 40 变 60。
- TextView 文案从“OK”变成一整段说明。
- View 背景色变化。
- 图片宽高从 100dp 改成 200dp。
- Compose Text 内容变长后换行。

## 本节实践任务

### 基础任务

- 在一个自定义 View 中改变内部颜色。
- 对比调用和不调用 `invalidate()` 的表现。
- 写下你观察到的差异。

### 进阶任务

- 修改一个 View 的宽高。
- 对比 `invalidate()` 和 `requestLayout()`。
- 解释为什么有些变化只重画不够。

## 本节小结

`invalidate()` 关注内容重画，`requestLayout()` 关注尺寸和位置变化。它们最终都会把刷新请求汇报到 ViewRootImpl，由 Choreographer 按 VSYNC 节拍安排下一帧。理解这条调度链路，是分析卡顿、掉帧和 UI 不刷新的第一步。
