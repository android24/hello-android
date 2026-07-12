# 13.5 Measure、Layout、Draw 与 Choreographer

窗口被系统接受之后，页面还需要真正完成一次绘制。

这一节我们从窗口管理切到 View 树刷新：

```text
为什么 UI 改变不是立刻随手画一笔，而是等待下一帧统一处理？
```

答案会遇到：measure、layout、draw 和 Choreographer。

## 本节剧情钩子

你点击按钮，状态变化，文本变了。

你感觉像是“马上刷新”。

但 Android 更像一支按节拍演奏的乐队：谁要改 UI，先把变化报上来，等下一次屏幕刷新节拍到达，再统一测量、布局、绘制。

Choreographer 就是这个节拍器。

## 本节定位

本节建立窗口显示后的刷新主线，为后续 View 绘制和性能优化打基础。

## 学习目标

学完本节后，你应该能够：

- 理解 measure / layout / draw 的基本职责。
- 知道 Choreographer 和一帧刷新有关。
- 明白 `requestLayout()` 与 `invalidate()` 的区别。
- 能把卡顿、掉帧和主线程耗时联系起来。

## 第一部分：一次绘制不是一步完成

View 树刷新通常会经历：

```text
measure -> layout -> draw
```

可以这样理解：

- measure：你想要多大？
- layout：你应该放在哪里？
- draw：把你画出来。

父 View 和子 View 会在这几个阶段中协作。

## 第二部分：requestLayout 和 invalidate

当 UI 发生变化时，经常有两类请求：

```text
requestLayout()
invalidate()
```

简化理解：

- `requestLayout()`：尺寸或位置可能变了，需要重新测量和布局。
- `invalidate()`：内容需要重画，但尺寸位置未必改变。

例如：

- 文本变长，可能需要 `requestLayout()`。
- 进度条颜色变化，可能只需要 `invalidate()`。

实际行为会受 View 实现影响，但这条区分很有用。

## 第三部分：Choreographer 是刷新节拍器

Android 不希望每一次 UI 改动都立刻同步画到屏幕上。

它会尽量把刷新安排到下一帧。

Choreographer 负责接收 VSYNC 节拍，并调度一帧里的工作：

```text
VSYNC
  -> input
      -> animation
          -> traversal
              -> measure / layout / draw
```

这里的 traversal，就是 View 树遍历刷新。

## 第四部分：ViewRootImpl 站在哪里

ViewRootImpl 会和 Choreographer 协作，安排 View 树遍历。

简化链路：

```text
View.requestLayout / invalidate
  -> ViewRootImpl 请求下一帧
      -> Choreographer 收到刷新节拍
          -> ViewRootImpl.performTraversals()
              -> measure / layout / draw
```

所以 ViewRootImpl 不只是“窗口桥梁”，它也深度参与 UI 刷新。

## 第五部分：为什么主线程不能卡

UI 刷新在主线程上调度。

如果主线程正在做耗时任务，下一帧就无法及时完成。

表现可能是：

- 点击后没反应。
- 动画掉帧。
- 列表滚动卡顿。
- 页面首帧迟迟不出来。
- 严重时触发 ANR。

这也把第 9 章性能优化和第 13 章 Framework 显示链路连接了起来。

## 本节小挑战

### 改变分类题

请判断下面变化更像 `requestLayout()` 还是 `invalidate()`：

- Text 文案从 2 个字变成 40 个字。
- View 背景颜色变化。
- ImageView 宽高变化。
- 自定义 View 内部进度值变化。
- RecyclerView item 高度变化。

## 本节实践任务

### 基础任务

- 找一个 Compose 或 View 页面。
- 修改一个状态，让文本变化。
- 观察页面是否重新布局或只是内容变化。

### 进阶任务

- 打开 Profile 或布局检查工具。
- 观察一次点击后的 UI 更新。
- 写下你理解的一帧刷新路径。

## 本节小结

窗口被添加之后，页面显示还需要 View 树刷新。measure 决定大小，layout 决定位置，draw 负责绘制，Choreographer 提供刷新节拍，ViewRootImpl 负责把 View 树遍历安排到正确时机。理解这条链路，卡顿、首帧、重绘和布局性能问题才有了共同解释。
