# 示例工程：Input 事件分发实验室

## 对应章节

第14章 Input 事件分发、触摸系统与交互响应机制

## 工程目标

本工程用于配合第 14 章，把 Input 系统、MotionEvent、Activity 入口、ViewGroup 分发拦截、Compose 手势和 Input ANR 放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕四个问题展开：

- 用户点到屏幕后，事件如何进入 Activity？
- 父容器为什么能拦截子 View 的 MOVE？
- `DOWN / MOVE / UP / CANCEL` 如何组成一次手势？
- Compose 的 `clickable`、`pointerInput` 和传统 View 事件有什么关系？

## 当前效果

运行后你会看到一个“第 14 章 Input 事件分发实验室”页面：

- `输入观察分数` 用 100 分制提示当前实验进度。
- `预期 vs 实际` 会在每次实验后显示你的判断和观察结果。
- `分发链路卡片` 展示 Activity、Parent、Child、Compose 四层最近一次事件证据。
- `原生 View 分发实验区` 嵌入一个父容器和子 View，用来观察 `dispatchTouchEvent`、`onInterceptTouchEvent`、`onTouchEvent`。
- `父容器拦截 MOVE` 开关可以模拟滑动冲突和 `CANCEL`。
- `滑动冲突实验区` 用横向 / 纵向拖动模拟外层容器和内层列表的处理权竞争。
- `Compose 手势实验区` 提供 pointerInput tap、drag、Button click 和可滚动列表。
- `模拟主线程忙碌` 用来观察输入响应延迟和 Input ANR 的影子。
- `输入问题诊断卡` 把点击无响应、滑动冲突、点击穿透、Input ANR 变成排查清单。
- `事件序列与分发轨迹` 记录 Activity、Parent、Child、Compose 的事件日志。

这个 demo 不模拟完整 InputFlinger，也不能直接读取系统内部 InputDispatcher 状态；它从 App 侧观察事件进入、分发、拦截、消费和取消，帮助你反推输入系统发生了什么。

## 探索玩法

建议把自己当成输入侦探，按三段路线完成。

你不是在“随便点一点页面”，而是在复原一次输入事件的现场：

```text
触摸屏产生线索
  -> 系统找到目标窗口
      -> Activity 接到事件
          -> Parent 判断是否拦截
              -> Child 决定是否消费
                  -> Compose 手势区给出另一种写法
```

每做完一段实验，都建议先写下自己的预期，再看 `预期 vs 实际` 和事件日志。读懂输入事件，最怕直接看结论；真正有效的是先猜一次，再让日志告诉你哪里猜错了。

### 初级侦探：找到事件入口

先完成：

```text
点击原生 Child View
  -> 观察 Activity.dispatchTouchEvent
      -> 观察 Parent.dispatchTouchEvent
          -> 观察 Child.dispatchTouchEvent / onTouchEvent
```

通关判断：

```text
你能说清楚点击不是直接送给 Button，而是从 Activity 和 View 树一路分发下来的。
```

### 中级侦探：观察父容器拦截

继续完成：

```text
关闭父容器拦截
  -> 在 Child 上滑动
      -> 打开父容器拦截
          -> 再次在 Child 上滑动
              -> 对比 MOVE / CANCEL
                  -> 在滑动冲突实验区横向 / 纵向拖动
```

通关判断：

```text
你能解释为什么父容器拦截 MOVE 后，子 View 的手势可能被取消，也能根据方向判断手势应该归谁处理。
```

### 高级侦探：追踪 Compose 手势和输入延迟

最后完成：

```text
点击 Compose pointerInput 区域
  -> 拖动 Compose drag 区域
      -> 点击 Compose Button
          -> 滚动 Compose 列表
              -> 模拟主线程忙碌
                  -> 写一份输入事件分发报告
```

通关判断：

```text
你能把 Compose 点击、拖动、滚动、传统 View 分发和主线程响应放进同一张输入链路地图里。
```

最小报告可以写成：

```text
操作：开启父容器 MOVE 拦截后，在 Child View 上滑动。
预期：DOWN 先到 Child，MOVE 时 Parent 可能拦截。
实际：Parent.onInterceptTouchEvent 在 MOVE 返回 true，Child 收到 CANCEL。
事件推断：父容器接管手势，子 View 的点击序列被打断。
源码入口：InputDispatcher, ViewRootImpl, ViewGroup, View
仍不确定：父容器拦截时系统如何保证后续 MOVE 不再送给子 View？
```

## 输入问题诊断表

遇到输入问题时，不要第一时间怀疑业务回调。先按下面这张表找证据。

| 现象 | 先看哪条日志 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 按钮点了没反应 | `Activity.dispatchTouchEvent` 是否出现 DOWN | 事件没有进入当前窗口、被上层窗口挡住、主线程忙碌 | 先确认窗口目标，再检查遮挡层和主线程任务 |
| Parent 有日志，Child 没日志 | `Parent.onInterceptTouchEvent` 和点击坐标 | 父容器提前拦截，或者触摸点没有命中 Child | 调整拦截条件，检查 View 边界和触摸区域 |
| Child 收到 CANCEL | MOVE 阶段 Parent 是否开始拦截 | 父容器中途接管手势 | 明确横向 / 纵向判断，必要时使用 `requestDisallowInterceptTouchEvent` |
| 点击穿透 | Activity / Window 层是否出现多组点击日志 | 弹层、遮罩或窗口属性配置不正确 | 回到第 13 章检查 Dialog、PopupWindow、Window flag 和遮罩点击区域 |
| 输入明显延迟 | 点击日志和响应日志之间的时间差 | 主线程被同步任务阻塞 | 把耗时任务移出主线程，使用协程、线程池或异步处理 |

推荐排查顺序：

```text
事件有没有进 Activity？
  -> 父容器有没有拦截？
      -> 子 View 有没有消费 DOWN？
          -> MOVE 过程中有没有 CANCEL？
              -> 主线程有没有及时处理？
```

## 运行方式

1. 使用 Android Studio 打开 `examples/14-input-event-dispatch-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `InputDispatchLab`。
5. 点击、滑动原生 View 和 Compose 手势区，观察事件轨迹和日志变化。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
14-input-event-dispatch-lab/
  app/
    src/main/java/com/helloandroid/input/
      InputLabApplication.kt
      MainActivity.kt
      TouchLoggingViews.kt
      InputLabState.kt
      InputLabStore.kt
      InputLabScreen.kt
  quality/
    input-dispatch-report-template.md
    input-reading-notes.md
```

## 关键源码入口

- `MainActivity.kt`：记录 Activity 层 `dispatchTouchEvent`，接入主线程忙碌实验。
- `TouchLoggingViews.kt`：原生父子 View 实验区，记录分发、拦截和消费。
- `InputLabState.kt`：定义输入实验状态、分数、诊断卡和事件日志。
- `InputLabStore.kt`：记录 MotionEvent、分发链路、滑动冲突、Compose 手势、拦截开关和 Logcat 日志。
- `InputLabScreen.kt`：展示输入观察页面、分发链路卡片、原生 View 实验区、滑动冲突实验区、Compose 手势区和事件轨迹。
- `quality/input-dispatch-report-template.md`：输入事件分发报告模板。
- `quality/input-reading-notes.md`：Input / ViewGroup 源码阅读建议。

## 推荐对照的 AOSP 入口

```text
frameworks/base/services/core/java/com/android/server/input/InputManagerService.java
frameworks/native/services/inputflinger/reader/InputReader.cpp
frameworks/native/services/inputflinger/dispatcher/InputDispatcher.cpp
frameworks/base/core/java/android/view/ViewRootImpl.java
frameworks/base/core/java/android/view/View.java
frameworks/base/core/java/android/view/ViewGroup.java
frameworks/base/core/java/android/app/Activity.java
```

建议带着问题看：

```text
InputDispatcher 如何选择目标窗口？
ViewRootImpl 如何接收输入事件？
Activity.dispatchTouchEvent 为什么适合做全局观察？
ViewGroup 什么时候调用 onInterceptTouchEvent？
为什么 DOWN 是否被消费会影响后续事件？
子 View 收到 CANCEL 通常意味着什么？
```

## 练习任务

### 基础任务

- 点击原生 Child View，记录事件分发顺序。
- 在 Child View 上滑动，观察 MOVE 事件。
- 开启父容器 MOVE 拦截，再次滑动，观察是否出现 CANCEL。
- 在滑动冲突实验区横向 / 纵向拖动，记录处理权归属。
- 点击 Compose pointerInput 区域，观察 Compose 手势日志。
- 滚动 Compose 可滚动列表，观察 scroll 如何进入分发链路卡片。
- 使用 `quality/input-dispatch-report-template.md` 写一份短报告。

### 进阶任务

- 增加一个“子 View 不消费 DOWN”的开关，观察后续事件变化。
- 把滑动冲突实验区改造成真实的外层横滑、内层纵向列表嵌套。
- 给 Compose drag 区域增加方向判断。
- 给 Compose 可滚动列表增加点击条目，比较 click 和 scroll 的优先级。
- 对照 AOSP 搜索 `InputDispatcher`、`ViewRootImpl`、`ViewGroup`。

## 通关目标

完成本工程后，你应该能说清楚：

- 触摸事件不是直接送给按钮。
- Activity、父容器、子 View 的分发顺序。
- `DOWN / MOVE / UP / CANCEL` 的含义。
- 父容器拦截 MOVE 后为什么可能出现 CANCEL。
- 滑动冲突里如何根据方向和距离判断处理权。
- Compose 点击、拖动、滚动和传统 View 事件的关系。
- Input ANR 与主线程阻塞的关系。
- 点击无响应、滑动冲突、点击穿透应该如何分类排查。
