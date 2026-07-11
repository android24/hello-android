# 12.3 Task、返回栈与 Activity 调度对象

页面启动以后，用户按返回键应该回到哪里？

答案通常和 Task、返回栈以及系统内部的 Activity 记录有关。

## 本节剧情钩子

把一次 App 使用过程想象成一摞卡片。

你打开一个页面，就像往桌面上放一张新卡片。按返回键，就像拿走最上面的卡片，露出下面那张。

但系统不会只记“卡片长什么样”。它还会记录卡片属于哪个任务、启动方式是什么、状态是否可见、是否已经暂停。

## 本节定位

本节建立 Task 和返回栈的基本模型，并认识常见调度概念。

## 学习目标

学完本节后，你应该能够：

- 理解 Task 与返回栈的关系。
- 知道 Activity 启动会影响用户返回路径。
- 认识 ActivityRecord、Task 等源码阅读关键词。
- 能用 demo 观察页面启动与返回日志。

## 第一部分：Task 是用户任务

Task 可以理解为用户正在完成的一组页面。

例如用户打开课程 App：

```text
首页
  -> 课程详情
      -> 练习页面
```

这些页面可能属于同一个 Task。

用户按返回键时，系统按照栈内顺序回退。

## 第二部分：返回栈不是随便堆

普通启动会把新 Activity 放到栈顶。

简化模型：

```text
MainActivity
  -> DetailActivity
      -> PracticeActivity
```

按返回键：

```text
PracticeActivity 结束
  -> 回到 DetailActivity
      -> 再回到 MainActivity
```

这就是常见返回体验的基础。

## 第三部分：ActivityRecord 是系统记录

源码里经常会看到类似 `ActivityRecord` 的概念。

它不是你的 Activity 对象本体，而是系统用来描述一个 Activity 实例和状态的记录。

可以先这样理解：

```text
Activity 对象：运行在 App 进程里。
ActivityRecord：系统服务中描述这个 Activity 的调度记录。
```

一个在 App 进程，一个在系统服务侧。它们通过 Binder 和调度消息协作。

## 第四部分：Task 里还有更多规则

真实系统里的 Task 不只是简单数组。

它还要处理：

- 多窗口。
- 最近任务。
- 启动模式。
- Intent Flag。
- 后台启动限制。
- 任务归属和复用。

入门阶段先抓住：Task 决定用户视角下的一组页面和返回路径。

## 第五部分：demo 中怎么观察

第 12 章 demo 会记录：

- `MainActivity.onCreate()` / `onResume()`。
- `DetailActivity.onCreate()` / `onDestroy()`。
- `SingleTopActivity.onNewIntent()`。
- 启动按钮触发的操作。

你可以用这些日志推断页面栈发生了什么变化。

## 本节小挑战

### 卡片栈推理题

如果你从首页连续普通启动两次详情页，然后按返回键两次，会依次回到哪里？

请先写出你的预测，再用 demo 验证。

## 本节实践任务

### 基础任务

- 普通启动详情页。
- 按返回键回到首页。
- 记录生命周期日志顺序。

### 进阶任务

- 连续打开两次详情页。
- 观察每次是否都会创建新的 Activity。
- 用卡片栈画出你的理解。

## 本节小结

Task 和返回栈决定用户的页面回退体验。系统服务不会只看 Activity 对象本身，还会维护 ActivityRecord、Task 等调度信息。理解这层记录，才能解释为什么同样是启动页面，不同 launchMode 和 Flag 会产生不同结果。
