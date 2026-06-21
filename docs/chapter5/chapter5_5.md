# 5.5 MVI / UDF：用单向数据流管理复杂交互

当页面变复杂时，状态可能来自很多地方：用户点击、输入框、网络刷新、缓存更新、配置变化、页面恢复。此时如果状态可以从很多方向被随意修改，页面就会变得难以推理。

MVI 和 UDF 的核心思想是：让数据流动方向变得清晰。

## 本节定位

本节是架构演进的第四站。

MVVM 已经让我们重视 UI State，MVI / UDF 则进一步强调事件、状态和渲染之间的单向流动。

## 学习目标

学完本节后，你应该能够：

- 理解单向数据流的基本思想。
- 区分 Intent、Action、State、Effect 的概念。
- 知道 MVI 和 UDF 为什么适合复杂页面。
- 用课程列表设计一个简单数据流。
- 理解 MVI 的收益和成本。

## 第一部分：什么是 UDF

UDF 是 Unidirectional Data Flow，单向数据流。

可以理解为：

```text
用户事件 -> ViewModel 处理 -> 产生新状态 -> UI 重新渲染
```

数据不应该在页面里到处乱改，而是沿着固定方向流动。

## 第二部分：MVI 的常见结构

MVI 常见概念：

- Intent：用户意图，例如点击刷新。
- State：页面状态。
- Effect：一次性事件，例如 Toast、导航。
- Reducer：根据旧状态和事件生成新状态。

课程列表可以这样描述：

```kotlin
sealed interface CourseIntent {
    data object RefreshClicked : CourseIntent
    data class LessonClicked(val id: Long) : CourseIntent
}

data class CourseState(
    val isLoading: Boolean = false,
    val lessons: List<LessonUiModel> = emptyList(),
    val errorMessage: String? = null
)
```

## 第三部分：为什么需要单向数据流

单向数据流的好处：

- 状态变化路径清晰。
- 复杂页面更容易排查问题。
- 更容易记录和回放事件。
- UI 只负责渲染状态。
- 事件入口统一。

当页面有很多交互时，它比“到处 setState”更容易维护。

## 第四部分：Effect

有些事情不是状态，而是一次性事件：

- 弹 Toast。
- 跳转页面。
- 打开弹窗。
- 触发震动。

这些可以用 Effect 表达：

```kotlin
sealed interface CourseEffect {
    data class OpenLessonDetail(val id: Long) : CourseEffect
    data class ShowMessage(val message: String) : CourseEffect
}
```

不要把一次性事件硬塞进长期状态里。

## 第五部分：MVI 的成本

MVI 也有成本：

- 文件和概念更多。
- 简单页面可能显得繁琐。
- 需要团队统一写法。
- 初学者需要适应事件、状态、Effect 的拆分。

所以它更适合复杂交互页面，不必所有页面都强行 MVI。

## 本节小挑战

请为课程列表写出三个用户意图：

- 点击刷新。
- 点击课程。
- 切换只看未完成课程。

然后思考：这些意图会如何改变状态，哪些会触发一次性 Effect？

## 本节实践任务

### 基础任务

- 定义 `CourseIntent`。
- 定义 `CourseState`。
- 定义 `CourseEffect`。
- 画出单向数据流。

### 进阶任务

- 将 MVVM 中的 `onRefreshClicked` 改成 `dispatch(CourseIntent.RefreshClicked)`。
- 思考导航应该是 State 还是 Effect。
- 比较 MVVM 和 MVI 的复杂度差异。

## 本节小结

MVI / UDF 不是为了让代码更花哨，而是为了让复杂交互更可控。它把用户意图、状态变化和一次性事件分开，让页面行为更容易推理。
