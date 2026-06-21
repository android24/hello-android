# 5.4 MVVM：用 ViewModel 承载 UI 状态

MVVM 是现代 Android 中非常常见的架构方式。它将页面展示和状态管理拆开，让 View 负责展示，ViewModel 负责准备和维护 UI 状态。

在 Compose 中，MVVM 和声明式 UI 非常契合：ViewModel 暴露状态，Composable 根据状态展示页面。

## 本节定位

本节是架构演进的第三站，也是现代 Android 项目中非常重要的一站。

它承接第 2 章的状态思想，也承接第 4 章的数据能力。

## 学习目标

学完本节后，你应该能够：

- 理解 View、ViewModel、Model 的职责。
- 知道 UI State 为什么重要。
- 理解 ViewModel 不应该直接持有 Android View。
- 用课程列表例子设计 `CourseListUiState`。
- 理解 MVVM 相比 MVP 的变化。

## 第一部分：MVVM 的角色

MVVM 包括：

- Model：数据和业务来源。
- View：界面展示，Compose 页面或传统 View。
- ViewModel：准备 UI 状态，处理页面事件。

课程列表可以这样分：

```text
View：CourseListScreen
ViewModel：CourseListViewModel
Model：LessonRepository
```

ViewModel 不直接命令 View “显示什么方法”，而是暴露状态。

## 第二部分：UI State

UI State 是页面在某一刻需要展示的全部信息：

```kotlin
data class CourseListUiState(
    val isLoading: Boolean = false,
    val lessons: List<LessonUiModel> = emptyList(),
    val errorMessage: String? = null
)
```

Composable 根据状态展示：

```kotlin
when {
    uiState.isLoading -> LoadingContent()
    uiState.errorMessage != null -> ErrorContent(uiState.errorMessage)
    else -> LessonList(uiState.lessons)
}
```

这比 MVP 中多个 `showXxx` 方法更贴合声明式 UI。

## 第三部分：ViewModel 处理事件

ViewModel 可以处理页面事件：

```kotlin
fun onRefreshClicked() {
    loadLessons()
}
```

页面只转发事件：

```kotlin
CourseListScreen(
    uiState = uiState,
    onRefreshClick = viewModel::onRefreshClicked
)
```

这让 View 更薄，状态变化也更集中。

## 第四部分：MVVM 的优点

MVVM 的优点：

- View 和状态管理分离。
- ViewModel 更容易测试。
- 与 Compose 状态模型契合。
- 适合配合 Repository。
- 能更好应对配置变化。

## 第五部分：MVVM 的问题

MVVM 也可能被滥用：

- ViewModel 变成万能类。
- 所有业务逻辑都塞进 ViewModel。
- UI State 设计混乱。
- Repository 边界不清。

所以后面需要 UseCase 和更清晰的分层。

## 本节小挑战

请为课程列表设计一个 `CourseListUiState`。它至少包含：

- 是否加载中。
- 课程列表。
- 错误提示。
- 是否来自缓存。

如果这个状态能完整描述页面，你就迈进了 MVVM 的门。

## 本节实践任务

### 基础任务

- 定义 `CourseListUiState`。
- 定义 `CourseListViewModel` 的方法草图。
- 让页面只接收 `uiState` 和事件回调。

### 进阶任务

- 思考 ViewModel 是否应该知道 Room 和 Retrofit 的具体实现。
- 将课程数据转换为 UI Model。
- 比较 MVVM 和 MVP 的页面更新方式。

## 本节小结

MVVM 把页面状态变成架构中的一等公民。对 Compose 来说，这是非常自然的一种组织方式。但复杂交互继续增多时，还需要更严格的数据流约束，下一节会进入 MVI / UDF。
