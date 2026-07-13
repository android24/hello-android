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

你可以把自己当成输入侦探。

第 13 章解决的是“舞台为什么能出现”，第 14 章追问的是“观众按下按钮之后，信号到底交给了谁”。屏幕上的一次点击，看起来只有一瞬间；在系统内部，它却像一张层层传递的案卷：InputReader 记录线索，InputDispatcher 寻找窗口，ViewRootImpl 送进现场，Activity、父容器和子 View 逐层确认谁来处理。

这一节的任务，就是拿着日志把这张案卷复原出来。

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
- `预期 vs 实际`：把你的判断和实验结果放在一起对照。
- `分发链路卡片`：展示 Activity、Parent、Child、Compose 四层最近一次事件证据。
- `原生 View 分发实验区`：嵌入父容器和子 View，展示 Activity、Parent、Child 的分发日志。
- `父容器拦截 MOVE`：切换父容器是否在 MOVE 阶段接管事件，用来观察 CANCEL 和滑动冲突的影子。
- `滑动冲突实验区`：通过横向 / 纵向拖动模拟外层容器和内层列表的处理权竞争。
- `Compose 手势实验区`：对比 pointerInput tap、drag 和 Compose Button click。
- `Compose 可滚动列表`：观察 scroll 如何成为 Compose 手势证据。
- `模拟主线程忙碌`：短暂阻塞主线程，观察输入响应延迟和 Input ANR 的影子。
- `输入问题诊断卡`：整理点击无响应、穿透、误触、滑动冲突和 ANR。

它的目标不是模拟完整 Input 系统，而是把输入事件变成可点击、可滑动、可复盘的实验。

建议按三段路线完成：

```text
初级侦探：点击 Child View
  -> 找到 Activity / Parent / Child 的分发顺序

中级侦探：打开父容器拦截 MOVE
  -> 对比拦截前后的 MOVE / CANCEL

高级侦探：操作 Compose 手势区并模拟主线程忙碌
  -> 把传统 View、Compose 手势、列表滚动和输入延迟放进同一份报告
```

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

## 第四部分：分发链路卡片

分发链路卡片把日志整理成四个观察点：

```text
Activity
  -> Parent
      -> Child
          -> Compose
```

每张卡片都记录：

- 这一层的角色。
- 最近一次动作。
- 这条结论来自哪条日志。

它的价值在于：读者不用在完整日志里迷路，可以先看“链路摘要”，再回到事件序列里找细节。

## 第五部分：滑动冲突实验

当前 demo 用两种方式观察滑动冲突：

- `父容器拦截 MOVE` 开关：观察父容器中途接管后，子 View 是否收到 CANCEL。
- `滑动冲突实验区`：横向或纵向拖动，观察处理权应该交给外层容器还是内层列表。

观察：

- 关闭拦截时，MOVE 主要经过哪些节点？
- 打开拦截后，Parent 是否在 MOVE 阶段接管？
- 父容器什么时候拦截？
- 事件是否出现 CANCEL？
- Child 的点击序列是否被打断？
- 横向 MOVE 和纵向 MOVE 的数量是否不同？
- 当前手势更应该交给外层容器还是内层列表？

滑动冲突不是玄学，它就是事件序列中的一次处理权协商。

这个实验不是为了替代真实业务里的嵌套滑动，而是让你先看到一个最关键的判断：当 MOVE 出现时，父容器和子 View 必须根据方向、距离和业务语义决定谁接管。

## 第六部分：Compose 手势实验

demo 的 Compose 区域包含：

- pointerInput tap 区域。
- drag 手势区域。
- Compose Button click。
- Compose 可滚动列表。

观察：

- pointerInput tap 什么时候触发。
- pointerInput 如何读取事件。
- drag 日志如何描述移动距离。
- Compose Button click 与传统 View 日志有什么差异。
- LazyColumn 滚动时，分发链路卡片如何记录 scroll。
- Compose 手势和传统 View 分发日志如何对应。

这能帮助读者理解：Compose 改变了写法，但没有脱离 Android 输入系统。

## 第七部分：输入问题诊断卡

demo 已经把常见问题做成诊断卡。阅读时不要只看结论，更要看“第一条应该去找哪条日志”。

| 现象 | 先看哪条日志 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 按钮点了没反应 | `Activity.dispatchTouchEvent` 是否出现 DOWN | 事件没有进入当前窗口、被上层窗口挡住、主线程忙碌 | 先确认窗口目标，再看主线程和遮挡层 |
| Parent 有日志，Child 没日志 | `Parent.onInterceptTouchEvent` | 父容器提前拦截，或者触摸区域没有命中子 View | 检查拦截条件、点击坐标和子 View 边界 |
| Child 收到 CANCEL | `Parent.onInterceptTouchEvent` 在 MOVE 阶段是否返回 true | 父容器中途接管手势 | 明确方向判断，必要时使用 `requestDisallowInterceptTouchEvent` |
| 点击穿透 | Activity / Window 层日志是否同时出现 | 弹层窗口属性、遮罩或可点击区域配置不正确 | 结合第 13 章窗口层级检查 Dialog、PopupWindow 或遮罩配置 |
| 输入明显延迟 | 点击前后日志时间间隔 | 主线程被同步任务阻塞 | 移出耗时任务，使用协程、线程池或异步队列 |

可以把它当成输入问题的第一张地图：

```text
先确认事件有没有进 Activity
  -> 再确认父容器有没有拦截
      -> 再确认子 View 有没有消费 DOWN
          -> 最后确认主线程是否及时处理
```

## 第八部分：输入事件报告

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

## 第九部分：本章通关检查

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
