# 8.3 协程、Flow 与 ViewModel 测试

第 6 章里，我们学习了协程、Flow、StateFlow 和 SharedFlow。第 8 章要继续追问：这些异步状态怎么测试？

很多 Android 问题并不是“代码完全不能跑”，而是状态在某个时刻不对：加载中没有关闭、错误提示没有出现、刷新成功但列表没更新。ViewModel 测试就是为了抓住这些状态变化。

## 本节定位

本节关注异步逻辑测试。

重点对象是：

- suspend 函数。
- Flow 数据流。
- StateFlow 页面状态。
- SharedFlow 一次性事件。
- ViewModel 中的协程。

## 学习目标

学完本节后，你应该能够：

- 理解为什么异步代码需要专门测试。
- 知道 ViewModel 测试关注 UI State。
- 为 Flow 的发射结果设计断言。
- 区分状态和一次性事件的测试方式。
- 避免测试中依赖真实延迟。

## 第一部分：异步测试难在哪里

异步测试容易出问题，是因为它涉及时间。

例如点击刷新后，页面状态可能经历：

```text
初始状态
  -> isRefreshing = true
  -> 数据源返回结果
  -> isRefreshing = false
  -> lessons 更新
```

如果测试没有控制好协程调度，就可能出现：

- 断言太早，状态还没更新。
- 测试依赖真实 `delay`，运行很慢。
- 一次性事件还没收集就已经发出。
- 失败时不知道是逻辑问题还是调度问题。

所以异步测试要尽量让时间可控。

## 第二部分：ViewModel 测试看什么

ViewModel 测试不应该关心 UI 长什么样。

它更适合关注：

- 初始 UI State 是否正确。
- 点击刷新后是否进入加载状态。
- 成功后列表是否更新。
- 失败后是否保留旧数据。
- 是否发出一次性提示。

可以把 ViewModel 看成页面的状态机器。测试就是给它一个动作，然后观察状态是否按预期变化。

## 第三部分：测试 StateFlow

StateFlow 表示当前状态。

例如：

```kotlin
data class CourseUiState(
    val isLoading: Boolean = false,
    val lessons: List<CourseLesson> = emptyList(),
    val message: String? = null
)
```

测试时可以关注：

```text
当 refresh 成功：
isLoading 先变为 true，
最后变为 false，
lessons 变成新课程列表。
```

如果状态设计得清晰，测试也会很自然。

## 第四部分：测试 Flow

Repository 暴露 Flow 时，可以测试它在不同输入下发出什么。

例如：

```text
本地缓存变化
  -> Repository observeDashboard()
      -> 发出新的 CourseDashboard
```

这里不需要启动页面，只需要给 Fake 数据源推入新数据，然后断言 Flow 的输出。

Flow 测试的关键是：

- 收集它。
- 触发输入变化。
- 断言输出顺序和内容。
- 在合适时间停止收集。

## 第五部分：测试 SharedFlow 事件

SharedFlow 常用于一次性事件，例如 Snackbar。

它和 StateFlow 不一样：

```text
StateFlow：我现在是什么状态？
SharedFlow：刚刚发生了什么事件？
```

测试一次性事件时，要先开始收集，再触发动作，否则事件可能已经发出而测试没有接到。

这也是为什么事件和状态要分开建模：它们的生命周期不同，测试方式也不同。

## 第六部分：不要让测试真的等

真实业务里可以有延迟，测试里不应该真的慢慢等。

例如 FakeApi 里如果有：

```kotlin
delay(1000)
```

测试不应该真的等 1 秒。更好的方式是使用测试调度器，或者让 Fake 数据源在测试中立即返回。

测试越快，开发者越愿意经常运行它。

## 本节小挑战

请为“课程刷新”设计一组 ViewModel 状态变化：

```text
点击刷新前
点击刷新后
刷新成功后
刷新失败后
```

每一步写出 `isRefreshing`、`lessons`、`message` 应该是什么。

## 本节实践任务

### 基础任务

- 找到一个 ViewModel。
- 写出它的 UI State。
- 为刷新成功设计状态断言。
- 为刷新失败设计状态断言。

### 进阶任务

- 为 Repository Flow 设计测试。
- 为 SharedFlow 一次性事件设计测试。
- 思考测试中如何避免真实 `delay`。

## 本节小结

协程和 Flow 测试的核心，是让异步状态变得可观察、可控制、可断言。ViewModel 测试不是打开页面，而是验证页面背后的状态机器是否可靠。
