# 6.6 错误处理、重试与结果建模

异步任务一定会失败。网络会断，接口会超时，数据库可能异常，后台任务可能被系统延后。

成熟的代码不是假装失败不会发生，而是把失败变成可表达、可展示、可恢复的状态。

真实用户不会关心 `SocketTimeoutException` 是什么。他只会看到：点了刷新，页面转了一会儿，然后没有结果。好的 App 不应该把用户丢在沉默里，而是告诉他发生了什么、还能不能继续看旧缓存、下一步能做什么。

这一节要做的，就是给失败安排一条体面的退场路线。

## 本节定位

本节关注异步链路中的错误处理。

前面我们已经学会启动任务、观察数据流、更新 UI State。本节会把失败、重试和结果建模补上。

## 学习目标

学完本节后，你应该能够：

- 使用 `try/catch` 处理协程异常。
- 使用 Flow 的 `catch` 处理数据流异常。
- 设计简单的 Result / UiState 模型。
- 理解重试不只是重新点一次按钮。
- 区分技术错误和用户可理解的提示。

## 第一部分：协程里的 try/catch

最直接的错误处理：

```kotlin
viewModelScope.launch {
    try {
        val lessons = repository.refreshLessons()
        uiState.value = LessonUiState.Success(lessons)
    } catch (e: Exception) {
        uiState.value = LessonUiState.Error("刷新失败，请稍后重试")
    }
}
```

这种方式简单直观，适合入门阶段。

但要注意：不要把底层异常信息原样丢给用户。用户不需要看到一串堆栈或技术术语。

## 第二部分：Flow 的 catch

Flow 可以使用 `catch` 捕获上游异常：

```kotlin
repository.observeLessons()
    .map { lessons -> LessonUiState.Success(lessons) }
    .catch { emit(LessonUiState.Error("加载课程失败")) }
```

`catch` 只捕获它上游的异常。

这意味着操作符顺序会影响错误处理范围。

## 第三部分：结果建模

很多项目会定义一个结果类型：

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String) : AppResult<Nothing>
}
```

或者直接定义 UI 状态：

```kotlin
sealed interface LessonUiState {
    data object Loading : LessonUiState
    data class Content(val lessons: List<Lesson>) : LessonUiState
    data class Error(val message: String) : LessonUiState
}
```

哪种更好，取决于层级：

- Repository 更适合返回业务结果。
- ViewModel 更适合整理 UI State。

## 第四部分：重试

重试不是简单地无限请求。

要考虑：

- 什么错误值得重试？
- 重试几次？
- 每次间隔多久？
- 用户是否知道正在重试？
- 重试失败后是否保留缓存？

Flow 提供 `retry`：

```kotlin
flow {
    emit(api.getLessons())
}.retry(2)
```

但真实项目中，重试策略需要谨慎，不要把服务端打得更忙。

## 第五部分：错误提示要面向用户

底层错误可能是：

```text
java.net.SocketTimeoutException
```

但页面提示应该是：

```text
网络有点慢，稍后再试
```

错误处理的目的不是把异常搬到 UI，而是帮助用户知道现在能做什么。

## 本节小挑战

请把下面错误翻译成用户能理解的提示：

- 网络超时。
- 没有本地缓存。
- 登录已过期。
- 服务器返回空数据。

这一步很小，但很像真实产品开发。

## 本节实践任务

### 基础任务

- 给刷新课程增加 `try/catch`。
- 失败时保留旧课程列表。
- 在 UI State 中增加错误提示。
- 添加一个“重试”按钮。

### 进阶任务

- 定义一个 `AppResult`。
- 将 Repository 的异常转换成业务错误。
- 使用 Flow 的 `catch` 和 `retry`。
- 思考错误提示适合放在 StateFlow 还是 SharedFlow。

## 本节小结

错误处理是异步能力的一部分。好的错误处理不是让代码没有异常，而是让异常进入可控路径：能记录、能展示、能重试，也能在失败时保留用户还能使用的内容。
