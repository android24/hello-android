# 13.2 从 setContentView / Compose 到 DecorView

第 13 章的入口问题很朴素：

```text
我们写下来的 UI，到底被放进了哪里？
```

无论你使用传统 XML + `setContentView()`，还是使用 Compose 的 `setContent {}`，最终都要进入 Activity 的窗口内容区域。

这一节，我们先从应用层最熟悉的 UI 设置动作，走到 `DecorView`。

## 本节剧情钩子

你写：

```kotlin
setContent {
    CourseHomeScreen()
}
```

或者：

```kotlin
setContentView(R.layout.activity_main)
```

你感觉像是“把页面画出来”。

但系统更像在做一件事：把你的内容塞进一个已经准备好的窗口外壳里。

这个外壳的根节点，就是 DecorView。

## 本节定位

本节建立应用 UI 到 DecorView 的第一段链路。

## 学习目标

学完本节后，你应该能够：

- 知道 `setContentView()` 和 Compose 内容都会进入 Activity 的窗口。
- 理解 DecorView 是窗口里的顶层 View。
- 知道内容区、状态栏、导航栏、系统装饰之间的关系。
- 能用 Layout Inspector 或日志观察页面根节点。

## 第一部分：setContentView 做了什么

传统 View 体系里，我们常写：

```kotlin
setContentView(R.layout.activity_main)
```

这行代码不是直接把 XML 扔给屏幕。

更准确地说，它会把布局内容安装到 Activity 对应 Window 的内容区域。

简化理解：

```text
Activity.setContentView()
  -> Window.setContentView()
      -> 把业务布局放进 DecorView 的 content 区域
```

Activity 是调用入口，Window 才是承载页面内容的窗口对象。

## 第二部分：Compose 的 setContent 也要进窗口

Compose 看起来和 XML 完全不同：

```kotlin
setContent {
    MaterialTheme {
        CourseHomeScreen()
    }
}
```

但它仍然运行在 Android 窗口体系里。

Compose 会创建能够承载 Compose 内容的宿主 View，并把它接入 Activity 的内容区域。也就是说，Compose 改变了 UI 编写方式，但没有绕开 Activity、Window、ViewRootImpl、WMS 这条系统显示链路。

可以这样记：

```text
Compose 是现代 UI 表达方式，
Window 仍然是系统显示管理的入口。
```

## 第三部分：DecorView 是什么

DecorView 可以理解为一个窗口里的顶层 View。

它不仅承载业务内容，还和系统装饰有关，例如：

- 状态栏区域。
- 导航栏适配。
- ActionBar 或标题栏。
- 内容区域。
- Window Insets 分发。

你的业务页面一般不是直接成为整个窗口的根，而是被放到 DecorView 的内容容器中。

简化结构可以这样看：

```text
DecorView
  -> system decor
  -> content parent
      -> your XML root / Compose host
```

## 第四部分：为什么要关心 DecorView

DecorView 不是只在源码里出现的名词。

很多实际问题都和它有关：

- 全屏、沉浸式状态栏为什么会影响内容布局？
- `fitsSystemWindows` 和 WindowInsets 为什么难理解？
- Dialog 为什么也有自己的窗口和根视图？
- 为什么有时截图会包含状态栏，有时不包含？
- 为什么输入法弹起会改变窗口可见区域？

当你知道业务 UI 并不是直接贴在屏幕上，而是放在 DecorView 下面，很多边界问题就会清晰得多。

## 第五部分：观察 DecorView

可以通过几种方式观察：

```kotlin
val decorView = window.decorView
Log.d("WindowLab", "decorView=${decorView.javaClass.name}")
```

也可以通过 Layout Inspector 查看运行时 View 树。

观察时重点看：

- 根节点是什么。
- 业务内容挂在哪里。
- Compose 页面是否也存在宿主 View。
- 状态栏、导航栏适配是否改变了根布局尺寸。

## 本节小挑战

### UI 去哪了

请补全这条链路：

```text
Activity.setContentView / setContent
  -> ?
      -> DecorView
          -> content 区域
              -> 业务 UI
```

## 本节实践任务

### 基础任务

- 在任意示例 Activity 中打印 `window.decorView`。
- 使用 Layout Inspector 找到页面根节点。
- 对照日志判断业务内容挂在什么位置。

### 进阶任务

- 尝试切换深色模式或横竖屏。
- 观察 DecorView 与业务内容尺寸是否变化。
- 写下系统装饰区域对内容布局的影响。

## 本节小结

`setContentView()` 和 Compose `setContent {}` 看起来是 UI 开发 API，但它们最终都要把内容放进 Activity 的 Window。DecorView 是窗口里的顶层 View，是业务 UI 进入系统显示链路前必须理解的关键节点。
