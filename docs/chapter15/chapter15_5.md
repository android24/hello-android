# 15.5 Surface、BufferQueue 与 SurfaceFlinger

前面几节讲的是 App 侧如何组织绘制和提交渲染。

但 App 画完之后，画面还没有自动出现在屏幕上。

这一节继续往系统显示侧走：

```text
App 的渲染结果如何交给系统合成？
```

关键词是 Surface、BufferQueue 和 SurfaceFlinger。

## 本节剧情钩子

你打开一个页面，页面上有 Activity 内容、状态栏、导航栏、Dialog、输入法。

它们看起来叠在同一块屏幕上。

但系统不是让每个窗口直接抢着画屏幕，而是让不同窗口提交自己的内容，再由 SurfaceFlinger 做最终合成。

屏幕像一块舞台，SurfaceFlinger 是最后的导播台。

## 本节定位

本节把 App 渲染结果和系统显示合成连接起来，为理解黑屏、闪烁、窗口层级、视频显示和性能问题打基础。

## 学习目标

学完本节后，你应该能够：

- 理解 Surface 是 App 和显示系统之间的重要出口。
- 知道 BufferQueue 大致解决生产者和消费者协作问题。
- 理解 SurfaceFlinger 负责最终合成。
- 能把窗口、Buffer 和屏幕显示联系起来。
- 知道 App 侧能观察哪些证据，哪些问题需要继续追到系统显示侧。

## 第一部分：Surface 是内容出口

在 Android 里，一个窗口通常会对应可供绘制的 Surface。

可以这样理解：

```text
Window 是窗口抽象，
Surface 是这块窗口内容的绘制出口。
```

App 侧通过渲染管线把内容画进 Buffer。

这些 Buffer 和 Surface 关联起来，再被系统拿去合成。

第 13 章讲过 Window 和 WMS，第 15 章要继续补上：

```text
窗口被管理之后，它的内容如何进入屏幕合成。
```

## 第二部分：BufferQueue 连接生产者和消费者

App 负责生产画面内容。

SurfaceFlinger 负责消费这些内容并合成到屏幕。

它们之间需要一个队列协调：

```text
App / RenderThread / GPU
  -> 生产 Buffer
      -> BufferQueue
          -> SurfaceFlinger 消费 Buffer
```

BufferQueue 的价值是：

- 解耦生产和消费。
- 避免双方直接互相阻塞。
- 支持多 Buffer 轮转。
- 配合 VSYNC 做稳定显示。

简化比喻：

```text
App 是厨房，
BufferQueue 是传菜台，
SurfaceFlinger 是出餐总控。
```

## 第三部分：SurfaceFlinger 是合成者

SurfaceFlinger 是 Android 显示系统中的核心服务。

它会收集多个 Layer 的内容，按层级、透明度、变换等规则合成最终画面。

常见 Layer 可能来自：

- Activity 主窗口。
- Dialog。
- PopupWindow。
- 输入法。
- 状态栏。
- 导航栏。
- 视频 Surface。

最终合成结果送到显示设备。

这解释了为什么第 13 章的窗口层级和第 15 章的合成链路必须连起来看。

## 第四部分：为什么有黑屏和闪烁

黑屏和闪烁往往意味着某个环节没有及时提供有效内容。

可能原因包括：

- 首帧 Buffer 迟迟没有提交。
- Surface 创建了，但内容还没画好。
- 窗口切换时旧 Buffer 和新 Buffer 衔接不好。
- Activity 启动、主题背景、内容绘制不同步。
- 视频或特殊 Surface 生命周期处理不当。

排查时可以问：

```text
窗口存在吗？
Surface 存在吗？
首帧 Buffer 提交了吗？
SurfaceFlinger 是否拿到了可合成内容？
```

## 第五部分：为什么视频和相机常提到 Surface

视频播放、相机预览、游戏渲染经常直接和 Surface 打交道。

原因是它们对性能和显示路径要求更高。

例如：

- 视频解码结果可以直接输出到 Surface。
- 相机预览可以直接写入 Surface。
- 游戏引擎可以直接渲染到 Surface。

这类场景不一定走普通 View 的完整绘制路径，但最终仍要进入系统合成。

## 第六部分：Compose 到 SurfaceFlinger 的关系

Compose 改变的是 UI 编写模型。

但最终显示仍然离不开：

```text
Compose UI
  -> Android 渲染管线
      -> Surface
          -> BufferQueue
              -> SurfaceFlinger
```

所以学习 SurfaceFlinger 不是只为传统 View 服务，它同样帮助理解 Compose 应用的显示结果。

## 第七部分：App 侧证据与系统侧边界

学习 SurfaceFlinger 时，初学者容易产生两个误区。

第一个误区是：

```text
我在 App 代码里看不到 SurfaceFlinger，所以它和我没关系。
```

第二个误区是：

```text
所有显示问题都能靠改业务布局解决。
```

更稳的做法是先分清边界。

| 观察层级 | 你能看到什么 | 常用线索 | 适合排查的问题 |
| --- | --- | --- | --- |
| App 业务层 | 状态变化、点击回调、数据加载 | 日志、断点、状态流 | 内容没变、点击后没刷新 |
| View / Compose 层 | 是否触发重组、布局、绘制 | Layout Inspector、日志、自定义 View 回调 | 尺寸异常、UI 不刷新、布局过重 |
| 主线程与帧调度 | 帧回调、主线程任务、慢帧 | Profiler、Choreographer、gfxinfo | 掉帧、点击反馈慢、动画卡顿 |
| 渲染提交层 | RenderThread、GPU 压力、图片和复杂绘制 | Perfetto、gfxinfo、GPU profile | 主线程不忙但仍掉帧 |
| 系统显示层 | Surface、Buffer、Layer、合成 | SurfaceFlinger、Perfetto、dumpsys | 黑屏、闪烁、视频预览异常、窗口合成问题 |

可以把排查顺序写成这样：

```text
先确认 App 有没有提交有效 UI
  -> 再确认 View / Compose 是否完成刷新
      -> 再看主线程和帧调度是否超时
          -> 再看渲染线程、GPU 和 Buffer 提交
              -> 最后追 SurfaceFlinger 合成与显示
```

这个顺序能避免一上来就跳进系统源码，也能避免只在业务层反复猜。

## 本节小挑战

### 合成现场判断题

请判断下面现象可能涉及哪些层：

- Dialog 盖住 Activity。
- 输入法弹出后页面被压缩。
- 视频画面黑屏但控制按钮正常。
- Activity 首帧白屏。
- 状态栏和 App 内容同时显示。

## 本节实践任务

### 基础任务

- 打开一个页面，列出你能看到的窗口元素。
- 判断哪些可能是独立窗口或系统 Layer。
- 写下它们最终为什么能出现在同一块屏幕上。

### 进阶任务

- 找一个视频、地图或相机预览页面。
- 思考它为什么常和 Surface 相关。
- 对比普通 Text / Button 绘制路径。

## 本节小结

App 侧绘制不是终点。渲染结果会进入 Surface 对应的 Buffer，再通过 BufferQueue 交给 SurfaceFlinger。SurfaceFlinger 负责把多个窗口和 Layer 合成到最终屏幕。理解这条链路，黑屏、闪烁、特殊窗口、视频显示和首帧问题才有了系统侧解释。
