# 6.7 WorkManager 与可靠后台任务

协程适合当前代码中的异步执行，Flow 适合持续变化的数据流。但有些任务需要更可靠：即使 App 退出、设备重启、网络稍后才恢复，也希望任务最终有机会执行。

这时就轮到 WorkManager。

比如用户在地铁里学完一节课，App 记录了学习进度，但此时网络很差。用户关掉 App，几分钟后手机重新连上网络。这个上传动作不适合绑在某个页面上，也不应该因为页面关闭就彻底消失。

WorkManager 像一个交给系统保管的任务单：现在条件不合适没关系，等网络、电量和系统调度都允许时，再尽量可靠地执行。

## 本节定位

本节引入第 6 章的后台任务能力。

它不替代协程和 Flow，而是解决另一类问题：系统托管的、可约束的、可靠执行的后台工作。

## 学习目标

学完本节后，你应该能够：

- 知道 WorkManager 适合什么任务。
- 区分协程、前台任务和 WorkManager。
- 理解 OneTimeWorkRequest 和 PeriodicWorkRequest。
- 设置网络、充电等执行约束。
- 设计一个课程进度后台同步任务。

## 第一部分：什么时候需要 WorkManager

适合 WorkManager 的任务：

- 上传学习进度。
- 同步离线笔记。
- 网络恢复后补发日志。
- 定期刷新课程缓存。
- 在设备空闲或充电时执行较重任务。

不适合 WorkManager 的任务：

- 页面点击后立刻更新 UI。
- 正在播放音乐这种持续前台任务。
- 必须精确到秒执行的任务。
- 用户正在等待结果的短任务。

简单区分：

```text
用户正在等：协程
页面持续观察：Flow
可以稍后可靠执行：WorkManager
```

## 第二部分：Worker

WorkManager 的任务通常写成 Worker：

```kotlin
class SyncCourseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            // 同步课程进度
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

`CoroutineWorker` 允许在 `doWork` 中使用挂起函数，很适合现代 Android。

## 第三部分：一次性任务

一次性任务使用 `OneTimeWorkRequest`：

```kotlin
val request = OneTimeWorkRequestBuilder<SyncCourseWorker>()
    .build()

WorkManager.getInstance(context).enqueue(request)
```

例如用户离线完成课程后，可以把“上传学习进度”交给 WorkManager。

## 第四部分：约束条件

后台任务不一定任何时候都适合执行。

可以设置约束：

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()
```

含义是：只有网络连接时才执行。

还可以限制：

- 需要充电。
- 需要设备空闲。
- 需要足够存储空间。

这让后台任务更尊重系统资源。

## 第五部分：周期任务

周期任务使用 `PeriodicWorkRequest`：

```kotlin
val request = PeriodicWorkRequestBuilder<SyncCourseWorker>(
    6,
    TimeUnit.HOURS
).build()
```

适合定期同步课程缓存、上传统计等。

但要注意：WorkManager 不是精确闹钟，系统会根据电量、网络和调度策略安排实际执行时间。

## 第六部分：观察任务状态

WorkManager 可以观察任务状态：

```kotlin
workManager.getWorkInfoByIdFlow(request.id)
```

这样页面可以展示：

- 排队中。
- 执行中。
- 成功。
- 失败。
- 等待重试。

这又回到了 Flow 的世界：后台任务状态也是一条数据流。

## 本节小挑战

请判断下面任务是否适合 WorkManager：

- 点击刷新课程列表。
- 每天同步一次学习进度。
- 用户离线收藏课程，联网后上传。
- 输入搜索关键字后请求结果。
- App 启动时读取本地配置。

## 本节实践任务

### 基础任务

- 设计一个 `SyncCourseWorker`。
- 设置网络连接约束。
- 点击按钮后入队一次性同步任务。
- 在页面上展示任务状态。

### 进阶任务

- 设计周期同步任务。
- 观察 WorkInfo 状态变化。
- 思考后台任务失败时应该 retry 还是 failure。
- 思考 WorkManager 是否应该直接操作 UI。

## 本节小结

WorkManager 让后台任务不再依赖页面是否还活着。它适合那些可以延迟、需要可靠、受系统约束管理的工作。协程、Flow 和 WorkManager 分工清晰后，Android App 的异步体系就完整了很多。
