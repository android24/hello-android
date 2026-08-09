# 19.1 为什么要学习进程模型、Zygote 与内存管理

第 18 章我们看见代码如何被加载：

```text
APK
  -> classes.dex
      -> ClassLoader
          -> Dalvik / ART
              -> 执行代码
```

第 19 章继续追问一个更底层的问题：

```text
代码被找到了
  -> 那它运行在哪个进程里？
      -> 这个进程是谁创建的？
          -> 为什么有时会被系统杀掉？
              -> 为什么不同 App 彼此隔离？
```

这会进入 Android 的进程模型、Zygote、UID、应用沙箱、主进程 / 多进程、进程优先级、OOM Adj、LMKD 和内存回收。

## 本章通关画面

学完第 19 章后，你应该能画出这条链路：

```text
startActivity / startService / bindService
  -> AMS 判断目标进程是否存在
      -> 不存在则请求 Zygote fork
          -> 新进程启动 ActivityThread.main()
              -> attach 到 system_server
                  -> bindApplication
                      -> 创建 Application
                          -> 加载组件并执行业务代码
                              -> AMS 持续更新进程状态和 OOM Adj
                                  -> 内存紧张时 LMKD 根据优先级回收进程
```

如果第 18 章像走进代码引擎室，第 19 章就是进入进程调度室。

代码能不能运行，取决于 Dex 和 ClassLoader；代码运行得是否稳定，则离不开进程、线程、内存和系统回收策略。

这一章会围绕四条主线展开：

| 主线 | 核心问题 | 典型证据 |
| --- | --- | --- |
| 出生 | App 进程如何从 Zygote fork 出来 | pid、ppid、ActivityThread、bindApplication |
| 身份 | App 为什么被系统隔离 | uid、userId、processName、SELinux context |
| 执行 | 代码在哪些线程里运行 | main、Binder、RenderThread、worker thread |
| 回收 | 后台进程为什么会消失 | procState、oom_score_adj、LMKD、进程重启日志 |

如果这一章只学会一个结论，那应该是：

```text
App 不是一个抽象图标
  -> 它是一个带 uid 身份的 Linux 进程
      -> 由 Zygote fork 出来
          -> 由 ActivityThread 接入 Framework 调度
              -> 在主线程、Binder 线程和业务线程中执行
                  -> 被 AMS 计算重要性
                      -> 在低内存时可能被 LMKD 回收
```

这条链路比“进程是什么”更重要。

## 本章剧情线

很多问题表面看起来像业务 bug：

```text
App 从后台回来白屏
后台任务突然中断
Service 莫名被杀
多进程数据不同步
ContentProvider 初始化太早
Application 初始化执行了多次
推送进程和主进程状态不一致
低内存设备上进程频繁重启
```

但它们背后经常不是“某个 if 写错了”，而是你没有看清进程地图：

- 一个 App 一定只有一个进程吗？
- `android:process` 会带来什么后果？
- Zygote 为什么要提前加载类和资源？
- App 进程是如何从 Zygote fork 出来的？
- `ActivityThread.main()` 为什么是 App 进程的入口？
- Binder 线程池和主线程是什么关系？
- 系统如何判断一个进程重要不重要？
- OOM Adj 为什么影响进程是否会被杀？
- LMKD 和 Java 内存泄漏有什么关系？
- 为什么多进程里单例、缓存、DataStore、Room 都要重新思考？

这一章会把这些问题放回 Android 进程机制里解释。

## 本章探索任务

```text
认识 Linux 进程和 Android UID
  -> 理解应用沙箱、权限和 SELinux
      -> 追踪 Zygote fork App 进程
          -> 观察 ActivityThread.main 与 bindApplication
              -> 区分主线程、Binder 线程、RenderThread 和业务线程
                  -> 理解进程状态、OOM Adj 和 LMKD
                      -> 分析多进程、远程 Service 和 isolatedProcess
                          -> 整理进程被杀、后台任务中断和多进程错乱的排查路线
```

## 本节定位

本节是第 19 章入口。

它负责回答：

- 第 19 章和第 12、18 章是什么关系？
- 为什么资深 Android 工程师必须理解进程模型？
- 为什么进程问题常常表现成生命周期、缓存、后台任务和稳定性问题？
- 第 19 章的学习顺序是什么？

## 学习目标

学完本节后，你应该能够：

- 说清楚“代码加载”和“进程运行环境”的区别。
- 初步理解 Zygote、App 进程、ActivityThread、AMS、LMKD 的关系。
- 知道多进程不是性能灵药，而是复杂度放大器。
- 能把常见后台死亡、多进程错乱和低内存问题放入正确排查方向。

## 第一部分：代码不是运行在真空里

第 18 章解决的是：

```text
系统如何找到代码？
```

第 19 章解决的是：

```text
代码在哪个进程里运行？
这个进程如何出生、活着、被回收？
```

这两个问题紧密相连。

如果没有进程，ClassLoader 没有运行空间；如果没有 ClassLoader，进程里也找不到业务类。

你可以这样理解：

```text
进程
  -> 提供运行空间、内存、线程、权限身份

ClassLoader
  -> 在这个运行空间里找到类

ART
  -> 在这个运行空间里执行代码
```

## 第二部分：Android App 进程不是自己出生的

很多初学者容易把 App 启动想成：

```text
点击图标
  -> 直接运行 MainActivity
```

真实情况更像：

```text
点击图标
  -> Launcher 请求 system_server
      -> AMS / ATMS 判断目标进程是否存在
          -> 如果不存在，请 Zygote fork 新进程
              -> 新进程进入 ActivityThread.main()
                  -> 和 AMS 建立 Binder 关系
                      -> 创建 Application
                          -> 创建 Activity
```

也就是说，App 进程不是 Activity 自己创建的，而是系统服务和 Zygote 协作创建的。

Activity 只是进入这个进程后的一个组件。

## 第三部分：为什么 Zygote 很关键

Zygote 的名字有点奇怪，但它承担的角色非常关键。

它像一个提前准备好的进程模板：

```text
Zygote
  -> 预加载常用类和资源
  -> 初始化运行时环境
  -> 等待系统请求
  -> fork 出 App 进程
```

为什么不每次都从零创建？

因为 Android 需要频繁启动 App。如果每个 App 进程都从空白状态初始化运行时、加载基础类、准备资源，启动成本会很高。

Zygote 通过 fork 让子进程继承一部分已经准备好的环境，再配合写时复制，让启动更快、内存更省。

## 第四部分：进程身份决定边界

Android 不只是“给每个 App 一个进程”。

它还会给 App 分配身份：

```text
packageName
  -> uid / appId
      -> Linux 进程身份
          -> 应用沙箱
              -> 权限和文件访问边界
```

所以不同 App 之间不能随便读写彼此的数据，不只是因为 Java 层 API 不允许，更是因为系统底层进程身份和文件权限就把边界划开了。

这也是 Android 安全模型的基础之一。

## 第五部分：为什么多进程会让事情复杂

Android 支持在 Manifest 里声明：

```xml
<service
    android:name=".RemoteService"
    android:process=":remote" />
```

这样组件会运行在另一个进程。

听起来像是“把事情拆开”，但它会立刻带来新问题：

- `Application.onCreate()` 可能在多个进程执行。
- 单例在不同进程里不是同一个对象。
- 内存缓存不能直接共享。
- Room / DataStore / SharedPreferences 要考虑跨进程一致性。
- 崩溃、日志、初始化、埋点都要带上进程名。
- Binder 成为进程间协作的必要通道。

多进程不是高级感，而是工程成本。

## 第六部分：进程为什么会被杀

Android 设备内存有限。

系统不会无限保留所有后台进程。

当内存紧张时，系统会根据进程重要性和 OOM Adj 选择回收对象：

```text
前台可见进程
  -> 感知进程
      -> 服务进程
          -> 缓存进程
              -> 更容易被回收
```

所以“后台回来页面没了”不一定是系统坏了。

可能是：

- 进程已经被杀。
- Activity 状态没有保存。
- Application 被重新创建。
- 内存缓存丢失。
- 后台任务依赖了不可靠进程存活。

资深工程师要能把这些现象和进程生命周期联系起来。

把第 19 章放进前面几章，你会看到它其实是在补一块底座：

```text
第 12 章 Activity 启动
  -> 为什么需要创建目标进程？

第 18 章 ClassLoader / ART
  -> 代码加载发生在谁的进程空间里？

第 9 章 稳定性治理
  -> 为什么后台进程死亡后状态会丢？

第 6 章 异步与后台任务
  -> 线程和任务为什么不能假设进程常驻？
```

所以第 19 章不是额外话题，而是把前面很多“现象”放回系统运行空间里。

## 本节小挑战

### 进程地图开场题

请回答：

```text
一个 App 从冷启动到首页显示，至少经历了哪些进程相关步骤？
```

建议包含：

- Launcher 发起启动请求。
- system_server 中 AMS / ATMS 调度。
- Zygote fork App 进程。
- App 进程进入 ActivityThread.main。
- Application 和 Activity 被创建。
- 进程重要性随前后台状态变化。

## 本节实践任务

### 基础任务

- 用 `adb shell ps -A | grep 包名` 查看 App 进程。
- 在 Logcat 中打印当前进程名和 pid。
- 杀掉后台进程后重新打开 App，观察 Application 是否重新创建。

### 进阶任务

- 查阅 `ActivityThread.main()` 的源码入口。
- 查阅 Zygote fork App 进程的调用链。
- 画出“点击图标 -> App 进程创建 -> Activity 创建”的进程链路图。

## 本节小结

第 19 章负责回答“App 代码运行在哪个进程里，以及这个进程如何被创建、隔离、调度和回收”。理解进程模型后，Zygote、ActivityThread、多进程、后台死亡、OOM Adj、LMKD、应用沙箱和低内存稳定性问题就会进入同一张地图。
