# 从零开始的 Android 全栈开发课程

欢迎来到《从零开始的 Android 全栈开发课程》文档站。

这套课程不是零散知识点列表，而是一条从入门到进阶、从应用开发到 Android Framework 的成长路线。你会先让 App 跑起来，再让它变得清晰、稳定、可维护，最后继续向下追问：一次启动、一次点击、一次页面跳转、一次渲染，究竟是怎样穿过 Framework、系统服务和底层机制，最终变成用户手中的 Android 体验。

## 如何学习

建议按照左侧目录顺序推进：

```text
基础工程
  -> Compose UI
      -> Activity 与导航
          -> 数据、网络与异步
              -> 架构与工程化
                  -> 测试、质量与性能
                      -> Android Framework 与源码阅读
```

每一章都按同一节奏学习：

- 先阅读章节文档，建立知识地图。
- 再打开配套示例工程，观察它如何运行。
- 然后改一个小功能，让代码留下你的痕迹。
- 最后完成章节练习，用自己的话复盘关键链路。

如果你已经完成第 20 章，建议先读一遍：

- [课程中期地图：从会写 App 到读懂系统](midterm_map.md)

它会帮你把前 20 章重新串成一条路线，并说明后续为什么要进入后台调度、存储、安全、观测工具和大型工程治理。

## 当前章节

目前课程已经推进到 Framework 与系统工程阶段：

- 第 10 章：Android Framework 入门、系统架构与源码阅读方法。
- 第 11 章：Binder、SystemServer 与系统服务入门。
- 第 12 章：AMS / ATMS、Activity 启动与任务栈调度。
- 第 13 章：WMS、Window、DecorView 与窗口显示机制。
- 第 14 章：Input 事件分发、触摸系统与交互响应机制。
- 第 15 章：View 绘制、RenderThread、SurfaceFlinger 与渲染链路。
- 第 16 章：PMS、应用安装、包管理与权限机制。
- 第 17 章：资源系统、AssetManager、Resources 与主题机制。
- 第 18 章：ClassLoader、Dex、Dalvik / ART 与动态加载机制。
- 第 19 章：Android 进程模型、Zygote、应用沙箱与内存管理机制。
- 第 20 章：ANR、Crash、Watchdog 与系统稳定性诊断机制。
- 第 21 章：后台任务、前台服务、Alarm、JobScheduler 与系统后台限制。
- 第 22 章：存储系统、Scoped Storage、MediaStore 与数据访问机制。
- 第 23 章：Android 安全模型、权限、签名、AppOps 与数据保护。
- 第 24 章：系统观测工具：Perfetto、dumpsys、bugreport 与证据链分析。
- 第 25 章：大型 Android 工程治理：模块边界、构建效率、发布、监控与团队协作。

这几章会逐步串起一条完整路径：

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
                                                      -> Zygote / 进程模型承载运行空间并参与后台回收
                                                              -> ANR / Crash / Watchdog / tombstone 记录异常现场并支撑稳定性治理
                                                                  -> 后台调度 / 前台服务 / Alarm / WorkManager 接受系统限制并完成可靠任务
                                                                      -> 存储系统 / MediaStore / SAF / URI 权限管理用户数据边界
                                                                          -> 安全模型 / 权限 / 签名 / AppOps / Keystore 保护敏感能力和数据
                                                                              -> 系统观测工具 / Perfetto / dumpsys / bugreport 把复杂事故转成可复盘证据链
                                                                                  -> 大型工程治理把边界、构建、发布、监控和协作沉淀成长期稳定能力
```

## 配套工程

所有示例工程统一放在仓库的 `examples/` 目录下。文档负责讲清楚“为什么学、学什么、怎么学”，示例工程负责把知识落到真实项目里。

学习时请记住一句话：

```text
真正的掌握，不是看懂代码，而是能把它改成自己的代码。
```
