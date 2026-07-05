# 11.4 ServiceManager：系统服务的通讯录

如果系统里有很多服务，App 怎么知道该找谁？

答案离不开 ServiceManager。

## 本节剧情钩子

想象系统办事大厅里有很多窗口：窗口管理、包管理、通知管理、输入法管理。

如果没有总咨询台，你进门后根本不知道该去几号窗口。

ServiceManager 就像这座大厅的通讯录和咨询台。服务启动后先登记，客户端需要时再按名字查找。

## 本节定位

本节理解系统服务的注册与查找思路。

## 学习目标

学完本节后，你应该能够：

- 理解服务为什么需要注册。
- 知道 ServiceManager 的基本作用。
- 区分服务本体和客户端拿到的代理。
- 理解 `getSystemService()` 背后的查找思路。

## 第一部分：服务需要名字

系统服务不是凭空被找到的。

它们通常会以一个名字注册到服务管理机制中。

简化理解：

```text
系统服务启动
  -> 向 ServiceManager 注册名字和 Binder
      -> 客户端按名字查询
          -> 拿到 Binder 代理
```

## 第二部分：客户端拿到的是代理

客户端查询服务后，通常拿不到服务本体。

它拿到的是一个能跨进程通信的 Binder 引用或封装后的 Manager。

这也是为什么你在 App 侧调用 `NotificationManager`，真正处理通知的却是系统进程里的通知服务。

## 第三部分：getSystemService 像查通讯录

你经常写：

```kotlin
val notificationManager = getSystemService(NotificationManager::class.java)
```

看起来很普通。

但从 Framework 视角看，它背后是：

```text
Context
  -> 查找对应系统服务入口
      -> 返回 App 侧 Manager
          -> Manager 内部持有或使用远程服务代理
```

不同服务细节不同，但“按名字或类型找到服务入口”是理解主线。

## 第四部分：为什么 ServiceManager 很关键

没有 ServiceManager，客户端和服务端就很难建立稳定连接。

它解决的是：

- 服务如何登记。
- 客户端如何发现服务。
- Binder 引用如何被传递。
- 系统服务入口如何统一管理。

它不是每个业务 App 都直接接触的 API，却是理解系统服务体系的关键节点。

## 第五部分：和 SystemServer 的关系

很多系统服务在 SystemServer 中启动。

服务启动后，会注册到 ServiceManager 或相关管理结构中，等待客户端查询和调用。

所以后面看 SystemServer 时，要特别关注：

```text
服务在哪里创建？
服务叫什么名字？
服务如何被注册？
客户端如何找到它？
```

## 本节小挑战

### 通讯录查询题

请解释：

- 为什么系统服务需要名字？
- 为什么客户端拿到的不是服务本体？
- `getSystemService()` 为什么像查通讯录？

## 本节实践任务

### 基础任务

- 打开第 11 章 demo。
- 查看系统服务列表和“系统服务通讯录模拟”。
- 找到 `NotificationManager`、`ActivityManager`、`WindowManager`。
- 对照通讯录里的服务名、注册方和 App 入口，解释它们之间的关系。

### 进阶任务

- 新增一个你熟悉的系统服务展示项。
- 写出它可能对应的系统服务职责。
- 说明 App 侧 Manager 和系统服务本体的区别。
- 在通讯录里补充一个新服务，例如 `power`、`input_method` 或 `clipboard`。

## 本节小结

ServiceManager 是系统服务体系里的通讯录。系统服务需要注册，客户端需要查找，查到以后通过 Binder 或 Manager 进行通信。理解这条线，就能把零散的系统服务名串起来。
