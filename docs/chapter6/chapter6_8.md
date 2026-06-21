# 6.8 综合实践：课程同步中心

第 6 章最后，我们把协程、Flow、StateFlow、错误处理和 WorkManager 放回一个完整功能里：课程同步中心。

这个功能不需要很大，但它要体现真实工程的异步能力：能刷新、能观察、能取消、能失败、能重试，也能把不急的任务交给后台。

## 本节定位

本节是第 6 章的综合实践。

它会承接前几章已有能力：

- 第 4 章的数据链路。
- 第 5 章的架构边界。
- 第 6 章的异步与数据流。

最终目标是让课程 App 从“能请求数据”走向“能管理同步”。

## 学习目标

学完本节后，你应该能够：

- 设计一个课程同步功能的数据流。
- 在 ViewModel 中组织协程和 StateFlow。
- 用 Flow 组合课程缓存和同步配置。
- 处理刷新失败、超时和重试。
- 判断哪些任务应该交给 WorkManager。
- 用一张图解释异步链路。

## 第一部分：目标功能

课程同步中心包含：

- 当前课程缓存列表。
- 手动刷新按钮。
- 同步状态。
- 错误提示。
- 最近同步时间。
- 是否仅在 Wi-Fi 下同步。
- 后台同步任务入口。

这不是一个复杂页面，但已经足够覆盖真实项目中的大部分异步问题。

## 第二部分：推荐数据流

可以这样设计：

```text
Room.observeLessons()
DataStore.observePreferences()
        |
        v
Repository.combine(...)
        |
        v
ViewModel.stateIn(...)
        |
        v
Compose collectAsStateWithLifecycle()
```

用户点击刷新：

```text
点击刷新
  -> ViewModel.launch
  -> Repository.refreshLessons()
  -> 网络请求
  -> 写入 Room
  -> 更新 DataStore 最近同步时间
  -> Room / DataStore Flow 自动发出新状态
```

这个设计的关键是：页面不直接拼数据，数据变化由 Flow 驱动。

## 第三部分：UI State

可以定义：

```kotlin
data class SyncCenterUiState(
    val isRefreshing: Boolean = false,
    val lessons: List<LessonUiModel> = emptyList(),
    val lastSyncText: String = "尚未同步",
    val onlyWifiSync: Boolean = false,
    val message: String? = null
)
```

它能描述页面当前画面。

一次性提示可以用 SharedFlow：

```kotlin
sealed interface SyncEvent {
    data class ShowMessage(val text: String) : SyncEvent
}
```

## 第四部分：Repository 职责

Repository 可以负责：

- 观察本地课程缓存。
- 观察用户同步偏好。
- 刷新远程课程。
- 将远程数据写入 Room。
- 更新最近同步时间。

Repository 不应该直接操作 Compose，也不应该直接弹 Toast。

## 第五部分：WorkManager 职责

WorkManager 可以负责：

- 仅在网络可用时同步学习进度。
- 定期刷新课程缓存。
- App 退出后继续上传离线操作。

它不适合负责用户正在等待的即时刷新。

也就是说：

```text
手动刷新：ViewModel + 协程
可靠后台同步：WorkManager
页面持续展示：Flow + StateFlow
```

## 第六部分：复盘表

| 能力 | 推荐工具 | 原因 |
| :-- | :-- | :-- |
| 点击刷新课程 | 协程 | 一次性异步任务 |
| 观察课程缓存 | Flow | 数据会持续变化 |
| 页面状态 | StateFlow | UI 需要当前值 |
| Toast / Snackbar | SharedFlow | 一次性事件 |
| 上传离线进度 | WorkManager | 需要可靠后台执行 |
| 超时与失败 | try/catch + Result | 让错误进入可控状态 |

## 本节小挑战

请画出“课程同步中心”的完整链路，并标出每一层职责：

- UI
- ViewModel
- Repository
- Room
- DataStore
- WorkManager

如果这张图画清楚，第 6 章就真正落地了。

## 本节实践任务

### 基础任务

- 定义 `SyncCenterUiState`。
- 用 StateFlow 暴露页面状态。
- 点击刷新时启动协程。
- 刷新失败时保留旧缓存。
- 展示最近同步时间。

### 进阶任务

- 增加 `SyncEvent`。
- 使用 SharedFlow 发送一次性提示。
- 为刷新增加超时。
- 增加 WorkManager 后台同步入口。
- 用 README 写下你的异步链路说明。

## 本章小结

第 6 章的重点不是记住某个 API，而是建立异步判断力。

当你看到一个需求时，能判断它是一次性任务、持续数据流、页面状态、一次性事件，还是后台可靠任务。这种判断力，会直接决定你的代码是否清晰、稳定、可维护。
