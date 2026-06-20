# 3.2 Intent、页面跳转与参数传递

上一节我们认识了 Activity、生命周期和应用入口。现在继续向前：一个页面如何打开另一个页面？数据如何从首页传到详情页？返回键为什么能回到上一个页面？

这些问题都绕不开 Intent 和任务栈。本节会围绕显式 Intent、参数传递、默认值处理和返回行为展开。

## 本节定位

本节属于 `03-activity-lifecycle-navigation` 的第二部分。

3.1 解决的是“Activity 如何被系统创建和管理”，3.2 解决的是“Activity 之间如何协作”。

本节重点包括：

- 使用显式 Intent 打开目标 Activity。
- 使用 `putExtra` 传递简单参数。
- 使用 `getStringExtra`、`getIntExtra` 读取参数。
- 为缺失参数提供默认值。
- 观察页面跳转前后的生命周期变化。
- 理解返回键与任务栈的第一层关系。

## 学习目标

学完本节后，你应该能够：

- 从 `MainActivity` 打开 `DetailActivity`。
- 使用 Intent 传递字符串和数字。
- 在目标 Activity 中安全读取参数。
- 理解参数缺失时为什么需要默认值。
- 通过 Logcat 观察两个 Activity 的生命周期顺序。
- 解释按返回键后为什么会回到上一个页面。

## 第一部分：显式 Intent

显式 Intent 会明确指定要打开哪个组件。

```kotlin
val intent = Intent(this, DetailActivity::class.java)
startActivity(intent)
```

这段代码表达的是：从当前 Activity 打开 `DetailActivity`。

显式 Intent 适合同一个 App 内部页面跳转，例如：

- 首页打开详情页。
- 列表页打开编辑页。
- 登录页打开主页。

## 第二部分：传递参数

打开详情页时，通常需要把一些数据传过去。例如课程编号、标题和描述：

```kotlin
val intent = Intent(this, DetailActivity::class.java).apply {
    putExtra(EXTRA_LESSON_INDEX, lesson.index)
    putExtra(EXTRA_LESSON_TITLE, lesson.title)
    putExtra(EXTRA_LESSON_DESCRIPTION, lesson.description)
}
startActivity(intent)
```

这里的 `EXTRA_LESSON_INDEX`、`EXTRA_LESSON_TITLE`、`EXTRA_LESSON_DESCRIPTION` 是参数 key。建议把它们定义成常量，避免手写字符串时拼错。

```kotlin
const val EXTRA_LESSON_INDEX = "com.helloandroid.navigation.EXTRA_LESSON_INDEX"
```

## 第三部分：读取参数

在目标 Activity 中，可以通过 `intent` 读取参数：

```kotlin
val index = intent.getIntExtra(EXTRA_LESSON_INDEX, 0)
val title = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: "默认标题"
val description = intent.getStringExtra(EXTRA_LESSON_DESCRIPTION) ?: "默认描述"
```

注意：

- `getIntExtra` 需要提供默认值。
- `getStringExtra` 可能返回 `null`。
- 字符串参数建议使用 `?:` 提供默认值。

这正好呼应了第 1 章 Kotlin 空安全。

## 第四部分：为什么要提供默认值

页面参数可能缺失，原因包括：

- 调用方忘记传。
- key 写错。
- 页面被系统以特殊方式重建。
- 后续代码改动导致协议不一致。

如果不处理缺失参数，页面可能显示异常，甚至崩溃。

更稳妥的做法是：

```kotlin
private fun readLessonFromIntent(): NavigationLesson {
    return NavigationLesson(
        index = intent.getIntExtra(EXTRA_LESSON_INDEX, 0),
        title = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: getString(R.string.default_detail_title),
        description = intent.getStringExtra(EXTRA_LESSON_DESCRIPTION)
            ?: getString(R.string.default_detail_description)
    )
}
```

这样即使参数缺失，详情页仍然可以安全显示。

## 第五部分：页面跳转时生命周期如何变化

从 `MainActivity` 打开 `DetailActivity` 时，通常会看到类似顺序：

```text
MainActivity: onPause
DetailActivity: onCreate
DetailActivity: onStart
DetailActivity: onResume
MainActivity: onStop
```

按返回键时，可能看到：

```text
DetailActivity: onPause
MainActivity: onRestart
MainActivity: onStart
MainActivity: onResume
DetailActivity: onStop
DetailActivity: onDestroy
```

具体日志顺序可能因系统版本和设备状态略有差异。重要的是理解：打开新页面并不是简单地“覆盖显示”，系统会同时管理两个 Activity 的生命周期。

## 第六部分：返回键与任务栈

当你从首页打开详情页，任务栈可以粗略理解为：

```text
[MainActivity]
```

打开详情页后：

```text
[MainActivity, DetailActivity]
```

按返回键后：

```text
[MainActivity]
```

这就是为什么详情页按返回键通常会回到首页。任务栈是后续学习 Navigation、启动模式、Deep Link 和 Framework 中 Activity 管理机制的重要基础。

## 第七部分：示例工程对应关系

在 `examples/03-activity-lifecycle-navigation` 中：

- `MainActivity.kt`：展示课程列表，点击卡片打开详情页。
- `DetailActivity.kt`：读取 Intent 参数并展示详情。
- `NavigationContract.kt`：集中定义 Intent 参数 key。
- `LessonModels.kt`：定义课程数据。
- Logcat 标签：`MainActivityLifecycle`、`DetailActivityLifecycle`。

建议你一边运行示例，一边对照本节内容阅读代码。

## 本节实践任务

### 基础任务

- 打开第 3 章示例工程。
- 点击任意课程卡片进入详情页。
- 确认详情页显示了课程编号、标题和描述。
- 打开 Logcat，过滤 `MainActivityLifecycle`。
- 再过滤 `DetailActivityLifecycle`。

### Android 工程任务

- 在 Intent 中新增一个参数，例如 `difficulty`。
- 在 `NavigationContract.kt` 中定义新的 key。
- 在 `MainActivity` 中通过 `putExtra` 传入难度。
- 在 `DetailActivity` 中读取并显示难度。
- 给缺失难度设置默认值。

### 进阶任务

- 故意注释掉某个 `putExtra`，观察详情页默认值是否生效。
- 按 Home 键再回到 App，观察生命周期日志。
- 从详情页按返回键，观察两个 Activity 的日志顺序。
- 尝试旋转屏幕，观察详情页是否重新读取 Intent 参数。

## 思考题

- 显式 Intent 和隐式 Intent 有什么区别？
- 为什么 Intent 参数 key 建议定义成常量？
- 为什么 `getStringExtra` 需要处理 `null`？
- 返回键为什么能回到上一个 Activity？
- 页面参数过多时，是否还适合全部通过 Intent 传递？

## 常见问题

### Intent 能传复杂对象吗？

可以，但需要对象支持序列化或 Parcelable。入门阶段建议先传字符串、数字、布尔值等简单参数。复杂对象传递会在后续架构课程中重新讨论。

### 为什么我打开详情页后 MainActivity 会 onStop？

当详情页完全遮住首页时，首页不可见，所以会进入 `onStop`。如果新页面没有完全遮住，生命周期表现可能不同。

### 返回按钮和系统返回键有什么区别？

示例工程中的按钮调用 `finish()`，系统返回键默认也会结束当前 Activity。两者在这个示例中的结果接近，但真实项目里你可能会拦截返回行为或处理未保存数据。

### 是否应该把大量业务数据都塞进 Intent？

不建议。Intent 更适合传递轻量参数，例如 id、标题、简单标记。复杂数据可以通过数据库、Repository、ViewModel 或共享状态重新加载。

## 本节小结

本节让第 3 章形成了多页面基础闭环：`MainActivity` 使用显式 Intent 打开 `DetailActivity`，通过 Extra 传递参数，详情页安全读取参数，并通过返回键回到上一页。

下一步可以继续学习 Navigation Compose，把多页面跳转从手写 Intent 逐步演进为声明式导航图。
