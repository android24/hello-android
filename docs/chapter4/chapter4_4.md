# 4.4 网络、本地缓存与离线可用小项目

第 4 章前面三节分别学习了网络请求、Room 数据库和 DataStore 配置存储。现在需要把这些能力合在一起，形成一个真实项目中经常出现的数据链路：从网络获取数据，保存到本地，离线时读取缓存，同时保存用户偏好。

本节是第 4 章的综合实践。

## 本节定位

本节属于 `04-network-room-datastore` 的收束小节。

它不是引入全新概念，而是把前三节串起来：

- 网络请求获取课程列表。
- Room 保存课程缓存。
- DataStore 保存用户配置。
- Compose 页面展示加载中、成功、失败、离线缓存状态。

## 学习目标

学完本节后，你应该能够：

- 设计一个简单的数据流。
- 区分远程数据、本地缓存和 UI 状态。
- 理解 Repository 的雏形职责。
- 使用 Room 保存网络数据。
- 使用 DataStore 保存配置。
- 在页面上展示离线缓存数据。
- 理解“先显示缓存，再刷新网络”的体验价值。

## 第一部分：目标项目

本节的小项目可以叫“课程列表缓存 Demo”。

它包含：

- 课程列表页面。
- 刷新按钮。
- 加载中状态。
- 请求失败提示。
- 本地缓存展示。
- 最近同步时间。
- 是否只显示未完成课程的配置。

这是很多真实 App 的基础形态。

## 第二部分：数据流设计

推荐先用这条简单数据流：

```text
页面启动
  -> 读取 Room 缓存并展示
  -> 请求网络课程列表
  -> 请求成功：写入 Room，更新同步时间
  -> 请求失败：继续展示缓存，提示错误
```

这条链路能提供更好的体验：

- 用户不用等网络完成才看到内容。
- 没网时也能看到旧数据。
- 网络成功后自动更新内容。

## 第三部分：Repository 雏形

虽然完整架构会在第 5 章讲，但第 4 章已经可以引入 Repository 的雏形。

Repository 负责协调数据来源：

```kotlin
class LessonRepository(
    private val api: LessonApi,
    private val lessonDao: LessonDao,
    private val preferences: UserPreferencesDataSource
)
```

它可以提供：

- 从网络刷新课程。
- 从 Room 读取课程。
- 保存最近同步时间。
- 读取用户偏好。

这样页面不需要关心数据到底来自网络、数据库还是配置存储。

## 第四部分：UI 状态设计

页面可以定义状态：

```kotlin
data class LessonListUiState(
    val isLoading: Boolean = false,
    val lessons: List<LessonUiModel> = emptyList(),
    val errorMessage: String? = null,
    val lastSyncText: String = "尚未同步",
    val showCompleted: Boolean = true
)
```

这种状态能同时表达：

- 是否加载中。
- 当前显示数据。
- 是否有错误。
- 用户配置。
- 同步信息。

第 2 章的状态与事件，在这里开始进入真实业务场景。

## 第五部分：离线优先体验

离线优先不是说永远不请求网络，而是不要让 App 在网络失败时变成空白。

更好的体验是：

```text
有缓存：先展示缓存，再提示刷新失败
无缓存：展示空状态和重试按钮
网络成功：更新缓存和页面
```

这会让 App 显得更稳定，也更接近真实产品。

## 第六部分：建议包结构

第 4 章示例工程可以按下面方式组织：

```text
app/src/main/java/com/helloandroid/data/
  remote/
    LessonApi.kt
    LessonDto.kt
  local/
    LessonEntity.kt
    LessonDao.kt
    AppDatabase.kt
  preferences/
    UserPreferences.kt
    UserPreferencesDataSource.kt
  repository/
    LessonRepository.kt
  ui/
    LessonListScreen.kt
    LessonListUiState.kt
```

这个结构已经开始接近真实工程。第 5 章会继续把它演进到更清晰的架构分层。

## 本节实践任务

### 基础任务

- 画出网络、Room、DataStore、UI 的数据流。
- 定义 `LessonDto`、`LessonEntity`、`LessonUiModel`。
- 写出 DTO 到 Entity 的转换函数。
- 写出 Entity 到 UI Model 的转换函数。

### Android 工程任务

- 创建第 4 章示例工程包结构。
- 先用假网络数据模拟接口返回。
- 将假数据保存到 Room。
- 页面启动时读取 Room 数据。
- 添加“刷新”按钮模拟重新拉取数据。
- 使用 DataStore 保存最近同步时间。

### 进阶任务

- 模拟网络失败，确认缓存仍然展示。
- 添加“只显示未完成课程”配置。
- 将该配置保存到 DataStore。
- 根据配置过滤课程列表。
- 思考 Repository 是否应该直接返回 UI 状态。

## 思考题

- 为什么先显示缓存比等待网络更友好？
- Repository 应该知道 UI 细节吗？
- DTO、Entity、UI Model 分层有什么价值？
- 如果网络数据和本地收藏状态冲突，应该如何处理？
- 离线缓存是否需要过期策略？

## 常见问题

### 第 4 章是否必须马上接真实接口？

不必须。先用假数据跑通数据流更重要。真实接口只是数据来源的一种实现。

### Repository 是第 5 章架构内容，为什么这里提前出现？

这里先把 Repository 当作“数据协调者”的朴素概念使用。第 5 章会系统讲它在架构分层中的位置。

### 离线缓存会不会显示过期数据？

会，所以需要同步时间、过期策略或刷新提示。第 4 章先建立基础能力，后续再讨论更完整策略。

## 本节小结

本节把第 4 章的能力合成了一条真实数据链路：网络获取数据，Room 保存缓存，DataStore 保存配置，Compose 根据 UI 状态展示页面。

学完这一章后，App 就不再只是静态界面或页面跳转 Demo，而是开始具备真实业务数据处理能力。下一章将进入应用架构，把这些数据能力整理成更清晰、更可维护的工程结构。
