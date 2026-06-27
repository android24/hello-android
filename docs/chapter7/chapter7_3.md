# 7.3 Module、Provides、Binds 与作用域

构造函数注入很方便，但并不是所有对象都能直接用 `@Inject constructor` 创建。

例如接口没有构造函数，第三方库对象不是你写的，Retrofit、Room Database、OkHttpClient 这类对象也需要专门配置。这个时候就需要 Hilt Module。

本节进入 Hilt 的装配台。

如果说 `@Inject constructor` 像是“这个零件我会自己安装”，那么 Module 就像一张装配说明：第三方对象怎么造、接口要换成哪个实现、哪些对象应该全局复用，都要在这里说清楚。

## 本节定位

本节讲 Hilt 中更贴近真实工程的部分：

- 如何提供第三方对象。
- 如何把接口绑定到实现。
- 如何控制对象生命周期。

## 学习目标

学完本节后，你应该能够：

- 理解 `@Module` 和 `@InstallIn`。
- 使用 `@Provides` 提供第三方或复杂对象。
- 使用 `@Binds` 绑定接口和实现。
- 理解 `@Singleton` 等作用域。
- 判断对象应该放在哪个生命周期范围。

## 第一部分：什么时候需要 Module

如果一个类可以改源码，并且构造函数简单，可以用：

```kotlin
class CourseRepository @Inject constructor(...)
```

但下面这些情况需要 Module：

- 对象来自第三方库。
- 构造过程需要 Builder。
- 需要返回接口类型。
- 需要根据环境提供不同实现。
- 需要配置单例。

例如 Retrofit：

```kotlin
Retrofit.Builder()
    .baseUrl("https://example.com")
    .build()
```

这种对象就适合放进 Module。

真实项目里，很多让新人卡住的 Hilt 报错都发生在这里：类本身写得没问题，但 Hilt 不知道 Retrofit 从哪里来，也不知道接口应该对应哪个实现。Module 的价值，就是把这些“工程约定”写成明确规则。

## 第二部分：@Provides

`@Provides` 用于告诉 Hilt 如何创建某个对象：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideCourseApi(): CourseApi {
        return Retrofit.Builder()
            .baseUrl("https://example.com")
            .build()
            .create(CourseApi::class.java)
    }
}
```

这里的含义是：

- `@Module`：这是一个依赖提供模块。
- `@InstallIn`：这个模块安装到哪个 Hilt 组件。
- `@Provides`：这个函数提供一个依赖。
- `@Singleton`：这个依赖在应用范围内复用。

## 第三部分：@Binds

如果你有接口和实现：

```kotlin
interface CourseRepository

class CourseRepositoryImpl @Inject constructor(...) : CourseRepository
```

可以用 `@Binds`：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCourseRepository(
        impl: CourseRepositoryImpl
    ): CourseRepository
}
```

`@Binds` 的意思是：当有人需要 `CourseRepository` 时，请给他 `CourseRepositoryImpl`。

## 第四部分：作用域

作用域决定对象能活多久。

常见作用域：

- `@Singleton`：跟随应用生命周期。
- `@ActivityScoped`：跟随 Activity。
- `@ViewModelScoped`：跟随 ViewModel。

不是所有对象都应该单例。

适合单例：

- Retrofit。
- OkHttpClient。
- Room Database。
- 全局配置数据源。

不适合随便单例：

- 页面临时状态。
- 与某个用户操作强绑定的对象。
- 持有 Activity 引用的对象。

## 第五部分：作用域选择的直觉

可以这样问：

```text
这个对象是否昂贵？
这个对象是否可以被全局复用？
这个对象是否持有页面引用？
这个对象是否包含页面临时状态？
```

如果对象昂贵、无页面引用、可复用，可能适合单例。

如果对象和页面状态绑定，就应该更谨慎。

## 本节小挑战

请判断下面对象适合什么方式提供：

- Retrofit
- Room Database
- CourseRepository 接口
- CourseViewModel
- 当前页面输入框状态

## 本节实践任务

### 基础任务

- 写一个 `NetworkModule`。
- 用 `@Provides` 提供 `CourseApi`。
- 写一个 `RepositoryModule`。
- 用 `@Binds` 绑定接口与实现。

### 进阶任务

- 给 Retrofit 或 Repository 添加 `@Singleton`。
- 思考哪些对象不应该单例。
- 故意去掉某个 Provides，观察 Hilt 报错信息。

## 本节小结

Module 是 Hilt 的装配台。`@Provides` 负责创建复杂对象，`@Binds` 负责接口到实现的绑定，作用域负责对象生命周期。掌握这些之后，Hilt 才真正进入真实工程。
