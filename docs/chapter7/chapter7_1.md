# 7.1 为什么需要依赖注入

第 5 章里，我们把代码分成 UI、ViewModel、Repository、UseCase 等角色。第 6 章里，我们让数据通过协程和 Flow 流动起来。现在问题来了：这些对象到底由谁创建？谁负责把它们连起来？

如果每个页面都自己创建 Repository，每个 ViewModel 都自己 new 数据源，工程很快会变成一张乱线缠成的网。你想换一个网络实现，可能要改很多地方；你想写测试，发现真实数据库和真实接口都被硬塞进来了。

依赖注入要解决的，就是对象装配问题。

## 本章通关画面

完成第 7 章后，你应该能看懂并设计这样的工程关系：

```text
UI
  -> ViewModel
      -> UseCase
          -> Repository 接口
              -> Repository 实现
                  -> RemoteDataSource
                  -> LocalDataSource
```

并且知道这些对象应该如何被 Hilt 创建、如何按模块拆分、如何在不同环境中替换实现。

这不是为了让代码看起来更高级，而是为了让工程能长大：新人能接手，测试能替换依赖，业务能拆模块，线上和测试环境能用不同配置。

## 本章故事线

可以把第 7 章想象成一次课程 App 的工程升级。

一开始，课程 App 已经能展示列表、请求数据、保存配置，也能用协程和 Flow 处理异步任务。它看起来能跑，但里面藏着几个隐患：

- 页面想要数据，就自己创建 Repository。
- Repository 想要接口，就自己创建 Api。
- 测试想换假数据源，却发现真实实现已经写死。
- 新同学想加一个功能，不知道代码应该放在哪一层。
- 准备打测试包时，才发现接口地址还写在 Kotlin 文件里。

第 7 章要做的，就是把这个“能跑的 App”整理成“能长大的 App”。

## 本节定位

本节是第 7 章入口。

第 7 章主线是：

- 先理解依赖和依赖注入。
- 再学习 Hilt 如何自动装配对象。
- 然后学习 Module、Provides、Binds 和作用域。
- 接着把 ViewModel、Repository、数据源放进注入链路。
- 最后进入模块化、构建变体和工程边界。

## 学习目标

学完本节后，你应该能够：

- 理解什么是依赖。
- 理解手动创建对象的问题。
- 理解依赖注入解决了什么。
- 区分“自己创建”和“由外部传入”。
- 知道 Hilt 在现代 Android 工程中的位置。

## 第一部分：什么是依赖

一个类需要另一个对象才能工作，这个对象就是它的依赖。

例如：

```kotlin
class CourseRepository(
    private val api: CourseApi,
    private val dao: CourseDao
)
```

`CourseRepository` 依赖 `CourseApi` 和 `CourseDao`。

如果它自己在内部创建：

```kotlin
class CourseRepository {
    private val api = RealCourseApi()
    private val dao = RealCourseDao()
}
```

它就和具体实现绑死了。

## 第二部分：手动创建对象的问题

小 Demo 里手动创建对象没问题。

但真实工程里，对象关系可能是这样：

```text
CourseViewModel
  -> GetCoursesUseCase
      -> CourseRepository
          -> CourseApi
          -> CourseDao
          -> UserPreferences
```

如果每一层都自己创建下一层，会出现几个问题：

- 具体实现到处散落。
- 测试时很难替换假对象。
- 生命周期不好统一管理。
- 构造逻辑重复。
- 模块边界越来越模糊。

这就像每个房间都自己偷偷接电线，灯是亮了，但后面维修会很痛苦。

如果某一天你只是想把 `RealCourseApi` 换成 `FakeCourseApi`，却一路改到 Activity、ViewModel、Repository、DataSource，那就说明对象创建已经变成了工程里的隐形负担。

## 第三部分：依赖注入的思路

依赖注入的核心很简单：

```text
不要在类内部自己创建依赖，
而是从外部把依赖传进来。
```

例如：

```kotlin
class CourseRepository(
    private val api: CourseApi,
    private val dao: CourseDao
)
```

这样 `CourseRepository` 不关心 `api` 和 `dao` 是谁创建的，只关心它们能提供什么能力。

测试时可以传入假的实现：

```kotlin
val repository = CourseRepository(
    api = FakeCourseApi(),
    dao = FakeCourseDao()
)
```

这就是可替换性的开始。

## 第四部分：Hilt 解决什么

手动传依赖也可以，但对象多了之后，手动装配会很繁琐。

Hilt 的作用是：

- 根据注解自动创建对象。
- 自动把依赖传入需要它的地方。
- 管理对象生命周期。
- 支持在测试中替换依赖。
- 和 Android 组件生命周期集成。

可以先这样理解：

```text
依赖注入是思想。
Hilt 是 Android 中常用的实现工具。
```

## 第五部分：依赖注入和架构的关系

依赖注入不是架构本身，但它能让架构更容易落地。

第 5 章里我们说 Repository、UseCase、ViewModel 应该有清晰边界。第 7 章会继续解决：

- Repository 实现从哪里来？
- UseCase 如何拿到 Repository？
- ViewModel 如何拿到 UseCase？
- 测试时如何替换 Repository？
- 多模块里如何避免互相乱依赖？

这就是依赖注入和模块化要进入课程的原因。

## 第六部分：历史支线：Butter Knife 解决过什么

在进入 Hilt 之前，有必要认识一个老朋友：Butter Knife。

很多早期 Android 项目里，你会看到这样的代码：

```java
@BindView(R.id.course_title)
TextView courseTitle;

@OnClick(R.id.refresh_button)
void refresh() {
    // 刷新课程
}
```

Butter Knife 主要解决的是 View 绑定和点击监听的样板代码。它通过注解处理生成代码，帮开发者少写大量 `findViewById` 和匿名点击监听。

它在当时很受欢迎，是因为传统 View 体系里样板代码真的很多：

```java
TextView title = findViewById(R.id.course_title);
Button button = findViewById(R.id.refresh_button);
button.setOnClickListener(...);
```

Butter Knife 让这些代码更短、更集中。

但要注意：Butter Knife 不是 Hilt 的前身，也不是 Hilt 的同类工具。

可以这样区分：

```text
Butter Knife：绑定 View 和点击事件，主要解决 UI 样板代码。
Hilt：装配对象依赖，主要解决架构和对象创建问题。
```

## 第七部分：为什么后来不再推荐 Butter Knife

Butter Knife 后来逐渐退出主流，原因包括：

- Android 官方提供了 View Binding。
- Kotlin Android Extensions 曾经短暂降低过 View 查找成本。
- Data Binding 和 Compose 改变了 UI 写法。
- Butter Knife 依赖注解处理，增加构建复杂度。
- 它解决的是 View 绑定问题，不解决 Repository、UseCase、DataSource 的对象装配。

Butter Knife 官方仓库也已经标注 deprecated，并建议切换到 View Binding。

所以现代课程中不应该把 Butter Knife 当作新项目推荐方案，而应该把它作为历史脉络讲清楚：它代表了 Android 开发从手写样板代码走向自动生成代码的一个阶段。

## 第八部分：从 Butter Knife 到 Hilt，真正变化是什么

更准确的演进不是：

```text
Butter Knife -> Hilt
```

而是两条线同时发生了变化：

```text
UI 绑定线：
findViewById -> Butter Knife -> View Binding / Data Binding -> Compose

对象装配线：
手动 new -> Dagger -> Hilt
```

Butter Knife 让 UI 层少写绑定代码；Hilt 让工程层少写对象装配代码。

它们都体现了同一个趋势：

```text
把重复、容易出错、没有业务价值的样板代码交给工具。
```

但工具服务的层级不同。

在本课程里，我们会这样处理：

- 理解 Butter Knife：帮助你读懂老项目。
- 不推荐新项目继续使用 Butter Knife：现代项目优先 View Binding 或 Compose。
- 学习 Hilt：帮助你管理 ViewModel、UseCase、Repository、DataSource 等对象依赖。

这会让学习者既能看懂历史代码，也能掌握现代工程实践。

## 本节小挑战

请找出第 6 章示例工程中的依赖关系：

- `SyncCenterViewModel` 依赖谁？
- `SyncRepository` 依赖谁？
- 如果要把 `FakeCourseApi` 替换成真实接口，需要改哪些地方？
- 如果老项目里使用 Butter Knife，它解决的是 View 绑定问题，还是 Repository 注入问题？

如果你能说清楚这些问题，第 7 章就有了入口。

## 本节实践任务

### 基础任务

- 写出一个 `CourseRepository` 的构造函数依赖。
- 尝试把内部创建对象改成构造函数传入。
- 写出一个假的 `CourseApi`，用于测试或预览。
- 找一段 Butter Knife 代码，说明它减少了哪些 `findViewById` 或点击监听样板代码。

### 进阶任务

- 画出课程 App 当前对象依赖图。
- 标出哪些对象适合由 Hilt 创建。
- 思考哪些对象需要单例，哪些对象应该跟随页面生命周期。
- 画出两条演进线：UI 绑定线和对象装配线。

## 本节小结

依赖注入的本质，是把对象创建和对象使用分开。它让代码更容易测试、更容易替换实现，也更适合多人协作的工程。Hilt 则是 Android 里帮助我们自动完成这件事的常用工具。
