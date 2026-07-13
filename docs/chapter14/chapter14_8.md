# 14.8 综合实践：输入事件分发观察实验

第 14 章最后一节，我们把 Input 系统、MotionEvent、Activity 入口、ViewGroup 分发、滑动冲突、Compose 手势和 Input ANR 放进一个观察实验。

目标是：让你能从一次点击或滑动，解释事件如何从系统进入 App，又如何在 View 树里找到处理者。

## 本节剧情钩子

现在你已经从窗口显示控制台走到了输入事件侦查室。

你不再只问：

```text
按钮有没有响应？
```

而是继续追问：

```text
DOWN 去了哪里？
谁拦截了 MOVE？
为什么子 View 收到 CANCEL？
为什么 Dialog 后面的页面被点到了？
主线程卡住时输入事件会怎样？
```

本节要做的，就是把这些问题串成一条输入事件路线。

## 本节定位

本节是第 14 章综合实践。

本节会使用配套工程：

```text
examples/14-input-event-dispatch-lab/
```

它围绕触摸日志、事件序列、父子 View 分发、Compose 手势和输入问题诊断做成一个可观察实验室。

## 学习目标

学完本节后，你应该能够：

- 观察一次触摸事件的 DOWN / MOVE / UP / CANCEL。
- 记录 Activity、父容器、子 View 的事件分发顺序。
- 判断父容器是否拦截了事件。
- 区分点击、滑动、取消和点击穿透。
- 写一份输入事件分发报告。

## 第一部分：实践工程入口

第 14 章 demo 已经把输入事件分发拆成这些可观察区域：

- `输入观察分数`：提示事件实验完成度。
- `事件序列轨迹`：显示 DOWN / MOVE / UP / CANCEL。
- `分发链路卡片`：展示 Activity、Parent、Child 的分发日志。
- `父容器拦截开关`：切换是否拦截 MOVE。
- `滑动冲突实验区`：模拟横向与纵向手势竞争。
- `Compose 手势实验区`：对比 clickable 和 pointerInput。
- `Input ANR 模拟按钮`：短暂阻塞主线程观察点击延迟。
- `输入问题诊断卡`：整理点击无响应、穿透、误触、滑动冲突和 ANR。

它的目标不是模拟完整 Input 系统，而是把输入事件变成可点击、可滑动、可复盘的实验。

## 第二部分：事件序列观察

先完成最基础观察：

```text
点击
  -> DOWN
      -> UP

滑动
  -> DOWN
      -> MOVE
          -> MOVE
              -> UP
```

你要回答：

- DOWN 是否一定最先出现？
- MOVE 会出现几次？
- UP 什么时候出现？
- 什么情况下会出现 CANCEL？

## 第三部分：分发链路观察

建议在这些位置记录日志：

```text
Activity.dispatchTouchEvent
Parent.dispatchTouchEvent
Parent.onInterceptTouchEvent
Parent.onTouchEvent
Child.dispatchTouchEvent
Child.onTouchEvent
```

观察：

- 点击子 View 时事件经过哪些层。
- 父容器拦截后子 View 是否还能收到事件。
- 子 View 不消费 DOWN 时后续事件是否继续给它。

这组实验能帮助你把“三个方法”从背诵变成证据。

## 第四部分：滑动冲突实验

设计一个外层可横滑、内层可纵滑的区域。

观察：

- 横向 MOVE 谁处理？
- 纵向 MOVE 谁处理？
- 父容器什么时候拦截？
- 子 View 什么时候请求父容器不要拦截？
- 事件是否出现 CANCEL？

滑动冲突不是玄学，它就是事件序列中的一次处理权协商。

## 第五部分：Compose 手势实验

demo 的 Compose 区域包含：

- `Modifier.clickable`。
- `Modifier.pointerInput`。
- 一个可拖动 Box。
- 一个可滚动列表。

观察：

- clickable 什么时候触发。
- pointerInput 如何读取事件。
- Compose 手势和传统 View 分发日志如何对应。
- ComposeView 嵌入传统 View 时边界在哪里。

这能帮助读者理解：Compose 改变了写法，但没有脱离 Android 输入系统。

## 第六部分：输入问题诊断卡

demo 已经把常见问题做成诊断卡：

```text
问题：按钮点了没反应
可能原因：事件没有进入 Activity、父容器拦截、子 View 没消费 DOWN、主线程阻塞
观察证据：Activity / Parent / Child 日志断点
修复方向：先找事件断点，再调整拦截、消费或主线程任务
```

再比如：

```text
问题：滑动时子 View 收到 CANCEL
可能原因：父容器中途拦截 MOVE
观察证据：Parent.onInterceptTouchEvent 返回 true
修复方向：明确手势方向，必要时 requestDisallowInterceptTouchEvent
```

## 第七部分：输入事件报告

建议报告格式：

```text
操作：
事件序列：
Activity 日志：
父容器日志：
子 View 日志：
是否发生拦截：
是否出现 CANCEL：
可能的 Framework 入口：
我的结论：
仍不确定：
```

推荐 AOSP 入口：

```text
frameworks/base/services/core/java/com/android/server/input/InputManagerService.java
frameworks/native/services/inputflinger/reader/InputReader.cpp
frameworks/native/services/inputflinger/dispatcher/InputDispatcher.cpp
frameworks/base/core/java/android/view/ViewRootImpl.java
frameworks/base/core/java/android/view/View.java
frameworks/base/core/java/android/view/ViewGroup.java
frameworks/base/core/java/android/app/Activity.java
```

## 第八部分：本章通关检查

完成第 14 章后，请确认自己能回答：

- 触摸事件如何从系统进入 App？
- InputReader 和 InputDispatcher 大致负责什么？
- MotionEvent 的 DOWN / MOVE / UP / CANCEL 各代表什么？
- Activity.dispatchTouchEvent 适合做什么？
- ViewGroup 的 dispatch / intercept / touch 如何配合？
- 为什么 DOWN 是否被消费很重要？
- 滑动冲突应该看哪些日志？
- Compose pointer input 和传统 View 事件有什么关系？
- Input ANR 和主线程阻塞有什么关系？

## 本节小挑战

### 输入侦探终局题

请为下面路径写一份输入事件分发报告：

```text
打开页面
  -> 点击子 View
      -> 在子 View 上滑动
          -> 父容器中途拦截
              -> 子 View 收到 CANCEL
                  -> 再次点击子 View
```

要求写出事件序列、拦截点和最终处理者。

## 本节实践任务

### 基础任务

- 打印 Activity、父容器、子 View 的触摸日志。
- 点击一次子 View。
- 滑动一次子 View。
- 写一份 10 行以内的输入事件报告。

### 进阶任务

- 增加一个拦截开关。
- 增加一个 Compose pointerInput 区域。
- 模拟一次主线程忙碌，观察点击延迟。
- 对照源码搜索 `InputDispatcher`、`ViewRootImpl`、`ViewGroup`。

## 本节小结

第 14 章把“用户点击”从业务回调推进到输入系统。你不需要一次读完 InputFlinger，但应该已经能把 InputReader、InputDispatcher、ViewRootImpl、Activity、ViewGroup、View、Compose 手势和 Input ANR 放在同一张地图上。下一步继续深入 View 绘制、SurfaceFlinger 和渲染链路，就能把“点一下到看见变化”的整条路径连起来。
