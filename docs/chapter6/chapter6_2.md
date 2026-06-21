# 6.2 从 Thread、线程池到 Coroutines

上一节我们知道了为什么 Android 需要异步，也看到了进程、线程、Thread 和线程池这些基础概念。本节继续往前走：从 Java 传统异步写法过渡到 Kotlin Coroutines。

这节不是为了否定 Java 的线程模型，而是帮助你看见 Kotlin 协程的便捷性来自哪里。

如果把协程比作一次有组织的任务执行，那么后半节的三个关键词就是：

- `suspend`：这个函数可能会等待。
- `CoroutineScope`：这件任务属于谁。
- `Dispatcher`：这件任务适合在哪类线程上执行。

这三个概念看懂后，很多协程代码就不再像魔法。

## 本节定位

本节属于第 6 章的基础语法部分。

它先对比传统 Java 异步模型，再建立 Android 工程中最常用的协程理解方式：

```text
Thread / 线程池
  -> 能把任务放到后台
  -> 但生命周期、取消、回调和线程切换都需要手动管理
  -> Coroutines 在此基础上提供更轻量、更结构化的任务表达
```

最终你会看到：

```text
ViewModel 发起任务
  -> suspend 函数执行耗时逻辑
  -> Dispatcher 切换合适线程
  -> 结果回到 UI State
```

## 学习目标

学完本节后，你应该能够：

- 理解 `Thread` 的基本使用方式和局限。
- 理解线程池为什么比直接创建线程更适合多任务。
- 说出协程相比传统回调和线程池写法的便利之处。
- 理解 `suspend` 函数的含义。
- 知道 `launch` 和 `async` 的基础区别。
- 理解 `viewModelScope` 为什么常用于页面任务。
- 知道 `Dispatchers.Main`、`IO`、`Default` 的基本分工。
- 写出一个从 ViewModel 调用 Repository 的协程示例。

## 第一部分：直接使用 Thread

最直接的后台任务写法是创建一个线程：

```kotlin
Thread {
    val lessons = loadLessonsFromNetwork()
}.start()
```

这段代码能让耗时任务离开主线程，但它没有解决完整问题。

比如你还需要考虑：

- 如何把结果切回主线程更新 UI？
- 页面关闭后这个线程是否还应该继续？
- 线程执行失败后异常在哪里处理？
- 多个任务同时执行时会创建多少线程？

直接使用 `Thread` 适合理解底层概念，但不适合作为现代 Android 业务代码的默认写法。

## 第二部分：线程池解决线程复用

线程创建有成本，所以 Java 里常用线程池：

```kotlin
val executor = Executors.newFixedThreadPool(4)

executor.execute {
    val lessons = loadLessonsFromNetwork()
}
```

线程池可以复用线程，避免频繁创建和销毁。

它适合：

- 执行多个后台任务。
- 控制并发线程数量。
- 避免线程无限增长。

但线程池仍然是偏底层的工具。它不天然知道 Android 生命周期，也不会自动帮你把结果变成 UI State。

## 第三部分：传统写法的痛点

传统异步模型经常会出现这些问题：

- 回调嵌套，代码阅读顺序被打断。
- 主线程和后台线程切换分散在各处。
- 任务取消需要手动设计。
- Activity / Fragment 销毁后仍可能回调旧页面。
- 异常处理容易散落。

例如：

```text
后台线程请求数据
  -> 回调成功
  -> 切回主线程
  -> 判断页面是否还存在
  -> 更新 UI
```

这些步骤都合理，但写多了会让页面代码变重。

协程的价值，就是把这条链路写得更直、更安全、更容易绑定生命周期。

## 第四部分：协程不是不用线程

协程最终仍然运行在线程上。

区别在于：

```text
Thread：你直接管理线程。
线程池：你管理一组可复用线程。
Coroutines：你管理任务，Dispatcher 帮你决定任务跑在哪类线程上。
```

这也是 Kotlin 从 Java 过渡过来后最明显的便利之一：开发者不用总是站在线程层面思考，而是站在“任务”和“数据流”层面组织代码。

## 第五部分：suspend 不是开线程

很多初学者会误以为：

```text
suspend = 自动切到后台线程
```

这不准确。

`suspend` 的意思是：这个函数可以挂起，也可以恢复。它允许协程在等待时把线程让出来，但它不等于自动开线程。

例如：

```kotlin
suspend fun refreshLessons(): List<Lesson> {
    return api.getLessons()
}
```

它告诉调用者：这个函数可能需要等待，请在协程里调用。

## 第六部分：CoroutineScope 表示任务归属

协程应该有归属。

在 Android 中，常见 Scope 包括：

- `viewModelScope`：跟随 ViewModel 生命周期。
- `lifecycleScope`：跟随 Activity / Fragment 生命周期。
- 自定义 Scope：用于更明确的工程边界。

最常见的写法是：

```kotlin
class LessonViewModel(
    private val repository: LessonRepository
) : ViewModel() {
    fun refresh() {
        viewModelScope.launch {
            val lessons = repository.refreshLessons()
            // 更新 UI State
        }
    }
}
```

当 ViewModel 被清理时，`viewModelScope` 中未完成的任务会自动取消。

这就是“任务有归属”的价值。

## 第七部分：launch 与 async

`launch` 用于启动一个不直接返回结果的协程：

```kotlin
viewModelScope.launch {
    repository.refreshLessons()
}
```

`async` 用于并发执行并返回结果：

```kotlin
val lessonsDeferred = async { repository.getLessons() }
val profileDeferred = async { repository.getProfile() }

val lessons = lessonsDeferred.await()
val profile = profileDeferred.await()
```

入门阶段先记住：

```text
大多数页面任务用 launch。
需要并发拿多个结果时，再考虑 async。
```

## 第八部分：Dispatcher 的分工

Dispatcher 决定协程在哪类线程上执行。

常见 Dispatcher：

- `Dispatchers.Main`：主线程，适合更新 UI 状态。
- `Dispatchers.IO`：网络、数据库、文件读写。
- `Dispatchers.Default`：CPU 密集计算。

例如：

```kotlin
suspend fun loadLessons(): List<Lesson> = withContext(Dispatchers.IO) {
    lessonDao.getLessons()
}
```

这样可以把数据库读取放到 IO 线程池。

## 第九部分：一个完整刷新例子

可以把课程刷新写成这样：

```kotlin
fun refresh() {
    viewModelScope.launch {
        uiState.value = uiState.value.copy(isLoading = true)

        val result = repository.refreshLessons()

        uiState.value = uiState.value.copy(
            isLoading = false,
            lessons = result
        )
    }
}
```

这里的职责是：

- ViewModel 启动协程。
- Repository 暴露 `suspend` 函数。
- 数据层决定是否切换到 IO。
- UI 只观察状态。

这和第 5 章的架构边界是连在一起的。

## 本节小挑战

请判断下面任务适合哪个 Dispatcher：

- 请求课程列表。
- 读取 Room 缓存。
- 解析一个很大的 JSON。
- 更新 Compose 状态。
- 压缩一张图片。

不要急着背答案，先问自己：它是在等 IO，还是在吃 CPU，还是必须回到 UI？

再补一个比较题：

```text
如果用 Thread、线程池、协程分别实现“刷新课程列表”，代码需要处理哪些事情？
```

你会发现，协程并没有让问题消失，而是让问题更集中、更结构化。

## 本节实践任务

### 基础任务

- 用 `Thread` 写一个模拟耗时任务。
- 用 `ExecutorService` 执行两个模拟耗时任务。
- 用协程重写同样逻辑。
- 对比三种写法在代码顺序、取消、错误处理上的差异。
- 写一个 `suspend fun loadLessons()`。
- 在 ViewModel 中用 `viewModelScope.launch` 调用它。
- 给 UI State 增加 `isLoading`。
- 刷新开始时显示加载中，结束后展示数据。

### 进阶任务

- 用 `withContext(Dispatchers.IO)` 包裹数据读取。
- 尝试用 `async` 并发加载课程列表和用户信息。
- 思考 `Repository` 是否应该知道 `viewModelScope`。

## 本节小结

Thread 和线程池帮助我们理解异步的底层基础，Coroutines 则让 Android 业务代码可以用更轻量、更结构化的方式表达异步任务。`suspend` 描述可挂起函数，`CoroutineScope` 描述任务归属，`Dispatcher` 描述任务执行位置。三者合在一起，构成了现代 Android 协程代码的基础骨架。
