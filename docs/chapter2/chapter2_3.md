# 2.3 状态与事件

前两节我们已经认识了 Compose 的基本入口，并学会用 `Composable`、`Modifier`、`Column`、`Row`、`Box` 组织静态页面。现在，页面要开始“活”起来。

一个真正的 App 不只是展示文字和按钮。用户会点击、输入、选择、返回；页面也会随着这些动作更新内容。Compose 的核心思想是：**状态变化，界面跟着变化**。本节会围绕状态与事件建立第一层理解。

## 本节小挑战

请给“开始学习”按钮加一个点击计数。第一次点击显示“开始学习”，第二次之后显示“继续学习”，并在页面上显示点击次数。

这个挑战会让你第一次感受到 Compose 的关键魔法：你改变的是状态，页面自己跟着变。

## 本节定位

本节属于 `02-compose-ui-basics` 的第三部分。

如果 2.1 解决“Compose 是什么”，2.2 解决“页面怎么摆”，那么 2.3 解决“页面如何响应用户操作”。

本节重点包括：

- 什么是 UI 状态。
- 什么是事件。
- 如何使用 `remember` 保存简单状态。
- 如何使用 `mutableStateOf` 或 `mutableIntStateOf` 触发界面更新。
- 如何把点击事件从子组件传回页面。
- 为什么状态应该尽量向上提升。

## 学习目标

学完本节后，你应该能够：

- 理解 Compose 中“状态驱动 UI”的基本思想。
- 使用 `remember` 保存一个简单状态。
- 使用按钮点击事件修改状态。
- 根据状态展示不同文案。
- 将事件处理函数作为参数传给子 Composable。
- 初步理解状态提升的意义。

## 第一部分：什么是状态

状态就是页面在某一刻依赖的数据。比如：

- 当前按钮点击次数。
- 用户是否已经开始学习。
- 输入框中的文字。
- 当前选中的课程。
- 列表是否为空。

在声明式 UI 中，页面由状态决定：

```text
UI = f(state)
```

也就是说，同一个 Composable 在不同状态下会展示不同内容。

## 第二部分：remember 保存状态

在 Compose 中，可以使用 `remember` 记住一个值：

```kotlin
var count by remember { mutableIntStateOf(0) }
```

这行代码包含几个关键点：

- `count` 是当前状态值。
- `mutableIntStateOf(0)` 创建一个可观察的整数状态。
- `remember` 让这个状态在重组时保留下来。
- 当 `count` 改变时，依赖它的 UI 会重新组合。

示例：

```kotlin
@Composable
fun CounterButton() {
    var count by remember { mutableIntStateOf(0) }

    Button(onClick = { count++ }) {
        Text(text = "点击了 $count 次")
    }
}
```

这就是最小的状态与事件闭环：按钮被点击，状态变化，文字更新。

## 第三部分：事件是什么

事件是用户或系统触发的动作。常见事件包括：

- 点击按钮。
- 输入文字。
- 切换开关。
- 选择列表项。
- 页面返回。

Compose 中很多组件都会通过回调暴露事件：

```kotlin
Button(onClick = { /* 处理点击 */ }) {
    Text(text = "开始学习")
}
```

这里的 `onClick` 就是点击事件回调。

## 第四部分：根据状态展示不同 UI

状态可以决定页面显示什么：

```kotlin
@Composable
fun LearningStatus(clickCount: Int) {
    if (clickCount == 0) {
        Text(text = "还没有开始学习")
    } else {
        Text(text = "已经点击开始学习 $clickCount 次")
    }
}
```

页面可以这样使用：

```kotlin
@Composable
fun CourseHomeScreen() {
    var clickCount by remember { mutableIntStateOf(0) }

    Column {
        LearningStatus(clickCount = clickCount)

        Button(onClick = { clickCount++ }) {
            Text(text = "开始学习")
        }
    }
}
```

这种写法比“手动找到某个 Text 再修改它”更符合 Compose 的思路。

## 第五部分：把事件传给子组件

如果按钮被拆成单独 Composable，不建议让它自己偷偷管理外部状态。更清晰的方式是把事件作为参数传进去：

```kotlin
@Composable
fun StartLearningButton(
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Text(text = "开始学习")
    }
}
```

页面负责状态：

```kotlin
@Composable
fun CourseHomeScreen() {
    var clickCount by remember { mutableIntStateOf(0) }

    Column {
        LearningStatus(clickCount = clickCount)
        StartLearningButton(onClick = { clickCount++ })
    }
}
```

这样页面结构更清楚：父组件持有状态，子组件触发事件。

## 第六部分：状态提升

状态提升是 Compose 中非常重要的设计思想。简单说，就是把状态放到更合适的上层组件中，由上层统一管理，再把状态和事件传给子组件。

一个常见模式是：

```kotlin
@Composable
fun CourseHomeScreen() {
    var clickCount by remember { mutableIntStateOf(0) }

    StageSummary(clickCount = clickCount)
    StartLearningButton(
        clickCount = clickCount,
        onClick = { clickCount++ }
    )
}
```

`StageSummary` 只负责展示状态，`StartLearningButton` 只负责触发事件，`CourseHomeScreen` 负责管理状态。

这会让代码更容易读，也更容易测试和维护。

## 本节实践任务

### 基础任务

- 在页面中创建一个 `clickCount` 状态。
- 点击按钮时让 `clickCount` 加 1。
- 根据 `clickCount` 展示不同提示文案。
- 将按钮拆成独立 Composable。

### Android 工程任务

- 打开 `examples/02-compose-ui-basics`。
- 找到 `CourseHomeScreen` 中的 `startClickCount`。
- 修改按钮点击后的提示文案。
- 新增一个“重置学习状态”按钮。
- 尝试把重置事件也作为参数传给子组件。

### 进阶任务

- 新增一个布尔状态 `isFavorite`。
- 点击按钮切换收藏状态。
- 根据收藏状态显示“已收藏”或“未收藏”。
- 思考这个状态应该放在哪个 Composable 中。

## 思考题

- 为什么 Compose 说 UI 由状态驱动？
- `remember` 解决了什么问题？
- 为什么状态变化后页面会自动更新？
- 什么情况下应该把状态放到父组件？
- 子组件应该直接修改父组件状态吗？

## 常见问题

### remember 会永久保存状态吗？

不会。`remember` 主要在当前组合中保存状态。配置变化、进程被杀或页面重建时，状态可能丢失。后续会学习 `rememberSaveable` 和 ViewModel。

### 状态是不是越多越好？

不是。状态越多，页面变化路径越复杂。应该只保存真正会变化、并且会影响 UI 的数据。

### 为什么不直接在子组件里写 remember？

可以，但要看职责。如果状态只影响子组件内部，可以放在子组件；如果多个组件都依赖这个状态，就应该提升到共同父组件。

## 本节小结

本节完成了 Compose 从静态页面到交互页面的第一步。你已经知道如何用 `remember` 保存状态，用事件修改状态，并让 UI 随状态变化自动更新。

下一节会继续扩展 Compose 基础，进入列表、主题与预览，让页面更接近真实应用开发。
