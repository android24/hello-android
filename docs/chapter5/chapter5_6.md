# 5.6 Repository、UseCase 与 Clean Architecture

前面几节主要关注页面层如何组织。现在要继续向下看：数据从哪里来？业务规则放哪里？网络、数据库和页面之间应该如何隔离？

这就需要 Repository、UseCase 和 Clean Architecture。

## 本节定位

本节是第 5 章从“页面架构”走向“应用架构”的关键。

它会承接第 4 章的数据能力，把网络、Room、DataStore 和 UI 状态放进更清晰的分层中。

## 学习目标

学完本节后，你应该能够：

- 理解 Repository 的职责。
- 理解 UseCase 的职责。
- 知道 Data、Domain、UI 三层的基本边界。
- 判断哪些逻辑不应该写在 ViewModel。
- 理解 Clean Architecture 的价值和成本。

## 第一部分：Repository 负责什么

Repository 负责协调数据来源。

它可以决定：

- 从网络拿数据。
- 从 Room 读缓存。
- 写入本地数据库。
- 合并远程数据和本地状态。
- 暴露给上层一个稳定的数据接口。

例如：

```kotlin
class LessonRepository(
    private val remoteDataSource: LessonRemoteDataSource,
    private val localDataSource: LessonLocalDataSource
) {
    suspend fun refreshLessons()
    fun observeLessons(): Flow<List<Lesson>>
}
```

Repository 不应该变成所有逻辑都往里塞的万能类。

## 第二部分：UseCase 负责什么

UseCase 表示一个业务动作。

例如：

- 获取课程列表。
- 刷新课程缓存。
- 收藏课程。
- 切换课程完成状态。

```kotlin
class RefreshLessonsUseCase(
    private val repository: LessonRepository
) {
    suspend operator fun invoke() {
        repository.refreshLessons()
    }
}
```

UseCase 的价值是把业务意图命名出来，让 ViewModel 更薄、更清晰。

## 第三部分：三层结构

常见分层：

```text
UI Layer
  Compose / ViewModel / UI State

Domain Layer
  UseCase / Domain Model / Business Rule

Data Layer
  Repository / Remote / Local / DTO / Entity
```

依赖方向应该尽量从外向内：

```text
UI -> Domain -> Data abstraction
```

具体实现通过接口和依赖注入组织。

## 第四部分：模型分层

常见模型：

- DTO：接口数据。
- Entity：数据库表。
- Domain Model：业务模型。
- UI Model：页面模型。

它们不一定都要在小项目中完整存在，但你要知道它们为什么会出现。

当项目变大时，模型分层可以避免接口变化、数据库变化、页面变化互相牵连。

## 第五部分：Clean Architecture 的边界

Clean Architecture 的价值：

- 业务逻辑更独立。
- 测试更容易。
- 模块边界更清楚。
- 外部实现可以替换。

成本：

- 文件更多。
- 抽象更多。
- 对小项目可能过重。
- 团队需要统一规则。

所以它不是银弹，而是工具。

## 本节小挑战

请把“刷新课程列表”拆成三个层次：

```text
ViewModel：
UseCase：
Repository：
```

每层只写一句职责说明。如果写不清楚，说明边界还需要继续打磨。

## 本节实践任务

### 基础任务

- 定义 `LessonRepository` 接口。
- 定义 `RefreshLessonsUseCase`。
- 让 ViewModel 调用 UseCase。
- 思考 UseCase 是否应该知道 Retrofit 或 Room。

### 进阶任务

- 为 DTO、Entity、UI Model 写转换函数。
- 画出 UI、Domain、Data 的依赖关系。
- 判断当前项目是否真的需要完整 Clean Architecture。

## 本节小结

Repository、UseCase 和 Clean Architecture 的核心，是让数据来源、业务动作和页面状态各自站在合适的位置。架构不是堆文件，而是管理变化。
