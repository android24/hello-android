# 示例工程：Activity 启动与任务栈实验室

## 对应章节

第12章 AMS / ATMS、Activity 启动与任务栈调度

## 工程目标

本工程用于配合第 12 章，把 `startActivity()`、launchMode、Intent Flag、任务栈和生命周期调度放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕三个问题展开：

- 一次页面启动为什么会走向系统服务？
- 不同 launchMode / Flag 为什么会改变返回栈？
- 如何用生命周期日志写出一次启动链路报告？

## 当前效果

运行后你会看到一个“第 12 章 Activity 启动与任务栈实验室”页面：

- `启动观察分数` 用 100 分制提示当前实验进度。
- `启动操作台` 提供普通启动、singleTop、CLEAR_TOP 等操作。
- `预期 vs 实际` 会在每次操作后显示你应该期待什么，以及日志最终证明了什么。
- `Activity 启动任务卡` 展示每个实验任务的完成状态。
- `当前观察状态` 展示当前页面、PID、线程、启动次数和 `onNewIntent` 次数。
- `任务栈推断图` 根据按钮操作和生命周期日志画出当前返回栈模型。
- `任务栈模型卡片` 解释 standard、singleTop、CLEAR_TOP、ActivityRecord / Task。
- `生命周期与启动轨迹` 记录每次启动、生命周期和返回路径。

这个 demo 不模拟 ATMS 源码，也无法直接读取系统内部真实 Task 数据；它会把 Activity 调度的结果变成可观察日志，再用一个“推断栈模型”帮助你从应用侧反推系统服务做了什么。

## 探索玩法

建议按下面顺序完成：

```text
普通启动详情页
  -> 再开一个详情页
      -> 按返回观察逐层退出
  -> 返回首页
      -> 打开 singleTop 页面
          -> 再次启动 singleTop 自己
              -> CLEAR_TOP 回首页
                  -> 写一份启动链路报告
```

最小报告可以写成：

```text
操作：Main -> SingleTop -> 再次启动 SingleTop
预期：第二次启动复用栈顶实例。
实际日志：出现 SingleTopActivity.onNewIntent。
返回栈推断：SingleTopActivity 没有重新创建。
源码入口：ActivityTaskManagerService, ActivityStarter, ActivityRecord
仍不确定：系统如何判断目标是否位于栈顶？
```

### 可玩性加分实验

如果你想更明显地感受 standard 模式，可以在 `DetailActivity` 里连续点击“再开一个详情页”：

```text
Main -> Detail #1 -> Detail #2 -> Detail #3
```

然后连续按返回。你会发现页面不是“一次回首页”，而是沿着任务栈一层层退回去。这个实验很适合理解重复页面、返回异常和业务里常见的“为什么按返回要退好多次”。

## 运行方式

1. 使用 Android Studio 打开 `examples/12-activity-task-launch-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `ActivityLaunchLab`。
5. 依次点击启动按钮，观察页面和日志变化。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
12-activity-task-launch-lab/
  app/
    src/main/java/com/helloandroid/launch/
      LaunchLabApplication.kt
      MainActivity.kt
      DetailActivity.kt
      SingleTopActivity.kt
      LaunchActions.kt
      LaunchLabState.kt
      LaunchTraceStore.kt
      LaunchLabScreen.kt
  quality/
    activity-launch-report-template.md
    activity-task-reading-notes.md
```

## 关键源码入口

- `MainActivity.kt`：任务栈入口，发起普通启动和 singleTop 启动。
- `DetailActivity.kt`：standard 模式详情页，用于观察重复创建和返回销毁。
- `SingleTopActivity.kt`：`singleTop` 页面，用于观察 `onNewIntent()`。
- `LaunchActions.kt`：集中封装普通启动、singleTop、CLEAR_TOP。
- `LaunchTraceStore.kt`：记录生命周期和启动操作。
- `LaunchLabScreen.kt`：展示实验页面、任务卡、分数、预期/实际对照、任务栈推断图和日志。
- `quality/activity-launch-report-template.md`：启动链路报告模板。
- `quality/activity-task-reading-notes.md`：ATMS / 任务栈源码阅读建议。

## 推荐对照的 AOSP 入口

```text
frameworks/base/core/java/android/app/Activity.java
frameworks/base/core/java/android/app/Instrumentation.java
frameworks/base/services/core/java/com/android/server/wm/ActivityTaskManagerService.java
frameworks/base/services/core/java/com/android/server/wm/ActivityStarter.java
frameworks/base/services/core/java/com/android/server/wm/ActivityRecord.java
frameworks/base/services/core/java/com/android/server/wm/Task.java
frameworks/base/core/java/android/app/ActivityThread.java
```

建议带着问题看：

```text
startActivity() 如何进入 Instrumentation？
ActivityStarter 如何处理启动请求？
singleTop 为什么会触发 onNewIntent？
CLEAR_TOP 如何影响目标之上的页面？
ActivityThread 什么时候接收生命周期调度？
```

## 练习任务

### 基础任务

- 普通启动详情页并返回。
- 在详情页连续启动多个详情页，观察重复页面如何进入返回栈。
- 打开 singleTop 页面，再次启动自己。
- 从子页面使用 CLEAR_TOP 回首页。
- 观察启动分数如何变化。
- 对照 `预期 vs 实际`，判断自己的推断是否被日志证实。
- 使用 `quality/activity-launch-report-template.md` 写一份短报告。

### 进阶任务

- 新增一个启动按钮，尝试不同 Intent Flag。
- 连续启动多个详情页，对照 `任务栈推断图` 手动画出返回栈。
- 给每次启动日志增加 Intent Flag 和 source 字段。
- 对照 AOSP 搜索 `ActivityStarter` 和 `ActivityRecord`。

## 通关目标

完成本工程后，你应该能说清楚：

- `startActivity()` 为什么会进入系统服务。
- standard 和 singleTop 的行为差异。
- `onNewIntent()` 与实例复用的关系。
- CLEAR_TOP 如何影响返回栈。
- 生命周期日志如何帮助你推断任务栈变化。
- Activity 对象和系统侧 ActivityRecord 不是同一件事。
