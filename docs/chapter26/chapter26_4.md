# 26.4 从一次用户操作追到 Framework 链路

毕业项目不能只停在应用层。

既然这门课已经走过 Framework 阶段，终章项目就应该能回答一个关键问题：

```text
用户点击一个课程卡片后，Android 系统到底帮我们做了什么？
```

这一节，我们把“打开课程详情”作为主线，追一次完整链路。

## 本节先记住三句话

```text
Framework 不是远处的源码，它就在每一次点击、跳转、绘制和崩溃现场里。
资深工程师看功能，不只看业务代码，还会看它经过了哪些系统通道。
能把一次操作讲成链路，才说明你不是只会调用 API。
```

## 主线场景

用户在首页点击一门课程：

```text
首页课程卡片
  -> 点击
      -> 打开详情页
          -> 加载课程详情
              -> 刷新 UI
                  -> 记录学习行为
```

应用层看起来只是一次点击。

系统层却至少涉及：

```text
Input
主线程消息队列
Compose / View 事件处理
Navigation / Activity 调度
Window 和 ViewRootImpl
Choreographer
RenderThread
SurfaceFlinger
```

如果详情页还触发数据读取和后台同步，还会继续涉及：

```text
协程调度
线程池
Room / DataStore
WorkManager
Binder
系统服务
进程状态
```

## 链路一：触摸事件如何进来

从硬件到 App，事件大致会走过：

```text
触摸屏
  -> Linux input 子系统
      -> InputReader
          -> InputDispatcher
              -> 目标窗口
                  -> ViewRootImpl
                      -> Activity / ComposeView
                          -> Compose pointer input / clickable
                              -> onClick
```

这条链路对应第 14 章。

毕业项目中可以把它做成一张“分发链路卡片”：

| 节点 | 作用 |
| --- | --- |
| InputReader | 从底层设备读取输入事件 |
| InputDispatcher | 找到应该接收事件的窗口 |
| ViewRootImpl | App 进程中连接窗口和 View 树的桥 |
| Compose / View | 把系统事件转成业务回调 |
| onClick | 进入应用业务逻辑 |

读者点击课程卡片时，页面可以追加一条事件日志：

```text
traceId=course-1024
Input -> ViewRootImpl -> Compose clickable -> CourseCard.onClick
```

它不一定要还原系统内部每一行源码，但要帮助读者建立方向感。

## 链路二：页面如何切换

如果毕业项目使用单 Activity + Compose Navigation，页面切换主要发生在应用内。

```text
CourseHomeScreen
  -> navController.navigate("course/{id}")
      -> CourseDetailRoute
          -> CourseDetailViewModel
              -> observe course detail
```

如果项目选择多 Activity，则会进入：

```text
startActivity()
  -> Instrumentation
      -> ActivityTaskManagerService
          -> ActivityRecord / Task
              -> ApplicationThread
                  -> ActivityThread
                      -> performLaunchActivity()
                          -> onCreate()
```

这条链路对应第 12 章。

终章项目可以同时说明两件事：

```text
Compose Navigation 通常不等于每个页面都是新 Activity。
但无论 UI 框架如何变化，ActivityThread、主线程消息和窗口接入仍是 App 运行的基础。
```

这样读者就不会误以为学了 Compose 就不需要理解 Framework。

## 链路三：UI 为什么能刷新到屏幕

课程详情加载完成后，状态变化会触发 UI 刷新：

```text
Repository emit data
  -> ViewModel update UiState
      -> Compose recomposition
          -> invalidate / schedule frame
              -> Choreographer doFrame
                  -> measure / layout / draw
                      -> RenderThread
                          -> BufferQueue
                              -> SurfaceFlinger
                                  -> 屏幕显示
```

这条链路对应第 15 章。

毕业项目里可以给详情页加入一个“帧观察卡片”：

```text
点击时间
详情数据返回时间
首个 loading frame
内容显示 frame
是否出现主线程阻塞
是否出现过长 recomposition
```

这些指标可以先用本地模拟数据呈现。

真正重要的是让学习者知道：UI 刷新不是“状态变了就神奇出现”，而是一条被 Choreographer 和渲染管线驱动的链路。

## 链路四：系统服务在哪里参与

很多能力背后都有 Binder 和系统服务。

例如：

| 功能 | 可能涉及的系统能力 |
| --- | --- |
| 打开页面 | ActivityTaskManagerService、ActivityThread |
| 显示窗口 | WindowManagerService、ViewRootImpl |
| 输入事件 | InputManagerService、InputDispatcher |
| 后台任务 | JobSchedulerService、AlarmManagerService |
| 存储访问 | MediaProvider、DocumentsProvider |
| 权限判断 | PackageManagerService、PermissionManager、AppOps |

毕业项目不需要把所有源码都读完。

但每个关键功能至少要能标出：

```text
应用层入口
Framework 关键类
system_server 中可能参与的服务
可观察证据
常见问题
```

这就是资深工程师读系统的方式。

## traceId：把业务和系统证据串起来

建议毕业项目所有关键操作都生成 `traceId`：

```text
course-open-1700000001
offline-sync-1700000002
permission-request-1700000003
release-check-1700000004
```

然后在这些位置记录：

```text
UI 点击
ViewModel intent
UseCase 执行
Repository 返回
后台任务开始 / 结束
诊断事件
治理报告
```

示例：

```text
[course-open-1700000001] click course card
[course-open-1700000001] load local cache
[course-open-1700000001] fake network refresh start
[course-open-1700000001] ui content displayed
[course-open-1700000001] frame cost warning: 28ms
```

当你开始用同一个 `traceId` 串起业务、日志、性能和治理，毕业项目就从“功能 Demo”升级成了“工程实验室”。

## Framework 链路不要背源码，要讲因果

很多人学 Framework 时容易掉进一个误区：

```text
背类名。
背调用栈。
背源码文件路径。
```

这些当然有用，但它们不是终点。

更重要的是讲清楚因果：

| 你看到的现象 | 应该追问的因果 |
| --- | --- |
| 点击没有响应 | 事件有没有进入目标窗口？主线程是否空闲？View / Compose 是否消费事件？ |
| 页面跳转异常 | Intent / route 是否正确？Activity / Task 状态是否符合预期？生命周期是否被重新调度？ |
| UI 刷新很慢 | 状态是否频繁变化？主线程是否被占用？Choreographer 是否错过 vsync？ |
| 列表滑动掉帧 | 重组、布局、绘制、图片解码、I/O、GC 谁占用了时间？ |
| 后台任务没执行 | 任务是否入队？约束是否满足？系统是否处于 Doze / App Standby？ |

终章项目里讲 Framework 链路，推荐使用这个句式：

```text
因为用户做了某个动作
  -> 所以系统需要把事件送到正确窗口
      -> 所以 InputDispatcher 要根据窗口状态选择目标
          -> 所以 ViewRootImpl 会把事件交给 App 侧 View 树
              -> 所以 Compose / View 才能触发业务回调
```

这比只说：

```text
InputReader 调 InputDispatcher，InputDispatcher 到 ViewRootImpl。
```

更有价值。

因为资深工程师不是只知道“调用了谁”，还要能解释：

```text
为什么要有这个节点。
它解决什么问题。
它出错时会出现什么现象。
我能用什么证据证明它真的参与了。
```

这也是第 26 章对 Framework 能力的要求：不追求背完整源码，而是追求能把业务现象、系统机制和诊断证据连成因果链。

## 本节练习

请选择一个毕业项目操作，比如：

```text
打开课程详情
收藏章节
添加离线任务
执行后台同步
请求存储权限
生成发布报告
```

为它写一份链路说明：

```text
用户动作
UI 事件
ViewModel / UseCase / Repository
线程或协程切换
可能涉及的系统服务
可能出现的问题
可观察证据
```

不要追求一开始就百分百准确。

先把链路画出来，再逐步修正。

## 本节复盘

本节我们把一次用户操作追到了 Framework 链路：

```text
触摸事件由 Input 系统进入 App。
页面切换离不开 ActivityThread、Navigation 或 Activity 调度。
UI 刷新由状态变化、Choreographer、RenderThread 和 SurfaceFlinger 共同完成。
后台、权限、存储和窗口能力背后都有系统服务参与。
traceId 可以把业务和系统证据串成一条线。
Framework 学习不要停在类名和调用栈，要能讲清楚现象背后的因果。
```

下一节，我们会对毕业项目做一次综合体检：性能、稳定性、安全与存储，一个都不能只凭感觉。
