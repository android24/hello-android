# 10.7 Binder 初识：为什么 Android 到处都是跨进程通信

Android 里很多事情都不是 App 独自完成的。

你启动页面、获取系统服务、申请权限、访问剪贴板、发送通知、绑定 Service，背后都可能发生跨进程通信。

Binder 就是 Android 最核心的跨进程通信机制。

## 本节定位

本节只做 Binder 入门，不展开驱动细节和完整源码。

目标是让你先理解：为什么需要 Binder，Binder 连接了谁，以及它为什么会在 Framework 里反复出现。

## 学习目标

学完本节后，你应该能够：

- 理解 App 进程和系统服务通常不在同一进程。
- 知道 Binder 用于跨进程通信。
- 理解系统服务调用为什么需要代理对象。
- 能把 `startActivity()` 和 Binder 建立初步联系。

## 第一部分：为什么需要跨进程

Android 是多进程系统。

你的 App 有自己的进程，系统服务通常运行在系统进程里，例如 `system_server`。

如果 App 想请求系统做事，就需要跨进程通信。

例如：

```text
App 进程
  -> 请求启动 Activity
      -> system_server 中的 ActivityTaskManagerService
```

这两个对象不在同一个进程里，不能像普通函数调用那样直接访问内存。

## 第二部分：Binder 解决什么问题

Binder 负责让一个进程可以调用另一个进程暴露的服务接口。

可以先这样理解：

```text
Binder 让跨进程调用看起来接近本地方法调用。
```

当然，“看起来像”不代表成本一样。

跨进程调用涉及数据序列化、线程调度、权限检查和系统服务处理，因此不能随便在主线程做大量重型 Binder 调用。

## 第三部分：系统服务为什么常用 Binder

系统服务集中管理系统能力。

例如：

- Activity 启动。
- 窗口管理。
- 包管理。
- 通知管理。
- 输入法。
- 剪贴板。

App 通过 Framework API 拿到某种代理，再通过 Binder 和系统服务通信。

简化理解：

```text
App 调用 Framework API
  -> 代理对象发起 Binder 调用
      -> system_server 中的系统服务处理请求
          -> 返回结果
```

## 第四部分：startActivity 背后的 Binder 味道

你写：

```kotlin
startActivity(intent)
```

看起来是本地调用。

但系统需要知道：

- 目标 Activity 是否存在。
- 目标应用进程是否已经启动。
- 任务栈如何调整。
- 权限是否允许。
- 是否需要创建新进程。

这些判断不可能只由你的 App 自己完成，所以最终会进入系统服务。

这就是 Binder 与页面启动的关系。

## 第五部分：Binder 和性能稳定性的关系

Binder 不只是源码知识，也会影响工程问题。

例如：

- 主线程频繁调用系统服务，可能造成响应变慢。
- 跨进程调用如果对端繁忙，可能带来等待。
- 系统服务异常或权限变化，会影响 App 行为。
- ANR 分析里经常能看到 Binder 相关线程和等待。

所以，理解 Binder 能帮助你看懂更多性能和稳定性现场。

## 第六部分：本章先看到哪里

本节只要求你记住：

```text
App 和系统服务常常不在同一进程。
Binder 是 Android 的核心 IPC 机制。
Framework API 经常把 Binder 调用包装得像普通方法。
```

后续深入 Framework 时，再学习：

- AIDL。
- Binder Proxy / Stub。
- ServiceManager。
- Binder 驱动。
- system_server 中的系统服务注册与查找。

## 本节小挑战

请判断下面哪些场景可能涉及 Binder：

- `startActivity()`
- 获取 `WindowManager`
- 普通 Kotlin 函数内部计算两个数字相加
- 发送通知
- 查询已安装应用信息

## 本节实践任务

### 基础任务

- 在第 10 章 demo 中阅读“Binder 初识”卡片。
- 找出页面里列出的三个系统服务。
- 说明它们为什么不能简单放在每个 App 进程里各自管理。

### 进阶任务

- 选择一个你常用的系统服务，例如通知、剪贴板或包管理。
- 写出它可能的调用链：

```text
App API -> Framework Manager -> Binder -> system_server 服务
```

## 本节小结

Binder 是 Android Framework 的血管。应用进程通过它请求系统服务，系统也通过它把能力组织起来。第 10 章只做初识，但只要你记住“跨进程通信”这条主线，后面再看 AMS、WMS、PMS 和系统服务就会清楚许多。
