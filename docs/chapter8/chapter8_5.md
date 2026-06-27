# 8.5 测试替身、Mock、Fake 与 Hilt 测试替换

测试时最常见的问题之一是：真实依赖太重。

真实网络不稳定，真实数据库准备麻烦，真实登录状态难以控制。如果测试每次都依赖这些东西，测试就会变慢、变脆、变得没人愿意运行。

这时就需要测试替身。

## 本节定位

本节讲测试替身和 Hilt 测试替换。

你会看到：

- Fake 是什么。
- Mock 是什么。
- Stub 是什么。
- 为什么课程里优先使用 Fake。
- Hilt 如何在测试中替换真实依赖。

## 学习目标

学完本节后，你应该能够：

- 理解测试替身的作用。
- 区分 Fake、Mock、Stub 的使用场景。
- 为 Repository 或 DataSource 编写 Fake。
- 理解 Hilt 测试替换的价值。
- 避免测试误连真实环境。

## 第一部分：为什么需要测试替身

假设你要测试课程刷新：

```text
ViewModel -> UseCase -> Repository -> RemoteDataSource
```

如果测试真的请求网络，就会遇到：

- 网络可能断。
- 接口可能慢。
- 数据可能变化。
- 测试可能污染真实环境。

测试替身就是把不可控的真实依赖，换成可控的测试依赖。

## 第二部分：Fake、Mock、Stub

可以先这样理解：

```text
Fake：一个可工作的简化实现。
Mock：一个用来验证调用行为的对象。
Stub：一个只返回固定结果的对象。
```

例如：

- FakeRepository：用内存列表模拟真实 Repository。
- MockAnalytics：验证某个埋点方法是否被调用。
- StubCourseApi：每次都返回固定课程列表。

课程里会优先使用 Fake，因为它更贴近真实行为，也更适合学习。

## 第三部分：Fake 要可控

好的 Fake 不只是“返回假数据”，还应该能模拟不同场景。

例如：

```kotlin
class FakeCourseRepository : CourseRepository {
    var shouldFail = false

    override suspend fun refreshCourses(): RefreshReport {
        if (shouldFail) error("network error")
        return RefreshReport(changedCount = 3, source = "fake")
    }
}
```

这样你可以在测试里控制：

- 成功。
- 失败。
- 空数据。
- 慢响应。
- 特殊边界值。

Fake 越可控，测试越清楚。

## 第四部分：Hilt 测试替换

第 7 章里，我们用 Hilt 管理依赖。

测试时也可以利用 Hilt 的能力，把真实实现替换成测试实现：

```text
正式环境：
CourseRepository -> CourseRepositoryImpl

测试环境：
CourseRepository -> FakeCourseRepository
```

这样 UI 测试或集成测试就不必访问真实网络，也不必依赖真实后端状态。

Hilt 的价值不只是少写 new，更是让依赖替换有规则、有入口。

## 第五部分：避免测试连到真实环境

测试误连真实环境是一种很危险的错误。

可能出现：

- 测试创建了真实订单。
- 测试上传了假用户数据。
- 测试依赖线上数据导致结果不稳定。
- 测试失败时难以定位是代码问题还是环境问题。

所以测试环境应该明确：

- 使用 Fake 或 Mock 数据源。
- 使用测试专用 baseUrl。
- 禁止访问生产写接口。
- 对危险操作加保护。

这也是第 7 章多环境配置在第 8 章继续发挥作用的地方。

## 本节小挑战

请为课程 App 设计三个 Fake：

- `FakeCourseRepository`
- `FakeCourseRemoteDataSource`
- `FakeUserSession`

分别说明它们需要支持哪些测试场景。

## 本节实践任务

### 基础任务

- 为一个 Repository 接口写 Fake 实现。
- 给 Fake 增加成功和失败两种模式。
- 用 Fake 替换真实实现进行测试。

### 进阶任务

- 设计 Hilt 测试替换方案。
- 为 UI 测试准备稳定测试数据。
- 写一条规则：测试环境不能访问生产接口。

## 本节小结

测试替身让测试变得稳定、快速、可控。Hilt 则让替换依赖这件事有了工程化入口。学会使用 Fake 和依赖替换后，你就能把很多“只能手动点”的场景变成可自动验证的测试。
