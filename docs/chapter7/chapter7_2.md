# 7.2 Hilt 入门：从手动创建到自动装配

上一节我们知道了依赖注入要解决什么问题。本节开始进入 Hilt。

如果说手动创建对象像自己接电线，Hilt 更像一套配电系统：你告诉它哪里需要电、哪里提供电，它负责把线路接好。

本节先看最基础的 Hilt 使用方式。

如果你从老 Android 项目过来，可能会问：以前 Butter Knife 也用了注解，现在 Hilt 也用了注解，它们是不是一回事？

答案是否定的。Butter Knife 的注解通常标在 View 字段和点击方法上，目标是少写 UI 绑定代码；Hilt 的注解标在 Application、Activity、ViewModel、构造函数和 Module 上，目标是装配对象依赖。它们都用注解处理，但服务的是两层完全不同的问题。

本节的剧情很简单：把一个原本需要手动拼装的对象链，交给 Hilt 自动接线。你仍然要设计好每个对象的职责，但不再需要在每个页面里重复写一串创建代码。

## 本节定位

本节属于 Hilt 入门部分。

目标不是一次讲完所有注解，而是先跑通最核心的链路：

```text
Application 启用 Hilt
Activity 接入 Hilt
ViewModel 由 Hilt 创建
Repository 通过构造函数注入
```

## 学习目标

学完本节后，你应该能够：

- 知道 `@HiltAndroidApp` 的作用。
- 知道 `@AndroidEntryPoint` 的作用。
- 知道 `@HiltViewModel` 的作用。
- 使用 `@Inject constructor` 创建可注入对象。
- 理解 Hilt 如何把 Repository 注入 ViewModel。

## 第一部分：启用 Hilt

使用 Hilt 通常需要一个 Application：

```kotlin
@HiltAndroidApp
class CourseApplication : Application()
```

`@HiltAndroidApp` 会让 Hilt 生成应用级依赖容器。

可以先理解为：从这里开始，Hilt 知道这个 App 要使用依赖注入。

这和 Butter Knife 里的 `ButterKnife.bind(this)` 很不一样。`bind` 是把布局里的 View 绑定到当前 Activity 或 Fragment；`@HiltAndroidApp` 是启动整个应用级依赖容器。

## 第二部分：让 Activity 成为入口

Android 组件需要标记为 Hilt 入口：

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

这样 Activity 以及它下面的 Hilt ViewModel 才能拿到依赖。

如果忘记加这个注解，经常会遇到 ViewModel 无法注入的问题。

这类错误很适合拿来当 Hilt 的入门提醒：它不是在运行时临时猜测对象从哪里来，而是要求你明确告诉它哪些 Android 组件允许进入依赖图。入口没开，后面的对象就算写得再漂亮，也拿不到票。

可以这样对比：

```text
Butter Knife 关心：这个按钮在哪里？
Hilt 关心：这个 ViewModel 需要的 Repository 从哪里来？
```

## 第三部分：注入 ViewModel

ViewModel 可以这样写：

```kotlin
@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel()
```

这里发生了几件事：

- `@HiltViewModel` 告诉 Hilt 这个 ViewModel 由它管理。
- `@Inject constructor` 告诉 Hilt 构造函数需要注入。
- `CourseRepository` 会由 Hilt 提供。

这一步已经超出了 Butter Knife 能解决的范围。Butter Knife 不会帮你创建 Repository，也不会帮你决定测试环境用真实接口还是假接口。

页面使用时仍然可以像普通 ViewModel 一样：

```kotlin
val viewModel: CourseViewModel = hiltViewModel()
```

## 第四部分：注入 Repository

如果 Repository 的依赖也能被 Hilt 创建，可以直接写：

```kotlin
class CourseRepository @Inject constructor(
    private val api: CourseApi,
    private val dao: CourseDao
)
```

这就是构造函数注入。

构造函数注入的好处是清晰：这个类需要什么，一眼就能看到。

## 第五部分：从手动装配到自动装配

手动装配可能是：

```kotlin
val api = CourseApi()
val dao = CourseDao()
val repository = CourseRepository(api, dao)
val viewModel = CourseViewModel(repository)
```

Hilt 装配后，代码变成：

```kotlin
@HiltViewModel
class CourseViewModel @Inject constructor(
    private val repository: CourseRepository
) : ViewModel()
```

你不再到处 new 对象，而是声明“我需要什么”。

这就是 Hilt 最直接的便利。

## 本节小挑战

请从第 6 章示例工程里选择一条链路：

```text
SyncCenterViewModel -> SyncRepository -> FakeCourseApi
```

思考如果使用 Hilt：

- 哪些类可以用 `@Inject constructor`？
- 哪个类需要 `@HiltViewModel`？
- Activity 需要加什么注解？

## 本节实践任务

### 基础任务

- 创建 `CourseApplication`。
- 给 Activity 添加 `@AndroidEntryPoint`。
- 创建一个 `CourseRepository @Inject constructor()`。
- 创建一个 `@HiltViewModel`。

### 进阶任务

- 将手动创建 Repository 的代码改为 Hilt 注入。
- 尝试给 ViewModel 增加一个 UseCase 依赖。
- 观察构造函数依赖如何变得更清晰。

## 本节小结

Hilt 入门的核心不是记注解，而是理解对象装配方式发生了变化：类只声明自己需要什么，创建和传递交给 Hilt。这个变化会让后面的架构分层和模块化更容易落地。
