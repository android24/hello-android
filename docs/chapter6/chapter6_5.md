# 6.5 StateFlow、SharedFlow 与 Compose 状态

Flow 负责表达数据流，但页面最终需要的是可以稳定观察的状态。现代 Android 中，`StateFlow` 和 `SharedFlow` 经常出现在 ViewModel 里。

本节要解决一个很实际的问题：哪些数据应该是 UI State，哪些事件不应该塞进 UI State？

假设课程刷新成功后，你想在页面上显示最新列表，同时弹出一句“同步完成”。列表应该一直存在，旋转屏幕后也还在；提示只应该出现一次，不应该因为页面重组又冒出来。

这就是状态和事件的分界线。`StateFlow` 负责“现在画面是什么样”，`SharedFlow` 负责“刚刚发生了什么事”。

## 本节定位

本节连接 Flow 和 Compose。

你会看到 ViewModel 如何把 Repository 的数据流整理成页面状态，再由 Compose 安全地收集和展示。

## 学习目标

学完本节后，你应该能够：

- 理解 `StateFlow` 适合表示页面状态。
- 理解 `SharedFlow` 适合表示一次性事件。
- 使用 `stateIn` 把 Flow 转成 StateFlow。
- 在 Compose 中使用 `collectAsStateWithLifecycle`。
- 区分状态和事件。

## 第一部分：StateFlow 表示当前状态

`StateFlow` 有一个当前值。

这很适合表示页面状态：

```kotlin
data class LessonUiState(
    val isLoading: Boolean = false,
    val lessons: List<Lesson> = emptyList(),
    val errorMessage: String? = null
)
```

ViewModel 中可以这样暴露：

```kotlin
private val _uiState = MutableStateFlow(LessonUiState())
val uiState: StateFlow<LessonUiState> = _uiState
```

UI 只读 `uiState`，事件通过函数进入 ViewModel。

## 第二部分：stateIn

Repository 往往暴露的是普通 Flow：

```kotlin
val lessonsFlow: Flow<List<Lesson>>
```

ViewModel 可以用 `stateIn` 转成 StateFlow：

```kotlin
val uiState: StateFlow<LessonUiState> = lessonsFlow
    .map { lessons -> LessonUiState(lessons = lessons) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LessonUiState()
    )
```

这样 Compose 订阅时就能拿到一个稳定的当前状态。

## 第三部分：Compose 如何收集

在 Compose 中推荐使用：

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

它会根据生命周期安全收集，避免页面不可见时还无意义地持续更新 UI。

这比直接 `collect` 更适合 Compose 页面。

## 第四部分：SharedFlow 表示一次性事件

有些事情不是状态，而是事件：

- 弹 Toast。
- 导航到详情页。
- 显示一次 Snackbar。
- 触发一次震动反馈。

这些不适合长期放在 UI State 里，否则可能发生重复消费。

可以用 `SharedFlow`：

```kotlin
private val _events = MutableSharedFlow<LessonEvent>()
val events: SharedFlow<LessonEvent> = _events
```

然后在 ViewModel 中发出：

```kotlin
_events.emit(LessonEvent.ShowMessage("刷新成功"))
```

## 第五部分：状态和事件的边界

一个简单判断：

```text
旋转屏幕后仍然应该存在的是状态。
只应该发生一次的是事件。
```

例如：

- 当前课程列表：状态。
- 是否加载中：状态。
- 错误文案：可以是状态，也可以是事件，取决于设计。
- 跳转详情页：事件。
- Toast 提示：事件。

边界清晰后，页面行为会更可预测。

## 本节小挑战

请判断下面内容是状态还是事件：

- 当前课程列表。
- 当前选中的 Tab。
- 点击课程后跳转详情页。
- 刷新成功弹出提示。
- 是否显示已完成课程。

这类判断会直接影响 ViewModel 的设计质量。

## 本节实践任务

### 基础任务

- 定义一个 `LessonUiState`。
- 使用 `MutableStateFlow` 保存页面状态。
- 在 Compose 中用 `collectAsStateWithLifecycle` 收集。
- 点击按钮后更新 StateFlow。

### 进阶任务

- 定义一个 `LessonEvent`。
- 用 `SharedFlow` 发送一次性消息。
- 思考错误提示应该放在 StateFlow 还是 SharedFlow。

## 本节小结

StateFlow 适合承载页面当前状态，SharedFlow 适合传递一次性事件。它们让 ViewModel 和 Compose 之间有了更清晰的契约：UI 观察状态，用户操作进入 ViewModel，事件被明确消费。
