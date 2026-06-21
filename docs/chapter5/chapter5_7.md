# 5.7 从一个页面演进到可维护工程

第 5 章最后，我们把前面所有模式收束到一个实际问题：如何把一个课程列表页面，从“能跑”演进到“可维护”。

这节不是再讲一个新模式，而是做一次工程复盘。

## 本节定位

本节是 `05-android-architecture-evolution` 的总结和综合实践。

它会把 MVC、MVP、MVVM、MVI/UDF、Repository、UseCase 和 Clean Architecture 放回同一个功能里比较。

## 学习目标

学完本节后，你应该能够：

- 比较 MVC、MVP、MVVM、MVI 的适用场景。
- 判断一个页面是否需要更复杂架构。
- 识别 Activity / ViewModel / Repository 过胖的问题。
- 设计一个课程列表功能的演进路线。
- 用架构语言解释自己的代码组织选择。

## 第一部分：同一个功能，不同阶段

课程列表功能可以这样演进：

```text
阶段 1：Activity 直接写所有逻辑
阶段 2：MVC 拆出 Model
阶段 3：MVP 拆出 Presenter
阶段 4：MVVM 引入 ViewModel 和 UI State
阶段 5：MVI / UDF 统一事件和状态流
阶段 6：Repository / UseCase 管理业务边界
```

每一步都不是为了“更高级”，而是为了解决上一阶段暴露出来的问题。

## 第二部分：如何选择架构

可以用几个问题判断：

- 页面是否复杂？
- 状态是否很多？
- 数据来源是否不止一个？
- 是否需要离线缓存？
- 是否需要多人协作？
- 是否需要单元测试？
- 未来需求是否可能频繁变化？

如果答案大多是否，简单架构就够。如果答案大多是，就需要更清晰的分层。

## 第三部分：常见坏味道

需要警惕这些信号：

- Activity 超过几百行，并且混合网络、数据库、导航。
- ViewModel 里同时写 UI 状态、网络实现、数据库 SQL、复杂业务规则。
- Repository 变成所有模块都依赖的大杂烩。
- UI State 无法完整描述页面。
- 点击事件散落在多个地方，难以追踪。

这些都是架构需要调整的信号。

## 第四部分：课程列表推荐结构

一个较清晰的结构可以是：

```text
feature/course/
  ui/
    CourseListScreen.kt
    CourseListViewModel.kt
    CourseListUiState.kt
  domain/
    GetLessonsUseCase.kt
    RefreshLessonsUseCase.kt
    Lesson.kt
  data/
    LessonRepositoryImpl.kt
    remote/
    local/
```

这不是唯一答案，但它表达了清晰边界。

## 第五部分：架构复盘表

可以用表格复盘：

| 模式 | 解决的问题 | 可能的代价 | 适合场景 |
| :-- | :-- | :-- | :-- |
| MVC | 初步分离数据和页面 | Activity 容易变胖 | 小 Demo |
| MVP | 页面逻辑离开 View | View 接口和生命周期成本 | 传统 View 项目 |
| MVVM | 状态集中在 ViewModel | ViewModel 可能膨胀 | 现代 Android 常用 |
| MVI/UDF | 状态流转清晰 | 概念和样板代码更多 | 复杂交互页面 |
| Clean Architecture | 业务边界清楚 | 抽象和文件数量增加 | 中大型项目 |

## 本节小挑战

请选一个你最熟悉的页面，给它做一次架构体检：

- 哪个文件最胖？
- 哪段逻辑最难测试？
- 哪些状态最容易出错？
- 如果需求变化，最可能改哪里？

这比背任何架构定义都更有价值。

## 本节实践任务

### 基础任务

- 画出课程列表功能的架构图。
- 标出 UI、ViewModel、Repository、UseCase 的职责。
- 给每一层写一句边界说明。

### 进阶任务

- 用 MVVM 方式重构一个简单页面。
- 再思考是否需要 MVI。
- 尝试为 Repository 写一个接口。
- 为一个 UseCase 写伪测试。

## 本章小结

第 5 章的重点不是让你迷信某种架构，而是让你建立判断力：知道代码为什么要拆，拆到哪里合适，什么时候应该停下来。

当你能说清楚 MVC、MVP、MVVM、MVI 和 Clean Architecture 各自解决什么问题时，你就已经开始从“会写功能”走向“会组织工程”。
