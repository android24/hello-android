# 5.3 MVP：把 View 和业务逻辑拆开

MVP 在 Android 早期非常流行。它试图解决 MVC 中 Activity 过重的问题：把页面逻辑放到 Presenter，让 Activity 或 Fragment 更像 View。

MVP 的核心思想是：View 只负责展示和转发事件，Presenter 负责处理页面逻辑。

## 本节定位

本节是架构演进的第二站。

如果 MVC 中 Activity 容易成为 Controller 和 View 的混合体，那么 MVP 会明确拆出 Presenter，让页面逻辑更容易测试。

## 学习目标

学完本节后，你应该能够：

- 理解 Model、View、Presenter 的职责。
- 用接口描述 View。
- 理解 Presenter 如何响应用户事件。
- 说出 MVP 相比 MVC 的改进。
- 说出 MVP 在 Android 中的成本。

## 第一部分：MVP 的三个角色

MVP 包括：

- Model：数据和业务来源。
- View：界面展示，通常是 Activity / Fragment 实现接口。
- Presenter：处理用户事件，协调 Model，并通知 View 更新。

课程列表可以这样分：

```text
View：CourseListView
Presenter：CourseListPresenter
Model：LessonRepository
```

## 第二部分：View 接口

MVP 常用接口描述 View 能做什么：

```kotlin
interface CourseListView {
    fun showLoading()
    fun showLessons(lessons: List<Lesson>)
    fun showError(message: String)
}
```

Presenter 不直接依赖 Activity，而是依赖这个接口。

## 第三部分：Presenter

Presenter 负责页面逻辑：

```kotlin
class CourseListPresenter(
    private val view: CourseListView,
    private val repository: LessonRepository
) {
    fun loadLessons() {
        view.showLoading()
        val lessons = repository.getLessons()
        view.showLessons(lessons)
    }
}
```

这样 Activity 可以轻很多，只负责实现 `CourseListView`。

## 第四部分：MVP 的优点

MVP 的优点：

- 页面逻辑从 Activity 中抽离。
- Presenter 更容易单元测试。
- View 变得更薄。
- 职责比 MVC 更清晰。

在传统 View 体系中，MVP 曾经非常实用。

## 第五部分：MVP 的问题

MVP 的问题也明显：

- View 接口容易越来越大。
- Presenter 持有 View，需要处理生命周期解绑。
- 页面状态容易变成一堆 `showXxx` 方法。
- 和 Compose 的声明式 UI 思想不完全契合。

现代 Android 中，MVP 使用少了，但理解它有助于理解架构演进。

## 本节小挑战

请为课程列表写一个 `CourseListView` 接口，至少包含：

- 显示加载中。
- 显示课程列表。
- 显示错误。

然后思考：如果页面状态越来越多，这个接口会变成什么样？

## 本节实践任务

### 基础任务

- 定义 `CourseListView`。
- 定义 `CourseListPresenter`。
- 让 Presenter 调用 View 方法更新页面。

### 进阶任务

- 为 Presenter 写一个伪单元测试思路。
- 思考 Activity 销毁后 Presenter 持有 View 会有什么风险。
- 比较 MVP 和 MVC 中 Activity 的职责差异。

## 本节小结

MVP 让页面逻辑离开 Activity，是 Android 架构演进中的重要一步。但它也带来了接口膨胀和生命周期绑定问题。下一节 MVVM 会用 ViewModel 和状态承载页面变化。
