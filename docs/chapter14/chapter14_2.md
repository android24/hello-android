# 14.2 从触摸屏到 App：InputReader、InputDispatcher 与 ViewRootImpl

上一节我们知道：点击不是直接送给按钮。

这一节继续往前追，看看触摸事件从硬件进入系统后，如何找到目标窗口，再进入 App 进程。

## 本节剧情钩子

你点了一下屏幕。

从用户视角看，这是一个很轻的动作。

从系统视角看，这是一次“输入快递”：触摸屏产生包裹，系统读取包裹，判断收件窗口，找到收件 App，最后交给 ViewRootImpl 签收。

## 本节定位

本节建立系统侧输入链路的简化模型。

## 学习目标

学完本节后，你应该能够：

- 知道输入事件来自系统底层，不是 App 自己生成的。
- 理解 InputReader 和 InputDispatcher 的基本职责。
- 知道 WMS 的窗口信息会影响事件派发目标。
- 理解 ViewRootImpl 是应用侧接收输入的重要入口。

## 第一部分：Kernel 产生原始输入

触摸屏、键盘、鼠标等输入设备产生原始输入事件。

这些事件会先进入系统底层。

入门阶段可以先这样理解：

```text
硬件输入
  -> Kernel
      -> Android 输入系统
```

App 不会直接从硬件读取触摸点，而是接收系统加工和派发后的输入事件。

## 第二部分：InputReader 读取事件

`InputReader` 负责读取输入设备事件，并把底层数据转换成 Android 能理解的输入事件。

它关心：

- 输入设备是什么。
- 事件类型是什么。
- 坐标、压力、按键等原始信息。
- 事件时间。
- 多指触控等信息。

可以把 InputReader 理解成“输入翻译员”。

## 第三部分：InputDispatcher 派发事件

`InputDispatcher` 负责决定事件要送到哪里。

它会结合窗口信息判断：

- 当前触摸位置落在哪个窗口。
- 目标窗口是否可见。
- 目标窗口是否可触摸。
- 是否存在遮挡。
- 是否需要派发 `CANCEL`。
- 目标 App 是否及时处理事件。

可以把 InputDispatcher 理解成“输入调度员”。

## 第四部分：WMS 提供窗口地图

InputDispatcher 想知道“事件该给谁”，就需要知道屏幕上有哪些窗口。

这些窗口信息和 WMS 密切相关。

例如：

- Activity 主窗口。
- Dialog。
- 输入法窗口。
- 系统状态栏。
- 悬浮窗。

这就是第 13 章和第 14 章的连接点：

```text
WMS 管理窗口秩序，
InputDispatcher 借助窗口秩序选择输入目标。
```

## 第五部分：ViewRootImpl 接收事件

当事件被送到 App 进程后，应用侧的重要入口是 `ViewRootImpl`。

它会把输入事件继续交给 View 树：

```text
InputDispatcher
  -> App 进程
      -> ViewRootImpl
          -> DecorView
              -> Activity / ViewGroup / View
```

所以 ViewRootImpl 不只参与绘制，也参与输入。

## 第六部分：Input ANR 的影子

如果目标 App 主线程迟迟不处理输入事件，就可能出现 Input ANR。

典型原因：

- 主线程执行耗时任务。
- 死锁或锁等待。
- 大量同步 IO。
- Binder 调用卡住。
- 布局或绘制过重。

用户感受到的是“点了没反应”，系统看到的是“输入事件没有被及时消费”。

## 本节小挑战

### 输入快递题

请补全这条链路：

```text
触摸屏
  -> Kernel
      -> ?
          -> ?
              -> App 进程
                  -> ?
```

## 本节实践任务

### 基础任务

- 在一个 Activity 里重写 `dispatchTouchEvent()` 打日志。
- 点击屏幕，观察事件是否进入 App。
- 记录 `ACTION_DOWN`、`ACTION_UP` 的顺序。

### 进阶任务

- 在点击回调里故意做一个短暂主线程耗时实验。
- 观察点击反馈是否变慢。
- 思考这和 Input ANR 的关系。

## 本节小结

输入事件从硬件开始，经过 Kernel、InputReader、InputDispatcher 和窗口目标选择，最后进入 App 进程，由 ViewRootImpl 接收并交给 View 树分发。理解这条系统侧链路后，点击无响应和 Input ANR 就不再只是应用层问题。
