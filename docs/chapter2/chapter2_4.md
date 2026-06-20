# 2.4 列表、主题与预览

前面几节我们已经掌握了 Compose 的基本构成：Composable、Modifier、基础布局、状态和事件。现在需要补上三个非常实用的能力：列表、主题和预览。

真实 App 很少只有一个按钮和一段文字。它通常会展示一组数据，有统一视觉风格，并且希望开发者不必每次都运行到手机上才能看见页面效果。本节会围绕这些场景展开。

## 本节小挑战

请给课程首页新增一节课的数据，然后让列表自动多显示一张卡片。不要复制粘贴一整块 UI，只改数据源。

当你看到页面自己多出一张卡片时，就会明白“数据驱动 UI”为什么舒服。

## 本节定位

本节是 `02-compose-ui-basics` 的收束小节。

它会把第 2 章前面的内容组合起来：

- 使用列表展示多条课程数据。
- 使用主题和颜色资源保持视觉一致。
- 使用 Preview 快速查看 Composable 效果。
- 完成一个更接近真实 App 的课程首页。

## 学习目标

学完本节后，你应该能够：

- 使用集合数据渲染多个 Composable。
- 理解 `forEach` 渲染列表的基础方式。
- 知道何时需要进一步学习 `LazyColumn`。
- 使用 `MaterialTheme` 管理基础视觉风格。
- 使用颜色资源或主题颜色组织页面样式。
- 为 Composable 添加 `@Preview`。
- 理解预览与真实运行环境的区别。

## 第一部分：用数据驱动列表

列表不是手写多个卡片，而是由数据驱动。

先定义数据：

```kotlin
data class LessonItem(
    val index: Int,
    val title: String,
    val description: String
)
```

再准备列表：

```kotlin
val lessons = listOf(
    LessonItem(1, "Jetpack Compose 入门", "理解声明式 UI。"),
    LessonItem(2, "基础布局", "学习 Column、Row、Box。"),
    LessonItem(3, "状态与事件", "让页面响应用户操作。")
)
```

最后渲染：

```kotlin
Column {
    lessons.forEach { lesson ->
        LessonCard(lesson = lesson)
    }
}
```

这比手写三个 `LessonCard` 更容易维护。未来新增课程，只需要修改数据。

## 第二部分：什么时候使用 LazyColumn

如果列表数量很少，直接使用 `Column + forEach` 没问题。

但如果列表很多，应该使用 `LazyColumn`：

```kotlin
LazyColumn {
    items(lessons) { lesson ->
        LessonCard(lesson = lesson)
    }
}
```

`LazyColumn` 只会组合当前需要显示的项目，更适合长列表。第 2 章示例工程为了保持简单，使用少量数据和普通 `Column`。后续真实项目会进一步学习 `LazyColumn`。

## 第三部分：主题的作用

主题负责统一页面的视觉语言。比如：

- 字体层级
- 颜色体系
- 组件默认样式
- 明暗模式适配

Compose 中常见主题入口：

```kotlin
MaterialTheme {
    Surface {
        CourseHomeScreen()
    }
}
```

这里的 `MaterialTheme` 会为内部组件提供默认排版、颜色和形状能力。

第 2 章阶段不需要立即设计复杂主题系统，但应该先养成习惯：页面不要到处散落随机颜色和字号。

## 第四部分：资源颜色与主题颜色

课程示例中会使用资源颜色：

```kotlin
colorResource(id = R.color.text_primary)
```

也会使用 Material 默认排版：

```kotlin
style = MaterialTheme.typography.headlineSmall
```

这两种方式可以共同使用：

```kotlin
Text(
    text = "Compose UI 基础",
    style = MaterialTheme.typography.headlineSmall,
    color = colorResource(id = R.color.text_primary)
)
```

初学阶段可以先这样做。后续进入工程化主题时，再把颜色、排版和组件样式进一步封装。

## 第五部分：Preview 是什么

Preview 可以让你在 Android Studio 中预览 Composable，不必每次都运行到设备。

示例：

```kotlin
@Preview(showBackground = true)
@Composable
fun CourseHomeScreenPreview() {
    HelloComposeBasicsApp()
}
```

Preview 的价值：

- 快速查看 UI。
- 辅助调整间距、排版和颜色。
- 让组件开发更高效。

但要注意，Preview 不等同于真实运行环境。涉及系统服务、Activity 结果、复杂依赖、网络或数据库时，Preview 可能无法完整模拟。

## 第六部分：完成章节首页

一个基础课程首页可以包含：

- 课程标题。
- 副标题。
- 当前阶段提示。
- 课程列表。
- 开始学习按钮。
- 点击状态反馈。

这正好覆盖第 2 章核心能力：

- Composable 拆分。
- Modifier 修饰。
- Column / Row / Box 布局。
- 状态与事件。
- 列表渲染。
- 主题和资源。
- Preview。

## 本节实践任务

### 基础任务

- 创建 `LessonItem` 数据类。
- 准备一个课程列表。
- 使用 `forEach` 渲染多个课程卡片。
- 给课程卡片添加标题和说明。
- 使用统一颜色资源。

### Android 工程任务

- 打开 `examples/02-compose-ui-basics`。
- 找到 `LessonItem` 和 `composeLessons`。
- 新增一个课程项。
- 修改课程卡片的颜色和间距。
- 为首页添加一个 Preview。

### 进阶任务

- 尝试把 `Column + forEach` 改成 `LazyColumn`。
- 为卡片增加不同状态，例如“已完成 / 未开始”。
- 为 Preview 准备一组专门的假数据。
- 尝试切换深色模式，观察页面效果。

## 思考题

- 为什么列表应该由数据驱动？
- `Column + forEach` 和 `LazyColumn` 有什么区别？
- 主题能解决哪些重复样式问题？
- Preview 适合验证什么，不适合验证什么？
- 示例工程中哪些内容可以继续抽成更小的 Composable？

## 常见问题

### 所有列表都要用 LazyColumn 吗？

不需要。少量固定内容可以直接用 `Column`。当列表较长、需要滚动或性能更重要时，再使用 `LazyColumn`。

### Preview 能替代真机运行吗？

不能。Preview 用于快速查看 UI，但真实交互、系统生命周期、权限、导航、性能等仍然需要真机或模拟器验证。

### 主题是不是必须一开始就设计得很完整？

不是。初学阶段先保证颜色和文字层级不要混乱。等页面多起来，再逐步抽象完整主题。

## 本节小结

本节让第 2 章形成了闭环：你已经能用 Compose 写出一个带布局、状态、事件、列表和基础主题的页面，并能通过 Preview 辅助查看效果。

下一章将进入 Activity、生命周期与导航。你会把 Compose 页面放回 Android 组件模型中，理解页面如何被系统启动、跳转和管理。
