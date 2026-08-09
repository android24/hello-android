# 19.3 Zygote：App 进程如何被 fork 出来

第 19.2 节讲清楚了进程身份。

本节进入进程出生现场：

```text
一个 App 进程到底是谁创建的？
```

答案绕不开 Zygote。

## 本节定位

本节负责回答：

- Zygote 是什么？
- 为什么 Android 不为每个 App 从零创建运行时？
- 从 `startActivity` 到 App 进程创建的大致链路是什么？
- `fork`、`ActivityThread.main()`、`attachApplication` 和 `bindApplication` 分别处在哪个阶段？
- Zygote 机制为什么会影响启动速度和内存？

## 学习目标

学完本节后，你应该能够：

- 画出 Zygote fork App 进程的粗略链路。
- 解释为什么 Zygote 会预加载类和资源。
- 知道新进程为什么会进入 `ActivityThread.main()`。
- 能把进程创建和 Activity 启动链路接起来。

## 第一部分：从点击图标开始

当你点击桌面图标时，不是 Launcher 自己创建目标 App 进程。

大致链路是：

```text
Launcher
  -> Binder 调用 system_server
      -> ATMS / AMS 处理启动请求
          -> 判断目标进程是否存在
              -> 不存在则请求创建进程
                  -> Zygote fork App 进程
                      -> 新进程进入 ActivityThread.main()
```

Launcher 只是发起请求。

真正有权调度 Activity、判断进程是否存在、请求创建进程的是 system_server 中的系统服务。

如果把 system_server 内部再展开一层，可以粗略看成：

```text
ActivityTaskManagerService
  -> ActivityStarter / RootWindowContainer
      -> ActivityRecord / Task
          -> 确认目标 Activity 所属进程
              -> ActivityManagerService
                  -> ProcessList.startProcessLocked
                      -> Process.start
                          -> ZygoteProcess
                              -> 通过 zygote socket 发送 fork 参数
```

这些类名不要求一次背熟，但你要看懂方向：

```text
启动 Activity
  -> 不是 Activity 自己启动进程
  -> 是系统服务根据组件记录和进程记录决定是否创建进程
```

这也是第 12 章 Activity 启动链路和第 19 章进程链路的交汇点。

## 第二部分：Zygote 为什么存在

如果每次启动 App 都这样：

```text
创建 Linux 进程
  -> 初始化 ART
      -> 加载基础 Java 类
          -> 加载 Framework 常用资源
              -> 再启动 App
```

成本会很高。

Zygote 的思路是：

```text
先创建一个准备好的父进程
  -> 预加载常用类和资源
      -> 等待系统请求
          -> fork 子进程
              -> 子进程继承准备好的环境
```

这就是 Android 启动体验里的一个关键设计。

Zygote 不是只有一个概念名字。真实系统里常见会有：

```text
zygote64
zygote
```

分别服务不同架构或兼容需求。现代 Android 还可能有更细的优化机制，比如为特定场景准备更快的 fork 路径。

课程这里先抓主干：

```text
system_server 请求
  -> Zygote 接收参数
      -> fork
          -> 子进程 specialize 成目标 App 进程
```

“specialize” 这一步很重要。

fork 出来的子进程不能继续保持 Zygote 的身份，它要变成某个具体 App：

```text
设置 uid / gid
设置进程名
设置 SELinux 信息
设置调试参数
设置 targetSdk / runtime flags
进入 ActivityThread.main
```

所以 Zygote fork 不是简单复制一个进程就完事，而是复制后立刻“换身份证、换名字、换入口”。

## 第三部分：fork 和写时复制

`fork` 会从父进程复制出子进程。

但它不是立刻把所有内存都复制一份。

操作系统会使用写时复制：

```text
父子进程初始共享部分内存页
  -> 只读时可以共享
      -> 某一方写入时再复制
```

这对 Android 很有价值：

- Zygote 预加载的类和资源可以被多个 App 进程共享一部分内存。
- App 进程启动时不用从零准备运行时环境。
- 系统能在启动速度和内存占用之间取得平衡。

这也是为什么 Zygote 不能随便加载业务 App 私有内容。

预加载内容越通用，收益越大；越私有，污染越明显。

这也解释了一个性能取舍：

```text
预加载越多
  -> App 冷启动可能受益
  -> 但 Zygote 占用和系统启动成本可能增加

预加载越少
  -> Zygote 更轻
  -> 但 App 进程可能要自己加载更多基础内容
```

系统会在启动速度、内存共享、系统启动时间之间找平衡。

这不是“预加载越多越好”。

## 第四部分：Zygote fork 后发生什么

当 Zygote fork 出 App 进程后，新进程会进入指定入口。

对普通 App 来说，关键入口是：

```text
ActivityThread.main()
```

粗略链路：

```text
Zygote fork
  -> 子进程初始化运行环境
      -> ActivityThread.main()
          -> Looper.prepareMainLooper()
              -> ActivityThread.attach()
                  -> Binder 调用 AMS.attachApplication()
                      -> AMS 回调 bindApplication
                          -> 创建 Application
                              -> 启动目标 Activity / Service / Provider
```

再贴近源码入口一点，可以这样记：

```text
ZygoteConnection 处理 fork 请求
  -> forkAndSpecialize
      -> 子进程进入 handleChildProc
          -> RuntimeInit.zygoteInit
              -> applicationInit
                  -> findStaticMain("android.app.ActivityThread")
                      -> ActivityThread.main()
```

这条链路的意义是：

```text
App 进程不是从 MainActivity.main 开始
  -> 而是从 ActivityThread.main 开始
      -> 再由 Framework 调度 Application、Provider、Activity、Service
```

所以 Android 没有让你写 `public static void main`。

你的入口是组件入口，真正的进程入口由 Framework 管。

这里有一个容易误解的点：

```text
ActivityThread 不是线程类。
```

它是 App 进程中负责和 system_server 协作、调度组件生命周期的重要对象。

名字叫 Thread，但它更像 App 进程里的主调度器。

## 第五部分：attachApplication 和 bindApplication

App 进程启动后，不能自己宣布“我已经准备好了”。

它需要和 system_server 建立联系。

大致过程：

```text
App 进程 ActivityThread.attach()
  -> 通过 Binder 通知 AMS
      -> AMS 记录这个进程
          -> AMS 发送 bindApplication
              -> App 进程创建 LoadedApk / Application
                  -> 调用 Application.onCreate()
```

所以 `Application.onCreate()` 不是进程刚 fork 出来立刻执行。

它发生在 App 进程和 AMS 完成绑定之后。

这解释了很多启动问题：

- ContentProvider 可能比 Application 更早初始化。
- 多进程里每个进程都可能走自己的 bindApplication。
- Application 初始化过重会影响冷启动。
- 进程创建成功不等于首屏已经显示。

更细一点，App 进程刚 attach 到 AMS 时，系统还要把一堆“应用运行材料”交给它：

```text
ApplicationInfo
LoadedApk
compat config
instrumentation
providers
app bind data
```

然后 App 进程才知道：

```text
我要加载哪个 APK？
我要用哪个 ClassLoader？
我要创建哪个 Application？
有哪些 ContentProvider 要安装？
```

这就是 `bindApplication` 的价值。

它不是普通生命周期回调，而是 App 进程和系统服务完成绑定后的初始化指令包。

## 第六部分：为什么冷启动和 Zygote 有关

冷启动可以粗略拆成：

```text
没有目标进程
  -> 创建进程
      -> bindApplication
          -> 创建 Activity
              -> 首帧绘制
```

其中进程创建阶段就会涉及 Zygote。

如果目标进程已经存在，则可以省掉进程创建成本。

这也是为什么：

- 冷启动比热启动慢。
- 进程被系统杀掉后再次打开更像冷启动。
- Application 初始化重会放大冷启动成本。
- ContentProvider 自动初始化也会拖慢启动。

## 第七部分：Zygote 和第 18 章的关系

第 18 章讲 ClassLoader 和 ART。

第 19 章讲 Zygote。

它们的关系是：

```text
Zygote
  -> 准备运行时基础环境
      -> fork App 进程
          -> App 进程创建 ClassLoader
              -> 加载 APK 中的 Dex
                  -> ART 执行业务代码
```

Zygote 解决“进程如何快速出生”。

ClassLoader 解决“进程里如何找到类”。

ART 解决“类里的方法如何执行”。

不要把它们混成一团。

## 本节小挑战

### 冷启动链路题

请解释：

```text
为什么杀掉 App 进程后再次点击图标，比从最近任务切回 App 更慢？
```

建议回答：

- 进程不存在时需要 Zygote fork。
- 需要重新进入 ActivityThread.main。
- 需要重新 bindApplication。
- Application、ContentProvider 和 Activity 都可能重新初始化。
- 已有内存缓存和 JIT 状态可能丢失。

## 本节实践任务

### 基础任务

- 使用 `adb shell am force-stop 包名` 后重新启动 App，观察 pid 变化。
- 在 Application、Activity 中打印 pid 和时间戳。
- 对比冷启动和从后台切回的日志差异。

### 进阶任务

- 阅读 `ActivityThread.main()`。
- 阅读 `ZygoteConnection`、`ZygoteProcess`、`ProcessList.startProcessLocked` 的调用方向。
- 阅读 App 进程 attach 到 AMS 的相关调用。
- 画出“Zygote fork -> ActivityThread.main -> bindApplication -> Activity.onCreate”的链路。

## 本节小结

Zygote 是 Android App 进程创建的关键角色。它通过预加载和 fork 降低进程创建成本。App 进程出生后会进入 `ActivityThread.main()`，再 attach 到 AMS，随后执行 `bindApplication`、创建 Application 和组件。理解这条链路后，冷启动、Application 初始化、多进程创建和进程重启问题都会更容易定位。
