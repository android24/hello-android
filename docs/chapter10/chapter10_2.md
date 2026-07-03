# 10.2 Android 系统架构全景：App、Framework、Native 与 Kernel

进入 Framework 之前，我们需要先看一张地图。

Android 不是只有你写的 App。一个页面能显示出来，背后至少经过应用层、Framework 层、Native 层和 Linux Kernel 层的协作。你不需要马上精通每一层，但必须知道它们各自负责什么。

## 本节定位

本节帮助你建立 Android 系统分层视角。

之后读启动、主线程、Binder、View 绘制和系统服务时，你会不断回到这张地图上。

## 学习目标

学完本节后，你应该能够：

- 说清 Android 系统大致分为哪些层。
- 理解 App 和 Framework 的关系。
- 知道 Native 与 Kernel 并不是离你很远的概念。
- 用分层视角分析一次页面展示。

## 第一部分：从 App 视角看系统

你平时写的代码大多在应用层：

- `Activity`
- `Application`
- `ViewModel`
- Compose UI
- Repository
- Room / DataStore
- Retrofit / OkHttp

这些代码运行在你的应用进程里。

但应用层并不能独自完成所有事情。启动页面、申请权限、显示窗口、处理输入、播放音视频、访问相机、安装应用，都需要系统能力参与。

于是 App 会通过 Framework API 请求系统完成协作。

## 第二部分：Framework 层负责什么

Framework 层可以理解为 Android 提供给应用的“高级系统能力接口”和“系统管理逻辑”。

常见角色包括：

- `ActivityManager` / `ActivityTaskManager`：管理应用进程、Activity 启动和任务栈。
- `WindowManager`：管理窗口、层级、尺寸和显示关系。
- `PackageManager`：管理安装包、组件信息和权限声明。
- `NotificationManager`：管理通知。
- `ContentResolver`：访问 ContentProvider。
- `Handler` / `Looper`：支撑线程消息循环。

你写 App 时调用的很多 API，背后都会进入 Framework。

## 第三部分：Native 层负责什么

Native 层通常由 C / C++ 实现，承担更靠近底层或性能敏感的能力。

例如：

- ART Runtime：运行 Kotlin / Java 字节码。
- Skia：图形绘制。
- Media Framework：音视频处理。
- SQLite：数据库底层能力。
- SurfaceFlinger：合成最终显示画面。

当你看到 Compose 或 View 最终绘制到屏幕上时，背后已经不只是 Kotlin 代码在工作了。

## 第四部分：Kernel 层负责什么

Linux Kernel 是 Android 的底座。

它负责：

- 进程与线程调度。
- 内存管理。
- 文件系统。
- 网络协议栈。
- 设备驱动。
- Binder 驱动。

Binder 很特别。它在 Framework 中表现为系统服务通信机制，但底层需要 Kernel 中的 Binder 驱动支撑。

## 第五部分：一次页面展示穿过了哪些层

可以用一个简化链路理解：

```text
你的 Compose 代码
  -> Android Framework 管理 Activity、Window、主线程消息
      -> Native 图形库完成绘制与合成
          -> Kernel 调度线程、分配内存、驱动硬件
              -> 屏幕显示结果
```

这就是为什么一个“页面卡顿”问题可能同时涉及：

- 应用层是否在主线程做了重活。
- Framework 是否及时处理消息。
- 渲染链路是否丢帧。
- 系统资源是否紧张。

## 第六部分：本课程中的 Framework 学习路线

第 10 章先建立入门视角：

```text
系统分层
  -> 源码阅读方法
      -> App 启动链路
          -> ActivityThread / Context / Instrumentation
              -> Handler / Looper / MessageQueue
                  -> Binder 初识
```

后续章节可以继续深入：

- AMS / ATMS 与应用启动。
- WMS 与窗口管理。
- PMS 与包管理。
- View 绘制与事件分发。
- Choreographer 与渲染节奏。
- SurfaceFlinger 与显示合成。

## 本节小挑战

请用自己的话解释：

- `Activity` 属于哪一层？
- `ActivityTaskManagerService` 属于哪一层？
- Binder 驱动属于哪一层？
- 一个 UI 卡顿问题为什么不能只看 Composable 代码？

## 本节实践任务

### 基础任务

- 打开第 10 章 demo。
- 阅读页面中的“系统分层地图”。
- 把你熟悉的类填到对应层级中。

### 进阶任务

- 搜索 demo 中的 `FrameworkLayer`。
- 新增一个你熟悉的系统能力，例如通知、权限或相机。
- 说明它可能会穿过哪些系统层。

## 本节小结

Android Framework 不存在于应用层之外的另一个宇宙。它是 App 能够启动、显示、交互、通信和管理资源的系统骨架。先建立系统分层地图，后面读源码才不会只看到一堆类名。
