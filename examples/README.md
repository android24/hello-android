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
```

## 设计原则

- 每一大章一个示例工程，避免每一小节都新建工程导致维护成本过高。
- 每个示例工程都包含独立 README，说明对应章节、知识点、运行方式和练习任务。
- 示例工程会随着课程推进逐步演进，先保持清晰、可读、可修改，再逐步引入复杂能力。

## 学习建议

建议先阅读对应章节文档，再打开示例工程运行。每完成一节课，都至少做一次小修改，例如修改文案、添加一个函数、调整一个布局或观察一次日志。

真正的掌握，不是看懂代码，而是能把它改成自己的代码。
