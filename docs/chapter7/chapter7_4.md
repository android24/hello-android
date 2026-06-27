# 7.4 ViewModel、Repository 与数据源注入

前面几节讲了 Hilt 的基本注解。本节把它放回课程 App 的主线：ViewModel、UseCase、Repository、RemoteDataSource、LocalDataSource 应该如何被装配。

这一步开始，依赖注入不再是单独的技术点，而是架构分层的连接方式。

本节可以当作一次“给 ViewModel 减负”的整理。过去 ViewModel 可能既要关心按钮点击，又要知道接口、数据库、缓存策略；整理之后，它只负责表达页面意图，真正的数据协作交给 UseCase、Repository 和 DataSource。

## 本节定位

本节连接第 5 章架构分层、第 6 章异步数据流和第 7 章 Hilt。

目标是让你看懂一条完整注入链路：

```text
CourseViewModel
  -> GetCoursesUseCase
      -> CourseRepository
          -> CourseRemoteDataSource
          -> CourseLocalDataSource
```

## 学习目标

学完本节后，你应该能够：

- 设计 ViewModel 到数据源的依赖链。
- 使用 Hilt 注入 UseCase 和 Repository。
- 理解接口注入带来的替换能力。
- 知道数据源不应该直接进入 UI。
- 为测试替换依赖预留空间。

## 第一部分：推荐依赖链

课程列表可以这样组织：

```kotlin
@HiltViewModel
class CourseViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase
) : ViewModel()
```

UseCase：

```kotlin
class GetCoursesUseCase @Inject constructor(
    private val repository: CourseRepository
)
```

Repository：

```kotlin
class CourseRepositoryImpl @Inject constructor(
    private val remote: CourseRemoteDataSource,
    private val local: CourseLocalDataSource
) : CourseRepository
```

这样每一层只关心下一层的能力，不关心它如何创建。

## 第二部分：为什么不要让 ViewModel 直接拿 Api

如果 ViewModel 直接依赖 `CourseApi`：

```kotlin
class CourseViewModel(
    private val api: CourseApi
)
```

短期看很快，长期会有问题：

- UI 层知道了网络细节。
- 缓存策略不好放。
- 测试需要模拟接口细节。
- 后续加入 Room / DataStore 会让 ViewModel 膨胀。

更好的做法是让 ViewModel 依赖 UseCase 或 Repository。

判断 ViewModel 是否开始变重，有一个很直观的信号：你读它的时候，已经不像在读页面状态，而像在读一份网络层和数据库层的混合说明书。这个时候，就该把职责往下拆了。

## 第三部分：数据源拆分

Repository 可以协调多个数据源：

```text
CourseRepositoryImpl
  -> CourseRemoteDataSource
  -> CourseLocalDataSource
  -> UserPreferencesDataSource
```

Remote 负责网络，Local 负责数据库，Preferences 负责配置。

Repository 负责决定：

- 先读缓存还是先请求网络。
- 网络成功后是否写入本地。
- 网络失败时是否保留旧数据。
- 配置变化如何影响结果。

这延续了第 4 章和第 6 章的数据链路。

## 第四部分：接口让替换更容易

定义接口：

```kotlin
interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>
    suspend fun refreshCourses()
}
```

实现：

```kotlin
class CourseRepositoryImpl @Inject constructor(...) : CourseRepository
```

测试时可以替换：

```kotlin
class FakeCourseRepository : CourseRepository
```

这就是依赖注入真正服务测试和工程扩展的地方。

## 第五部分：注入不是越多越好

依赖注入不是把所有东西都塞进 Hilt。

不适合注入：

- 简单 data class。
- 页面临时变量。
- Composable 内部的纯展示状态。
- 一次性局部对象。

适合注入：

- Repository。
- UseCase。
- DataSource。
- Retrofit / Room / DataStore。
- 统一的日志、配置、监控对象。

判断标准是：它是否跨层使用，是否需要替换，是否需要生命周期管理。

## 本节小挑战

请为“课程同步中心”画出依赖链：

```text
SyncCenterViewModel
  -> ?
      -> ?
```

再思考：哪些对象应该由 Hilt 注入，哪些对象只应该在函数内部创建？

## 本节实践任务

### 基础任务

- 定义 `CourseRepository` 接口。
- 定义 `CourseRepositoryImpl`。
- 定义 `GetCoursesUseCase`。
- 将 ViewModel 的依赖改为 UseCase。

### 进阶任务

- 用 Hilt 绑定 Repository 接口和实现。
- 为 Repository 写一个 Fake 实现。
- 思考 ViewModel 测试时应该替换哪一层。

## 本节小结

Hilt 的价值不只是少写 new，而是让架构边界真正可替换。ViewModel 不直接知道网络和数据库，Repository 不直接知道 UI，UseCase 承载业务动作。对象由 Hilt 装配，职责由架构约束。
