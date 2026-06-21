# 5.2 MVC：最朴素的分层尝试

MVC 是很多人接触架构时听到的第一个模式。它把应用拆成 Model、View、Controller 三个部分，试图让数据、界面和控制逻辑各自承担不同职责。

在 Android 里，MVC 经常被理解为：Activity 或 Fragment 作为 Controller，XML / Compose 作为 View，数据类和数据来源作为 Model。

## 本节定位

本节是架构演进的第一站。

MVC 的价值在于：它让我们第一次意识到，页面代码不应该什么都管。但在 Android 中，MVC 也很容易让 Activity 变成“超级控制器”。

## 学习目标

学完本节后，你应该能够：

- 理解 Model、View、Controller 的职责。
- 用课程列表功能描述 MVC 分工。
- 知道 Android 中 MVC 为什么容易让 Activity 变胖。
- 理解 MVC 的优点和局限。

## 第一部分：MVC 的三个角色

MVC 通常包括：

- Model：数据与业务对象。
- View：界面展示。
- Controller：接收用户操作，协调 Model 和 View。

以课程列表为例：

```text
Model：Lesson、LessonRepository
View：课程列表 UI
Controller：MainActivity
```

Controller 负责响应点击、获取数据、更新 View。

## 第二部分：Android 中的 MVC

在 Android 中，Activity 很容易承担 Controller：

```kotlin
class CourseActivity : Activity() {
    fun onRefreshClicked() {
        val lessons = repository.getLessons()
        renderLessons(lessons)
    }
}
```

这看起来很自然，但问题是 Activity 本身还要处理：

- 生命周期。
- 权限。
- 页面跳转。
- UI 初始化。
- 事件绑定。

再加上 Controller 职责，Activity 很快会变胖。

## 第三部分：MVC 的优点

MVC 的优点是简单直观：

- 容易理解。
- 适合小页面。
- 不需要太多额外类。
- 比所有代码完全混在一起更有方向。

对于非常小的 Demo，MVC 并不差。

## 第四部分：MVC 的问题

在 Android 中，MVC 常见问题是：

- Activity / Fragment 太胖。
- View 和 Controller 边界不清。
- 业务逻辑容易和生命周期混在一起。
- 单元测试困难。

当页面变复杂，MVC 很容易退化为“Activity 管一切”。

## 本节小挑战

请用 MVC 思路重写一个课程列表页面的职责表：

```text
Model：
View：
Controller：
```

然后问自己：Controller 会不会太忙？

## 本节实践任务

### 基础任务

- 找出第 3 章示例工程中 Activity 承担了哪些职责。
- 判断这些职责哪些像 Controller。
- 将课程数据类标记为 Model。

### 进阶任务

- 尝试把假数据获取逻辑从 Activity 拆到一个 `CourseModel`。
- 保留 Activity 作为 Controller。
- 观察代码是否更清晰。

## 本节小结

MVC 是架构意识的第一步：它告诉我们代码需要分角色。但在 Android 中，它也容易让 Activity 过重。下一节 MVP 会继续拆分，把页面逻辑从 Activity 中进一步拿出来。
