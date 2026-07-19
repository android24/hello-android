# 15.4 HardwareRenderer、DisplayList 与 RenderThread

前面我们看到 View 树如何完成 measure、layout、draw。

但 Android 现代渲染并不是简单地让主线程直接把每个像素画完。

这一节继续往下看：

```text
View draw 之后，渲染命令交给谁？
```

答案会遇到 HardwareRenderer、DisplayList 和 RenderThread。

## 本节剧情钩子

你写了一个页面。

主线程没有明显耗时，但动画仍然偶尔不顺。

这时你会意识到：画面流畅不只取决于业务线程，也取决于渲染命令、RenderThread 和 GPU 的协作。

主线程负责组织 UI，但它不是唯一的角色。

## 本节定位

本节从 View 树刷新过渡到硬件加速渲染，帮助你理解 App 侧渲染如何从“画 View”进入“提交渲染命令”。

## 学习目标

学完本节后，你应该能够：

- 初步理解硬件加速为什么存在。
- 知道 DisplayList / RenderNode 大致记录什么。
- 理解 HardwareRenderer 和 RenderThread 的角色。
- 能把主线程卡顿和渲染线程压力区分开。

## 第一部分：为什么需要硬件加速

早期理解绘制时，很容易想象成：

```text
主线程一边走 View 树，一边把像素画到屏幕
```

但现代 UI 很复杂：

- 圆角。
- 阴影。
- 动画。
- 透明度。
- 缩放。
- 列表滚动。
- Compose 绘制。

这些工作如果都压在 CPU 和主线程上，成本会很高。

硬件加速的核心思路是：

```text
让 GPU 更擅长的工作交给 GPU。
```

## 第二部分：DisplayList 是绘制命令记录

View 的 draw 阶段不是每次都要立即把所有内容变成最终像素。

它可以把绘制操作记录成一组命令。

简化理解：

```text
drawCircle
drawText
drawBitmap
clip
translate
alpha
```

这些命令可以被记录、复用、重新排序或提交给后续渲染流程。

在 Android 中，你会遇到 RenderNode、DisplayList 等相关概念。

不用一开始记住所有细节，先抓住一句话：

```text
App 侧 draw 会产出渲染命令，而不只是直接画完像素。
```

## 第三部分：HardwareRenderer 负责提交渲染

HardwareRenderer 可以理解为 ViewRootImpl 和底层渲染系统之间的重要桥梁。

它负责把 View 树产生的渲染信息提交给硬件渲染管线。

简化链路：

```text
ViewRootImpl.performTraversals()
  -> draw
      -> HardwareRenderer
          -> RenderThread
              -> GPU
```

这条链路让渲染工作不完全堵在主线程上。

## 第四部分：RenderThread 是渲染侧工人

RenderThread 是 Android 渲染链路里的重要线程。

可以粗略理解为：

```text
主线程负责构建 UI 与提交任务，
RenderThread 负责执行部分渲染工作。
```

它能帮助减少主线程压力，但并不意味着主线程可以随便卡。

因为很多关键步骤仍然离不开主线程：

- input 回调。
- 状态更新。
- measure / layout。
- View 树遍历。
- 部分绘制命令生成。

主线程迟到，后面的渲染也会迟到。

### 渲染链路角色卡

这一节最容易混淆的是“谁负责什么”。先用一张角色卡压住边界：

| 角色 | 主要职责 | 如果它慢了，可能看到什么 |
| --- | --- | --- |
| 主线程 | 处理输入、业务状态、View 树遍历、measure / layout / 部分 draw | 点击延迟、动画第一帧卡、列表滚动不跟手 |
| ViewRootImpl | 连接 Window 和 View 树，调度 traversal，驱动测量布局绘制 | 首帧慢、布局更新不及时、刷新节奏异常 |
| DisplayList / RenderNode | 记录绘制命令和部分可复用的渲染信息 | 复杂绘制命令过多，后续渲染压力变大 |
| HardwareRenderer | 把 View 树渲染结果提交到硬件渲染管线 | draw 阶段之后仍可能出现提交或同步成本 |
| RenderThread | 执行部分渲染任务，与 GPU 协作 | 主线程看似空闲，但动画或渲染仍然掉帧 |
| GPU | 执行图形绘制、纹理、混合、裁剪等工作 | 复杂阴影、透明叠加、超大图片导致帧耗时 |

这张表不是源码级精确定义，而是排查问题时的第一张地图。

当你看到掉帧，可以先问：

```text
是主线程来晚了？
还是渲染命令太重？
还是 GPU / RenderThread 压力太大？
```

## 第五部分：动画为什么有时还能跑

有些属性动画在硬件加速下可以更高效。

例如：

- translation。
- alpha。
- scale。

如果只是改变已有 RenderNode 的某些属性，成本可能比重新 layout 整棵 View 树低。

这也是为什么性能优化里常说：

```text
能用 transform 解决的动画，不要轻易触发布局。
```

当然，这不是绝对规则，但它是一个很好的优化方向。

## 第六部分：如何区分主线程和渲染压力

看到掉帧时，不要只说“主线程卡了”。

可以先问：

- 主线程是否有长任务？
- measure / layout 是否太重？
- draw 是否太复杂？
- 图片是否太大？
- 阴影、模糊、裁剪是否太多？
- RenderThread 或 GPU 是否压力过高？

真正的性能分析，需要证据。

常见证据包括：

- Profiler。
- Layout Inspector。
- Frame timing。
- Perfetto。
- `dumpsys gfxinfo`。

第 15 章不要求你马上掌握所有工具，但要先建立分层意识。

## 本节小挑战

### 渲染压力判断题

请判断下面优化方向是否合理：

- 把每一帧都改变宽高的动画，改成 translation。
- 自定义 View 每次 onDraw 都创建大量对象。
- 列表滚动时加载超大图片。
- 使用复杂阴影和透明叠加后出现掉帧。
- 主线程空闲但仍然掉帧，继续只看业务代码。

## 本节实践任务

### 基础任务

- 找一个有动画的页面。
- 判断它改变的是 layout 属性，还是 transform 属性。
- 写下你认为哪种成本更低。

### 进阶任务

- 用 Android Studio Profiler 观察滚动或动画。
- 记录主线程是否有明显长任务。
- 思考是否可能存在渲染线程或 GPU 压力。

## 本节小结

View 树刷新之后，Android 还会通过硬件加速管线提交渲染命令。DisplayList / RenderNode 记录绘制操作，HardwareRenderer 负责提交，RenderThread 和 GPU 协作完成渲染。理解这些角色，才能把掉帧问题从“主线程卡了”升级为“整条渲染链路哪里慢了”。
