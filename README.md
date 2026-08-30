#《从零开始的 Android 全栈开发课程》

## 🎯 项目定位
《从零开始的 Android 全栈开发课程》是一套面向 Android 学习者的系统化开源课程。它不满足于让你“照着写出一个页面”，而是希望带你真正走完一条从入门到进阶、从应用到架构、从业务代码到 Android Framework 的成长路径。

课程以 Kotlin 与现代 Android 技术栈为主线，覆盖 Jetpack Compose、架构设计、网络与本地数据、协程与 Flow、依赖注入、模块化、测试、性能优化、稳定性治理，以及 Android Framework 核心机制。每一门课程都将尽量保持“一个 README + 一个可运行代码工程”的组织方式：README 负责讲清楚为什么学、学什么、怎么学；代码工程负责把知识落到真实项目里。

这不是一本零散知识点合集，而是一条面向工程实践的路线。你会先学会搭建一个应用，再学会让它变得清晰、稳定、可维护，最后继续向下理解：一次启动、一次点击、一次页面跳转、一次渲染，究竟是怎样穿过 Framework、系统服务和底层机制，最终变成用户手中的 Android 体验。

## 课程玩法

这门课会围绕一个逐步长大的“课程 App”展开。你不是在一页页背知识点，而是在亲手搭建一个应用：

- 第 1 章：点亮第一个工程，让 App 真正跑起来。
- 第 2 章：做出课程首页，让界面从空白变得可看、可点、可变化。
- 第 3 章：打开第二个页面，让 App 有页面流转和返回路径。
- 第 4 章：接入数据能力，让 App 不再只展示写死的内容。
- 第 5 章：梳理架构边界，让同一个功能从能跑走向清晰、稳定、可维护。
- 第 6 章：掌握异步与数据流，让刷新、取消、错误和后台任务都有章法。
- 第 7 章：引入依赖注入与模块化，让工程从一个包裹长成一座有边界的城市。
- 第 8 章：建立测试与质量保障，让每一次修改都有安全网，每一次交付都有检查表。
- 第 9 章：进入性能与稳定性治理，让 App 不只是能交付，还要跑得快、稳得住、出了问题能定位。
- 第 10 章：打开 Framework 的后台通道，从一次启动、一次消息和一次跨进程通信开始读懂系统。
- 第 11 章：进入 Binder 与系统服务，让 App 和 system_server 的协作不再神秘。
- 第 12 章：拆解 Activity 启动与任务栈，让页面跳转背后的系统调度浮出水面。
- 第 13 章：继续追踪窗口显示链路，看懂 Window、DecorView、ViewRootImpl 与 WMS 如何把页面送上屏幕。
- 第 14 章：进入输入事件链路，看懂一次点击如何从触摸屏走到 View 与 Compose。
- 第 15 章：拆解一帧渲染，看懂 Choreographer、RenderThread 与 SurfaceFlinger 如何协作。
- 第 16 章：理解安装、包管理与权限，让 Manifest、签名、组件匹配不再只是配置项。
- 第 17 章：深入资源系统，看懂 R 文件、AAPT、Resources、主题与动态换肤背后的匹配规则。
- 第 18 章：进入代码加载与 ART，理解 Dex、ClassLoader、插件化和热修复的边界。
- 第 19 章：回到进程模型，看懂 Zygote、沙箱、OOM Adj、LMKD 和后台回收。
- 第 20 章：进入稳定性诊断现场，把 ANR、Crash、tombstone、Watchdog 和 bugreport 串成证据链。
- 第 21 章：继续面对后台任务，理解前台服务、Alarm、WorkManager、Doze 和系统后台限制。
- 第 22 章：走进存储系统，理解 Scoped Storage、MediaStore、SAF 和 URI 权限如何守住用户数据边界。
- 第 23 章：进入安全模型，理解 UID、权限、签名、AppOps、Keystore 与数据保护如何共同形成系统门禁。
- 第 24 章：进入系统观测工具，学会用 logcat、dumpsys、bugreport、Perfetto 和性能证据把复杂事故串成证据链。

每一章都像一个小关卡：先读文档拿地图，再运行示例看效果，最后改一处代码留下自己的痕迹。学完一章，你都应该能回答三个问题：我做出了什么？它为什么能运行？如果让我重新写一遍，我会从哪里开始？

当你完成第 20 章，可以先停下来读一遍 [课程中期地图：从会写 App 到读懂系统](docs/midterm_map.md)。它会把前 20 章重新串成一条完整路线，并说明后续为什么会继续进入后台调度、存储、安全、观测工具和大型工程治理。

## 项目受众
本课程非常适合以下同学：

🏁 0 基础 / 无编程经验但想进入 Android 开发的人
从编程语言、开发工具、项目结构开始铺路，不要求你一开始就理解复杂工程。

🔰 有一点编程经验但从未系统学习 Android 的人
帮助你快速建立 Android 的知识地图，避免只会复制代码，却不知道代码运行在哪里。

🎓 已经能写 Android，但想补齐 Jetpack、Compose、架构与工程能力的人
课程会从“功能可用”继续推进到“结构清晰、边界明确、质量可控”。

🚀 想冲击中高级、资深 Android 工程师的人
课程后半程会进入性能、稳定性、源码阅读、Binder、AMS、WMS、PMS、View 绘制、系统观测等 Framework 主题，帮助你把经验沉淀成体系。

🧭 想用一个完整项目沉淀简历作品的人
课程会通过阶段项目串联知识点，让每一步学习都能留下可展示、可复盘、可继续演进的代码。

## 目录

### 已完成章节

- 第1章 Kotlin 与 Android 基础
  - 通关目标：运行第一个 App，看懂工程骨架，并完成一次自己的修改
  - [1.1 开发环境与第一个 Android 工程](docs/chapter1/chapter1.md)
  - [1.2 Kotlin 基础与 Android 工程语法](docs/chapter1/chapter1_2.md)
  - [1.3 Android 工程结构与资源基础](docs/chapter1/chapter1_3.md)
  - [配套示例工程](examples/01-kotlin-and-android-basics/)
- 第2章 Compose UI 基础
  - 通关目标：做出一个课程首页，掌握布局、状态、点击和预览
  - [2.1 Jetpack Compose 入门](docs/chapter2/chapter2_1.md)
  - [2.2 Composable、Modifier 与基础布局](docs/chapter2/chapter2_2.md)
  - [2.3 状态与事件](docs/chapter2/chapter2_3.md)
  - [2.4 列表、主题与预览](docs/chapter2/chapter2_4.md)
  - [配套示例工程](examples/02-compose-ui-basics/)
- 第3章 Activity、生命周期与导航
  - 通关目标：完成首页到详情页的跳转，并能用日志解释页面生命周期
  - [3.1 Activity、生命周期与应用入口](docs/chapter3/chapter3_1/chapter3_1_1.md)
  - [3.2 Intent、页面跳转与参数传递](docs/chapter3/chapter3_1/chapter3_1_2.md)
  - [配套示例工程](examples/03-activity-lifecycle-navigation/)
- 第4章 网络、Room 与 DataStore
  - 通关目标：设计一条数据链路，让 App 具备网络、本地缓存和配置存储思路
  - [4.1 网络请求基础](docs/chapter4/chapter4_1.md)
  - [4.2 Room 本地数据库](docs/chapter4/chapter4_2.md)
  - [4.3 DataStore 配置存储](docs/chapter4/chapter4_3.md)
  - [4.4 网络、本地缓存与离线可用小项目](docs/chapter4/chapter4_4.md)
  - [配套示例工程](examples/04-network-room-datastore/)
- 第5章 Android 应用架构演进
  - 通关目标：用同一个课程列表功能看懂 MVC、MVP、MVVM、MVI/UDF 和 Clean Architecture 的取舍
  - [5.1 为什么需要架构](docs/chapter5/chapter5_1.md)
  - [5.2 MVC：最朴素的分层尝试](docs/chapter5/chapter5_2.md)
  - [5.3 MVP：把 View 和业务逻辑拆开](docs/chapter5/chapter5_3.md)
  - [5.4 MVVM：用 ViewModel 承载 UI 状态](docs/chapter5/chapter5_4.md)
  - [5.5 MVI / UDF：用单向数据流管理复杂交互](docs/chapter5/chapter5_5.md)
  - [5.6 Repository、UseCase 与 Clean Architecture](docs/chapter5/chapter5_6.md)
  - [5.7 从一个页面演进到可维护工程](docs/chapter5/chapter5_7.md)
  - [配套示例工程](examples/05-android-architecture-evolution/)
- 第6章 Kotlin Coroutines、Flow 与后台任务
  - 通关目标：从进程、线程、Thread 与线程池过渡到协程、Flow 和后台任务，让 App 的异步逻辑清晰可控
  - [6.1 为什么 Android 需要异步编程](docs/chapter6/chapter6_1.md)
  - [6.2 从 Thread、线程池到 Coroutines](docs/chapter6/chapter6_2.md)
  - [6.3 结构化并发、取消与超时](docs/chapter6/chapter6_3.md)
  - [6.4 Flow：会随时间变化的数据](docs/chapter6/chapter6_4.md)
  - [6.5 StateFlow、SharedFlow 与 Compose 状态](docs/chapter6/chapter6_5.md)
  - [6.6 错误处理、重试与结果建模](docs/chapter6/chapter6_6.md)
  - [6.7 WorkManager 与可靠后台任务](docs/chapter6/chapter6_7.md)
  - [6.8 综合实践：课程同步中心](docs/chapter6/chapter6_8.md)
  - [配套示例工程](examples/06-coroutines-flow-workmanager/)
- 第7章 Hilt 依赖注入、模块化与工程化
  - 通关目标：理解对象如何被装配、模块如何被拆分，让课程 App 从清晰分层走向可协作、可扩展的工程结构
  - [7.1 为什么需要依赖注入](docs/chapter7/chapter7_1.md)
  - [7.2 Hilt 入门：从手动创建到自动装配](docs/chapter7/chapter7_2.md)
  - [7.3 Module、Provides、Binds 与作用域](docs/chapter7/chapter7_3.md)
  - [7.4 ViewModel、Repository 与数据源注入](docs/chapter7/chapter7_4.md)
  - [7.5 模块化：从单 App 到多 Module](docs/chapter7/chapter7_5.md)
  - [7.6 Gradle、多环境配置与构建变体](docs/chapter7/chapter7_6.md)
  - [7.7 组件边界、接口设计与工程治理](docs/chapter7/chapter7_7.md)
  - [7.8 综合实践：课程 App 工程化重组](docs/chapter7/chapter7_8.md)
  - [配套示例工程](examples/07-hilt-modularization-engineering/)
- 第8章 测试、质量保障与可交付
  - 通关目标：建立从单元测试、ViewModel 状态测试到 CI 与发布检查的质量链路，让课程 App 从“能运行”走向“可交付”
  - [8.1 为什么需要测试与质量保障](docs/chapter8/chapter8_1.md)
  - [8.2 单元测试：保护 UseCase 与业务规则](docs/chapter8/chapter8_2.md)
  - [8.3 协程、Flow 与 ViewModel 测试](docs/chapter8/chapter8_3.md)
  - [8.4 Compose UI 测试：验证关键用户路径](docs/chapter8/chapter8_4.md)
  - [8.5 测试替身、Mock、Fake 与 Hilt 测试替换](docs/chapter8/chapter8_5.md)
  - [8.6 CI、静态检查与质量门禁](docs/chapter8/chapter8_6.md)
  - [8.7 发布前检查：从能运行到可交付](docs/chapter8/chapter8_7.md)
  - [8.8 综合实践：课程 App 质量体检](docs/chapter8/chapter8_8.md)
  - [配套示例工程](examples/08-testing-quality-delivery/)
- 第9章 Android 性能优化与稳定性治理
  - 通关目标：理解启动、卡顿、内存、ANR、崩溃和包体积治理，让课程 App 从“可交付”继续走向“体验稳定、问题可查、优化有据”
  - [9.1 为什么需要性能优化与稳定性治理](docs/chapter9/chapter9_1.md)
  - [9.2 启动优化：从点击图标到首屏展示](docs/chapter9/chapter9_2.md)
  - [9.3 卡顿分析：主线程、掉帧与 Compose 性能](docs/chapter9/chapter9_3.md)
  - [9.4 内存优化：泄漏、对象分配与图片资源](docs/chapter9/chapter9_4.md)
  - [9.5 ANR 治理：耗时任务、锁等待与线程调度](docs/chapter9/chapter9_5.md)
  - [9.6 崩溃治理：Crash 收集、日志与问题分级](docs/chapter9/chapter9_6.md)
  - [9.7 包体积优化：资源、依赖、R8 与构建产物](docs/chapter9/chapter9_7.md)
  - [9.8 综合实践：课程 App 性能与稳定性体检](docs/chapter9/chapter9_8.md)
  - [配套示例工程](examples/09-performance-stability-lab/)
- 第10章 Android Framework 入门、系统架构与源码阅读方法
  - 通关目标：建立系统分层视角，理解 App 启动、主线程消息、Context、Binder 和 AOSP 源码阅读入口
  - [10.1 为什么要学习 Android Framework](docs/chapter10/chapter10_1.md)
  - [10.2 Android 系统架构全景：App、Framework、Native 与 Kernel](docs/chapter10/chapter10_2.md)
  - [10.3 AOSP 源码阅读方法：从迷路到能定位](docs/chapter10/chapter10_3.md)
  - [10.4 从一次 App 启动看 Framework 调用链](docs/chapter10/chapter10_4.md)
  - [10.5 ActivityThread、Application、Instrumentation 与 Context](docs/chapter10/chapter10_5.md)
  - [10.6 Handler、Looper、MessageQueue 与主线程模型](docs/chapter10/chapter10_6.md)
  - [10.7 Binder 初识：为什么 Android 到处都是跨进程通信](docs/chapter10/chapter10_7.md)
  - [10.8 综合实践：从一次点击追踪到 Framework 调用链](docs/chapter10/chapter10_8.md)
  - [配套示例工程](examples/10-framework-source-walkthrough/)
- 第11章 Binder、SystemServer 与系统服务入门
  - 通关目标：理解系统服务为什么存在，Binder 如何连接 App 与 system_server，并能观察一次跨进程调用链
  - [11.1 为什么系统服务是 Framework 的核心](docs/chapter11/chapter11_1.md)
  - [11.2 Binder 通信模型：从本地调用到跨进程调用](docs/chapter11/chapter11_2.md)
  - [11.3 AIDL、Stub、Proxy 与 Parcel](docs/chapter11/chapter11_3.md)
  - [11.4 ServiceManager：系统服务的通讯录](docs/chapter11/chapter11_4.md)
  - [11.5 SystemServer：系统服务从这里集结](docs/chapter11/chapter11_5.md)
  - [11.6 从 getSystemService 看系统服务调用](docs/chapter11/chapter11_6.md)
  - [11.7 Binder 线程、权限与稳定性风险](docs/chapter11/chapter11_7.md)
  - [11.8 综合实践：系统服务观察与 Binder 调用链](docs/chapter11/chapter11_8.md)
  - [配套示例工程](examples/11-binder-system-service-lab/)
- 第12章 AMS / ATMS、Activity 启动与任务栈调度
  - 通关目标：理解 startActivity 如何进入系统服务，掌握 Task、返回栈、launchMode、Intent Flag 与生命周期调度的关系
  - [12.1 为什么要学习 AMS / ATMS 与 Activity 启动](docs/chapter12/chapter12_1.md)
  - [12.2 从 startActivity 到系统服务](docs/chapter12/chapter12_2.md)
  - [12.3 Task、返回栈与 Activity 调度对象](docs/chapter12/chapter12_3.md)
  - [12.4 进程创建、Zygote 与 ActivityThread 协作](docs/chapter12/chapter12_4.md)
  - [12.5 launchMode 与 Intent Flag](docs/chapter12/chapter12_5.md)
  - [12.6 启动限制、权限与异常路径](docs/chapter12/chapter12_6.md)
  - [12.7 启动体验问题：黑屏、白屏、重复页面与返回异常](docs/chapter12/chapter12_7.md)
  - [12.8 综合实践：Activity 启动与任务栈观察实验](docs/chapter12/chapter12_8.md)
  - [配套示例工程](examples/12-activity-task-launch-lab/)
- 第13章 WMS、Window、DecorView 与窗口显示机制
  - 通关目标：理解 Activity 内容如何进入 Window，掌握 DecorView、ViewRootImpl、WMS、窗口层级、特殊窗口与一帧刷新之间的关系
  - [13.1 为什么要学习 WMS、Window 与窗口显示](docs/chapter13/chapter13_1.md)
  - [13.2 从 setContentView / Compose 到 DecorView](docs/chapter13/chapter13_2.md)
  - [13.3 Window、PhoneWindow 与 ViewRootImpl](docs/chapter13/chapter13_3.md)
  - [13.4 WindowManager、WMS、Token 与窗口层级](docs/chapter13/chapter13_4.md)
  - [13.5 Measure、Layout、Draw 与 Choreographer](docs/chapter13/chapter13_5.md)
  - [13.6 Dialog、PopupWindow、Toast 与输入法窗口](docs/chapter13/chapter13_6.md)
  - [13.7 窗口体验问题：白屏、遮挡、泄漏与 BadToken](docs/chapter13/chapter13_7.md)
  - [13.8 综合实践：窗口显示链路观察实验](docs/chapter13/chapter13_8.md)
  - [配套示例工程](examples/13-window-display-lab/)
- 第14章 Input 事件分发、触摸系统与交互响应机制
  - 通关目标：理解触摸事件如何从系统进入 App，掌握 MotionEvent、Activity 入口、ViewGroup 分发拦截、Compose 手势与 Input ANR 的排查思路
  - [14.1 为什么要学习 Input 事件分发](docs/chapter14/chapter14_1.md)
  - [14.2 从触摸屏到 App：InputReader、InputDispatcher 与 ViewRootImpl](docs/chapter14/chapter14_2.md)
  - [14.3 MotionEvent、坐标体系与事件序列](docs/chapter14/chapter14_3.md)
  - [14.4 Activity、Window、DecorView 的事件入口](docs/chapter14/chapter14_4.md)
  - [14.5 ViewGroup 事件分发：dispatchTouchEvent、onInterceptTouchEvent 与 onTouchEvent](docs/chapter14/chapter14_5.md)
  - [14.6 点击、手势、滑动冲突与 Compose pointer input](docs/chapter14/chapter14_6.md)
  - [14.7 输入体验问题：点击无响应、误触、滑动冲突与 Input ANR](docs/chapter14/chapter14_7.md)
  - [14.8 综合实践：输入事件分发观察实验](docs/chapter14/chapter14_8.md)
  - [配套示例工程](examples/14-input-event-dispatch-lab/)
- 第15章 View 绘制、RenderThread、SurfaceFlinger 与渲染链路
  - 通关目标：理解 UI 状态变化如何被调度成一帧，掌握 measure / layout / draw、Choreographer、RenderThread、Surface、BufferQueue、SurfaceFlinger 与掉帧排查思路
  - [15.1 为什么要学习 View 绘制与渲染链路](docs/chapter15/chapter15_1.md)
  - [15.2 从 invalidate 到 Choreographer：一帧如何被调度](docs/chapter15/chapter15_2.md)
  - [15.3 measure、layout、draw：View 树如何产出绘制命令](docs/chapter15/chapter15_3.md)
  - [15.4 HardwareRenderer、DisplayList 与 RenderThread](docs/chapter15/chapter15_4.md)
  - [15.5 Surface、BufferQueue 与 SurfaceFlinger](docs/chapter15/chapter15_5.md)
  - [15.6 VSYNC、帧率、掉帧与 Jank](docs/chapter15/chapter15_6.md)
  - [15.7 渲染体验问题：白屏、闪烁、过度绘制、黑屏与掉帧](docs/chapter15/chapter15_7.md)
  - [15.8 综合实践：一帧渲染链路观察实验](docs/chapter15/chapter15_8.md)
  - [配套示例工程](examples/15-rendering-frame-lab/)
- 第16章 PMS、应用安装、包管理与权限机制
  - 通关目标：理解系统如何识别、安装、解析和管理 App，掌握 Manifest、组件注册、Intent 解析、签名、权限、包可见性与安装失败排查思路
  - [16.1 为什么要学习 PMS、应用安装与包管理](docs/chapter16/chapter16_1.md)
  - [16.2 APK 安装流程：从文件到已安装应用](docs/chapter16/chapter16_2.md)
  - [16.3 AndroidManifest 解析与组件注册](docs/chapter16/chapter16_3.md)
  - [16.4 Intent 解析、组件匹配与包可见性](docs/chapter16/chapter16_4.md)
  - [16.5 签名、权限与安装校验](docs/chapter16/chapter16_5.md)
  - [16.6 应用升级、卸载、数据保留与多用户状态](docs/chapter16/chapter16_6.md)
  - [16.7 包管理体验问题：安装失败、组件找不到、权限异常与包不可见](docs/chapter16/chapter16_7.md)
  - [16.8 综合实践：包管理、安装与权限观察实验](docs/chapter16/chapter16_8.md)
  - [配套示例工程](examples/16-package-manager-lab/)
- 第17章 资源系统、AssetManager、Resources 与主题机制
  - 通关目标：理解 Android 资源从 res 编译到 R 文件、resources.arsc、AssetManager、Resources、Theme 与 Configuration 的运行链路，掌握多语言、夜间模式、密度适配、资源合并和资源问题排查思路
  - [17.1 为什么要学习资源系统、AssetManager 与 Resources](docs/chapter17/chapter17_1.md)
  - [17.2 从 res 到 R 文件：AAPT2、资源 ID 与 resources.arsc](docs/chapter17/chapter17_2.md)
  - [17.3 AssetManager 与 Resources：运行时如何加载资源](docs/chapter17/chapter17_3.md)
  - [17.4 Configuration 与资源限定符：多语言、密度、横竖屏和夜间模式](docs/chapter17/chapter17_4.md)
  - [17.5 Theme、Style 与 Attribute：界面气质如何被资源系统塑形](docs/chapter17/chapter17_5.md)
  - [17.6 资源合并、依赖模块与资源冲突](docs/chapter17/chapter17_6.md)
  - [17.7 资源体验问题：NotFound、主题错乱、多语言失败、图片模糊与包体积](docs/chapter17/chapter17_7.md)
  - [17.8 综合实践：资源系统、主题与配置观察实验](docs/chapter17/chapter17_8.md)
  - [17 附录：主题替换、动态换肤与资源覆盖方案](docs/chapter17/appendix_theme_skinning.md)
  - [配套示例工程](examples/17-resource-system-lab/)
- 第18章 ClassLoader、Dex、Dalvik / ART 与动态加载机制
  - 通关目标：理解 Android 代码从源码编译到 Dex、再由 ClassLoader 和 Dalvik / ART 加载执行的链路，掌握 R8 / MultiDex、native so、动态加载、插件化、热修复和代码加载问题排查思路
  - [18.1 为什么要学习 ClassLoader、Dex、ART 与动态加载](docs/chapter18/chapter18_1.md)
  - [18.2 从源码到 Dex：classes.dex、D8、R8 与 MultiDex](docs/chapter18/chapter18_2.md)
  - [18.3 ClassLoader：PathClassLoader、DexClassLoader 与类查找路径](docs/chapter18/chapter18_3.md)
  - [18.4 从 Dalvik 到 ART：解释执行、JIT、AOT 与 Profile](docs/chapter18/chapter18_4.md)
  - [18.5 Native 库加载：System.loadLibrary、JNI、ABI 与 so 冲突](docs/chapter18/chapter18_5.md)
  - [18.6 动态加载、插件化与热修复：能力边界与工程代价](docs/chapter18/chapter18_6.md)
  - [18.7 代码加载体验问题：ClassNotFound、NoSuchMethod、VerifyError 与 UnsatisfiedLinkError](docs/chapter18/chapter18_7.md)
  - [18.8 综合实践：代码加载、ClassLoader 与运行时观察实验](docs/chapter18/chapter18_8.md)
  - [配套示例工程](examples/18-code-loading-lab/)
- 第19章 Android 进程模型、Zygote、应用沙箱与内存管理机制
  - 通关目标：理解 App 进程如何被 Zygote fork 出来，掌握 pid / uid / processName、应用沙箱、多进程、主线程 / Binder 线程、OOM Adj、LMKD 和后台恢复问题排查思路
  - [19.1 为什么要学习进程模型、Zygote 与内存管理](docs/chapter19/chapter19_1.md)
  - [19.2 Linux 进程、UID、应用沙箱与 SELinux](docs/chapter19/chapter19_2.md)
  - [19.3 Zygote：App 进程如何被 fork 出来](docs/chapter19/chapter19_3.md)
  - [19.4 App 进程里的线程：主线程、Binder 线程、RenderThread 与业务线程](docs/chapter19/chapter19_4.md)
  - [19.5 进程优先级、OOM Adj 与 LMKD：为什么后台进程会被杀](docs/chapter19/chapter19_5.md)
  - [19.6 多进程、远程 Service、ContentProvider 与 isolatedProcess](docs/chapter19/chapter19_6.md)
  - [19.7 进程体验问题：后台死亡、状态丢失、多进程错乱与保活误区](docs/chapter19/chapter19_7.md)
  - [19.8 综合实践：进程、Zygote、多进程与内存回收观察实验](docs/chapter19/chapter19_8.md)
  - [配套示例工程](examples/19-process-zygote-lab/)
- 第20章 ANR、Crash、Watchdog 与系统稳定性诊断机制
  - 通关目标：理解 Android 如何发现、记录和诊断 App 与系统稳定性问题，掌握 ANR trace、Java Crash、Native tombstone、Watchdog、DropBox、bugreport 和稳定性治理闭环
  - [20.1 为什么要学习 ANR、Crash、Watchdog 与系统稳定性诊断](docs/chapter20/chapter20_1.md)
  - [20.2 ANR：系统如何判断 App 无响应](docs/chapter20/chapter20_2.md)
  - [20.3 ANR trace：如何读 main、Binder、锁和系统超时](docs/chapter20/chapter20_3.md)
  - [20.4 Java Crash：异常如何杀死进程](docs/chapter20/chapter20_4.md)
  - [20.5 Native Crash 与 tombstone：signal、JNI 和 so 崩溃](docs/chapter20/chapter20_5.md)
  - [20.6 Watchdog、DropBox 与 bugreport：系统级事故证据](docs/chapter20/chapter20_6.md)
  - [20.7 稳定性体验问题：误判、漏报、恢复和降级](docs/chapter20/chapter20_7.md)
  - [20.8 综合实践：稳定性诊断实验室](docs/chapter20/chapter20_8.md)
  - [配套示例工程](examples/20-stability-diagnosis-lab/)
- [课程中期地图：从会写 App 到读懂系统](docs/midterm_map.md)
- 第21章 后台任务、前台服务、Alarm、JobScheduler 与系统后台限制
  - 通关目标：理解 Android 为什么限制后台执行，掌握 Service、Foreground Service、AlarmManager、JobScheduler、WorkManager、Doze、App Standby 和后台任务诊断思路
  - [21.1 为什么要学习后台任务、前台服务与系统调度限制](docs/chapter21/chapter21_1.md)
  - [21.2 Android 后台限制：系统为什么不让 App 随便运行](docs/chapter21/chapter21_2.md)
  - [21.3 Service 与 Foreground Service：后台执行的边界](docs/chapter21/chapter21_3.md)
  - [21.4 AlarmManager：定时任务为什么不一定准时](docs/chapter21/chapter21_4.md)
  - [21.5 JobScheduler 与 WorkManager：可靠后台任务如何被系统调度](docs/chapter21/chapter21_5.md)
  - [21.6 Doze、App Standby、Battery Saver 与厂商限制](docs/chapter21/chapter21_6.md)
  - [21.7 后台任务体验问题：任务丢失、耗电、保活误区与合规风险](docs/chapter21/chapter21_7.md)
  - [21.8 综合实践：后台任务与系统调度观察实验](docs/chapter21/chapter21_8.md)
  - [配套示例工程](examples/21-background-scheduling-lab/)
- 第22章 存储系统、Scoped Storage、MediaStore 与数据访问机制
  - 通关目标：理解 Android 存储为什么从路径访问走向范围访问，掌握 App 私有存储、MediaStore、Photo Picker、SAF、URI 权限、存储权限演进和数据迁移排查思路
  - [22.1 为什么要学习存储系统、Scoped Storage 与数据访问机制](docs/chapter22/chapter22_1.md)
  - [22.2 Android 存储分区与 Scoped Storage：系统为什么不让 App 随便翻柜子](docs/chapter22/chapter22_2.md)
  - [22.3 App 私有存储、缓存、Room、DataStore 与备份恢复](docs/chapter22/chapter22_3.md)
  - [22.4 MediaStore：图片、视频、音频为什么要交给系统索引](docs/chapter22/chapter22_4.md)
  - [22.5 SAF、DocumentsProvider 与 URI 权限：让用户亲自打开文件柜](docs/chapter22/chapter22_5.md)
  - [22.6 存储权限演进：从大权限到照片选择器与部分授权](docs/chapter22/chapter22_6.md)
  - [22.7 存储体验问题：文件丢失、媒体不可见、URI 失效与数据迁移](docs/chapter22/chapter22_7.md)
  - [22.8 综合实践：存储访问观察实验室](docs/chapter22/chapter22_8.md)
  - [配套示例工程](examples/22-storage-access-lab/)
- 第23章 Android 安全模型、权限、签名、AppOps 与数据保护
  - 通关目标：理解 Android 如何通过 UID、沙箱、权限、AppOps、签名、Keystore 和组件边界保护系统能力与用户数据，掌握权限拒绝、签名不一致、组件暴露、日志泄露和敏感数据保护问题的排查思路
  - [23.1 为什么要学习 Android 安全模型、权限、签名、AppOps 与数据保护](docs/chapter23/chapter23_1.md)
  - [23.2 应用沙箱、UID、SELinux 与进程边界](docs/chapter23/chapter23_2.md)
  - [23.3 权限系统：Manifest、runtime permission、权限组与用户授权](docs/chapter23/chapter23_3.md)
  - [23.4 AppOps：为什么授权了也可能被系统继续拦](docs/chapter23/chapter23_4.md)
  - [23.5 签名、证书、安装升级、签名权限与供应链风险](docs/chapter23/chapter23_5.md)
  - [23.6 Keystore、加密存储、备份恢复与敏感数据保护](docs/chapter23/chapter23_6.md)
  - [23.7 安全体验问题：权限拒绝、组件暴露、日志泄露与合规风险](docs/chapter23/chapter23_7.md)
  - [23.8 综合实践：安全模型观察实验室](docs/chapter23/chapter23_8.md)
  - [配套示例工程](examples/23-security-permission-lab/)
- 第24章 系统观测工具：Perfetto、dumpsys、bugreport 与证据链分析
  - 通关目标：理解复杂事故如何从现象变成证据链，掌握 logcat、dumpsys、bugreport、Perfetto、gfxinfo、meminfo、procstats、simpleperf 等观测工具的使用场景与分析方法
  - [24.1 为什么要学习系统观测工具、证据链与事故分析](docs/chapter24/chapter24_1.md)
  - [24.2 logcat、结构化日志与时间点定位](docs/chapter24/chapter24_2.md)
  - [24.3 dumpsys：系统服务状态快照与问题定位](docs/chapter24/chapter24_3.md)
  - [24.4 bugreport、DropBox、ANR trace 与 tombstone：完整事故包怎么读](docs/chapter24/chapter24_4.md)
  - [24.5 Perfetto：从系统时间线看线程、Binder、调度与帧](docs/chapter24/chapter24_5.md)
  - [24.6 gfxinfo、meminfo、procstats 与 simpleperf：性能现场证据](docs/chapter24/chapter24_6.md)
  - [24.7 观测体验问题：误读、隐私、复现与团队协作](docs/chapter24/chapter24_7.md)
  - [24.8 综合实践：系统证据链分析实验室](docs/chapter24/chapter24_8.md)
  - [24 附录：从 Perfetto 到 CausalPerf / Smart Perfetto](docs/chapter24/appendix_smart_perfetto.md)

### 项目说明

- [总路线图](#总路线图)
- [学习顺序](#学习顺序)
- [环境要求](#环境要求)
- [GitHub Pages 文档站](#github-pages-文档站)
- [示例工程](#示例工程)

### 总路线图

这门课程按照“先会写，再写好，最后懂系统”的顺序展开。前半程建立应用开发能力，中段训练真实工程能力，后半程进入 Framework 与源码视角。

### 阶段一：开发环境与 Kotlin 基础

- Android Studio 安装、SDK 配置、模拟器与真机调试
- Gradle、项目结构、常见目录与构建流程
- Kotlin 基础语法、空安全、集合、函数、类与扩展
- 第一个 Android 工程：从 Hello World 到理解 App 如何启动

### 阶段二：Android 应用基础

- Activity、生命周期、Intent、资源系统与权限
- 日志、调试、断点、异常分析
- 基础控件、状态保存、页面跳转与参数传递
- 阶段项目：Todo / 记账 / 个人资料 App

### 阶段三：现代 UI 与 Jetpack Compose

- Compose 基础、Composable、Modifier、State
- 列表、表单、主题、动画与响应式布局
- Navigation、ViewModel 与 UI 状态管理
- 阶段项目：用 Compose 重构基础 App

### 阶段四：数据、网络与异步编程

- Retrofit / OkHttp 网络请求
- Room 本地数据库与 DataStore 配置存储
- Kotlin Coroutines、Flow、错误处理与取消
- WorkManager 与后台任务
- 阶段项目：新闻 / 笔记 / 阅读类 App

### 阶段五：应用架构与工程化

- MVC、MVP、MVVM、MVI / UDF、Repository、UseCase、Clean Architecture
- Hilt 依赖注入、分层架构与模块化
- 多环境配置、构建变体、资源隔离
- 组件边界、接口设计、可维护性与可测试性
- 阶段项目：把单体 App 演进成清晰的工程结构

### 阶段六：质量、性能与发布

- 单元测试、UI 测试、Mock 与测试替身
- 启动优化、内存优化、卡顿分析、包体积优化
- 崩溃治理、日志体系、埋点与可观测性
- R8 / ProGuard、签名、打包与发布流程
- 阶段项目：让 App 从“能跑”走向“可交付”

### 阶段七：Android Framework 与源码进阶

- Android 系统架构、AOSP 代码结构与源码阅读方法
- Binder 通信机制与系统服务
- SystemServer、AMS / ATMS、WMS、PMS
- View 绘制流程、Input 事件分发、Choreographer
- SurfaceFlinger、渲染链路与应用显示原理
- 阶段项目：从一次点击追踪到 Framework 调用链

当前 Framework 阶段已经展开到第 24 章。它不是突然跳进源码深水区，而是沿着一条非常具体的应用行为往下走：先从一次点击和一次系统服务调用建立入口，再追踪 Activity 如何被启动、窗口如何被添加、内容如何走向屏幕，然后观察触摸事件如何被系统派发并在 View 树中找到处理者，继续追问 UI 状态变化如何变成屏幕上的下一帧，再回到系统识别 App 的入口，理解安装、包解析、组件、权限和签名，接着进入资源系统，解释字符串、图片、主题和多语言资源为什么能被正确读取，再补齐代码加载主线，理解 Dex、ClassLoader、Dalvik、ART、so、动态加载和热修复如何共同决定 App 代码能否被找到并执行，然后继续追问这些代码运行在哪个进程里，理解 Zygote、应用沙箱、多进程、线程、OOM Adj 和 LMKD 如何共同决定 App 的运行空间与回收命运，再进入稳定性事故调查室，理解 ANR、Crash、tombstone、Watchdog、DropBox 和 bugreport 如何把异常现场变成可诊断证据，随后把后台调度、存储访问和安全模型接起来，理解系统为什么既要让 App 完成任务，又要守住电量、数据、权限和隐私边界，最后进入系统观测工具，把 logcat、dumpsys、bugreport、Perfetto、gfxinfo、meminfo、procstats 和 simpleperf 串成一条能复现、能定位、能复盘的证据链。

- 第 10 章负责打开入口：建立 Android 系统分层视角，认识 ActivityThread、Context、Handler、Looper、Binder 和 AOSP 源码阅读方法。它像一张进城地图，先告诉你 Framework 这座城市大概有哪些路。
- 第 11 章负责建立通信主线：从 Binder、AIDL、ServiceManager、SystemServer 到系统服务调用，让你理解 App 为什么不能直接调用系统内部能力，而要通过 Binder 和 system_server 协作。
- 第 12 章负责拆解页面启动：从 `startActivity()` 进入 AMS / ATMS，理解 Task、返回栈、ActivityRecord、launchMode、Intent Flag、进程创建和 ActivityThread 生命周期调度。学完这一章，你应该能解释“为什么这个页面会被创建、复用、销毁或回到前台”。
- 第 13 章负责追踪窗口显示：从 `setContentView` / Compose `setContent` 进入 Window、PhoneWindow、DecorView、ViewRootImpl、WindowManager 和 WMS，继续理解 Dialog、PopupWindow、输入法、窗口层级、Token、BadToken、白屏和一帧刷新。学完这一章，你应该能解释“Activity 已经启动之后，页面为什么真的能显示到屏幕上”。
- 第 14 章负责拆解输入事件：从触摸屏、InputReader、InputDispatcher 到 ViewRootImpl、Activity、ViewGroup、View 和 Compose pointer input，理解点击、滑动、拦截、CANCEL、滑动冲突与 Input ANR。学完这一章，你应该能解释“用户点到屏幕后，事件为什么由这个控件处理，而不是另一个控件处理”。
- 第 15 章负责深入渲染链路：从 `invalidate()`、`requestLayout()`、Choreographer、measure / layout / draw 到 HardwareRenderer、RenderThread、Surface、BufferQueue 和 SurfaceFlinger，理解掉帧、Jank、白屏、闪烁、过度绘制和黑屏。学完这一章，你应该能解释“业务状态变化之后，下一帧为什么能真的出现在屏幕上”。
- 第 16 章负责补齐包管理主线：从 APK 安装、Manifest 解析、组件注册、Intent 匹配到签名、权限、包可见性和多用户状态，理解安装失败、组件找不到、权限异常和查询不到 App。学完这一章，你应该能解释“系统为什么知道这个 App、组件和权限存在”。
- 第 17 章负责补齐资源系统主线：从 `res`、`R` 文件、`resources.arsc` 到 `AssetManager`、`Resources`、`Configuration`、Theme 和资源合并，理解多语言、夜间模式、密度适配、资源找不到、主题错乱和图片模糊。学完这一章，你应该能解释“系统为什么能为当前设备选出正确的字符串、图片和主题值”。
- 第 18 章负责补齐代码加载主线：从源码、class、Dex、D8 / R8 到 ClassLoader、Dalvik / ART、Profile、JNI、so 和动态加载，理解 `ClassNotFoundException`、`NoSuchMethodError`、`UnsatisfiedLinkError`、插件化和热修复的工程边界。学完这一章，你应该能解释“系统为什么能找到并执行这个 App 的代码”，也能看懂早期 Dalvik 文章和现代 ART 优化之间的差异。
- 第 19 章负责补齐进程运行空间：从 Linux 进程、uid、应用沙箱到 Zygote fork、ActivityThread、主线程 / Binder 线程、多进程、OOM Adj 和 LMKD，理解后台死亡、状态丢失、多进程错乱和保活误区。学完这一章，你应该能解释“这个 App 进程从哪里来、为什么能隔离运行、又为什么会被系统回收”。
- 第 20 章负责补齐稳定性诊断主线：从 ANR、Java Crash、Native Crash、tombstone 到 Watchdog、DropBox、bugreport 和 dumpsys，理解系统如何发现无响应、记录崩溃、保存事故证据，并把事故转成可修复、可回归、可监控的工程闭环。学完这一章，你应该能解释“App 或系统出问题后，证据在哪里、如何读、如何证明修复有效”。
- 第 21 章负责补齐后台调度主线：从 Service、Foreground Service、Alarm、JobScheduler 到 WorkManager、Doze 和 App Standby，理解后台任务为什么会延迟、丢失、被限制，以及如何在系统规则内设计可靠任务。
- 第 22 章负责补齐数据访问主线：从 App 私有存储、缓存、MediaStore、Photo Picker 到 SAF 和 URI 权限，理解 Android 为什么从路径访问走向范围访问，以及如何让用户数据既可用又不越界。
- 第 23 章负责补齐安全边界主线：从 UID、沙箱、runtime permission、AppOps、签名、Keystore 到组件暴露和日志脱敏，理解系统如何判断“谁在访问、能不能访问、访问之后留下什么证据”。
- 第 24 章负责补齐系统观测主线：从 logcat、dumpsys、bugreport、Perfetto 到 gfxinfo、meminfo、procstats 和 simpleperf，理解复杂事故如何从“感觉不对”变成一份可复现、可验证、可回归的工程证据链。

这几章串起来后，会形成一条完整的 Framework 入门链路：

```text
一次点击
  -> App 主线程消息
      -> Binder 请求系统服务
          -> AMS / ATMS 调度 Activity
              -> Window / DecorView 接入窗口
                  -> WMS 管理窗口层级
                      -> Choreographer 驱动一帧刷新
                          -> InputDispatcher 派发触摸事件
                              -> ViewRootImpl / ViewGroup / View 处理交互
                                  -> Choreographer 调度 UI 刷新
                                      -> RenderThread / SurfaceFlinger 完成渲染与合成
                                          -> PMS 管理安装包、组件、权限和签名
                                              -> Resources 选择字符串、图片、主题和配置资源
                                                  -> ClassLoader / Dalvik / ART 加载和执行代码
                                                      -> Zygote / 进程模型承载运行空间并在低内存时参与回收
                                                          -> ANR / Crash / Watchdog / tombstone 记录异常现场并支持稳定性治理
                                                              -> 后台任务 / 前台服务 / Alarm / WorkManager 在系统限制下完成可靠调度
                                                                  -> 存储系统 / MediaStore / SAF / URI 权限管理用户数据访问边界
                                                                      -> 安全模型 / 权限 / 签名 / AppOps / Keystore 守住敏感能力和隐私数据
                                                                          -> 系统观测工具 / Perfetto / dumpsys / bugreport 把复杂事故转成可复盘证据链
```

### 学习顺序

建议按照目录顺序学习，不要急着跳到 Framework。Android 的底层知识并不是孤立存在的，它最好从真实业务问题里长出来：当你写过页面，才会真正关心 View 如何绘制；当你处理过页面跳转，才会理解 AMS / ATMS 的价值；当你遇到卡顿和内存问题，Framework 的调用链才不再是抽象名词。

推荐节奏如下：

- 第 1 步：先完成环境搭建，确保可以独立创建、运行、调试一个 Android 工程。
- 第 2 步：掌握 Kotlin 与 Android 基础，能写出简单但完整的页面和交互。
- 第 3 步：进入 Compose 与 Jetpack，建立现代 Android UI 开发方式。
- 第 4 步：加入网络、本地数据、协程和 Flow，让 App 具备真实业务能力。
- 第 5 步：重构项目架构，学习分层、依赖注入、模块化和测试。
- 第 6 步：补齐性能、稳定性、发布与工程协作能力。
- 第 7 步：回到系统底层，从应用行为反向理解 Framework。

每一章建议都按照“阅读 README -> 运行代码工程 -> 修改一个小功能 -> 完成进阶任务 -> 复盘关键问题”的方式学习。真正的掌握，不是看懂一遍，而是能在自己的项目里重新写出来。

### 环境要求

为了保证课程体验一致，建议使用以下开发环境：

- 操作系统：macOS、Windows 或 Linux，推荐使用内存 16GB 及以上的开发机器。
- IDE：Android Studio 稳定版，建议使用当前官方稳定版本或更新版本。
- JDK：JDK 17 及以上，优先使用 Android Studio 内置 JDK。
- Android Gradle Plugin：建议使用课程工程中声明的版本，不建议自行随意升级。
- Gradle：正式工程建议使用项目自带的 Gradle Wrapper，即通过 `./gradlew` 或 `gradlew.bat` 执行构建；当前示例工程也可以直接用 Android Studio 打开并完成 Gradle Sync。
- Android SDK：安装课程工程要求的 `compileSdk`、`minSdk` 以及对应 Build Tools。
- 运行设备：Android 模拟器或 Android 8.0 及以上真机，推荐准备一台真机用于权限、性能和系统行为调试。
- Git：用于拉取课程代码、提交练习记录和对比不同阶段的实现。

首次运行建议：

- 使用 Android Studio 打开对应章节目录下的代码工程。
- 等待 Gradle Sync 完成。
- 选择模拟器或真机运行 `app` 模块。
- 如果构建失败，优先检查 JDK、SDK、Gradle Sync 日志和网络代理配置。

### GitHub Pages 文档站

课程文档已经通过 GitHub Pages 发布，可以直接访问：

[https://android24.github.io/hello-android/](https://android24.github.io/hello-android/)

当前文档站使用 Docsify 承载：

- `docs/index.html`：Docsify 文档站入口。
- `docs/README.md`：文档站首页。
- `docs/_sidebar.md`：章节侧边栏。
- `docs/.nojekyll`：确保 GitHub Pages 不会忽略 `_sidebar.md`。
- `.github/workflows/deploy-pages.yml`：自动发布 `docs/` 到 GitHub Pages。

后续维护方式：

1. 修改 `docs/` 下的章节内容或侧边栏。
2. 推送到 `main` 分支。
3. 等待 `Deploy docs to GitHub Pages` 工作流完成。
4. 刷新上面的 Pages 地址，即可看到最新课程文档。

### 示例工程

课程配套示例工程统一放在 [examples](examples/) 目录下。每个大章节对应一个独立示例工程，建议按照“阅读章节文档 -> 运行示例工程 -> 完成练习任务 -> 自己做一次改造”的方式学习。

[examples/README.md](examples/README.md) 现在也整理成了一张课程闯关地图：你可以按“入门与 UI -> 数据与架构 -> 质量与性能 -> Framework 主线 -> 系统工程实验室”的顺序推进，并用每个实验室里的任务板、事故剧本和诊断报告确认自己是否真正通关。

当前已创建：

- [第1章 Kotlin 与 Android 基础示例工程](examples/01-kotlin-and-android-basics/)
- [第2章 Compose UI 基础示例工程](examples/02-compose-ui-basics/)
- [第3章 Activity、生命周期与导航示例工程](examples/03-activity-lifecycle-navigation/)
- [第4章 网络、Room 与 DataStore 示例工程](examples/04-network-room-datastore/)
- [第5章 Android 应用架构演进示例工程](examples/05-android-architecture-evolution/)
- [第6章 Kotlin Coroutines、Flow 与后台任务示例工程](examples/06-coroutines-flow-workmanager/)
- [第7章 Hilt 依赖注入、模块化与工程化示例工程](examples/07-hilt-modularization-engineering/)
- [第8章 测试、质量保障与可交付示例工程](examples/08-testing-quality-delivery/)
- [第9章 Android 性能优化与稳定性治理示例工程](examples/09-performance-stability-lab/)
- [第10章 Android Framework 入门、系统架构与源码阅读方法示例工程](examples/10-framework-source-walkthrough/)
- [第11章 Binder、SystemServer 与系统服务入门示例工程](examples/11-binder-system-service-lab/)
- [第12章 AMS / ATMS、Activity 启动与任务栈调度示例工程](examples/12-activity-task-launch-lab/)
- [第13章 WMS、Window、DecorView 与窗口显示机制示例工程](examples/13-window-display-lab/)
- [第14章 Input 事件分发、触摸系统与交互响应机制示例工程](examples/14-input-event-dispatch-lab/)
- [第15章 View 绘制、RenderThread、SurfaceFlinger 与渲染链路示例工程](examples/15-rendering-frame-lab/)
- [第16章 PMS、应用安装、包管理与权限机制示例工程](examples/16-package-manager-lab/)
- [第17章 资源系统、AssetManager、Resources 与主题机制示例工程](examples/17-resource-system-lab/)
- [第18章 ClassLoader、Dex、Dalvik / ART 与动态加载机制示例工程](examples/18-code-loading-lab/)
- [第19章 Android 进程模型、Zygote、应用沙箱与内存管理机制示例工程](examples/19-process-zygote-lab/)
- [第20章 ANR、Crash、Watchdog 与系统稳定性诊断机制示例工程](examples/20-stability-diagnosis-lab/)
- [第21章 后台任务、前台服务、Alarm、JobScheduler 与系统后台限制示例工程](examples/21-background-scheduling-lab/)
- [第22章 存储系统、Scoped Storage、MediaStore 与数据访问机制示例工程](examples/22-storage-access-lab/)
- [第23章 Android 安全模型、权限、签名、AppOps 与数据保护示例工程](examples/23-security-permission-lab/)

## 贡献者名单

| 姓名 | 职责 | 简介 |
| :----| :---- | :---- |
| 龚福均 | 项目负责人 | 一个有十年Android开发经验的从业者 |
| 虚位以待 | 第1章贡献者 | 小明的朋友 |
| 虚位以待 | 第2章贡献者 | 小明的朋友 |

## 参与贡献

- 如果你发现了一些问题，可以提Issue进行反馈，如果提完没有人回复你可以联系[保姆团队](https://github.com/datawhalechina/DOPMC/blob/main/OP.md)的同学进行反馈跟进~
- 如果你想参与贡献本项目，可以提Pull request，如果提完没有人回复你可以联系[保姆团队](https://github.com/datawhalechina/DOPMC/blob/main/OP.md)的同学进行反馈跟进~
- 如果你对 Datawhale 很感兴趣并想要发起一个新的项目，请按照[Datawhale开源项目指南](https://github.com/datawhalechina/DOPMC/blob/main/GUIDE.md)进行操作即可~

## 关注我们

<div align=center>
<p>扫描下方二维码关注公众号：Datawhale</p>
<img src="https://raw.githubusercontent.com/datawhalechina/pumpkin-book/master/res/qrcode.jpeg" width = "180" height = "180">
</div>

## LICENSE

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="知识共享许可协议" style="border-width:0" src="https://img.shields.io/badge/license-CC%20BY--NC--SA%204.0-lightgrey" /></a><br />本作品采用<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">知识共享署名-非商业性使用-相同方式共享 4.0 国际许可协议</a>进行许可。

*注：默认使用CC 4.0协议，也可根据自身项目情况选用其他协议*
