# 3.1 Activity、生命周期与应用入口

前两章，我们已经完成了 Android 工程基础与 Compose UI 基础。现在回到 Android 应用运行模型本身：一个 Android App 到底是怎样被系统启动的？为什么我们没有写传统意义上的 `main` 函数，点击桌面图标后，页面却能出现在手机屏幕上？

本节会正式带你认识 Android 应用开发中最核心的概念之一：Activity。你会理解应用入口、`AndroidManifest.xml`、生命周期回调、页面跳转和 Intent 的基础用法。它们是后续学习 Navigation、ViewModel、任务栈、进程保活、Framework 源码时绕不开的地基。

## 本节定位

本节属于 `03-activity-lifecycle-navigation`，也就是 Activity、生命周期与导航章节。

本节重点解决五个问题：

- Android App 为什么不从 `main` 函数启动？
- 系统如何知道应该打开哪个页面？
- Activity 是什么，它和一个屏幕页面是什么关系？
- 页面从创建到销毁会经历哪些生命周期？
- 一个页面如何跳转到另一个页面，并传递简单数据？

学完本节，你会对 Android 的组件模型有第一层完整认识。后面无论学习 Navigation、任务栈，还是深入 AMS / ATMS 源码，都会不断回到这些概念。

## 学习目标

学完本节后，你应该能够：

- 理解 Activity 在 Android 应用中的作用。
- 看懂 `AndroidManifest.xml` 中入口 Activity 的声明。
- 理解 `onCreate`、`onStart`、`onResume`、`onPause`、`onStop`、`onDestroy` 的基本含义。
- 使用 Logcat 观察 Activity 生命周期变化。
- 理解 Intent 的基本作用。
- 完成从一个 Activity 跳转到另一个 Activity。
- 使用 Intent 传递简单字符串或数字参数。
- 初步理解任务栈、返回键和页面返回之间的关系。

## 前置知识

开始本节前，建议你已经完成：

- 第 1 章：可以创建、运行、调试一个 Android 工程，并理解 Kotlin 基础。
- 第 2 章：已经理解 Compose 页面如何被挂载到 Activity 中。

如果你还不熟悉 `MainActivity.kt` 在哪里，建议先回到第二课完成 Android 工程任务。

## 第一部分：Android App 为什么没有 main 函数

很多编程语言或普通程序都会从 `main` 函数开始执行。但 Android App 不是这样启动的。

Android 应用运行在 Android 系统之上，它的组件由系统管理。用户点击桌面图标后，并不是直接调用你写的某个 `main` 函数，而是系统根据应用的清单文件找到入口 Activity，再创建并启动它。

这意味着 Android 应用开发有一个非常重要的特点：

```text
不是应用主动掌控一切，而是应用组件被系统按规则调度。
```

Activity 的生命周期、权限申请、页面跳转、后台限制、进程回收，都和这个特点有关。

## 第二部分：认识 AndroidManifest.xml

`AndroidManifest.xml` 是 Android 应用的清单文件。它告诉系统：这个 App 有哪些组件，需要哪些权限，入口页面是谁，应用的基础配置是什么。

一个简化后的入口声明可能长这样：

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

这里有几个关键点：

- `android:name=".MainActivity"`：声明这个 Activity 的类名。
- `android:exported="true"`：表示这个 Activity 可以被外部启动，入口 Activity 通常需要设置。
- `MAIN`：表示这是应用的主入口动作。
- `LAUNCHER`：表示这个入口会显示在启动器中，也就是桌面图标列表里。

系统正是通过这段配置知道：用户点击图标时，应该启动 `MainActivity`。

## 第三部分：Activity 是什么

Activity 可以理解为 Android 应用中的一个页面承载者。它负责和系统交互，接收生命周期回调，并承载具体 UI。

在现代 Android 开发中，Activity 常常会变得越来越“薄”：

- Activity 负责接入系统生命周期。
- Compose 或 View 负责显示界面。
- ViewModel 负责管理页面状态。
- Repository 负责处理数据来源。

但在学习初期，Activity 仍然是理解 Android 的关键入口。你需要先知道它什么时候创建、什么时候显示、什么时候进入后台、什么时候销毁。

一个常见的 Activity 代码大致如下：

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Text(text = "Hello Android")
        }
    }
}
```

暂时先记住：

- `MainActivity` 是你自己定义的页面类。
- `ComponentActivity` 是 AndroidX 提供的基础 Activity。
- `onCreate` 是 Activity 创建时系统回调的方法。
- `setContent` 常用于设置 Compose 页面内容。

## 第四部分：Activity 生命周期

Activity 不是一次创建后永远停在那里。它会随着用户操作和系统状态不断变化。

常见生命周期方法包括：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
}

override fun onStart() {
    super.onStart()
}

override fun onResume() {
    super.onResume()
}

override fun onPause() {
    super.onPause()
}

override fun onStop() {
    super.onStop()
}

override fun onDestroy() {
    super.onDestroy()
}
```

可以先用一条简单路线理解：

```text
创建 -> 可见 -> 可交互 -> 暂停 -> 不可见 -> 销毁
```

对应关系大致是：

- `onCreate`：Activity 被创建，适合做初始化。
- `onStart`：Activity 即将对用户可见。
- `onResume`：Activity 可以和用户交互。
- `onPause`：Activity 即将失去焦点。
- `onStop`：Activity 已经不可见。
- `onDestroy`：Activity 即将被销毁。

生命周期不是死记硬背出来的，最好的学习方式是用日志观察。

## 第五部分：用 Logcat 观察生命周期

你可以在 Activity 中加入日志：

```kotlin
private const val TAG = "MainActivity"

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "onCreate")
}

override fun onStart() {
    super.onStart()
    Log.d(TAG, "onStart")
}

override fun onResume() {
    super.onResume()
    Log.d(TAG, "onResume")
}

override fun onPause() {
    super.onPause()
    Log.d(TAG, "onPause")
}

override fun onStop() {
    super.onStop()
    Log.d(TAG, "onStop")
}

override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "onDestroy")
}
```

然后尝试执行这些操作：

- 启动 App。
- 按 Home 键回到桌面。
- 再次打开 App。
- 旋转屏幕。
- 点击返回键退出页面。

观察 Logcat 中生命周期方法的执行顺序。你会发现 Android 页面并不是“打开”和“关闭”这么简单，它有一套由系统管理的状态变化。

## 第六部分：Intent 是什么

Intent 可以理解为 Android 组件之间传递意图的对象。它常用于：

- 启动另一个 Activity。
- 传递页面参数。
- 请求系统或其他应用完成某个动作。

显式 Intent 示例：

```kotlin
val intent = Intent(this, DetailActivity::class.java)
startActivity(intent)
```

这里的意思是：从当前 Activity 跳转到 `DetailActivity`。

传递参数：

```kotlin
val intent = Intent(this, DetailActivity::class.java)
intent.putExtra("title", "Activity 入门")
startActivity(intent)
```

在目标 Activity 中读取参数：

```kotlin
val title = intent.getStringExtra("title") ?: "默认标题"
```

注意这里再次用到了第二课的空安全。`getStringExtra` 可能返回 `null`，所以我们用 `?:` 提供默认值。

## 第七部分：创建第二个 Activity

本节可以创建一个简单的详情页，例如 `DetailActivity`。

步骤如下：

- 新建 `DetailActivity.kt`。
- 让它继承 `ComponentActivity`。
- 在 `AndroidManifest.xml` 中声明它。
- 从 `MainActivity` 使用 Intent 启动它。
- 在 `DetailActivity` 中读取并展示参数。

清单文件中声明：

```xml
<activity android:name=".DetailActivity" />
```

示例代码：

```kotlin
class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("title") ?: "详情页"

        setContent {
            Text(text = title)
        }
    }
}
```

如果你使用的是传统 View 工程，也可以通过 `setContentView` 加载布局。课程前期会优先围绕现代 Android 工程讲解，但核心思想是一致的。

## 第八部分：返回键与任务栈

当你从 `MainActivity` 打开 `DetailActivity` 后，按返回键通常会回到 `MainActivity`。这是因为 Android 会把 Activity 按启动顺序放入任务栈中。

可以先用下面的方式理解：

```text
打开 MainActivity
任务栈：[MainActivity]

打开 DetailActivity
任务栈：[MainActivity, DetailActivity]

按返回键
任务栈：[MainActivity]
```

任务栈是后续理解页面跳转、登录流程、返回行为、Deep Link、启动模式以及 Framework 中 Activity 管理机制的重要基础。

本节只需要先建立第一印象：页面不是孤立存在的，系统会按照栈结构管理它们的打开和返回。

## 本节实践任务

### 基础任务

- 找到当前工程中的 `AndroidManifest.xml`。
- 找到入口 Activity 的 `MAIN` 和 `LAUNCHER` 配置。
- 在 `MainActivity` 中添加生命周期日志。
- 运行 App，观察启动时的生命周期顺序。
- 按 Home 键、返回键、重新打开 App，并记录日志变化。

### Android 工程任务

- 新建一个 `DetailActivity`。
- 在清单文件中声明 `DetailActivity`。
- 从 `MainActivity` 跳转到 `DetailActivity`。
- 通过 Intent 向 `DetailActivity` 传递一个标题。
- 在 `DetailActivity` 中读取标题并展示。

### 进阶任务

- 尝试传递一个数字参数，例如课程编号。
- 在 `DetailActivity` 中使用 `?:` 给缺失参数提供默认值。
- 观察从 `MainActivity` 跳转到 `DetailActivity` 时两个页面的生命周期日志。
- 按返回键后，观察两个 Activity 的生命周期变化。
- 尝试旋转屏幕，看看 Activity 是否重新创建。

## 思考题

- 为什么 Android 应用不需要我们手写 `main` 函数？
- `AndroidManifest.xml` 在应用启动过程中承担什么角色？
- `onCreate` 和 `onResume` 有什么区别？
- 为什么 `intent.getStringExtra` 的结果可能为空？
- Activity 任务栈为什么适合描述页面返回行为？
- 如果页面旋转后数据丢失，可能和什么机制有关？

## 常见问题

### 生命周期方法为什么会执行多次？

Activity 由系统管理。页面切到后台、回到前台、旋转屏幕、系统回收资源，都可能导致生命周期方法再次执行。不要假设 `onCreate` 只会在整个应用生命周期中执行一次。

### 是否所有初始化都应该放在 onCreate？

不一定。`onCreate` 适合做页面级初始化，但一些与可见性、交互状态、资源占用有关的逻辑，可能更适合放在 `onStart`、`onResume`、`onPause` 或 `onStop` 中。初学阶段先通过日志理解它们的执行时机。

### 为什么创建新 Activity 后要改 Manifest？

Activity 是 Android 应用组件，需要在清单文件中声明，系统才能识别并管理它。入口 Activity 还需要配置 `MAIN` 和 `LAUNCHER`。

### Intent 传参适合传复杂对象吗？

入门阶段建议只传字符串、数字、布尔值等简单数据。复杂对象传递会涉及序列化、Parcelable、页面状态和架构设计，后续课程会逐步展开。

## 本节小结

第三课让你第一次真正碰到 Android 应用的核心运行模型：系统通过 Manifest 找到入口 Activity，Activity 接收生命周期回调，页面之间通过 Intent 跳转，返回行为由任务栈管理。

从这一课开始，Android 不再只是一个可以运行的工程，而是一个被系统调度、由组件组成、随用户操作不断变化的应用。理解 Activity 和生命周期，是继续学习 Compose、Navigation、ViewModel 和 Framework 的重要基础。

## 下一节预告

下一节将继续深入 `03-activity-lifecycle-navigation`，聚焦 Intent、页面跳转、参数传递和返回栈，为后续 Navigation 学习做好准备。
