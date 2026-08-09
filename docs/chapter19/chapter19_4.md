# 19.4 App 进程里的线程：主线程、Binder 线程、RenderThread 与业务线程

进程是运行空间。

线程是在这个空间里真正执行任务的队伍。

一个 Android App 进程里通常不只有主线程：

```text
App 进程
  -> main thread
  -> Binder threads
  -> RenderThread
  -> coroutine / thread pool
  -> GC / JIT / runtime 相关线程
```

本节把这些角色摆清楚。

## 本节定位

本节负责回答：

- 主线程为什么重要？
- Binder 线程池从哪里来？
- RenderThread 和主线程是什么关系？
- 业务线程、线程池、协程和进程是什么关系？
- 为什么 ANR、卡顿和 Binder 阻塞常常是线程问题？

## 学习目标

学完本节后，你应该能够：

- 区分进程和线程。
- 解释主线程、Binder 线程、RenderThread 的基本职责。
- 解释为什么 Android 要把 UI 更新收束到主线程。
- 知道为什么主线程阻塞会导致卡顿和 ANR。
- 能看懂线程 dump 里常见线程名。

## 第一部分：进程和线程不要混

进程像一间工作室。

线程像工作室里的工作台。

```text
进程
  -> 拥有独立内存空间、uid、文件描述符、ClassLoader 环境

线程
  -> 共享进程内存
  -> 执行具体代码
```

同一个进程里的线程可以访问同一份堆内存。

不同进程之间不能直接访问彼此内存，需要 Binder、Socket、文件或其他 IPC。

## 第二部分：主线程是 App 的调度中枢

App 进程启动后会进入：

```text
ActivityThread.main()
```

里面会准备主 Looper：

```text
Looper.prepareMainLooper()
  -> ActivityThread
      -> Looper.loop()
```

主线程负责很多关键任务：

- 组件生命周期回调。
- View / Compose UI 更新。
- 输入事件处理。
- Choreographer 帧回调。
- 部分 Binder 回调转发后的 UI 处理。

更贴近运行时的链路是：

```text
ActivityThread.main()
  -> Looper.prepareMainLooper()
      -> 创建 ActivityThread
          -> attach 到 AMS
              -> Looper.loop()
                  -> 从 MessageQueue 取消息
                      -> 分发生命周期、输入、绘制、回调任务
```

所以主线程不是“专门画 UI 的线程”这么简单。

它是 App 进程和 Framework 调度体系接轨后的主事件循环。

所以主线程一旦长时间阻塞，就会影响：

- 点击响应。
- 页面绘制。
- 生命周期调度。
- ANR 风险。

典型主线程阻塞可能长这样：

```text
main
  -> Activity.onCreate
      -> SDK.init
          -> synchronized lock
              -> 等待后台线程释放锁
```

或者：

```text
main
  -> ContentProvider.onCreate
      -> 数据库初始化
          -> 磁盘 I/O
```

这些问题表面看是“启动慢”或“ANR”，本质是主线程事件循环被长任务占住了。

## 第三部分：为什么要区分主线程和其他线程

现在可以追问一个更核心的问题：

```text
为什么 Android 要规定 UI 主要在主线程更新？
为什么不让所有线程都能直接改 View？
```

答案不是“因为规定如此”，而是为了让 UI 状态、输入事件、生命周期和绘制调度保持可预测。

如果所有线程都能随便修改 UI，会出现这样的混乱：

```text
线程 A 正在 layout View 树
  -> 线程 B 同时修改某个 View 的宽高
      -> 线程 C 同时触发 removeView
          -> 输入事件又在读取 View 层级
              -> 绘制线程拿到半更新状态
```

这会让 UI 树进入非常难维护的竞争状态。

所以 Android 选择了一个更可控的模型：

```text
UI 相关变化
  -> 收束到主线程 MessageQueue
      -> 按顺序处理生命周期、输入、状态更新、绘制请求
          -> Choreographer 按 VSYNC 节奏组织一帧
```

这样做的目的有四个。

### 目的一：让 UI 状态串行化

View 树、窗口状态、生命周期回调和输入事件都非常依赖顺序。

例如：

```text
onCreate
  -> setContentView
      -> onResume
          -> 用户点击
              -> requestLayout
                  -> draw
```

这些操作如果被多个线程并行打散，系统就很难保证：

- 当前 View 是否已经 attach。
- 当前 Activity 是否还处于 resumed。
- 当前输入事件应该给哪个控件。
- 当前绘制命令对应的是哪一版 UI 状态。

主线程模型让这些变化进入同一个队列，按顺序执行。

### 目的二：避免 UI 框架到处加锁

如果允许任意线程直接改 UI，View、ViewGroup、Window、Resources、Compose 状态读写都要大量加锁。

这会带来新问题：

- 性能变差。
- 锁顺序复杂。
- 死锁风险增加。
- 线程 dump 更难读。
- UI 行为更难预测。

Android 的选择是：

```text
UI 框架不把自己设计成完全线程安全
  -> 业务必须回到主线程更新 UI
      -> 后台线程负责计算、I/O、网络、数据库
          -> 结果再投递回主线程
```

这是一种工程取舍。

它牺牲了“任意线程直接改 UI”的自由，换来了 UI 系统的简单、稳定和可预测。

### 目的三：保护用户响应

主线程负责：

- 输入事件。
- 生命周期。
- UI 状态更新。
- 一帧调度。

其他线程负责：

- 网络请求。
- 文件读写。
- 数据库访问。
- 图片解码。
- 大量计算。
- SDK 后台任务。

分工的目标是：

```text
主线程保持短任务和快速响应
  -> 后台线程承接耗时任务
      -> 结果回到主线程更新 UI
```

这也是为什么你经常看到：

```kotlin
withContext(Dispatchers.IO) {
    // 读数据库 / 网络 / 文件
}

withContext(Dispatchers.Main) {
    // 更新 UI 状态
}
```

协程只是把这个分工写得更顺滑，背后的原则没有变。

### 目的四：和帧调度对齐

UI 不是“数据一变就立刻随便画”。

它要和系统帧节奏协作：

```text
状态变化
  -> invalidate / requestLayout
      -> Choreographer 注册下一帧回调
          -> VSYNC 到来
              -> measure / layout / draw
                  -> RenderThread / Surface / SurfaceFlinger
```

主线程把 UI 变化收束起来，Choreographer 才能在合适的帧时机处理。

如果多个线程随意改 UI，帧边界会变得混乱。

所以主线程也可以理解成 UI 世界的“秩序入口”。

后台线程不是为了和主线程抢 UI，而是为了不让耗时工作堵住这个入口。

## 第四部分：Binder 线程池

App 进程会和 system_server 频繁通信。

这些通信通过 Binder 完成。

Binder 调用不是都在主线程上执行。

进程里会有 Binder 线程池，用来处理进入本进程的 Binder 请求。

例如：

```text
system_server
  -> Binder 调用 App 进程
      -> App 进程 Binder 线程接收
          -> 必要时切到主线程处理组件生命周期
```

如果 Binder 线程里做了耗时操作，会带来两个风险：

- 阻塞调用方，甚至拖慢 system_server 或其他进程。
- Binder 线程池被占满，后续 IPC 处理延迟。

所以 Binder 回调里也不能随便做重活。

更具体一点，Binder 线程常见在线程 dump 里类似：

```text
"Binder:12345_1"
"Binder:12345_2"
```

它们负责处理进入当前进程的 Binder 事务。

一个危险模式是：

```text
Binder 线程收到远程调用
  -> 持有业务锁
      -> 同步等待主线程结果
          -> 主线程又在等待这个业务锁
              -> 死锁 / ANR
```

这类问题非常隐蔽，因为你看到的可能只是：

```text
Input dispatching timed out
```

但真正的锁链在 Binder 线程和主线程之间。

排查时要同时看：

- main 线程栈。
- Binder 线程栈。
- 是否有锁等待。
- 是否有同步 Binder 调用。
- system_server 是否也被反向阻塞。

## 第五部分：RenderThread

从第 15 章我们知道，现代 Android 渲染链路里有 RenderThread。

它和主线程大致关系是：

```text
主线程
  -> 处理 measure / layout / draw
  -> 构建或更新绘制命令
      -> RenderThread
          -> 执行硬件渲染相关工作
              -> Surface / BufferQueue / SurfaceFlinger
```

RenderThread 能减轻主线程的一部分渲染负担，但它不是万能救星。

如果主线程一直阻塞：

- 输入事件无法及时处理。
- UI 状态无法更新。
- 新一帧无法及时发起。
- RenderThread 也可能无命令可渲染。

所以优化掉帧不能只盯 RenderThread。

也要区分两种掉帧：

```text
主线程没有及时产出绘制任务
  -> RenderThread 没有新命令可执行

主线程产出了任务
  -> RenderThread / GPU 侧执行太重
      -> 渲染仍然超时
```

前者更像业务、布局、Compose 重组、主线程 I/O 问题。

后者更像复杂阴影、过度绘制、纹理上传、GPU 压力问题。

所以看到 RenderThread，不代表问题一定在 RenderThread。

## 第六部分：业务线程、线程池和协程

第 6 章讲过：

- Thread。
- 线程池。
- Coroutines。
- Flow。
- WorkManager。

第 19 章把它们放回进程里看：

```text
App 进程
  -> 主线程
  -> IO 线程池
  -> Default 线程池
  -> WorkManager 后台执行线程
  -> 自定义 SDK 线程
```

协程不是进程，也不等于线程。

协程最终仍然要调度到某个线程执行。

所以遇到问题时要问：

```text
这段代码在哪个进程？
在哪个线程？
是否阻塞主线程？
是否占满线程池？
是否跨进程调用？
```

还要问一个更工程化的问题：

```text
这个线程池有没有被占满？
```

例如：

```text
IO 线程池
  -> 大量同步网络 / 文件任务排队
      -> 数据加载延迟

Default 线程池
  -> CPU 任务过重
      -> 后续协程迟迟拿不到执行机会

自定义单线程 executor
  -> 某个任务卡住
      -> 后面所有任务一起堵住
```

协程让代码写起来更轻，但不会让线程资源无限。

## 第七部分：线程问题如何变成体验问题

常见对应关系：

| 现象 | 可能线程原因 | 第一证据 |
| --- | --- | --- |
| 卡顿 | 主线程任务过重 | trace / main thread stack |
| ANR | 主线程或 Binder 调用长时间阻塞 | ANR trace |
| IPC 超时 | Binder 线程池阻塞 | binder thread stack |
| 首屏慢 | Application / Provider / Activity 初始化重 | startup trace |
| 后台任务慢 | 线程池排队或 WorkManager 约束未满足 | worker log / thread dump |

线程 dump 是排查这些问题的重要证据。

不要只看异常名，也要看当时线程在做什么。

## 第八部分：如何读线程 dump

线程 dump 不需要一开始全读。

先找几类关键线程：

```text
"main"
"Binder:<pid>_<n>"
"RenderThread"
"DefaultDispatcher-worker"
"pool-"
"OkHttp"
"FinalizerDaemon"
```

然后看线程状态：

| 状态 | 常见含义 | 排查重点 |
| --- | --- | --- |
| RUNNABLE | 正在运行或等待 CPU | 是否 CPU 计算过重 |
| BLOCKED | 等待进入 synchronized | 谁持有锁 |
| WAITING | 无限期等待 | 等待条件是否会被唤醒 |
| TIMED_WAITING | 限时等待 / sleep | 是否人为 sleep 或等待超时 |

读线程 dump 的顺序可以是：

```text
先看 main
  -> 再看它是否等待锁 / Binder / I/O
      -> 找持有锁的线程
          -> 看 Binder 线程是否被占满
              -> 看业务线程池是否排队
```

如果只截一段 main 线程，很容易漏掉真正的持锁方。

## 第九部分：ANR trace 怎么连回进程和线程

ANR 不是一个单独异常，它是系统判断 App 长时间没有响应。

常见类型：

| 类型 | 典型场景 | 第一关注线程 |
| --- | --- | --- |
| Input ANR | 输入事件长时间未处理 | main |
| Broadcast ANR | 广播处理超时 | main / receiver 线程 |
| Service ANR | Service 生命周期或前台服务处理超时 | main |
| ContentProvider ANR | Provider 调用或启动卡住 | main / Binder |

一个简化 trace 可能长这样：

```text
"main" prio=5 tid=1 BLOCKED
  at com.example.SessionStore.getToken(SessionStore.kt:42)
  - waiting to lock <0x1234>

"Binder:23456_2" prio=5 tid=18 RUNNABLE
  at com.example.SessionStore.refresh(SessionStore.kt:80)
  - locked <0x1234>
  at android.os.BinderProxy.transact(Native Method)
```

这说明 main 线程在等锁，而 Binder 线程持有锁并进行同步调用。

修复方向可能是：

- 不在 Binder 回调中持锁做耗时操作。
- 不让主线程同步等待远端结果。
- 缩小锁范围。
- 将耗时任务移到异步链路，并设计超时。

这就是“线程问题”真正连接到 ANR 的方式。

## 第十部分：线程证据清单

遇到卡顿、ANR、IPC 超时、多进程初始化错乱，可以收集：

```text
processName
pid
threadName
main thread stack
Binder thread stack
RenderThread stack
worker thread pool queue
held locks
waiting locks
是否有同步 Binder 调用
是否有磁盘 / 网络 / 数据库 I/O
```

如果是多进程，还要给每个进程分别抓。

因为每个进程都有自己的 main 和 Binder 线程池。

## 第十一部分：多进程里的线程更容易误判

多进程会让线程排查更复杂。

例如：

```text
com.example.app
com.example.app:push
```

两个进程里都可能有：

- main 线程。
- Binder 线程。
- 工作线程。
- SDK 线程。

如果日志不带进程名，你可能会误以为同一个主线程执行了两套初始化。

实际上是两个进程各自有一个主线程。

因此多进程项目的日志最好包含：

```text
processName
pid
threadName
```

## 本节小挑战

### 线程定位题

下面这个日志：

```text
Thread[main] init sdk
Thread[main] init sdk
```

出现了两次。它一定是同一个进程重复初始化吗？

请回答：

- 是否打印了 pid？
- 是否打印了 processName？
- 是否可能来自主进程和 push 进程？
- 初始化逻辑是否应该只在主进程执行？

## 本节实践任务

### 基础任务

- 在 App 中打印当前线程名、pid、processName。
- 打开 Android Studio Profiler 或 `adb shell ps -T` 观察线程。
- 制造一次主线程 sleep，观察页面响应。

### 进阶任务

- 抓取一次 ANR trace 或线程 dump。
- 标注 main、Binder、RenderThread、业务线程分别在做什么。
- 找出一个 BLOCKED 线程等待的锁由谁持有。
- 记录一次 Binder 线程和主线程互相等待的假想链路。
- 给日志工具增加 processName 和 threadName。

## 本节小结

进程提供运行空间，线程执行具体代码。主线程负责 UI 和生命周期调度，Binder 线程处理 IPC，RenderThread 参与渲染，业务线程和协程承载后台任务。很多卡顿、ANR、IPC 延迟和多进程初始化问题，本质都是“在哪个进程、哪个线程执行了什么”的问题。
