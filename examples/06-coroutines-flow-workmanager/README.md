# 示例工程：Coroutines、Flow 与 WorkManager

## 对应章节

第6章 Kotlin Coroutines、Flow 与后台任务

## 工程目标

本工程用于配合第 6 章，把协程、Flow、StateFlow、SharedFlow、错误处理和 WorkManager 放到同一个“课程同步中心”里理解。

它不是为了堆 API，而是帮助你看清楚：一次刷新、一次状态变化、一次失败提示、一次后台同步，应该分别放在哪里。

## 当前效果

运行后你会看到一个“课程同步中心”：

- 点击 `Thread 模拟任务`，观察传统后台线程如何把结果切回主线程。
- 点击 `线程池并发任务`，观察多个任务如何复用线程池。
- 点击 `协程刷新课程`，用 `viewModelScope`、`withTimeout` 和 `suspend` 完成一次刷新。
- 点击 `模拟失败刷新`，观察错误如何进入 UI State 与 SharedFlow 事件。
- 切换 `仅 Wi-Fi 同步`，观察 Flow 如何驱动页面状态变化。
- 点击 `启动后台同步`，用 WorkManager 入队一次可靠后台任务，并观察任务状态。

这个示例使用内存版缓存和偏好数据源，重点放在第 6 章的异步模型本身。后续可以把内存数据源替换为第 4 章里的 Room 和 DataStore。

## 运行方式

1. 使用 Android Studio 打开 `examples/06-coroutines-flow-workmanager`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 依次点击页面上的 Thread、线程池、协程刷新、失败刷新、WorkManager 按钮。
5. 对照源码观察每种异步模型负责的边界。

## 工程结构

```text
06-coroutines-flow-workmanager/
  app/
    src/main/java/com/helloandroid/async/
      data/
        legacy/
        local/
        model/
        preferences/
        remote/
        repository/
      worker/
        SyncCourseWorker.kt
        BackgroundSyncScheduler.kt
      ui/
        SyncCenterScreen.kt
        SyncCenterViewModel.kt
        SyncCenterUiState.kt
        SyncEvent.kt
```

## 覆盖知识点

- 进程与线程
- Java Thread
- 线程池与 ExecutorService
- Handler 与主线程切换
- `suspend` 函数
- `viewModelScope`
- `Dispatchers`
- 结构化并发
- 取消与超时
- Flow
- StateFlow
- SharedFlow
- `combine` / `map` / `catch` / `retry`
- WorkManager
- 后台同步状态观察

## 练习任务

### 基础任务

- 用 `Thread` 模拟一次耗时任务，并观察为什么不能直接更新 UI。
- 用线程池执行多个模拟请求，理解线程复用的价值。
- 再用协程重写同样逻辑，对比代码可读性和取消方式。
- 用协程实现手动刷新课程。
- 用 StateFlow 暴露页面状态。
- 用 Flow 观察课程缓存和用户配置。
- 用 SharedFlow 发送一次性提示。
- 给刷新增加错误处理。

### 进阶任务

- 给刷新增加超时。
- 使用 `combine` 合并 Room 和 DataStore。
- 使用 WorkManager 模拟后台同步课程进度。
- 观察 WorkInfo 状态并展示到页面。
- 写一张异步链路图，说明每一层的职责。

## 通关目标

完成本章后，你应该能说清楚：

- 协程、Flow、StateFlow、SharedFlow、WorkManager 分别解决什么问题。
- 为什么 ViewModel 适合启动页面级协程。
- 为什么 Repository 更适合暴露数据流而不是直接操作 UI。
- 错误、取消和超时为什么是异步代码的正常组成部分。
- 什么任务应该立即执行，什么任务应该交给后台可靠调度。
