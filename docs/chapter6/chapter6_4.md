# 6.4 Flow：会随时间变化的数据

协程可以很好地处理一次性任务，但真实 App 里很多数据不是“一次返回”就结束的。

Room 表数据会变化，DataStore 配置会变化，搜索输入会变化，网络状态会变化，页面状态也会变化。这些场景更适合用 Flow 表达。

Flow 可以理解为：一条会随时间发出多个值的数据流。

比如课程 App 打开后，页面先显示本地缓存；几秒后网络刷新成功，Room 写入新数据；用户又切换了“只看未完成课程”的开关。你不希望手动到处通知页面刷新，而是希望数据自己沿着管道流到 UI。

Flow 就像这条管道：上游数据一动，下游页面自然收到新的状态。

## 本节定位

本节进入第 6 章的第二个核心：Flow。

第 4 章里我们已经见过 Room 和 DataStore 暴露 Flow，本节会把它背后的思想讲清楚。

## 学习目标

学完本节后，你应该能够：

- 理解 Flow 和普通 `suspend` 函数的区别。
- 知道冷流的基本含义。
- 使用 `map`、`filter`、`catch` 等常见操作符。
- 理解 `collect` 是数据流开始被消费的时刻。
- 将 Flow 转换成 UI 需要的数据。

## 第一部分：一次结果 vs 多次结果

`suspend` 函数适合一次结果：

```kotlin
suspend fun getLessons(): List<Lesson>
```

Flow 适合多次结果：

```kotlin
fun observeLessons(): Flow<List<Lesson>>
```

可以这样理解：

```text
suspend：问一次，等一个结果
Flow：订阅一条数据流，未来有变化就继续收到
```

Room 的查询很适合 Flow，因为数据库内容变化后，页面希望自动更新。

## 第二部分：创建一个 Flow

可以用 `flow` 创建数据流：

```kotlin
fun lessonFlow(): Flow<List<Lesson>> = flow {
    emit(emptyList())
    emit(repository.getLessons())
}
```

`emit` 表示发出一个值。

页面或 ViewModel 通过 `collect` 接收这些值。

## 第三部分：冷流

Flow 默认是冷流。意思是：

```text
只有被 collect 时，Flow 里面的代码才真正执行。
```

例如：

```kotlin
val flow = flow {
    println("开始请求")
    emit(api.getLessons())
}
```

如果没有人 `collect`，这段代码不会执行。

这让 Flow 很适合描述数据来源，而不是马上执行任务。

## 第四部分：常见操作符

Flow 的强大之处在于可以像管道一样变换数据。

`map`：转换数据。

```kotlin
lessonDao.observeLessons()
    .map { entities -> entities.map { it.toUiModel() } }
```

`filter`：过滤数据。

```kotlin
lessonsFlow.map { lessons ->
    lessons.filterNot { it.isCompleted }
}
```

`catch`：捕获上游异常。

```kotlin
lessonsFlow
    .catch { emit(emptyList()) }
```

这些操作符让数据流可以一步步变成 UI 需要的形状。

## 第五部分：collect

`collect` 是消费 Flow 的地方：

```kotlin
viewModelScope.launch {
    repository.observeLessons().collect { lessons ->
        uiState.value = uiState.value.copy(lessons = lessons)
    }
}
```

在 Compose 中，我们更常见的是把 Flow 转成 `StateFlow`，然后通过 `collectAsStateWithLifecycle` 收集。

这能更好地配合生命周期。

## 第六部分：组合多个 Flow

真实页面经常需要组合多个数据来源。

例如：

- Room 发出课程列表。
- DataStore 发出是否显示已完成课程。

可以使用 `combine`：

```kotlin
combine(lessonsFlow, preferencesFlow) { lessons, preferences ->
    if (preferences.showCompleted) {
        lessons
    } else {
        lessons.filterNot { it.isCompleted }
    }
}
```

这就是第 4 章示例工程里已经出现过的思想。

## 本节小挑战

请用一句话区分：

- `suspend fun getLessons()`
- `fun observeLessons(): Flow<List<Lesson>>`

如果你能说出“一个是单次请求，一个是持续观察”，Flow 的门就打开了。

## 本节实践任务

### 基础任务

- 创建一个发出课程列表的 Flow。
- 使用 `map` 把 Entity 转成 UI Model。
- 使用 `filter` 实现只显示未完成课程。
- 使用 `collect` 打印每次数据变化。

### 进阶任务

- 用 `combine` 组合课程列表和用户偏好。
- 给 Flow 增加 `catch`，观察异常如何进入 UI。
- 思考 Repository 应该暴露 Flow 还是直接暴露 UI State。

## 本节小结

Flow 让“会变化的数据”有了清晰表达。它不是只属于响应式编程爱好者的高级玩具，而是现代 Android 中连接 Room、DataStore、ViewModel 和 Compose 的常用管道。
