# 11.6 从 getSystemService 看系统服务调用

很多系统服务学习，可以从一个你每天都会写的 API 开始：

```kotlin
getSystemService(...)
```

它像一扇小门，背后连着 Context、Manager、系统服务和 Binder。

## 本节剧情钩子

你在 App 里写 `getSystemService()`，感觉像从抽屉里拿出一个工具。

但从 Framework 视角看，更像你拿到了一张办事窗口的通行证。工具在手里，真正的系统能力却在另一端。

## 本节定位

本节从 App 常用 API 反向理解系统服务调用链。

## 学习目标

学完本节后，你应该能够：

- 理解 `Context` 是系统能力入口。
- 知道 Manager 和系统服务之间不是同一个对象。
- 能写出 `getSystemService()` 的简化调用链。
- 用 demo 观察 App 侧可见的系统服务信息。

## 第一部分：Context 是入口

在 Android 中，`Context` 不只是“上下文”。

它还是访问系统能力的入口。

例如：

```kotlin
val activityManager = getSystemService(ActivityManager::class.java)
val windowManager = getSystemService(WindowManager::class.java)
val notificationManager = getSystemService(NotificationManager::class.java)
```

这些 API 都说明：App 需要通过 Context 找到系统能力入口。

## 第二部分：Manager 不是系统服务本体

App 侧拿到的 Manager 通常是封装后的访问对象。

它可能内部持有远程服务代理，也可能通过其他 Framework 机制转发请求。

简化理解：

```text
App
  -> Context.getSystemService()
      -> Manager
          -> Binder / Framework 内部调用
              -> system_server 中的服务
```

不要把 Manager 和系统服务本体混为一谈。

## 第三部分：为什么这样设计

这样设计有几个好处：

- App API 更稳定。
- 系统服务实现可以隐藏。
- 权限检查可以集中处理。
- 不同 Android 版本可以调整内部实现。
- 系统能力可以统一管理。

这就是 Framework 的价值：给 App 一个相对稳定的门面，同时在系统内部完成复杂调度。

## 第四部分：demo 中观察系统服务

第 11 章 demo 会展示一些 App 侧可见的系统服务入口。

你可以观察：

- Manager 类型。
- 获取方式。
- 它可能对应的系统能力。
- 是否可能涉及 Binder。

重点不是记住所有 Manager，而是建立“App 入口 -> 系统服务”的思维。

## 第五部分：从 API 追到源码

建议你选择一个熟悉 API，例如：

```kotlin
getSystemService(NotificationManager::class.java)
```

然后按下面路线写笔记：

```text
Context API
  -> Manager 对象
      -> 远程服务接口或代理
          -> system_server 服务
```

如果第一轮追不完整，也没关系。先标出不确定点。

## 本节小挑战

### 工具抽屉题

请解释：

- `Context` 为什么能拿系统服务？
- Manager 和系统服务本体有什么区别？
- 为什么 Framework 不让 App 直接操作所有系统服务对象？

## 本节实践任务

### 基础任务

- 运行第 11 章 demo。
- 查看系统服务观察卡片。
- 选择一个 Manager 写出它的职责。

### 进阶任务

- 新增一个系统服务观察项。
- 为它补充“可能涉及 Binder”的说明。
- 写一条从 `getSystemService()` 到系统服务的简化链路。

## 本节小结

`getSystemService()` 是理解系统服务最贴近日常开发的入口。App 通过 Context 获取 Manager，再由 Manager 隐藏跨进程和系统服务细节。越熟悉这条线，越能把日常 API 和 Framework 源码连接起来。
