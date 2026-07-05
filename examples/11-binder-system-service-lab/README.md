# 示例工程：Binder 与系统服务实验室

## 对应章节

第11章 Binder、SystemServer 与系统服务入门

## 工程目标

本工程用于配合第 11 章，把 Binder、系统服务、Manager、远程 Service 和调用链笔记放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕三个问题展开：

- App 侧如何看到系统服务入口？
- Binder 为什么不是普通本地函数调用？
- 一次跨进程消息如何被记录成调用链报告？

## 当前效果

运行后你会看到一个“第 11 章 Binder 与系统服务实验室”页面：

- `系统服务追踪任务卡` 会把本章练习拆成四个可执行任务。
- `App 进程观察` 展示本地 PID、线程名、进程启动年龄和包名。
- `远程 Service / Binder 通道` 可以绑定、解绑、发送一次跨进程消息。
- `系统服务观察` 展示 ActivityManager、WindowManager、NotificationManager、ClipboardManager 等 App 侧入口。
- `Binder 调用模型` 展示 Client、Proxy、Binder Driver、Server、Reply 的简化链路。
- `Binder 调用轨迹` 记录绑定、发送、远程回复和错误信息。

这个 demo 使用 `Messenger` 绑定一个运行在 `:binder` 独立进程中的 `RemoteEchoService`。它不是系统服务本体，但可以帮助你观察 Binder 通信的基本形态。

## 探索玩法

建议按下面顺序完成：

```text
看系统服务观察卡片
  -> 绑定远程 Service
      -> 发送 Binder 消息
          -> 查看本地 PID、远程 PID、线程名和耗时
              -> 写一份 10 行以内的 Binder 调用链报告
```

最小报告可以写成：

```text
问题：Messenger 消息为什么能送到 :binder 进程？
现象：点击发送后，页面收到 remotePid 和 roundTrip。
本地入口：MainActivity.sendBinderMessage()
远程入口：RemoteEchoService.IncomingHandler
链路：MainActivity -> Messenger -> Binder -> RemoteEchoService -> replyTo
风险：如果远程处理很慢，调用方需要关注等待和线程。
```

## 运行方式

1. 使用 Android Studio 打开 `examples/11-binder-system-service-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `BinderSystemLab`。
5. 点击 `绑定`，再点击 `发送`，观察页面和日志变化。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
11-binder-system-service-lab/
  app/
    src/main/java/com/helloandroid/binder/
      BinderLabApplication.kt
      MainActivity.kt
      RemoteEchoService.kt
      BinderLabState.kt
      BinderLabViewModel.kt
      BinderLabScreen.kt
  quality/
    binder-call-report-template.md
    system-service-reading-notes.md
```

## 关键源码入口

- `BinderLabApplication.kt`：记录应用进程启动信息。
- `MainActivity.kt`：绑定远程 Service、发送 Binder 消息、接收 replyTo 回复。
- `RemoteEchoService.kt`：运行在 `:binder` 进程中的远程 Service。
- `BinderLabState.kt`：集中放置任务卡、系统服务观察项和 Binder 模型。
- `BinderLabViewModel.kt`：管理绑定状态、回复结果和调用轨迹。
- `BinderLabScreen.kt`：展示实验页面。
- `quality/binder-call-report-template.md`：Binder 调用链报告模板。
- `quality/system-service-reading-notes.md`：系统服务源码阅读建议。

## 推荐对照的 AOSP 入口

```text
frameworks/base/core/java/android/os/Binder.java
frameworks/base/core/java/android/os/IBinder.java
frameworks/base/core/java/android/os/Parcel.java
frameworks/base/core/java/android/os/ServiceManager.java
frameworks/base/services/java/com/android/server/SystemServer.java
frameworks/base/core/java/android/app/SystemServiceRegistry.java
frameworks/base/core/java/android/app/ContextImpl.java
```

建议带着问题看：

```text
SystemServer 中服务如何启动？
ServiceManager 如何管理服务入口？
getSystemService() 如何返回 App 侧 Manager？
Manager 如何继续走向远程系统服务？
```

## 练习任务

### 基础任务

- 按照页面里的 `系统服务追踪任务卡` 完成一轮观察。
- 点击 `绑定`，确认远程 Service 已连接。
- 点击 `发送`，记录本地 PID 和远程 PID 是否不同。
- 打开 Logcat，搜索 `BinderSystemLab`。
- 使用 `quality/binder-call-report-template.md` 写一份短报告。

### 进阶任务

- 给远程 Service 增加一个新的返回字段。
- 给远程处理增加轻微延迟，观察往返耗时变化。
- 新增一个系统服务观察项，例如 `PowerManager` 或 `InputMethodManager`。
- 把某个 `getSystemService()` API 追到 `SystemServiceRegistry`。
- 写出 `App API -> Manager -> Binder -> system_server 服务` 的简化链路。

## 通关目标

完成本工程后，你应该能说清楚：

- 系统服务为什么不能由每个 App 各自管理。
- Binder 为什么是跨进程通信通道。
- Messenger 为什么可以用来观察 Binder 通信。
- Manager 和系统服务本体为什么不是同一个对象。
- ServiceManager 和 SystemServer 在系统服务体系中的位置。
- Binder 调用为什么要关注耗时、权限和稳定性。
