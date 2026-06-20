# 2.2 Composable、Modifier 与基础布局

上一节我们认识了 Jetpack Compose 的基本入口：使用 `@Composable` 描述 UI，通过 `setContent` 把页面挂载到 Activity 中。现在，我们继续向前一步：如何把多个 UI 元素组织成真正的页面。

本节会学习 Compose 中最常用的三个基础能力：Composable 拆分、Modifier 修饰和基础布局。它们是写 Compose 页面时每天都会用到的东西。

## 本节小挑战

请把一个大大的 `CourseHomeScreen` 拆成至少三个小组件：标题区、课程说明区、按钮区。拆完之后再看代码，如果你能一眼说出每个函数负责什么，就算过关。

Compose 的乐趣之一，就是把页面拆成一块块清楚的小积木。

## 本节定位

本节仍然属于 `02-compose-ui-basics`。

如果说 2.1 解决的是“Compose 是什么”，那么 2.2 解决的是“Compose 页面怎么组织”。

这一节重点包括：

- 如何把页面拆成多个 Composable。
- `Modifier` 为什么重要。
- 如何使用 `Column`、`Row`、`Box` 组织布局。
- 如何设置间距、尺寸、对齐和背景。
- 如何写一个结构清晰的课程首页。

## 学习目标

学完本节后，你应该能够：

- 将一个页面拆分成多个小 Composable。
- 使用 `Modifier` 设置尺寸、间距、背景和点击区域。
- 使用 `Column` 纵向排列内容。
- 使用 `Row` 横向排列内容。
- 使用 `Box` 叠放或定位内容。
- 理解 `padding`、`fillMaxSize`、`fillMaxWidth`、`height` 等常见修饰符。
- 完成一个结构清晰的课程首页布局。

## 第一部分：为什么要拆分 Composable

初学时，我们很容易把所有 UI 都写在一个函数里：

```kotlin
@Composable
fun CourseHomeScreen() {
    Text(text = "从零开始的 Android 全栈开发课程")
    Text(text = "从基础、架构到 Framework")
    Button(onClick = {}) {
        Text(text = "开始学习")
    }
}
```

页面一复杂，这种写法会很快变得难读。

更好的方式是拆分：

```kotlin
@Composable
fun CourseHomeScreen() {
    CourseHeader()
    CourseSummary()
    StartButton()
}
```

拆分的好处是：

- 每个函数只表达一个小块 UI。
- 代码更容易阅读和复用。
- 后续添加状态和事件时更容易维护。

## 第二部分：Modifier 是什么

`Modifier` 是 Compose 中非常核心的概念。它用于修饰一个 Composable，例如设置：

- 尺寸
- 间距
- 背景
- 点击行为
- 对齐方式
- 滚动
- 语义信息

示例：

```kotlin
Text(
    text = "Hello Android",
    modifier = Modifier.padding(16.dp)
)
```

可以链式组合多个修饰：

```kotlin
Text(
    text = "Hello Android",
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
)
```

理解 `Modifier` 的一个简单方式是：

```text
Composable 决定“是什么”
Modifier 决定“怎么摆、怎么修饰、怎么响应”
```

## 第三部分：Column 纵向布局

`Column` 用于纵向排列子内容。

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
) {
    Text(text = "课程标题")
    Text(text = "课程说明")
    Button(onClick = {}) {
        Text(text = "开始学习")
    }
}
```

常见参数：

```kotlin
Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(text = "居中内容")
}
```

- `verticalArrangement` 控制纵向排列方式。
- `horizontalAlignment` 控制横向对齐方式。

## 第四部分：Row 横向布局

`Row` 用于横向排列子内容。

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(text = "第 2 章")
    Text(text = "Compose UI 基础")
}
```

`Row` 适合用来做：

- 标题栏
- 标签组
- 图标 + 文本
- 左右信息排列

## 第五部分：Box 叠放布局

`Box` 可以让子元素叠放，也适合做简单定位。

```kotlin
Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Text(text = "居中显示")
}
```

如果页面中有背景图、浮动按钮、角标等内容，`Box` 会很常用。

## 第六部分：完成课程首页布局

可以把课程首页拆成几个部分：

```kotlin
@Composable
fun CourseHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        CourseHeader()
        Spacer(modifier = Modifier.height(16.dp))
        CourseDescription()
        Spacer(modifier = Modifier.height(24.dp))
        StartLearningButton()
    }
}
```

示例子组件：

```kotlin
@Composable
fun CourseHeader() {
    Text(text = "从零开始的 Android 全栈开发课程")
}

@Composable
fun CourseDescription() {
    Text(text = "从 Kotlin、Compose 到 Framework，建立完整工程能力。")
}

@Composable
fun StartLearningButton() {
    Button(onClick = {}) {
        Text(text = "开始学习")
    }
}
```

这就是 Compose 中非常典型的页面组织方式：大的页面负责布局，小的 Composable 负责具体内容。

## 本节实践任务

### 基础任务

- 使用 `Column` 创建一个纵向页面。
- 使用 `Text` 展示课程标题和说明。
- 使用 `Spacer` 添加元素间距。
- 使用 `Button` 创建一个按钮。
- 使用 `Modifier.padding` 设置页面边距。

### Android 工程任务

- 将首页拆成至少 3 个 Composable。
- 给根布局添加 `fillMaxSize`。
- 给按钮添加 `fillMaxWidth` 或合适宽度。
- 使用 `Row` 展示“章节编号 + 章节名称”。
- 使用 `Box` 创建一个居中展示区域。

### 进阶任务

- 尝试调整 `Arrangement` 和 `Alignment`，观察布局变化。
- 将文案替换为字符串资源。
- 给页面添加背景色。
- 将按钮点击事件作为参数传入 `StartLearningButton`。

## 思考题

- 为什么 Compose 页面适合拆成多个小函数？
- `Modifier` 的顺序会不会影响最终效果？
- `Column` 和 `Row` 的区别是什么？
- `Box` 适合解决什么布局问题？
- 页面级 Composable 和组件级 Composable 应该如何分工？

## 常见问题

### Modifier 的顺序重要吗？

重要。`Modifier` 是链式执行的，不同顺序可能产生不同效果。比如先设置背景再设置 padding，和先 padding 再背景，视觉范围可能不同。

### 页面拆太细会不会麻烦？

拆分要服务于可读性。一个函数承担一个清晰职责即可，不需要为了拆分而拆分。

### Compose 还需要 ConstraintLayout 吗？

很多页面用 `Column`、`Row`、`Box` 就能完成。复杂约束布局可以再考虑 ConstraintLayout for Compose，但入门阶段先掌握基础布局更重要。

## 本节小结

这一节让 Compose 页面真正有了结构。你已经认识了 Composable 拆分、Modifier 修饰和三种基础布局容器。后续学习状态和事件时，这些布局会承载动态内容，页面也会从“静态展示”走向“可交互应用”。
