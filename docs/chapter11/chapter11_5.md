# 11.5 SystemServer：系统服务从这里集结

理解系统服务，绕不开 SystemServer。

很多核心系统服务都在这个进程中启动和运行。它像 Android Framework 世界里的中枢工作区。

## 本节剧情钩子

如果 ServiceManager 是通讯录，那么 SystemServer 就像办事大厅正式开门前的后台办公区。

窗口人员在这里集合，服务在这里启动，规则在这里准备。等大厅开门后，App 才能通过各种 Manager 去提交请求。

## 本节定位

本节建立 SystemServer 的入门视角。

## 学习目标

学完本节后，你应该能够：

- 知道 SystemServer 是系统服务的重要运行环境。
- 理解系统服务启动、注册、等待调用的大致过程。
- 知道为什么 AMS、WMS、PMS 经常和 SystemServer 一起出现。
- 能带着问题阅读 `SystemServer.java`。

## 第一部分：SystemServer 是什么

SystemServer 是 Android 系统启动过程中创建的重要进程。

它负责启动大量核心系统服务。

例如：

- Activity 相关服务。
- Window 相关服务。
- Package 相关服务。
- Power、Input、Notification 等服务。

这些服务共同支撑 Android 的应用管理、窗口显示、包管理、输入和系统行为。

## 第二部分：服务启动不是一锅端

SystemServer 启动服务通常有阶段和顺序。

可以先粗略理解为：

```text
启动基础服务
  -> 启动核心服务
      -> 启动其他系统服务
          -> 进入系统可用状态
```

不同 Android 版本细节会变化，但“按阶段启动服务”是重要思路。

## 第三部分：为什么顺序重要

系统服务之间有依赖。

例如，包管理、Activity 管理、窗口管理、输入管理都可能互相协作。

如果基础服务没准备好，上层服务就可能无法正常工作。

这也是为什么读 SystemServer 时不要只看单个类名，而要关注启动阶段。

## 第四部分：SystemServer 和 Binder

SystemServer 中的服务需要被其他进程调用。

因此它们会暴露 Binder 接口或通过 Framework 封装提供访问入口。

简化链路：

```text
SystemServer 创建系统服务
  -> 服务注册
      -> App 查询服务
          -> App 通过 Binder 调用服务
```

这条线把 SystemServer、ServiceManager、Binder 和 Manager 连接了起来。

## 第五部分：读源码时看什么

读 `SystemServer.java` 时，建议先找：

- 服务在哪个阶段启动。
- 服务对象如何创建。
- 服务是否注册。
- 服务和哪些其他服务有依赖。
- 关键日志或异常处理说明了什么。

不要第一轮就追所有服务细节，否则很容易被数量淹没。

## 本节小挑战

### 后台集合题

请回答：

- 为什么很多系统服务要集中在 SystemServer 中启动？
- 为什么服务启动顺序不能随便打乱？
- SystemServer 和 Binder 有什么关系？

## 本节实践任务

### 基础任务

- 打开 AOSP 中的 `SystemServer.java`。
- 搜索 `startBootstrapServices`。
- 搜索 `startCoreServices`。
- 搜索 `startOtherServices`。

### 进阶任务

- 选择一个系统服务名。
- 记录它在哪个阶段被启动。
- 写下它可能负责的系统能力。

## 本节小结

SystemServer 是理解 Android 系统服务的关键入口。很多服务在这里启动、注册，并通过 Binder 等机制等待客户端调用。读懂 SystemServer 的启动阶段，你就不再只是背 AMS、WMS、PMS 的名字，而是在看系统如何把它们组织起来。
