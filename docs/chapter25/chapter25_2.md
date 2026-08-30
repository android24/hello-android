# 25.2 模块边界、依赖方向与架构防腐

大型 Android 工程最容易坏掉的地方，往往不是某个函数。

而是边界。

一个边界清晰的工程，开发者会知道：

```text
这个功能应该放在哪个模块。
这个模块可以依赖谁。
这个接口应该暴露给谁。
这个实现细节不应该泄漏到哪里。
```

一个边界混乱的工程，最后会变成：

```text
首页依赖支付。
支付依赖课程。
课程依赖账号。
账号又依赖首页。
公共模块装下所有东西。
任何修改都像在拉一团打结的线。
```

## 本节先记住三句话

```text
模块化的目的不是让目录变多，而是让变化有边界。
依赖方向一旦失控，架构图就会变成装饰画。
大型工程要用规则保护边界，而不是只靠大家自觉。
```

## 贯穿案例：课程 App 的模块开始打架

`Hello Android 学习中心` 继续发展后，可能会出现这些模块：

```text
app
core:model
core:network
core:database
core:designsystem
core:analytics
feature:home
feature:course
feature:note
feature:download
feature:profile
feature:payment
```

一开始看起来很清楚。

但开发几个月后，问题来了：

```text
feature:course 想直接调用 feature:payment 的支付页面。
feature:home 想直接读取 note 模块的数据库实体。
core:designsystem 里开始出现业务状态。
core:network 被塞进了用户登录判断。
app 模块承担了越来越多胶水逻辑。
```

这时你要做的不是再建一个 `common`。

你要先问：

```text
模块的职责是什么？
依赖方向是什么？
跨模块通信靠什么？
公共能力和业务能力的边界在哪里？
```

## 现场侦探问题

如果你看到一个 `core:common` 模块里有这些类：

```text
UserRepository
CourseDetailState
PaymentResult
HomeBannerConfig
NetworkClient
DateFormatter
AnalyticsTracker
```

你会不会觉得它很方便？

方便是真的。

危险也是真的。

因为它可能已经从“公共能力模块”变成了“全工程垃圾桶”。一旦所有模块都依赖它，它里面任何业务变化都会影响整个工程。

## 学习目标

学完本节后，你应该能够：

- 区分 app、feature、core、domain、data 等模块职责。
- 解释依赖方向为什么必须稳定。
- 识别循环依赖、公共模块膨胀和业务泄漏。
- 设计跨模块通信方式。
- 用 Gradle、lint、review checklist 保护模块边界。

## 第一部分：常见模块类型

大型 Android 工程常见模块可以分成几类：

| 模块类型 | 职责 | 不应该做什么 |
| --- | --- | --- |
| app | 组装应用、导航入口、全局配置 | 承担具体业务细节 |
| feature | 承载具体业务页面和交互 | 直接依赖其他 feature 内部实现 |
| core | 提供通用基础能力 | 混入具体业务状态 |
| data | 数据源、Repository 实现、缓存 | 直接依赖 UI |
| domain | UseCase、业务规则、实体 | 依赖 Android UI 细节 |
| designsystem | 主题、组件、图标、交互规范 | 包含业务流程 |
| build-logic | 构建约定、插件、脚本 | 包含运行时代码 |

模块拆分不是越细越好。

好的拆分应该让变化更便宜。

## 第二部分：依赖方向

一个常见的依赖方向是：

```text
app
  -> feature:home
  -> feature:course
  -> feature:note

feature
  -> domain
  -> data interface
  -> core
  -> designsystem

data implementation
  -> network
  -> database
  -> datastore
```

更关键的是不要让依赖倒流：

```text
core 不依赖 feature
domain 不依赖 UI
data 不依赖 Compose 页面
feature 不直接访问其他 feature 的内部类
designsystem 不写业务状态
```

依赖方向稳定，工程才稳定。

可以用一张依赖风险图来判断问题：

```text
app
  -> feature:home
  -> feature:course
      -> feature:payment
          -> feature:profile

core:common
  -> feature:profile
```

这张图里至少有三个风险：

| 依赖关系 | 风险 | 建议 |
| --- | --- | --- |
| `feature:course -> feature:payment` | 课程模块知道支付模块实现，支付改动会影响课程编译 | 抽出 `PaymentNavigator` 或 payment contract |
| `feature:payment -> feature:profile` | 支付流程和个人资料耦合，后续账号体系调整会牵动支付 | 抽出用户身份接口或 domain usecase |
| `core:common -> feature:profile` | core 反向依赖业务模块，所有依赖 core 的模块都会被污染 | 立即阻断，业务类型移出 core |

判断模块依赖风险时，可以按四个等级看：

```text
P0：出现循环依赖，或者 core / domain 反向依赖 feature
P1：feature 直接依赖另一个 feature 的实现类
P2：公共模块开始承载业务状态，但还没有形成广泛依赖
P3：命名、目录或 contract 不清晰，暂未造成实际耦合
```

这张表的价值是把“我觉得架构不舒服”变成明确决策：

```text
哪些必须马上拆。
哪些可以随本次需求一起改。
哪些记录成技术债。
哪些只需要补文档和命名。
```

## 第三部分：模块化、组件化与插件化的区别

第 25 章需要把三个容易混用的词说清楚：

```text
模块化
组件化
插件化
```

它们都和“拆分”有关，但解决的问题不一样。

| 方向 | 核心目标 | 典型能力 | 治理风险 |
| --- | --- | --- | --- |
| 模块化 | 按职责拆分代码，降低维护和构建成本 | core / feature / app / data / domain | 依赖倒流、common 膨胀、模块过细 |
| 组件化 | 让业务能力具备独立边界和协作契约 | 独立运行、路由契约、接口隔离、团队分工 | 跨组件调用混乱、生命周期不统一、契约失控 |
| 插件化 | 在运行时动态加载能力或代码 | 宿主插件协议、动态交付、按需加载 | ClassLoader、资源、签名、安全、调试和发布复杂 |

可以这样理解：

```text
模块化解决代码组织问题。
组件化解决业务边界和团队协作问题。
插件化解决运行时动态交付问题。
```

所以第 25 章讲插件化时，不会重复第 18 章的 Dex、ClassLoader 和热修复原理。

这里更关注工程治理问题：

```text
插件谁负责发布？
宿主和插件如何约定接口？
插件资源如何隔离？
插件崩溃如何归因？
插件版本如何和宿主兼容？
插件是否影响签名、安全和隐私边界？
```

如果这些问题没有答案，插件化带来的动态能力很可能会变成动态风险。

## 第四部分：跨模块通信

feature 之间不建议互相偷看内部实现。

更好的方式有几类：

| 场景 | 推荐方式 |
| --- | --- |
| 页面跳转 | 由 app 层或 navigation contract 统一路由 |
| 业务能力调用 | 暴露接口或 UseCase |
| 事件通知 | 事件总线要谨慎，优先显式状态流 |
| 结果返回 | typed route result / shared ViewModel / contract |
| 插件式功能 | SPI、接口注册、Hilt multibinding |

例如课程模块想发起支付，不应该直接依赖支付页面实现：

```text
feature:course
  -> PaymentNavigator 接口

app
  -> 注入 PaymentNavigator 实现
      -> 打开 feature:payment 页面
```

这样课程模块只知道“我要发起支付”，不知道支付模块内部怎么做。

对于组件化工程，还要进一步明确：

```text
组件暴露什么路由？
组件暴露什么接口？
组件能否独立运行？
组件依赖哪些基础能力？
组件的埋点、错误码、日志和监控如何统一？
```

组件化不是把 feature 换个名字。

真正的组件化要让业务模块可以被独立理解、独立验证、独立协作。

## 第五部分：架构防腐

防腐不是拒绝变化。

防腐是防止外部变化污染内部模型。

常见防腐手段：

```text
DTO 和 domain model 分离
数据库 entity 和 UI state 分离
第三方 SDK 包一层 adapter
跨模块只暴露 contract
不要让网络错误码直接穿透到 UI
不要让业务模块到处读取 BuildConfig
```

例如支付 SDK 返回：

```text
code = "P2001"
message = "user cancel"
rawData = ...
```

业务层不应该到处判断 `"P2001"`。

更好的方式是转换成：

```kotlin
sealed interface PaymentResult {
    data object Success : PaymentResult
    data object UserCanceled : PaymentResult
    data class Failed(val reason: PaymentFailureReason) : PaymentResult
}
```

这就是防腐层的价值。

## 第六部分：如何保护边界

边界如果只靠口头约定，很快会被业务压力冲破。

可以用这些手段保护：

```text
Gradle 依赖约束
模块命名规范
API / implementation 区分
dependency analysis
custom lint
code review checklist
架构图和 ADR
定期依赖图检查
```

一个简单的 review checklist 可以是：

```text
这个模块是否依赖了不该依赖的 feature？
有没有把业务类放进 core？
有没有把 DTO 直接暴露给 UI？
有没有新增全局单例？
有没有绕过 contract 直接调用实现？
组件是否绕过路由契约直接依赖实现？
插件能力是否有宿主版本兼容检查？
```

## 第七部分：模块边界坏掉时怎么修

不要一上来大重构。

可以按四步做：

```text
第一步：画出当前依赖图
第二步：找出最危险的循环依赖和公共模块膨胀
第三步：抽出 contract 或 adapter
第四步：逐步把直接依赖改成接口依赖
```

修边界像拆线团，急不得。

每次只拆一个结，工程就会轻一点。

## 本节自测

- 为什么 feature 模块之间不应该随意互相依赖？
- `core:common` 膨胀通常意味着什么？
- 防腐层解决的核心问题是什么？
- 模块边界应该靠文档保护，还是靠工具和流程一起保护？

## 本节总结

模块边界决定大型工程的可演进能力。

好的模块化不是把代码切碎，而是让每一块代码知道：

```text
我是谁。
我能依赖谁。
谁能依赖我。
我应该暴露什么。
我应该隐藏什么。
```

当这些问题有答案，工程才不会越长越乱。
