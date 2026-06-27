# 示例工程：Hilt、模块化与工程化

## 对应章节

第7章 Hilt 依赖注入、模块化与工程化

## 工程目标

本工程用于配合第 7 章，把课程 App 从“一个能运行的功能”继续整理成更接近真实项目的工程结构。

它会围绕三个问题展开：

- 对象从哪里来？
- 代码应该放在哪个模块？
- 不同环境应该如何构建？

可以把这个示例看成一次“课程 App 工程升级演练”：从一个功能能跑的单体 App 出发，逐步把对象创建、模块边界和环境配置整理出来。目标不是炫技，而是让读者看到工程变清楚的过程。

## 当前效果

运行后你会看到一个“第 7 章工程化演练”页面：

- 页面顶部展示当前构建环境、同步课程数和 `baseUrl`。
- 点击 `通过 Hilt 刷新课程`，观察 `ViewModel -> UseCase -> Repository -> DataSource` 如何完成一次刷新。
- 页面中部展示模块依赖方向，帮助你理解代码为什么要拆到不同模块。
- 页面下方展示课程列表，刷新后会看到来自当前环境配置的数据说明。

这个示例不会真正请求网络，而是用 `FakeCourseApi` 模拟远端接口，把重点放在 Hilt、模块化和环境配置本身。

## 运行方式

1. 使用 Android Studio 打开 `examples/07-hilt-modularization-engineering`。
2. 等待 Gradle Sync 完成。
3. 选择 `devDebug` 或 `prodDebug` 变体。
4. 运行 `app` 模块。
5. 点击页面上的刷新按钮，观察页面文案和课程同步状态变化。

## 工程结构

```text
07-hilt-modularization-engineering/
  app/
  core-model/
  core-network/
  feature-course/
  domain-course/
  data-course/
```

核心模块职责：

- `app`：应用入口、`@HiltAndroidApp`、`@AndroidEntryPoint`、dev/prod 构建环境。
- `core-model`：通用课程模型。
- `core-network`：网络环境配置模型。
- `domain-course`：业务接口和 UseCase。
- `data-course`：Repository 实现、本地数据源、远端数据源、Hilt Module。
- `feature-course`：Compose 页面和 `@HiltViewModel`。

推荐依赖方向：

```text
app -> feature-course
feature-course -> domain-course
feature-course -> core-model
app -> data-course
data-course -> domain-course
data-course -> core-network
```

## Hilt 注入链路

```text
CourseEngineeringApplication
  -> AppEnvironmentModule
      -> NetworkConfig

CourseEngineeringRoute
  -> CourseEngineeringViewModel
      -> GetCourseDashboardUseCase
      -> RefreshCoursesUseCase
          -> CourseRepository 接口
              -> CourseRepositoryImpl
                  -> CourseRemoteDataSource
                  -> CourseLocalDataSource
```

这里有几个值得重点观察的地方：

- `app` 模块通过 `AppEnvironmentModule` 提供 `NetworkConfig`。
- `data-course` 模块用 `@Binds` 把 `CourseRepository` 接口绑定到 `CourseRepositoryImpl`。
- `data-course` 模块用 `@Provides` 提供 `CourseRemoteDataSource`。
- `feature-course` 只依赖 `domain-course`，不直接知道数据实现细节。

## 关键源码入口

- `app/src/main/java/com/helloandroid/engineering/CourseEngineeringApplication.kt`
- `app/src/main/java/com/helloandroid/engineering/di/AppEnvironmentModule.kt`
- `feature-course/src/main/java/com/helloandroid/feature/course/CourseEngineeringViewModel.kt`
- `feature-course/src/main/java/com/helloandroid/feature/course/CourseEngineeringScreen.kt`
- `domain-course/src/main/java/com/helloandroid/domain/course/CourseRepository.kt`
- `data-course/src/main/java/com/helloandroid/data/course/di/RepositoryModule.kt`
- `data-course/src/main/java/com/helloandroid/data/course/di/DataSourceModule.kt`
- `data-course/src/main/java/com/helloandroid/data/course/repository/CourseRepositoryImpl.kt`

## 计划覆盖知识点

- Butter Knife 历史背景
- View Binding / Compose 与 Butter Knife 的关系
- Butter Knife 和 Hilt 的职责差异
- 依赖注入
- Hilt
- `@HiltAndroidApp`
- `@AndroidEntryPoint`
- `@HiltViewModel`
- `@Inject`
- `@Module`
- `@Provides`
- `@Binds`
- 作用域
- Repository 接口与实现
- 多 Module 工程
- Gradle buildTypes
- productFlavors
- BuildConfig 环境配置
- 模块边界与依赖方向

## Butter Knife 对比观察

本工程不引入 Butter Knife，因为它已经不适合作为现代新项目推荐方案。

但你可以通过这个示例理解两条演进线：

```text
UI 绑定线：
findViewById -> Butter Knife -> View Binding / Data Binding -> Compose

对象装配线：
手动 new -> Dagger -> Hilt
```

本示例使用 Compose 解决 UI 构建问题，使用 Hilt 解决对象依赖装配问题。它们服务的是不同层级的问题。

## 练习任务

### 基础任务

- 写下当前工程里最容易变乱的三个点，并说明它们分别属于对象创建、模块边界还是环境配置问题。
- 阅读一段 Butter Knife 风格代码，判断它解决的是 View 绑定还是对象依赖。
- 写出 `findViewById -> Butter Knife -> View Binding / Compose` 的 UI 绑定演进线。
- 写出 `手动 new -> Dagger -> Hilt` 的对象装配演进线。
- 找到 `CourseRepository` 接口和 `CourseRepositoryImpl` 实现。
- 找到 `@Binds` 和 `@Provides` 分别出现在哪里。
- 找到 `NetworkConfig` 是在哪个模块被创建的。
- 画出课程列表依赖链路。

### 进阶任务

- 将 `FakeCourseApi` 替换成真正 Retrofit 接口。
- 给 `data-course` 增加一个本地缓存实现。
- 为 `dev` 和 `prod` 增加不同 app name 或主题颜色。
- 新增一个 `feature-profile` 模块，并思考它能否直接依赖 `feature-course`。
- 写一份模块依赖规则说明。
- 写一份“新同学接手指南”，说明页面、数据、网络、配置分别应该从哪里开始读。

## 通关目标

完成本章后，你应该能说清楚：

- Butter Knife 解决过什么问题，以及为什么现代新项目不再推荐它。
- Butter Knife 和 Hilt 为什么不是同一类工具。
- 为什么需要依赖注入。
- Hilt 如何装配 ViewModel、UseCase、Repository。
- `@Provides` 和 `@Binds` 的区别。
- 为什么大型项目需要模块化。
- Gradle 构建变体如何服务多环境。
- 模块边界和接口设计如何影响长期维护。
