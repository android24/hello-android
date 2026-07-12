# 13.3 Window、PhoneWindow 与 ViewRootImpl

上一节我们看到了 DecorView。

这一节继续往下追：DecorView 属于谁？它又是如何真正接入系统显示链路的？

答案会遇到三个名字：

```text
Window
PhoneWindow
ViewRootImpl
```

## 本节剧情钩子

你可以把 Activity 想成一个页面导演。

导演不能直接把布景搬到屏幕上，它需要一个舞台框架。

`Window` 是抽象舞台，`PhoneWindow` 是 Android 里常见的具体舞台，`ViewRootImpl` 则像舞台调度员：它负责让这套布景开始测量、布局、绘制，并把输入事件送进 View 树。

## 本节定位

本节解释 Activity 内容如何从 Window 走向 ViewRootImpl。

## 学习目标

学完本节后，你应该能够：

- 理解 Window 是抽象概念，PhoneWindow 是常见实现。
- 知道 DecorView 通常由 PhoneWindow 管理。
- 理解 ViewRootImpl 是 View 树和系统之间的关键桥梁。
- 能解释为什么很多绘制、输入、窗口异常堆栈里会出现 ViewRootImpl。

## 第一部分：Window 是抽象入口

`Window` 可以理解为 Activity 展示内容的窗口抽象。

它负责描述窗口行为，例如：

- 设置内容视图。
- 管理窗口属性。
- 控制状态栏、导航栏相关行为。
- 提供 DecorView。
- 和 WindowManager 协作添加窗口。

但 `Window` 本身是抽象类，常见具体实现是 `PhoneWindow`。

## 第二部分：PhoneWindow 管理 DecorView

`PhoneWindow` 负责创建和维护 DecorView。

简化链路：

```text
Activity
  -> PhoneWindow
      -> DecorView
          -> content parent
              -> your content
```

当你调用 `setContentView()` 时，最终会把业务内容放进 PhoneWindow 准备好的 DecorView 内容区。

这也是为什么你在 Activity 里可以通过：

```kotlin
window.decorView
```

拿到窗口根视图。

## 第三部分：ViewRootImpl 是桥

DecorView 只是 View 树根节点。

但 View 树要真正参与系统显示，还需要一个桥接对象：`ViewRootImpl`。

它负责很多关键工作：

- 把 DecorView 和窗口连接起来。
- 发起 measure / layout / draw。
- 处理 Choreographer 的刷新回调。
- 接收并分发输入事件。
- 处理窗口尺寸变化。
- 和 WMS 进行窗口相关通信。

可以这样理解：

```text
DecorView 是树根，
ViewRootImpl 是让这棵树接入系统的根管家。
```

## 第四部分：为什么叫 Impl 却很重要

`ViewRootImpl` 的名字很容易让人误以为它只是内部实现细节。

但在排查问题时，它经常出现：

- `CalledFromWrongThreadException`
- View 绘制异常。
- 输入事件分发堆栈。
- 窗口添加或移除异常。
- Choreographer 刷新链路。

原因是它站在 View 树和系统窗口之间。应用 UI 只要要显示、刷新、接收输入，就绕不开它。

## 第五部分：简化调用链

第 13 章先记住这条线：

```text
Activity.onCreate()
  -> setContent / setContentView
      -> PhoneWindow 准备 DecorView
          -> ActivityThread.handleResumeActivity()
              -> WindowManager.addView(decorView)
                  -> 创建 ViewRootImpl
                      -> ViewRootImpl.setView()
```

细节会随着 Android 版本变化，但主线稳定：DecorView 最终要通过 WindowManager 和 ViewRootImpl 接入系统。

## 本节小挑战

### 三人分工题

请用自己的话解释：

- PhoneWindow 负责什么？
- DecorView 负责什么？
- ViewRootImpl 负责什么？

要求不能只写“源码类名”，要写出它们在页面显示中的位置。

## 本节实践任务

### 基础任务

- 打印 `window.javaClass.name`。
- 打印 `window.decorView.javaClass.name`。
- 在日志中记录 Activity `onResume()` 之后页面已经可见的时机。

### 进阶任务

- 搜索一次崩溃或异常堆栈中的 `ViewRootImpl`。
- 判断它和绘制、输入、线程或窗口哪类问题有关。

## 本节小结

Window 是 Activity 的显示窗口抽象，PhoneWindow 是常见实现，DecorView 是窗口里的 View 树根节点，ViewRootImpl 则负责把这棵树接入系统显示、输入和刷新调度。理解这组关系，后面看 WMS、绘制流程和窗口异常会轻松很多。
