# 7.8 综合实践：课程 App 工程化重组

第 7 章最后，我们把 Hilt、模块化、Gradle 配置和工程边界放回同一个实践：重组课程 App。

目标不是一口气做成大型商业项目，而是让课程 App 从“功能能跑”进入“工程能长大”的阶段。

这一节可以看成第 7 章的最终任务：你接手了一个已经能跑的课程 App，现在要让它具备团队协作、环境切换、测试替换和持续扩展的能力。它不需要一夜之间变成巨型工程，但要从今天开始有清晰的骨架。

## 本节定位

本节是第 7 章综合实践。

它会把前面内容合在一起：

- 用 Hilt 装配对象。
- 用接口隔离实现。
- 用模块拆分边界。
- 用 Gradle 管理环境。
- 用规则约束依赖方向。

## 学习目标

学完本节后，你应该能够：

- 设计课程 App 的模块结构。
- 为课程列表建立 Hilt 注入链路。
- 拆分 feature、domain、data、core。
- 设计 dev/prod 两套环境配置。
- 用工程图解释模块依赖。

## 第一部分：目标结构

可以从这个结构开始：

```text
app/
core/
  model/
  network/
  database/
  common/
feature/
  course/
data/
  course/
domain/
  course/
```

如果项目还小，也可以先简化：

```text
app/
core-model/
core-network/
feature-course/
```

课程里更重要的是理解拆分理由，而不是一次拆到最细。

## 第二部分：对象注入链路

课程列表可以这样装配：

```text
CourseScreen
  -> CourseViewModel
      -> GetCoursesUseCase
          -> CourseRepository
              -> CourseRemoteDataSource
              -> CourseLocalDataSource
```

Hilt 负责创建对象，模块边界负责限制依赖方向。

## 第三部分：模块依赖建议

可以设计成：

```text
app -> feature-course
feature-course -> domain-course
feature-course -> core-model
data-course -> domain-course
data-course -> core-network
data-course -> core-database
```

其中：

- app 负责组装入口。
- feature-course 负责课程页面。
- domain-course 负责业务接口和 UseCase。
- data-course 负责数据实现。
- core 模块负责通用能力。

## 第四部分：环境配置

至少准备两个环境：

```text
devDebug：开发调试
prodRelease：正式发布
```

可以配置：

- 不同 baseUrl。
- 不同 appName。
- 不同日志开关。
- 不同包名后缀。

这样读者能开始理解：App 不是只在本机运行，还要走向测试、预发和发布。

## 第五部分：重组路线

不要一次性大拆。推荐路线：

```text
第 1 步：整理包结构
第 2 步：抽 Repository 接口
第 3 步：引入 Hilt
第 4 步：拆 core-model
第 5 步：拆 feature-course
第 6 步：加入 dev/prod 配置
第 7 步：补充边界说明
```

每一步都应该能运行。

这很重要：工程化不是炸掉重来，而是边走边稳。

更推荐把它当作一组小型升级任务：每完成一步，都让工程比之前更清楚一点。今天抽出 Repository 接口，明天接入 Hilt，后天拆出 core-model。读者会感觉自己不是在背工程规范，而是在亲手把一团线整理成一张图。

## 第六部分：复盘表

| 问题 | 工程化手段 |
| :-- | :-- |
| `findViewById` 和点击监听样板代码很多 | Butter Knife 曾经解决过，现代项目优先 View Binding / Compose |
| 对象到处手动创建 | Hilt |
| 接口和实现绑死 | Repository 接口 + Binds |
| 所有代码挤在 app | 模块化 |
| 环境配置写死 | Gradle flavor |
| 模块互相乱调 | 依赖方向规则 |
| 测试不好替换依赖 | 构造函数注入 + Fake 实现 |

## 本节小挑战

请给课程 App 写一份“工程化说明书”，至少包含：

- 模块结构图。
- 每个模块职责。
- Hilt 注入链路。
- Butter Knife、View Binding、Compose、Hilt 分别解决哪类问题。
- dev/prod 环境差异。
- 模块依赖规则。

这份说明书就是未来协作的地图。

如果一位新同学只靠这份说明书，就能知道“课程模型在哪里”“网络实现在哪里”“测试环境怎么切”“Repository 为什么要有接口”，说明第 7 章的实践目标就达成了。

## 本节实践任务

### 基础任务

- 画出课程 App 模块结构。
- 标出 Hilt 注入链路。
- 写出 dev/prod 配置差异。
- 为 Repository 定义接口。

### 进阶任务

- 创建一个多 Module 示例工程。
- 将课程模型移动到 core 模块。
- 将课程页面移动到 feature 模块。
- 使用 Hilt 注入 Repository。
- 添加 dev/prod flavor。

## 本章小结

第 7 章把课程从“代码能写”推进到“工程能组织”。Hilt 让对象装配清晰，模块化让代码边界清晰，Gradle 配置让构建环境清晰，工程治理让团队协作清晰。

当你能解释一个对象从哪里来、一个模块为什么存在、一个环境如何构建时，你就开始具备真正的 Android 工程化能力。
