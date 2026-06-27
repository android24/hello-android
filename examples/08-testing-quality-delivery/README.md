# 示例工程：测试、质量保障与可交付

## 对应章节

第8章 测试、质量保障与可交付

## 工程目标

本工程用于配合第 8 章，把第 7 章已经模块化的课程 App 继续推进到“可测试、可检查、可交付”的状态。

它会围绕三个问题展开：

- 关键业务规则如何被测试保护？
- 测试环境如何替换真实依赖？
- 一个版本发布前应该经过哪些检查？

## 当前效果

本工程基于第 7 章的多 Module 课程 App 继续演进，已经补上第一批质量保障内容：

- 可运行的课程 App 页面。
- `RefreshCoursesUseCase` 单元测试。
- `CourseRepositoryImpl` 数据流测试。
- `CourseEngineeringViewModel` 状态测试。
- `CourseEngineeringScreen` Compose UI 测试。
- CI 检查清单。
- 发布前检查清单。
- GitHub Actions 示例配置。

运行 App 后，你会看到一个“第 8 章质量保障演练”页面。页面仍然展示课程、环境和 Hilt 注入链路，但中间的说明会聚焦测试金字塔：UseCase、Repository、ViewModel 和 Compose UI 分别保护什么。

## 运行方式

1. 使用 Android Studio 打开 `examples/08-testing-quality-delivery`。
2. 等待 Gradle Sync 完成。
3. 选择 `devDebug` 或 `prodDebug` 变体。
4. 运行 `app` 模块，观察质量保障演练页面。
5. 在 Android Studio 的测试面板中运行 `domain-course`、`data-course`、`feature-course` 下的测试。

如果工程里配置了 Gradle Wrapper，也可以参考这些命令：

```text
./gradlew :domain-course:testDebugUnitTest
./gradlew :data-course:testDebugUnitTest
./gradlew :feature-course:testDebugUnitTest
./gradlew :app:connectedDevDebugAndroidTest
./gradlew :feature-course:connectedDebugAndroidTest
```

## 工程结构

```text
08-testing-quality-delivery/
  app/
  core-model/
  core-network/
  domain-course/
  data-course/
  feature-course/
  quality/
    release-checklist.md
    ci-checklist.md
    github-actions-android-quality.yml
```

核心模块职责：

- `app`：应用入口、Hilt Application、构建环境配置。
- `core-model`：课程模型。
- `core-network`：网络环境配置。
- `domain-course`：Repository 接口、UseCase 和 UseCase 单元测试。
- `data-course`：Repository 实现、数据源和 Repository 测试。
- `feature-course`：Compose 页面、ViewModel、ViewModel 测试和 UI 测试。
- `quality`：CI 与发布前检查清单。

## 测试入口

```text
domain-course/src/test/
  RefreshCoursesUseCaseTest.kt

data-course/src/test/
  CourseRepositoryImplTest.kt

feature-course/src/test/
  CourseEngineeringViewModelTest.kt
  MainDispatcherRule.kt

feature-course/src/androidTest/
  CourseEngineeringScreenTest.kt

app/src/androidTest/
  HiltTestRunner.kt
  CourseRepositoryHiltReplacementTest.kt
```

这些测试分别对应第 8 章的几个重点：

- UseCase 测试保护业务动作。
- Repository 测试保护数据流和刷新规则。
- ViewModel 测试保护 UI State。
- Compose UI 测试保护关键用户路径。
- Hilt instrumentation 测试演示如何用 Fake Repository 替换正式绑定。

## 质量清单入口

- `quality/ci-checklist.md`：最小 CI 检查清单。
- `quality/release-checklist.md`：发布前检查清单。
- `quality/github-actions-android-quality.yml`：GitHub Actions 示例配置。

## 计划覆盖知识点

- 单元测试
- UseCase 测试
- Repository 测试
- Fake / Mock / Stub
- 协程测试
- Flow 测试
- ViewModel 状态测试
- Compose UI 测试
- Hilt 测试替换
- CI
- lint
- 发布前检查清单
- dev/prod 质量门禁

## 练习任务

### 基础任务

- 运行 `RefreshCoursesUseCaseTest`，观察 UseCase 如何通过 Fake Repository 测试。
- 运行 `CourseRepositoryImplTest`，观察 Repository 如何测试刷新和 Flow 输出。
- 运行 `CourseEngineeringViewModelTest`，观察 ViewModel 如何测试成功和失败状态。
- 阅读 `CourseEngineeringScreenTest`，理解 UI 测试保护的是关键用户路径。
- 阅读 `CourseRepositoryHiltReplacementTest`，理解测试环境如何替换 Hilt 依赖。
- 阅读 `quality/ci-checklist.md`，写下你认为必须保留的最小质量门禁。

### 进阶任务

- 为 `GetCourseDashboardUseCase` 新增测试。
- 给 `CourseRepositoryImplTest` 增加远端失败场景。
- 给 ViewModel 测试增加“失败时保留旧数据”的断言。
- 给 Compose UI 测试增加课程卡片展示断言。
- 将 `quality/github-actions-android-quality.yml` 迁移到真正项目的 `.github/workflows/` 目录。
- 为 `prodRelease` 补充团队自己的发布前检查表。

## 通关目标

完成本章后，你应该能说清楚：

- 为什么测试保护的是行为，而不是代码行数。
- UseCase、Repository、ViewModel、UI 分别适合测试什么。
- Fake、Mock、Stub 的区别。
- Hilt 如何帮助测试替换依赖。
- CI 和质量门禁如何保护主分支。
- 发布前检查清单为什么是交付能力的一部分。
