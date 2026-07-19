# 15.3 measure、layout、draw：View 树如何产出绘制命令

上一节我们知道了：UI 刷新会被调度到一帧里。

这一节进入一帧的核心过程：

```text
measure
  -> layout
      -> draw
```

这三个词看起来简单，但它们决定了 View 多大、放哪、画什么。

## 本节剧情钩子

你写了一个自定义布局。

页面能显示，但子 View 总是偏一点，或者高度不对。

这时候问题不在 SurfaceFlinger，也不在 GPU，而在 View 树内部的三件事：

```text
有没有量对？
有没有摆对？
有没有画对？
```

## 本节定位

本节讲清楚 View 树刷新中最核心的三个阶段，为后续 RenderThread、GPU 和性能问题分析打基础。

## 学习目标

学完本节后，你应该能够：

- 理解 measure、layout、draw 的职责边界。
- 知道父 View 和子 View 如何协作。
- 理解自定义 View / ViewGroup 常见问题来自哪里。
- 能用三阶段模型解释 UI 错位、尺寸异常和重绘问题。

## 第一部分：measure 问“你想多大”

measure 阶段解决尺寸问题。

父 View 会带着限制条件询问子 View：

```text
在这些约束下，你希望自己多大？
```

Android 里常见约束来自 MeasureSpec：

- EXACTLY：大小已经确定。
- AT_MOST：最多这么大。
- UNSPECIFIED：不做明确限制。

可以这样理解：

```text
父容器给边界
  -> 子 View 计算期望尺寸
      -> 父容器再综合决定
```

如果 measure 写错，常见表现是：

- View 看不见。
- View 尺寸过大或过小。
- 文本被截断。
- 列表 item 高度异常。

## 第二部分：layout 问“你放哪里”

layout 阶段解决位置问题。

measure 只决定大小，不决定坐标。

layout 会把每个子 View 放到具体位置：

```text
left
top
right
bottom
```

自定义 ViewGroup 最容易在这里出错。

例如：

- 忘了给子 View 调用 layout。
- 只考虑第一个子 View。
- 没处理 padding / margin。
- 横向和纵向偏移计算错。

## 第三部分：draw 问“你画什么”

draw 阶段负责真正的绘制描述。

简化过程：

```text
draw background
  -> onDraw
      -> dispatchDraw children
          -> draw foreground
```

不同 View 可能略有差异，但有一个关键点：

```text
ViewGroup 默认通常不绘制自己的内容，更多负责绘制子 View。
```

如果你写自定义 View，通常会重写 `onDraw()`。

如果你写自定义 ViewGroup，更多要关心 `onMeasure()` 和 `onLayout()`。

## 第四部分：父子协作不是单向命令

View 树刷新不是父 View 独自决定一切。

它更像一场协商：

```text
父 View：我给你这些限制
子 View：我想要这个大小
父 View：好，我把你放在这里
子 View：我按自己的内容绘制
```

这种协作关系解释了很多 UI 问题：

- 子 View 明明设置了宽高，为什么没生效？
- 父容器为什么会裁剪子 View？
- ScrollView 里为什么高度测量容易出问题？
- RecyclerView item 为什么重布局成本高？

## 第五部分：Compose 的 measure / layout / draw

Compose 也有测量、布局和绘制。

只是它不再要求你直接继承 View 写 `onMeasure()`。

Compose 中的类似过程是：

```text
Composition
  -> Layout
      -> Draw
```

Modifier 也会参与布局和绘制。

例如：

- `padding()` 会影响布局。
- `background()` 会影响绘制。
- `size()` 会改变测量约束。
- `drawBehind()` 会参与绘制。

所以 Compose 改变的是表达方式，不是消灭渲染规律。

## 第六部分：三阶段和性能

三阶段成本不同。

通常来说：

- 只重画，比重新布局成本低。
- 大范围 requestLayout，可能带来更多测量和布局。
- 复杂 onDraw，可能导致绘制耗时。
- 列表中频繁改变 item 尺寸，容易引起卡顿。

排查时可以先问：

```text
这次变化到底影响了尺寸，还是只影响了内容？
```

这个问题很小，但非常有用。

## 本节小挑战

### 三阶段判断题

请判断下面问题更可能出在哪个阶段：

- 自定义 View 完全不显示。
- 子 View 全部堆在左上角。
- 背景颜色没有更新。
- 文案变长后被裁剪。
- 列表滚动时 item 高度频繁变化导致卡顿。

## 本节实践任务

### 基础任务

- 找一个自定义 View 或简单布局。
- 标出它的 measure、layout、draw 职责。
- 写下哪些变化需要重新 layout，哪些只需要 draw。

### 进阶任务

- 写一个简单自定义 ViewGroup。
- 手动测量并摆放两个子 View。
- 故意改错 layout 坐标，观察 UI 表现。

## 本节小结

measure 决定大小，layout 决定位置，draw 决定绘制内容。父子 View 在这三个阶段协作，最终产出一帧中 App 侧的绘制结果。理解这三个阶段，UI 错位、尺寸异常、自定义 View 不刷新和布局性能问题都会更容易定位。
