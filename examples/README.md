# 示例工程

这里存放课程配套示例工程。每个大章节对应一个独立示例工程，用于承接该章节的 README 内容，并让学习者形成完整实践闭环。

## 示例工程玩法

每个示例工程都不是一次性看完的答案，而是一块可以继续改造的练习场。建议你用下面的节奏学习：

```text
先运行 -> 看页面 -> 找入口 -> 改一行 -> 再运行 -> 记录变化
```

如果你不知道从哪里改，就从最小的地方开始：改一段文案、改一个颜色、加一条日志、让按钮多显示一个状态。小改动会让工程从“别人的代码”变成“你的代码”。

## 工程目录

```text
examples/
  01-kotlin-and-android-basics/
  02-compose-ui-basics/
  03-activity-lifecycle-navigation/
  04-network-room-datastore/
  05-android-architecture-evolution/
  06-coroutines-flow-workmanager/
  07-hilt-modularization-engineering/
  08-testing-quality-delivery/
  09-performance-stability-lab/
  10-framework-source-walkthrough/
  11-binder-system-service-lab/
  12-activity-task-launch-lab/
  13-window-display-lab/
  14-input-event-dispatch-lab/
  15-rendering-frame-lab/
  16-package-manager-lab/
  17-resource-system-lab/
  18-code-loading-lab/
  19-process-zygote-lab/
  20-stability-diagnosis-lab/
  21-background-scheduling-lab/
  22-storage-access-lab/
```

## 课程闯关地图

示例工程可以按 5 段推进。不要把它们当成孤立 demo，而要把它们当成一条逐步升级的工程路线。

| 阶段 | 示例工程 | 你在练什么 |
| --- | --- | --- |
| 入门与 UI | `01` - `03` | 跑通 App、写 Compose 页面、理解 Activity 和导航 |
| 数据与架构 | `04` - `08` | 网络、本地数据、协程、架构、依赖注入、测试 |
| 质量与性能 | `09` - `10` | 性能、稳定性、源码阅读入口 |
| Framework 主线 | `11` - `18` | Binder、AMS、WMS、Input、渲染、PMS、资源、ClassLoader |
| 系统工程实验室 | `19` - `22` | 进程、稳定性、后台调度、存储访问和事故诊断 |

## 推荐玩法

每个示例工程都建议按同一套节奏完成：

```text
第一步：跑起来
  -> 确认工程能启动，找到主页面和任务板

第二步：点一遍
  -> 触发核心实验，观察页面状态和事件时间线

第三步：查证据
  -> 按 README 里的 adb / logcat / dumpsys 命令取证

第四步：改一点
  -> 改文案、加字段、补日志、加一个按钮或一个状态

第五步：写复盘
  -> 用章节诊断报告模板写出现象、证据、根因、修复和回归
```

## 通关称号

你可以给自己设置几个阶段性称号，让学习过程更像一条路线，而不是一堆文件夹。

| 通关范围 | 称号 | 通关标志 |
| --- | --- | --- |
| `01` - `03` | App 入门者 | 能独立改一个页面并解释 Activity 生命周期 |
| `04` - `08` | 工程搭建者 | 能把数据、异步、架构和测试串起来 |
| `09` - `10` | 质量守门员 | 能用证据解释性能或稳定性问题 |
| `11` - `18` | Framework 调查员 | 能从一次交互追到系统服务和源码入口 |
| `19` - `22` | 系统诊断工程师 | 能写出进程、稳定性、后台、存储事故诊断报告 |

## 当前重点实验室

如果你已经学到第 20 章以后，优先把下面几个实验室跑通：

- `20-stability-diagnosis-lab/`：学习 ANR、Crash、Watchdog 和稳定性证据链。
- `21-background-scheduling-lab/`：学习 Foreground Service、WorkManager、Alarm 和后台限制。
- `22-storage-access-lab/`：学习 filesDir、cacheDir、MediaStore、Photo Picker、SAF、FileProvider 和存储事故排查。

这三章组合起来，会形成一个很接近真实工作的闭环：

```text
App 出问题
  -> 先判断是稳定性、后台调度，还是存储边界
      -> 找系统证据
          -> 写诊断报告
              -> 做修复和回归
```

## 设计原则

- 每一大章一个示例工程，避免每一小节都新建工程导致维护成本过高。
- 每个示例工程都包含独立 README，说明对应章节、知识点、运行方式和练习任务。
- 示例工程会随着课程推进逐步演进，先保持清晰、可读、可修改，再逐步引入复杂能力。

## 学习建议

建议先阅读对应章节文档，再打开示例工程运行。每完成一节课，都至少做一次小修改，例如修改文案、添加一个函数、调整一个布局或观察一次日志。

真正的掌握，不是看懂代码，而是能把它改成自己的代码。
