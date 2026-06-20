# 第二课：Kotlin 基础与 Android 工程语法

上一课，我们完成了 Android 学习的第一件大事：让开发环境跑起来。现在，你已经知道 Android Studio、SDK、Gradle、JDK 和运行设备分别承担什么角色，也完成了从“代码”到“手机上的 App”的最小闭环。

从这一课开始，我们正式进入代码世界。但这一课不会把 Kotlin 当作一本孤立的语法手册来讲。Android 开发中的语言学习，最好的方式不是背概念，而是在真实工程里理解：变量为什么会出现在界面状态里，函数为什么会组织一次点击行为，类为什么会描述一个页面或一条数据，空安全为什么能减少崩溃。

本课的目标，是让你掌握 Android 开发中最常用、最先需要理解的 Kotlin 基础，并能读懂一个简单 Android 工程里的核心代码。

## 本课小挑战

请在这一课结束前，给课程 App 加一个属于自己的欢迎语。它可以来自一个 `data class`，也可以来自一个函数。比如输入课程名和阶段名，生成一句“欢迎来到第 1 章”的提示。

这个挑战很小，但它会把变量、函数、数据类、字符串模板和 Android 页面显示串在一起。

## 本课定位

第二课仍然属于“阶段一：开发环境与 Kotlin 基础”。如果第一课解决的是“我如何把 App 跑起来”，那么第二课解决的就是“我如何看懂并修改 App 中的代码”。

这一课会建立三层理解：

- Kotlin 是 Android 现代开发的主要语言之一。
- Kotlin 语法不是孤立存在的，它会直接出现在 Activity、ViewModel、Compose、数据模型和异步任务里。
- 写 Android 代码时，我们关注的不只是语法是否正确，还要关注可读性、可维护性和空值安全。

学完这一课，你应该能独立阅读一个简单页面背后的 Kotlin 代码，并能完成小范围修改。

## 学习目标

学完本课后，你应该能够：

- 理解 Kotlin 中变量、常量、类型推断和字符串模板的基本用法。
- 理解函数的定义、参数、返回值和表达式函数。
- 理解类、对象、属性、构造函数和数据类。
- 理解 Kotlin 空安全机制，包括可空类型、非空类型、安全调用和 Elvis 操作符。
- 理解集合、条件判断、循环和常见 Lambda 写法。
- 在 Android 工程中找到 Kotlin 源码位置，并能修改简单业务逻辑。
- 初步理解 Android 代码中包名、导入、类名、方法名之间的关系。

## 前置知识

开始本课前，建议你已经完成第一课中的内容：

- Android Studio 可以正常启动。
- 能创建或打开一个 Android 工程。
- 能在模拟器或真机上运行默认 App。
- 能找到工程中的 `app` 模块。
- 能打开 Logcat 并观察简单日志。

如果你还没有完全熟悉这些内容，也可以一边学习本课，一边回头补齐第一课的操作。

## 为什么 Android 要学习 Kotlin

Kotlin 是一种简洁、安全、适合工程开发的编程语言。对于 Android 开发来说，它有几个非常重要的优势：

- 语法简洁：同样的逻辑通常可以写得更少、更清楚。
- 空安全：把很多空指针问题提前暴露在编译阶段。
- 与 Java 兼容：可以调用大量已有 Java 和 Android API。
- 适合现代 Jetpack：Compose、协程、Flow 等现代 Android 技术都大量使用 Kotlin。

在本课程中，Kotlin 不只是入门语言，而是贯穿 Compose、架构、协程、测试和 Framework 调试的基础工具。

## 第一部分：变量与常量

Kotlin 中最常见的两个声明关键字是 `val` 和 `var`。

```kotlin
val appName = "Hello Android"
var count = 0
```

- `val` 表示只读引用，赋值后不能重新指向新的值。
- `var` 表示可变引用，后续可以重新赋值。

在 Android 开发中，建议优先使用 `val`。当你确实需要让某个值变化时，再使用 `var`。

例如，一个页面标题通常不会在创建后被随意修改：

```kotlin
val title = "我的第一个 Android 页面"
```

而一个计数器的数值会随着点击发生变化：

```kotlin
var clickCount = 0
clickCount = clickCount + 1
```

### 类型推断

Kotlin 通常可以自动推断变量类型：

```kotlin
val name = "Android"
val version = 15
val isReady = true
```

你也可以显式写出类型：

```kotlin
val name: String = "Android"
val version: Int = 15
val isReady: Boolean = true
```

初学时建议多观察类型，但不必每一行都手动写类型。工程代码中常常依赖类型推断来保持简洁。

### 字符串模板

字符串模板可以把变量嵌入字符串中：

```kotlin
val name = "Android"
val message = "Hello, $name"
```

如果表达式更复杂，可以使用 `${}`：

```kotlin
val count = 3
val message = "你已经点击了 ${count + 1} 次"
```

这在日志、提示文案、调试输出中非常常见。

## 第二部分：函数

函数是组织行为的基本单位。一次点击、一次数据转换、一次网络结果处理，都可以被封装成函数。

```kotlin
fun sayHello(name: String): String {
    return "Hello, $name"
}
```

这个函数包含：

- `fun`：声明函数。
- `sayHello`：函数名。
- `name: String`：参数。
- `String`：返回值类型。
- `return`：返回结果。

如果函数只有一个表达式，可以写得更简洁：

```kotlin
fun sayHello(name: String): String = "Hello, $name"
```

没有返回值的函数可以使用 `Unit`，也可以省略：

```kotlin
fun printLog(message: String) {
    println(message)
}
```

在 Android 中，一个按钮点击后触发的逻辑，最终也可以被理解为调用某个函数。

## 第三部分：条件判断

Kotlin 的 `if` 不只是语句，也可以作为表达式返回值：

```kotlin
val score = 90
val level = if (score >= 60) {
    "通过"
} else {
    "未通过"
}
```

当条件分支更多时，可以使用 `when`：

```kotlin
val page = "home"

val title = when (page) {
    "home" -> "首页"
    "profile" -> "个人中心"
    "settings" -> "设置"
    else -> "未知页面"
}
```

在 Android 开发中，`when` 常用于处理页面状态、网络结果、用户操作类型和错误类型。

## 第四部分：类与对象

类用于描述一种事物。比如，一个用户可以被描述为：

```kotlin
class User(
    val id: Long,
    val name: String,
    val age: Int
)
```

创建对象：

```kotlin
val user = User(
    id = 1L,
    name = "小安",
    age = 18
)
```

访问属性：

```kotlin
val userName = user.name
```

在 Android 工程中，类可能代表很多东西：

- 一个页面：`MainActivity`
- 一条数据：`User`
- 一个状态：`UiState`
- 一个仓库：`UserRepository`
- 一个工具：`DateFormatter`

从第二课开始，你要逐渐建立一个意识：Android 工程不是一堆随机代码，而是由一组承担不同职责的类组织起来的。

## 第五部分：数据类

当一个类主要用于保存数据时，可以使用 `data class`：

```kotlin
data class Article(
    val id: Long,
    val title: String,
    val author: String
)
```

数据类会自动生成一些常用能力，例如：

- 更可读的 `toString`
- 基于属性的相等比较
- `copy` 方法

例如：

```kotlin
val oldArticle = Article(
    id = 1L,
    title = "Android 入门",
    author = "课程组"
)

val newArticle = oldArticle.copy(title = "Android Kotlin 入门")
```

后续学习列表、网络请求、数据库和 UI 状态时，`data class` 会非常频繁地出现。

## 第六部分：空安全

Android 开发中，空指针异常曾经是非常常见的问题。Kotlin 的空安全机制，就是为了让空值风险更早被发现。

非空类型：

```kotlin
val name: String = "Android"
```

可空类型：

```kotlin
val name: String? = null
```

可空类型不能直接当作非空值使用：

```kotlin
val length = name?.length
```

这里的 `?.` 是安全调用。如果 `name` 为 `null`，表达式结果就是 `null`，不会崩溃。

还可以使用 Elvis 操作符提供默认值：

```kotlin
val displayName = name ?: "匿名用户"
```

在 Android 里，很多数据都可能为空：接口返回、页面参数、本地缓存、系统回调。学会正确处理空值，是写稳定 App 的第一步。

## 第七部分：集合与遍历

常见集合包括 `List`、`Set` 和 `Map`。

```kotlin
val lessons = listOf("环境准备", "Kotlin 基础", "Activity 入门")
```

遍历列表：

```kotlin
for (lesson in lessons) {
    println(lesson)
}
```

常见集合操作：

```kotlin
val longNames = lessons.filter { it.length > 4 }
val titles = lessons.map { "课程：$it" }
```

这里的 `{ it.length > 4 }` 和 `{ "课程：$it" }` 是 Lambda 表达式。你暂时不需要背下所有语法，只要先记住：Lambda 通常用于把“一段小逻辑”传给另一个函数。

后续 Compose、Flow、列表渲染、事件回调中都会大量使用 Lambda。

## 第八部分：把 Kotlin 放回 Android 工程

在一个 Android 工程中，Kotlin 代码通常位于：

```text
app/src/main/java/你的包名/
```

或者：

```text
app/src/main/kotlin/你的包名/
```

常见文件包括：

- `MainActivity.kt`：应用入口页面之一。
- `User.kt`：数据模型。
- `UiState.kt`：页面状态。
- `Repository.kt`：数据仓库。
- `ViewModel.kt`：页面逻辑与状态管理。

在课程早期，你最常见到的是 `MainActivity.kt`。它通常会继承 Android 提供的 Activity 类，并在其中配置页面内容。

你暂时不需要完全理解 Activity 的生命周期，但需要知道：Android 不是从 `main` 函数开始执行应用代码，而是由系统根据清单文件和组件规则启动对应的 Activity。

## 本课实践任务

### 基础任务

- 在 Kotlin 文件中声明一个 `val` 和一个 `var`。
- 写一个接收名字并返回欢迎语的函数。
- 创建一个 `data class Student`，包含 `name`、`age`、`courseName` 三个属性。
- 使用字符串模板输出学生信息。
- 使用 `if` 或 `when` 根据年龄输出不同提示。

### Android 工程任务

- 打开第一课创建的 Android 工程。
- 找到 `MainActivity.kt`。
- 修改页面中展示的文字。
- 添加一个简单函数，例如 `buildWelcomeMessage`。
- 在页面或日志中使用这个函数的返回值。
- 添加一个数据类，例如 `CourseInfo`，并创建一个对象。

示例：

```kotlin
data class CourseInfo(
    val name: String,
    val stage: String,
    val lessonIndex: Int
)

fun buildWelcomeMessage(course: CourseInfo): String {
    return "欢迎来到${course.name}，当前阶段：${course.stage}，第 ${course.lessonIndex} 课"
}
```

### 进阶任务

- 尝试创建一个可空字符串 `String?`，并使用 `?.` 和 `?:` 处理它。
- 创建一个课程列表 `listOf(...)`，使用 `for` 遍历输出。
- 使用 `map` 给每个课程标题加上序号或前缀。
- 使用 Logcat 输出你的 Kotlin 练习结果。

## 思考题

- 为什么 Kotlin 推荐优先使用 `val`？
- `String` 和 `String?` 有什么区别？
- `data class` 适合描述什么类型的数据？
- 为什么 Android 工程里很少看到传统意义上的 `main` 函数？
- Lambda 为什么会在 Compose 和点击事件中频繁出现？

## 常见问题

### Kotlin 需要学到多深才能继续学 Android？

不需要等到完全精通 Kotlin 再开始 Android。更好的方式是先掌握高频语法，然后在 Android 场景中逐步加深。变量、函数、类、空安全、集合和 Lambda，是前期最重要的基础。

### 看不懂 `override fun onCreate` 怎么办？

先记住它是 Activity 创建时由系统调用的方法。`override` 表示当前类重写了父类提供的方法。生命周期会在后续课程详细展开。

### 为什么有些代码没有写类型？

这是 Kotlin 的类型推断。编译器可以根据右侧表达式推断变量类型。工程代码中适度使用类型推断可以让代码更简洁。

### `!!` 能不能用？

`!!` 表示强制把可空值当作非空值使用，如果实际为 `null` 就会崩溃。初学阶段不建议依赖它。优先使用安全调用 `?.`、默认值 `?:`，或者通过条件判断确认非空。

## 本课小结

第二课完成了从“能运行工程”到“能读懂代码”的第一步。你已经接触了 Kotlin 中最常见的语言元素：变量、函数、条件判断、类、数据类、空安全、集合和 Lambda。

更重要的是，你开始把这些语法放回 Android 工程里理解。后续学习 Activity、Compose、ViewModel、网络请求和数据库时，这些基础会不断出现。你现在写下的每一个小函数、每一个数据类，都会在后面的真实项目中长成更完整的结构。

## 下一课预告

下一课将进入 Android 应用基础。我们会正式认识 Activity、生命周期、Intent、资源系统和应用入口，开始理解一个 Android 页面是如何被系统创建、展示和销毁的。
