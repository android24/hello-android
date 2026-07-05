# 11.1 为什么系统服务是 Framework 的核心

第 10 章里，我们第一次走进 Framework 后台：看到了系统分层、App 启动、`ActivityThread`、主线程消息模型，也知道了 Binder 是 Android 跨进程通信的核心通道。

第 11 章继续往里走。

这一次，我们把目光放到系统服务上。

## 本章通关画面

完成第 11 章后，你应该能把一次系统能力调用讲成这样：

```text
App 调用 Framework API
  -> 拿到 Manager 或 Binder 代理
      -> 通过 Binder 发起跨进程请求
          -> system_server 中的系统服务处理
              -> 返回结果或触发后续调度
```

你不需要在这一章掌握 Binder 驱动实现，但要开始理解：

```text
系统服务为什么存在，Binder 为什么重要，SystemServer 为什么是 Framework 世界里的中枢。
```

## 本章剧情线

如果第 10 章是“参观 Android 后台控制室”，第 11 章就是“走到系统办事大厅窗口前”。

你的 App 不是万能的。它想启动页面、显示窗口、发通知、查安装包、获取剪贴板、请求输入法，都需要向系统提交申请。

系统服务就是这些窗口背后的办事员。Binder 是窗口和窗口之间传递材料的通道。SystemServer 则像整座办事大厅的工作区，很多核心服务都在那里启动、注册、等待调用。

## 本章探索任务

本章建议按下面路线推进：

```text
认识系统服务
  -> 看懂 Binder 调用模型
      -> 理解 Stub、Proxy、Parcel
          -> 认识 ServiceManager
              -> 进入 SystemServer
                  -> 从 getSystemService 追一次调用
                      -> 分析 Binder 风险
                          -> 完成系统服务观察实验
```

## 本节定位

本节是第 11 章入口。

我们先回答：

- 为什么 Android 需要系统服务？
- 为什么 App 不能自己管理所有系统能力？
- 为什么 Framework 里到处都是 Manager？

## 学习目标

学完本节后，你应该能够：

- 理解系统服务是 Android 管理系统能力的核心方式。
- 说清 App、Framework API、系统服务之间的关系。
- 知道 `system_server` 为什么值得重点关注。
- 明白第 11 章 demo 要观察什么。

## 第一部分：App 不能自己管理整个系统

一个 App 可以管理自己的页面、数据和业务状态。

但它不能独自决定：

- 哪个 Activity 应该启动。
- 哪个窗口可以显示。
- 哪个应用拥有哪些权限。
- 通知应该如何展示。
- 输入事件应该分发给谁。
- 安装包信息是否可信。

这些能力必须由系统统一管理，否则每个 App 都可以各自为政，安全、稳定性和用户体验都会失控。

## 第二部分：系统服务负责统一管理

Android 把很多核心能力收拢到系统服务里。

例如：

- `ActivityTaskManagerService`：管理 Activity 启动和任务栈。
- `WindowManagerService`：管理窗口。
- `PackageManagerService`：管理安装包、组件和权限声明。
- `NotificationManagerService`：管理通知。
- `InputMethodManagerService`：管理输入法。

这些服务大多运行在系统进程中，由系统统一维护。

## 第三部分：Manager 是 App 侧入口

你在 App 里通常不会直接调用 `ActivityTaskManagerService`。

你接触到的往往是：

- `ActivityManager`
- `WindowManager`
- `PackageManager`
- `NotificationManager`
- `ClipboardManager`

它们像 App 侧的服务窗口。你调用这些 Manager，背后可能通过 Binder 把请求送到系统服务。

## 第四部分：第 11 章 demo 怎么用

第 11 章示例工程是：

```text
examples/11-binder-system-service-lab/
```

它会做三件事：

- 展示 App 侧可见的系统服务入口。
- 使用一个独立进程的远程 Service 演示 Binder 通信。
- 用任务卡引导你写出“App API -> Binder -> 远程服务”的调用链笔记。

这个 demo 不是系统源码模拟器，而是一个“系统服务办事大厅观察台”。

## 本节小挑战

### 办事大厅入口题

请判断下面哪些能力应该由系统统一管理：

- 页面启动。
- App 内部字符串拼接。
- 通知展示。
- 安装包查询。
- Compose 按钮颜色。

## 本节实践任务

### 基础任务

- 打开第 11 章 demo。
- 阅读首页的系统服务任务卡。
- 找出页面中列出的三个 Manager。

### 进阶任务

- 选择一个你常用的系统能力。
- 写出它可能涉及的 App API、Manager、系统服务。
- 说明为什么这个能力不能只由 App 自己决定。

## 本节小结

系统服务是 Framework 的核心组织方式。App 通过 Framework API 和 Manager 请求系统能力，系统服务在更高权限、更统一的系统环境中处理请求。理解系统服务，Binder、SystemServer、AMS、WMS、PMS 这些后续主题才会真正连成一张图。
