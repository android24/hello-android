# 26.3 从业务需求到模块架构设计

需求确定以后，不要立刻把所有代码都塞进 `app` 模块。

毕业项目要训练的第一项资深能力，就是把复杂度放到合适的位置。

模块架构不是为了显得高级。

它是为了让项目继续长大时，不至于变成一团互相缠住的线。

## 本节先记住三句话

```text
模块边界不是目录整理，而是依赖关系的秩序。
业务模块应该依赖抽象能力，不应该到处伸手拿具体实现。
好的架构不是没有复杂度，而是复杂度有位置、有方向、有证据。
```

## 建议模块结构

第 26 章毕业项目可以采用这样的模块结构：

```text
app
feature-course
feature-diagnosis
feature-governance
domain-course
data-course
core-ui
core-model
core-observability
core-governance
```

每个模块的职责如下：

| 模块 | 职责 |
| --- | --- |
| `app` | 应用入口、主题、导航图、依赖装配 |
| `feature-course` | 课程首页、章节列表、详情、收藏、笔记、离线入口 |
| `feature-diagnosis` | 性能、稳定性、Framework 链路和事故观察页面 |
| `feature-governance` | 发布检查、风险评分、治理报告和终章答辩材料 |
| `domain-course` | 课程实体、UseCase、Repository 接口、业务规则 |
| `data-course` | 本地数据、fake network、缓存、同步任务实现 |
| `core-ui` | 通用组件、主题 token、空态、错误态、加载态 |
| `core-model` | 跨模块共享的轻量模型和枚举 |
| `core-observability` | traceId、结构化日志、性能事件、诊断事件 |
| `core-governance` | 风险规则、评分模型、发布门禁和报告生成 |

这套结构不是唯一答案。

但它有一个明确目标：

```text
业务功能、数据实现、观测能力和治理能力不要混在一起。
```

## 依赖方向

建议依赖方向如下：

```text
app
  -> feature-course
  -> feature-diagnosis
  -> feature-governance

feature-course
  -> domain-course
  -> core-ui
  -> core-observability

feature-diagnosis
  -> domain-course
  -> core-ui
  -> core-observability

feature-governance
  -> core-ui
  -> core-governance
  -> core-observability

data-course
  -> domain-course
  -> core-model
  -> core-observability

domain-course
  -> core-model

core-*
  -> 不依赖 feature-*
```

最重要的规则是：

```text
feature 可以使用 domain。
data 可以实现 domain 的接口。
domain 不知道 data。
core 不知道 feature。
app 负责装配，而不是承载业务逻辑。
```

这就是第 25 章讲过的工程治理在毕业项目中的第一次落地。

## 为什么要有 domain-course

很多初学者会问：

```text
课程列表直接在 ViewModel 里写不行吗？
```

小 Demo 可以。

毕业项目不建议。

因为课程业务很快会出现这些规则：

```text
章节是否已完成
进度如何计算
收藏和笔记如何合并
离线任务是否允许重复添加
同步失败后如何重试
哪些章节属于 Framework 阶段
哪些章节可以进入终章答辩
```

这些规则不应该散落在页面里。

更好的方式是：

```text
UI 负责展示和交互
ViewModel 负责状态编排
UseCase 负责业务动作
Repository 接口负责定义数据能力
data 模块负责具体实现
```

这样你以后换存储、换网络、换 UI，都不会把整条链路拆掉。

## 数据流设计

以“打开课程详情”为例：

```text
CourseDetailScreen
  -> CourseDetailViewModel
      -> ObserveCourseDetailUseCase
          -> CourseRepository
              -> LocalCourseDataSource
              -> FakeCourseRemoteDataSource
          -> CourseDetailUiState
              -> CourseDetailScreen
```

如果用户点击收藏：

```text
CourseDetailScreen
  -> onFavoriteClick(courseId)
      -> CourseDetailViewModel
          -> ToggleFavoriteUseCase
              -> CourseRepository.updateFavorite()
                  -> LocalCourseDataSource
                      -> Room / DataStore / fake storage
          -> emit new UiState
```

这条链路看起来比“按钮里直接改变量”更长。

但它更像真实工程。

它让你可以回答：

```text
状态在哪里产生？
副作用在哪里发生？
数据一致性由谁保证？
失败后 UI 如何恢复？
这条链路如何测试？
这次操作如何埋点和诊断？
```

## 架构观察点

毕业项目的 Demo 不应该只把模块写出来，还应该让读者看到模块之间的关系。

可以在 `feature-diagnosis` 中提供一个架构观察页面：

| 观察卡片 | 展示内容 |
| --- | --- |
| 模块地图 | 展示 app、feature、domain、data、core 的依赖方向 |
| 数据流卡片 | 展示一次用户操作从 UI 到 data 再回到 UI 的路径 |
| 越界风险卡片 | 展示 feature 直接依赖 data、core 依赖 feature 等风险 |
| 可测试性卡片 | 展示哪些层可以单测，哪些层需要 fake 实现 |

这个页面的意义不是画图好看。

它是让学习者把第 5 章、第 7 章、第 25 章的架构知识看成一条连续能力。

## 常见坏味道

毕业项目中要主动避免这些情况：

| 坏味道 | 后果 |
| --- | --- |
| 所有逻辑都在 `MainActivity` | 页面越来越难改，生命周期问题难排查 |
| ViewModel 直接访问数据库实现 | 测试困难，数据实现和 UI 状态强绑定 |
| feature 模块互相依赖实现类 | 业务边界失效，组件无法独立演进 |
| core 模块放业务代码 | core 变成公共垃圾桶，后续很难拆 |
| 日志散落且无 traceId | 事故发生后无法串起证据 |
| 发布规则只写在文档里 | 治理无法被执行，靠人记忆迟早漏 |

如果你能在毕业项目中指出这些坏味道，并给出替代方案，它就不只是一个 App，而是一份工程判断力展示。

## 本节练习

请为毕业项目写一份模块说明，至少包含：

```text
模块列表
每个模块的职责
允许依赖谁
禁止依赖谁
一个核心用户操作的数据流
一个可能发生的架构越界案例
```

你可以先用 Markdown 写，不必一开始就画复杂图。

最重要的是把边界说清楚。

## 本节复盘

本节我们从需求进入模块架构：

```text
毕业项目需要 app、feature、domain、data、core 等清晰层次。
依赖方向要单向，业务规则要从页面中抽出来。
架构本身也要成为 Demo 的可观察内容。
```

下一节，我们会从一次用户操作开始，把业务链路继续向下追到 Framework。
