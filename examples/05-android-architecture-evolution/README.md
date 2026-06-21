# 示例工程：Android 应用架构演进

## 对应章节

第5章 Android 应用架构演进

## 工程目标

本工程将用于配合第 5 章，对比同一个课程列表功能在 MVC、MVP、MVVM、MVI/UDF 和 Clean Architecture 下的不同组织方式。

它不是为了证明某一种架构永远最好，而是帮助你看清楚：每一种架构解决了什么问题，又带来了什么成本。

## 当前效果

运行后你会看到一个“架构对照实验室”：

- 顶部可以切换 MVC、MVP、MVVM、MVI、Clean 五种写法。
- 每种写法都展示同一个课程列表功能。
- 每种写法都有刷新按钮和“显示已完成课程”开关。
- 页面会提示当前模式的职责边界和代码特点。
- 你可以一边看页面，一边打开对应包里的代码做对比。

## 运行方式

1. 使用 Android Studio 打开 `examples/05-android-architecture-evolution`。
2. 等待 Gradle Sync 完成。
3. 选择模拟器或真机运行 `app`。
4. 在页面顶部切换不同架构模式。
5. 对照源码观察：同一个功能的状态、事件、数据来源分别放在哪里。

## 工程结构

本工程采用单工程多包结构：

```text
05-android-architecture-evolution/
  app/
    src/main/java/com/helloandroid/architecture/
      common/
      ui/
      mvc/
      mvp/
      mvvm/
      mvi/
      clean/
```

同一个“课程列表”功能，会分别放在不同包中实现。这样最适合对比：

- 哪些代码变少了？
- 哪些边界更清楚了？
- 哪些模式引入了更多文件？
- 哪种方式更容易测试？

## 覆盖知识点

- MVC
- MVP
- MVVM
- MVI / UDF
- Repository
- UseCase
- Clean Architecture
- UI State
- 单向数据流
- 架构取舍

## 练习任务

### 基础任务

- 用 MVC 写一个课程列表职责草图。
- 用 MVP 写出 View 接口和 Presenter。
- 用 MVVM 写出 UI State 和 ViewModel。
- 用 MVI 写出 Intent、State 和 Effect。
- 在 Clean Architecture 中找出 domain、data、presentation 的边界。

### 进阶任务

- 为课程列表设计 Repository 接口。
- 为刷新课程设计 UseCase。
- 比较五种写法的文件数量和职责边界。
- 写一段架构选择说明：如果这是你的项目，你会选哪种方式，为什么？
- 尝试给 `GetCourseLessonsUseCase` 写一个单元测试。

## 通关目标

完成本章后，你应该能说清楚：

- Activity 为什么容易变胖。
- Presenter 和 ViewModel 的区别。
- MVVM 和 MVI 的主要差异。
- Repository 和 UseCase 分别解决什么问题。
- Clean Architecture 什么时候值得引入。
