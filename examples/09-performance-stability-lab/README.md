# 示例工程：性能优化与稳定性治理实验室

## 对应章节

第9章 Android 性能优化与稳定性治理

## 工程目标

本工程用于配合第 9 章，把启动、卡顿、内存、ANR、崩溃和包体积这些主题放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕三个问题展开：

- 一个性能问题如何被观察？
- 一个稳定性风险如何被模拟和解释？
- 发布前应该检查哪些性能与稳定性指标？

## 当前效果

运行后你会看到一个“第 9 章性能与稳定性实验室”页面：

- 顶部展示构建类型、日志开关和启动节点记录。
- `性能体检分数` 会用 100 分制展示当前风险状态。
- `实验观察点` 会提示当前实验应该看页面、Logcat 还是 Profiler。
- `启动体检` 展示进程创建到首个 Activity `onCreate` 的时间。
- `卡顿与 ANR 风险` 可以模拟主线程阻塞。
- `后台 CPU 实验` 会把计算放到 `Dispatchers.Default`。
- `内存体检` 可以保留和释放小块内存，观察引用持有带来的变化。
- `崩溃治理` 会捕获一次模拟异常，展示稳定性报告思路。
- `Compose 列表观察` 展示稳定 key 和列表性能提示。
- `发布前性能稳定性体检` 提供启动、卡顿、内存、ANR、崩溃、包体积的检查清单。
- `重置实验结果` 可以清空实验状态，方便反复观察。

体检分数是教学用风险可视化，不代表真实线上评分。它的作用是让学习者直观看到：主线程阻塞、ANR 风险、内存持有和异常处理都会影响体验分。

这个 demo 里的“卡顿”和“ANR 风险”是受控演示，不会故意制造真正长时间 ANR。学习重点是理解现象、定位方向和治理原则。

## 运行方式

1. 使用 Android Studio 打开 `examples/09-performance-stability-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 依次点击页面中的实验按钮。
5. 结合 Logcat、Profiler、Layout Inspector 或系统帧率工具观察变化。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
09-performance-stability-lab/
  app/
    src/main/java/com/helloandroid/performance/
      PerformanceLabApplication.kt
      MainActivity.kt
      PerformanceLabState.kt
      PerformanceLabViewModel.kt
      PerformanceLabScreen.kt
  quality/
    performance-checklist.md
    stability-checklist.md
    profiling-notes.md
```

## 关键源码入口

- `PerformanceLabApplication.kt`：记录进程创建时间。
- `MainActivity.kt`：记录首个 Activity 创建时间，并把构建信息传给 ViewModel。
- `PerformanceLabViewModel.kt`：管理卡顿、CPU、内存、异常等实验动作。
- `PerformanceLabScreen.kt`：展示实验页面和体检清单。
- `quality/performance-checklist.md`：性能体检清单。
- `quality/stability-checklist.md`：稳定性体检清单。
- `quality/profiling-notes.md`：观察与分析建议。

## 计划覆盖知识点

- 启动链路
- 首屏时间
- 主线程阻塞
- 掉帧与卡顿
- ANR 风险
- 后台 CPU 调度
- 内存分配与释放
- 崩溃捕获与错误报告
- Compose 列表性能
- release 日志开关
- 发布前性能稳定性检查

## 练习任务

### 基础任务

- 点击 `模拟 180ms 卡顿`，观察页面短暂停顿，并解释为什么主线程不能做重活。
- 观察 `性能体检分数` 如何随着卡顿、ANR 和内存实验变化。
- 点击 `在后台计算质数`，观察 UI 为什么仍然可以响应。
- 连续点击 `保留 2MB`，观察内存报告变化，再点击 `释放内存`。
- 点击 `捕获一次模拟异常`，写出一份崩溃报告至少应该包含哪些信息。
- 阅读 `quality/performance-checklist.md`，为课程 App 写一份性能体检清单。

### 进阶任务

- 搜索 `PerformanceLab`，观察每个实验输出的诊断日志。
- 为每个实验扩展更具体的日志字段，例如耗时、内存块数量和实验名称。
- 使用 Android Studio Profiler 观察 CPU 和 Memory。
- 给 Compose 列表增加更多 item，观察滚动体验。
- 把某个耗时任务从主线程迁移到 `Dispatchers.Default`。
- 为 release 构建补充包体积检查说明。

## 通关目标

完成本章后，你应该能说清楚：

- 启动慢应该先看哪些节点。
- 卡顿和 ANR 为什么通常和主线程有关。
- 内存问题为什么要关注引用持有和大对象。
- 崩溃治理为什么需要堆栈、版本、设备和用户路径。
- 包体积优化为什么要从资源、依赖和构建产物入手。
- 性能优化为什么必须先观察、再定位、最后验证。
