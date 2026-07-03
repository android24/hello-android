# 第10章源码阅读建议

## 先读主干

Framework 源码里有大量兼容逻辑、权限判断、异常处理和历史分支。第一轮阅读不要试图每一行都吃透，先抓住主干。

推荐顺序：

```text
ActivityThread.main()
  -> Looper.prepareMainLooper()
  -> Looper.loop()
```

然后再看：

```text
Handler.post()
  -> MessageQueue
  -> Looper.loop()
  -> Handler.dispatchMessage()
```

## 带着问题读

不要把目标写成“读完 ActivityThread”。

可以把目标写成：

- `Application.onCreate()` 是谁触发的？
- `Activity.onCreate()` 为什么一定在主线程？
- `Handler.post()` 如何进入消息队列？
- `startActivity()` 为什么需要系统服务？

## 三层笔记法

每次读源码只记三件事：

```text
一句话结论
简化调用链
仍不确定的问题
```

示例：

```text
一句话结论：
ActivityThread 是应用进程主线程上的 Framework 调度核心。

简化调用链：
ActivityThread.main()
  -> Looper.prepareMainLooper()
  -> ActivityThread.attach()
  -> Looper.loop()

仍不确定：
ActivityClientRecord 里每个字段分别负责什么？
```

## 不要急着深入 Binder 驱动

第 10 章只要求理解 Binder 的作用：

```text
App 进程通过 Binder 与 system_server 中的系统服务通信。
```

等后续学习系统服务时，再深入 ServiceManager、Proxy / Stub、AIDL 和 Binder 驱动。
