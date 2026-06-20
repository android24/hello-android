# 2.1 Jetpack Compose 入门

第 1 章解决的是 Android 开发的起点：环境、Kotlin、工程结构和资源系统。到了第 2 章，我们正式进入现代 Android UI 开发的核心：Jetpack Compose。

Compose 是 Android 官方推出的声明式 UI 工具包。它改变了我们组织界面的方式：你不再主要通过 XML 描述控件树，再在 Kotlin 中查找和修改控件；而是直接使用 Kotlin 函数描述界面在某个状态下应该长什么样。

这一节是 Compose UI 基础的入口。我们先不追求复杂页面，而是理解 Compose 的基本思想：什么是 Composable，为什么 UI 可以由函数组成，`setContent` 在 Activity 中扮演什么角色，以及一个最小 Compose 页面如何运行起来。

## 本节定位

本节属于 `02-compose-ui-basics`，也就是“Compose UI 基础”章节。

这一章的主线是：

- 先理解 Compose 的声明式 UI 思想。
- 再学习 Composable、Modifier 和基础布局。
- 然后学习状态、事件和页面交互。
- 最后扩展到列表、表单、主题和预览。

这一节只解决第一步：让你看懂 Compose 页面是如何被写出来、挂载到 Activity 上，并显示到屏幕上的。

## 学习目标

学完本节后，你应该能够：

- 理解声明式 UI 与传统命令式 UI 的基本区别。
- 理解 `@Composable` 的作用。
- 理解 `setContent` 如何把 Compose 页面放进 Activity。
- 写出一个最小的 Compose 页面。
- 使用 `Text`、`Button` 等基础组件展示简单内容。
- 知道 Compose 页面不是“运行一次就结束”，而是会根据状态重新组合。

## 前置知识

开始本节前，建议你已经完成第 1 章：

- 可以运行 Android 工程。
- 理解 Kotlin 中函数、类、Lambda 和空安全。
- 知道 `Activity` 是 Android 页面入口之一。
- 知道资源系统中字符串、颜色和图片的基本作用。

如果你暂时还不熟悉 Activity 生命周期，也可以先学习 Compose 入门，但后续理解页面状态和导航时，Activity 基础仍然会派上用场。

## 第一部分：为什么需要 Compose

在传统 Android UI 中，我们经常会这样工作：

- 在 XML 中写布局。
- 在 Kotlin 或 Java 中找到控件。
- 根据数据变化手动修改控件内容。
- 处理生命周期、状态恢复和页面刷新。

这种方式可以工作很久，但当页面变复杂时，UI 状态和控件操作容易散落在很多地方。

Compose 的思路是另一种：

```text
界面 = 状态在某一刻的展示结果
```

你写的是“当数据是这样时，页面应该长这样”。当数据变化时，Compose 会重新执行相关 Composable，让界面自动反映新的状态。

这就是声明式 UI 的核心思想。

## 第二部分：认识 Composable

Composable 是 Compose 中用于描述 UI 的函数。它通过 `@Composable` 注解标记。

```kotlin
@Composable
fun Greeting(name: String) {
    Text(text = "Hello, $name")
}
```

这段代码表达的是：

- `Greeting` 是一个可以参与 Compose UI 构建的函数。
- 它接收一个 `name` 参数。
- 它在页面上显示一段文字。

和普通 Kotlin 函数相比，Composable 的特殊之处在于：它不是只为了计算一个结果，而是参与描述界面结构。

## 第三部分：setContent 的作用

在 Compose 项目中，Activity 里常见这样的代码：

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Greeting(name = "Android")
        }
    }
}
```

`setContent` 的作用是：把 Compose UI 内容设置到当前 Activity 中。

可以先这样理解：

```text
Activity 负责接入 Android 系统
setContent 负责承载 Compose 页面
Composable 负责描述具体 UI
```

这是现代 Android Compose 工程里非常常见的入口结构。

## 第四部分：最小 Compose 页面

一个最小页面可以只有一段文字：

```kotlin
@Composable
fun CourseHomeScreen() {
    Text(text = "从零开始学 Android")
}
```

然后在 Activity 中使用：

```kotlin
setContent {
    CourseHomeScreen()
}
```

此时你已经完成了一个非常小的 Compose 页面。虽然它看起来简单，但结构已经很重要：

- `MainActivity` 是页面入口。
- `setContent` 设置 Compose 内容。
- `CourseHomeScreen` 描述 UI。
- `Text` 是基础 UI 组件。

## 第五部分：Compose 组件是函数

Compose 的很多 UI 组件本质上都是 Composable 函数，例如：

```kotlin
Text(text = "课程标题")

Button(onClick = {}) {
    Text(text = "开始学习")
}
```

你会发现 UI 结构像 Kotlin 函数调用一样自然嵌套：

```kotlin
Button(onClick = {}) {
    Text(text = "开始学习")
}
```

这里的 `Text` 是 `Button` 的内容。后续学习布局时，你还会看到 `Column`、`Row`、`Box` 等容器组件，它们也通过类似方式组织子内容。

## 第六部分：声明式 UI 的第一印象

假设我们有一个布尔值：

```kotlin
val isLoggedIn = false
```

页面可以根据它展示不同内容：

```kotlin
@Composable
fun LoginStatus(isLoggedIn: Boolean) {
    if (isLoggedIn) {
        Text(text = "欢迎回来")
    } else {
        Text(text = "请先登录")
    }
}
```

这就是声明式 UI 的感觉：我们不直接命令某个控件隐藏或显示，而是描述不同状态下页面应该是什么样。

后面学习状态时，我们会让 `isLoggedIn`、点击次数、输入框内容真正变化起来。

## 本节实践任务

### 基础任务

- 打开一个 Compose 工程。
- 找到 `MainActivity.kt`。
- 找到 `setContent` 代码块。
- 新建一个 `CourseHomeScreen` Composable。
- 在页面中显示课程标题和欢迎语。

### Android 工程任务

- 使用 `Text` 显示课程名称。
- 使用 `Button` 添加一个“开始学习”按钮。
- 把页面 Composable 从 `MainActivity` 中拆出来，保持入口代码简洁。
- 尝试把页面文案改为第 1 章中定义的字符串资源。

### 进阶任务

- 新建一个 `LoginStatus` Composable。
- 通过参数控制显示“欢迎回来”或“请先登录”。
- 尝试给 `CourseHomeScreen` 添加参数，例如课程名、阶段名。
- 观察参数变化后页面代码结构是否更清晰。

## 思考题

- Compose 为什么叫声明式 UI？
- `@Composable` 和普通 Kotlin 函数有什么不同？
- `setContent` 出现在 Activity 中说明了什么？
- 为什么把页面拆成多个 Composable 会更容易维护？
- 如果页面内容取决于一个状态值，Compose 会如何表达这种关系？

## 常见问题

### Compose 是否完全不需要 XML？

Compose 负责 UI 声明，但 Android 工程仍然会使用 `AndroidManifest.xml`、资源文件、图标、字符串、颜色和主题等内容。Compose 不是删除整个 Android 工程体系，而是改变 UI 的组织方式。

### Composable 可以随便调用普通函数吗？

Composable 可以调用普通函数，但普通函数不能直接调用 Composable，除非它本身也是 Composable。因为 Composable 需要运行在 Compose 的上下文中。

### 为什么 Button 里面还能写 Text？

这是 Kotlin Lambda 和 Compose 内容插槽共同工作的结果。你可以先理解为：`Button` 允许你传入一段 UI 内容作为按钮内部显示。

## 本节小结

这一节完成了 Compose 的入门：你已经知道 Compose 是声明式 UI，Composable 是描述界面的函数，`setContent` 把 Compose 页面挂载到 Activity 中。

下一节会继续深入 Compose 的基本工具：`Composable` 的拆分方式、`Modifier` 的作用，以及 `Column`、`Row`、`Box` 等基础布局。
