# 14.1 为什么要学习 Input 事件分发

第 13 章里，我们已经知道：Activity 的内容不是直接贴到屏幕上，而是通过 Window、DecorView、ViewRootImpl、WMS 和 Choreographer 进入显示链路。

现在页面已经显示出来了，新的问题出现了：

```text
用户手指点到屏幕之后，系统如何知道该让哪个 View 响应？
```

这就是第 14 章要追的主线。

## 本章通关画面

完成第 14 章后，你应该能把一次点击讲成这样：

```text
用户触摸屏幕
  -> Kernel 产生输入事件
      -> InputReader 读取并加工事件
          -> InputDispatcher 找到目标窗口
              -> 事件送到 App 进程
                  -> ViewRootImpl 接收事件
                      -> DecorView / Activity 分发事件
                          -> ViewGroup 判断是否拦截
                              -> View 处理点击、滑动或手势
```

第 14 章不要求你一次读完 Input 系统源码，但要建立一条判断线：

```text
触摸不是 View 自己凭空收到的，而是系统输入链路、窗口焦点和 View 树分发共同决定的结果。
```

## 本章剧情线

如果第 13 章是“舞台管理处”，第 14 章就是观众伸手按下舞台上的按钮。

屏幕先感知到触摸，系统要判断：这次触摸落在哪个显示区域，属于哪个窗口，窗口是否有焦点，事件要送给哪个 App，App 内部哪棵 View 树应该接收，父容器要不要拦截，最终哪个 View 才有资格说“我来处理”。

你看到的是一次点击，系统看到的是一条输入事件路线。

## 本章探索任务

建议按下面路线推进：

```text
认识 Android Input 系统
  -> 从触摸屏到 InputReader / InputDispatcher
      -> 理解 MotionEvent 与事件序列
          -> 找到 Activity / Window / DecorView 的事件入口
              -> 掌握 ViewGroup 分发与拦截
                  -> 分析点击、滑动、手势和冲突
                      -> 连接 Compose pointer input
                          -> 完成输入事件分发观察实验
```

## 本节定位

本节是第 14 章入口。

我们先回答：

- 为什么点击事件值得单独学？
- Input 系统和 WMS、ViewRootImpl 有什么关系？
- 为什么点击无响应、滑动冲突、误触不是简单“按钮坏了”？
- 第 14 章 demo 应该如何观察一次输入事件？

## 学习目标

学完本节后，你应该能够：

- 理解 Input 事件分发是 Framework 的核心链路。
- 知道触摸事件要经过系统、窗口和 View 树。
- 初步区分 InputDispatcher、ViewRootImpl、Activity、ViewGroup、View 的角色。
- 知道第 14 章要解决哪些真实交互问题。

## 第一部分：点击不是直接送给按钮

初学时很容易以为：

```text
用户点了按钮，按钮就收到点击。
```

这句话能描述结果，但解释不了过程。

系统至少要判断：

- 触摸发生在哪块屏幕区域。
- 当前哪个窗口位于触摸位置。
- 这个窗口是否可见、可触摸、可获得焦点。
- 事件应该送到哪个 App 进程。
- App 内部哪棵 View 树接收事件。
- 父容器是否拦截子 View 的事件。
- 最终是点击、长按、滑动还是取消。

所以点击不是按钮私自收到的，而是一路分发过来的。

## 第二部分：Input 系统连接硬件和 UI

Android 输入链路大致可以分成两段：

```text
系统侧：读取输入、判断目标窗口、派发到 App
应用侧：ViewRootImpl 接收事件、View 树分发、业务处理
```

系统侧会涉及：

- Kernel。
- InputReader。
- InputDispatcher。
- WMS 中的窗口信息。

应用侧会涉及：

- ViewRootImpl。
- DecorView。
- Activity。
- ViewGroup。
- View。

这条链路把硬件触摸和应用 UI 连接了起来。

## 第三部分：为什么点击问题难排

很多交互问题看起来像 UI bug：

- 按钮点了没反应。
- 列表滑不动。
- 子控件抢了父容器的滑动。
- 外层 ViewPager 和内层 RecyclerView 冲突。
- Dialog 外部点击没有关闭。
- 页面卡顿时点击延迟。
- Input ANR。

这些问题可能来自：

- 窗口没有焦点。
- 触摸区域被其他窗口覆盖。
- ViewGroup 拦截了事件。
- 子 View 消费了事件。
- 手势判断冲突。
- 主线程阻塞，事件无法及时处理。

只有理解输入链路，才能避免靠猜。

## 第四部分：第 14 章要解决的问题

第 14 章重点解决：

- 触摸事件如何从系统进入 App。
- `MotionEvent` 的 `DOWN / MOVE / UP / CANCEL` 是什么。
- Activity、Window、DecorView 在事件分发里站在哪里。
- `dispatchTouchEvent()`、`onInterceptTouchEvent()`、`onTouchEvent()` 如何配合。
- 滑动冲突如何判断和处理。
- Compose 里的 `clickable`、`pointerInput` 和传统 View 事件有什么关系。
- 点击无响应、误触、Input ANR 应该如何排查。

## 本节小挑战

### 点击路线题

请判断下面哪些环节可能影响一次点击：

- 按钮是否可见。
- 当前窗口是否有焦点。
- 父 ViewGroup 是否拦截事件。
- 主线程是否卡住。
- 网络接口是否返回成功。

## 本节实践任务

### 基础任务

- 找一个按钮点击场景。
- 在点击回调里加日志。
- 思考这条日志出现前，事件可能经过了哪些系统和 UI 节点。

### 进阶任务

- 找一个可滑动列表。
- 在外层和内层都加触摸日志。
- 观察 `DOWN / MOVE / UP` 的顺序。

## 本节小结

第 14 章的核心不是背三个方法名，而是理解：一次触摸要经过系统输入链路、窗口目标选择和 View 树分发。只有把这条链路连起来，点击无响应、滑动冲突、误触和 Input ANR 才会有清晰的排查入口。
