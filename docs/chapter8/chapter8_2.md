# 8.2 单元测试：保护 UseCase 与业务规则

单元测试是最适合入门测试的地方。它运行快、反馈清楚，不需要启动模拟器，也不需要真的打开页面。

第 5 章里我们引入 UseCase，第 7 章里我们用 Hilt 装配 UseCase。到了第 8 章，UseCase 会成为第一批被测试保护的对象。

## 本节定位

本节讲单元测试的基本思路。

目标不是一次讲完所有测试框架，而是先建立一个朴素判断：

```text
有规则的地方，就值得写测试。
```

## 学习目标

学完本节后，你应该能够：

- 理解单元测试的作用。
- 为 UseCase 编写测试思路。
- 使用 Fake Repository 替代真实数据源。
- 判断测试应该断言什么。
- 避免只测试实现细节。

## 第一部分：什么是单元测试

单元测试关注一个很小的对象。

例如：

```text
RefreshCoursesUseCase
```

它不关心 Compose 页面怎么画，也不关心 Retrofit 如何请求网络。它只关心一件事：

```text
调用刷新用例时，是否正确触发 Repository，并返回正确结果。
```

测试越小，失败时越容易定位。

## 第二部分：先从 UseCase 开始

UseCase 通常很适合测试，因为它表达业务动作。

例如：

```kotlin
class RefreshCoursesUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): RefreshReport {
        return repository.refreshCourses()
    }
}
```

可以测试：

- 是否调用了 Repository。
- Repository 成功时返回什么。
- Repository 失败时是否抛出异常或返回错误结果。
- 是否对输入做了业务判断。

如果 UseCase 暂时只是转发，也可以先不急着写复杂测试。等它承载更多业务规则时，再补测试会更有价值。

## 第三部分：Fake Repository

测试里不应该直接依赖真实网络或真实数据库。

可以写一个 Fake：

```kotlin
class FakeCourseRepository : CourseRepository {
    var refreshCalled = false

    override fun observeDashboard(): Flow<CourseDashboard> {
        return flowOf(fakeDashboard)
    }

    override suspend fun refreshCourses(): RefreshReport {
        refreshCalled = true
        return RefreshReport(
            changedCount = 3,
            source = "fake"
        )
    }
}
```

Fake 的好处是可控：你可以让它成功、失败、返回空列表，或者模拟某个边界状态。

## 第四部分：断言行为而不是实现

测试应该更关注外部行为。

好的断言：

```text
刷新成功后，返回 changedCount = 3。
```

不太好的断言：

```text
内部第 2 行代码必须先执行。
```

测试如果太依赖内部实现，重构时会很脆弱。我们希望测试保护业务规则，而不是把实现锁死。

## 第五部分：命名要像说明书

测试名称最好能描述场景。

例如：

```text
refreshCourses_returnsReport_whenRepositorySucceeds
refreshCourses_keepsOldLessons_whenRemoteFails
```

读测试名称时，应该像读一份业务规则说明书。

这也是测试隐藏的好处：它会迫使你把需求说清楚。

## 本节小挑战

请为第 7 章示例工程中的 `RefreshCoursesUseCase` 设计三个测试场景：

- Repository 成功。
- Repository 失败。
- Repository 返回空课程列表。

每个场景写出输入、动作和期望结果。

## 本节实践任务

### 基础任务

- 找到一个 UseCase。
- 写出它依赖的接口。
- 为这个接口设计 Fake 实现。
- 写出 3 条测试用例名称。

### 进阶任务

- 为 Repository 的缓存规则设计测试。
- 为错误结果设计测试。
- 思考哪些业务规则不适合只用单元测试验证。

## 本节小结

单元测试不是为了炫耀覆盖率，而是为了保护业务规则。UseCase 和 Repository 是最适合开始的地方，因为它们离业务近、离 UI 远，测试成本低，收益很高。
