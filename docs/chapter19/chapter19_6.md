# 19.6 多进程、远程 Service、ContentProvider 与 isolatedProcess

前面几节讲了普通 App 进程。

本节进入多进程。

多进程是 Android 工程里很有诱惑力、也很容易踩坑的能力。

它可以做隔离，也会制造复杂度。

## 本节定位

本节负责回答：

- Android 多进程如何声明？
- `:remote` 和完整进程名有什么区别？
- 多进程里 Application 为什么会执行多次？
- 远程 Service、ContentProvider、isolatedProcess 分别适合什么场景？
- 多进程常见坑如何排查？

## 学习目标

学完本节后，你应该能够：

- 使用 `android:process` 创建远程进程。
- 解释多进程为什么不共享内存单例。
- 知道 ContentProvider 可能触发进程提前启动。
- 理解 isolatedProcess 的隔离意义。
- 能判断一个需求是否真的需要多进程。

## 第一部分：如何声明多进程

Manifest 中可以为组件配置：

```xml
<service
    android:name=".RemoteWorkerService"
    android:process=":worker" />
```

这表示该 Service 运行在：

```text
com.example.app:worker
```

如果写完整名字：

```xml
android:process="com.example.shared.worker"
```

则可能变成一个全局命名进程。

大多数业务场景使用 `:worker` 这种私有进程名更常见。

命名规则背后有一个边界：

```text
android:process=":worker"
  -> 私有进程名
  -> 实际进程名通常是 com.example.app:worker
  -> 只属于当前应用语义

android:process="com.example.worker"
  -> 全局进程名
  -> 语义上不是当前包名的私有后缀
```

普通业务优先使用私有进程名。

完整进程名容易让工程边界变模糊，除非你非常清楚它的目的。

## 第二部分：多进程不是多线程

多线程共享同一进程内存。

多进程不共享。

```text
主进程
  -> Singleton.instance = A

:worker 进程
  -> Singleton.instance = B
```

这两个单例不是同一个对象。

所以这些东西都要重新思考：

- 内存缓存。
- 登录态缓存。
- SDK 初始化。
- Repository 单例。
- 数据同步。
- 事件总线。
- 埋点队列。

如果业务代码默认“全 App 只有一份内存状态”，多进程会让这个假设直接失效。

更底层一点：

```text
主进程
  -> 自己的 ART runtime 状态
  -> 自己的 Java heap
  -> 自己的 ClassLoader 实例
  -> 自己的静态字段

:worker 进程
  -> 另一份 ART runtime 状态
  -> 另一份 Java heap
  -> 另一份 ClassLoader 实例
  -> 另一份静态字段
```

所以多进程里最危险的代码通常长这样：

```kotlin
object SessionManager {
    var token: String? = null
}
```

它看起来全局唯一，但只在“当前进程”唯一。

一旦进入 remote 进程，这个对象就是另一份。

## 第三部分：Application 会执行多次

每个 App 进程都有自己的 Application 实例。

所以：

```text
com.example.app
  -> Application.onCreate()

com.example.app:worker
  -> Application.onCreate()
```

这意味着：

- 初始化 SDK 可能执行多次。
- 主进程专属初始化会跑到 remote 进程。
- remote 进程启动会拖慢。
- 某些 SDK 不支持多进程，会出现状态错乱。

因此 Application 里常见做法是：

```text
判断当前 processName
  -> 主进程才做完整初始化
  -> remote 进程只做必要初始化
```

这不是形式主义，而是多进程项目的基本卫生。

主进程判断最好放在所有重初始化之前。

示意：

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val process = currentProcessName()

        if (process == packageName) {
            initMainProcessOnlySdk()
        }

        initProcessRequiredSdk(process)
    }
}
```

这里的关键不是代码长什么样，而是初始化分类：

| 初始化类型 | 是否每个进程都需要 |
| --- | --- |
| Crash 采集 | 通常每个进程都要 |
| 主 UI 路由 | 通常只主进程 |
| Push SDK | 可能 push 进程需要 |
| 数据库 | 看访问场景和多进程支持 |
| 图片库 / UI 组件 | 通常只 UI 进程 |
| 日志基础设施 | 每个进程都应带 processName |

## 第四部分：远程 Service 适合什么

远程 Service 可以把某些任务放到独立进程：

```xml
<service
    android:name=".UploadService"
    android:process=":upload" />
```

可能适合：

- 高风险 SDK 隔离。
- 大内存任务隔离。
- 长任务和主进程稳定性解耦。
- 插件或扩展能力隔离。

但它也带来成本：

- 进程启动成本。
- Binder 通信成本。
- 状态同步复杂。
- 崩溃归因复杂。
- 日志和监控必须区分进程。

所以不要为了“看起来高级”使用远程进程。

判断是否需要远程进程，可以问四个问题：

```text
这个任务是否高风险，崩溃后希望主进程仍存活？
这个任务是否占用大量内存，释放时希望整个进程退出？
这个任务是否需要独立权限或隔离边界？
这个任务是否愿意承担 IPC、启动和状态同步成本？
```

如果四个问题都答不上来，多半不该上多进程。

## 第五部分：ContentProvider 的提前启动

ContentProvider 有一个重要特点：

```text
进程启动时，Provider 通常会先于 Application.onCreate 被安装
```

很多 SDK 利用 Provider 自动初始化。

这带来便利，也带来风险：

- 启动路径被悄悄拉长。
- 多进程里 Provider 可能在 remote 进程初始化。
- 初始化顺序不符合业务预期。
- Provider authority 冲突会导致安装失败。

在多进程项目中，要特别关注 Provider 属于哪个进程。

Provider 初始化顺序可以这样记：

```text
进程启动
  -> installContentProviders
      -> Provider.onCreate()
          -> Application.onCreate()
              -> Activity / Service 等组件继续启动
```

严格顺序在不同实现细节中可能有差异，但工程风险是明确的：

```text
Provider 可能早于你以为的业务初始化时机执行
```

如果一个三方 SDK 通过 Provider 自动初始化，它可能悄悄进入冷启动路径。

如果这个 Provider 又被放到 remote 进程，初始化问题会更绕。

## 第六部分：isolatedProcess

某些 Service 可以声明：

```xml
<service
    android:name=".SandboxService"
    android:isolatedProcess="true" />
```

isolatedProcess 会让服务运行在隔离程度更高的进程中。

它适合安全敏感或不可信代码隔离场景。

关键差异：

```text
普通远程进程
  -> 仍属于应用 uid

isolatedProcess
  -> 使用隔离 uid
  -> 权限更受限
  -> 更强调安全边界
```

它不是常规业务优化手段。

使用它之前要清楚：

- 它能访问哪些资源？
- 它如何和主进程通信？
- 权限是否足够？
- 崩溃和回收如何处理？

一个比较合理的 isolatedProcess 场景是：

```text
运行不完全可信的解析逻辑
  -> 只通过 Binder 暴露极小接口
      -> 不授予主 App 的完整权限
          -> 崩溃后可以单独重启
```

它强调的是安全隔离，不是“让任务更快”。

## 第七部分：多进程通信方式

多进程之间不能直接共享对象。

常见通信方式：

| 方式 | 适合场景 | 风险 |
| --- | --- | --- |
| Binder / AIDL | 强类型 IPC、服务调用 | 线程、死亡、版本兼容 |
| Messenger | 简单消息通信 | 协议能力较弱 |
| ContentProvider | 跨进程数据访问 | 初始化时机、权限、性能 |
| 文件 / 数据库 | 持久化状态共享 | 锁、一致性、性能 |
| Broadcast | 松散通知 | 时效性、后台限制 |

选择方式前先问：

```text
这是命令调用、数据查询、状态同步，还是事件通知？
```

不要用一种 IPC 扛所有问题。

## 第八部分：多进程排查表

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 初始化执行多次 | pid、processName | 多进程各自启动 Application | 按进程名分流初始化 |
| 单例状态不一致 | 进程名、对象地址 | 进程间内存不共享 | 用 IPC 或持久化同步 |
| remote 崩溃主进程无感 | 崩溃进程名 | 监控未区分进程 | 日志带 processName |
| Provider 抢启动 | provider authority、进程 | SDK 自动初始化 | 控制 Provider 进程和初始化时机 |
| AIDL 调用卡住 | Binder 线程栈 | 远端耗时或线程池阻塞 | 异步化、超时、死亡监听 |

多进程还要特别关注死亡通知：

```text
主进程绑定 remote 服务
  -> remote 进程被杀
      -> Binder 连接断开
          -> DeathRecipient 收到通知
              -> 主进程清理状态或重新绑定
```

如果没有处理 binder death，主进程可能还以为远端服务活着，结果下一次调用才失败。

这类问题看起来像“偶发 IPC 异常”，本质是远端进程生命周期没有纳入协议。

## 本节小挑战

### 多进程初始化题

一个 App 有主进程和 `:push` 进程。

你发现：

```text
Analytics.init()
```

执行了两次。

请回答：

- 这是 bug 还是多进程正常现象？
- 如何确认两次日志来自不同进程？
- 哪些 SDK 应该只在主进程初始化？
- 哪些 SDK 需要每个进程都初始化？

## 本节实践任务

### 基础任务

- 给一个 Service 配置 `android:process=":remote"`。
- 在 Application 和 Service 中打印 pid、processName。
- 对比主进程和 remote 进程里的单例状态。

### 进阶任务

- 用 Messenger 或 AIDL 做一次主进程和 remote Service 通信。
- 给 Binder 连接加上死亡监听，观察 remote 进程崩溃后的回调。
- 给 remote 进程制造崩溃，观察主进程是否存活。
- 整理一份多进程初始化白名单和黑名单。

## 本节小结

多进程能带来隔离，也会放大复杂度。`android:process` 会让组件进入独立进程，Application 可能执行多次，单例和内存缓存不再共享。远程 Service、ContentProvider、isolatedProcess 都有适用边界。多进程工程要把 processName、pid、IPC 协议、初始化策略和崩溃监控作为一等公民。
