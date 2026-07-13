# 14.6 点击、手势、滑动冲突与 Compose pointer input

前面我们理解了 ViewGroup 事件分发。

这一节把事件分发放回真实业务场景：点击、长按、拖拽、列表滑动、嵌套滑动和 Compose 手势。

## 本节剧情钩子

真实 App 里的触摸从来不只有“点按钮”。

用户会：

- 点一下。
- 长按。
- 左右滑。
- 上下滚。
- 两根手指缩放。
- 在列表里横滑卡片。
- 在外层页面和内层列表之间来回滑。

这时，事件分发就变成一场手势协商。

## 本节定位

本节把传统 View 事件和 Compose 手势放在同一张地图上理解。

## 学习目标

学完本节后，你应该能够：

- 理解点击和滑动的判断差异。
- 知道滑动冲突为什么常发生在嵌套容器里。
- 理解父容器拦截和子 View 请求不拦截的基本思路。
- 初步理解 Compose 的 `clickable`、`pointerInput` 与事件分发的关系。

## 第一部分：点击是一个判断结果

点击不是只有 `ACTION_UP`。

它通常需要满足：

- 有 DOWN。
- 手指没有移动太远。
- 没有被取消。
- 在合适时间内抬起。
- View 处于可点击状态。

所以“点了没反应”可能不是 click 回调错了，而是事件序列中途被拦截、取消或没有被消费。

## 第二部分：滑动冲突为什么常见

滑动冲突常见于：

- ViewPager 中嵌套 RecyclerView。
- ScrollView 中嵌套横向列表。
- 地图控件嵌套在可滚动页面里。
- BottomSheet 里放列表。
- Compose 中多个可滚动容器嵌套。

本质问题是：

```text
同一段 MOVE 事件，父容器和子 View 都想处理。
```

## 第三部分：外部拦截法

外部拦截法通常由父容器判断是否拦截。

思路：

```text
DOWN 不拦截
MOVE 根据方向和距离判断
如果符合父容器手势，就拦截
```

例如：

- 横向滑动交给 ViewPager。
- 纵向滑动交给 RecyclerView。

关键是：父容器要在合适时机做判断，不要一开始就吞掉所有事件。

## 第四部分：内部请求不拦截

子 View 也可以请求父容器不要拦截：

```kotlin
parent.requestDisallowInterceptTouchEvent(true)
```

这常用于：

- 子 View 想优先处理当前手势。
- 内层滚动控件需要完整 MOVE 序列。
- 自定义手势控件需要避免父容器抢事件。

但它不是万能钥匙。父容器仍然需要合理设计，子 View 也不能滥用。

## 第五部分：Compose 里的手势

Compose 提供了更声明式的手势 API：

- `clickable`
- `combinedClickable`
- `draggable`
- `scrollable`
- `pointerInput`

Compose 的写法不同，但底层仍然运行在 Android 输入链路之上。

可以这样理解：

```text
传统 View 看 dispatch / intercept / touch，
Compose 看 Modifier 和 pointer input，
但它们最终都离不开系统输入事件和窗口目标。
```

## 第六部分：Compose 与 AndroidView 混用

当 Compose 页面里嵌入 AndroidView，或者传统 View 页面里嵌入 ComposeView 时，触摸边界更值得注意。

要关注：

- 谁先收到事件。
- 哪一层消费了手势。
- 嵌套滚动是否协作。
- 是否出现点击穿透或滑动冲突。

这类问题不能只看 Compose，也不能只看 View，要看整体层级。

## 本节小挑战

### 手势协商题

请判断下面场景更可能用哪种思路：

- 外层横向页面，内层纵向列表。
- 地图控件放在可滚动详情页中。
- Compose 中一个 Card 既要点击又要横向拖动。
- BottomSheet 中嵌套 RecyclerView。

## 本节实践任务

### 基础任务

- 给一个 Compose `Button` 和 `Box.clickable` 打日志。
- 对比点击事件触发时机。
- 思考它们和传统 View 点击有什么相似之处。

### 进阶任务

- 设计一个横向滑动区域和纵向列表嵌套的页面。
- 打印手势方向。
- 尝试解释谁应该优先处理 MOVE。

## 本节小结

点击、滑动、长按、拖拽和嵌套滚动，本质都是输入事件序列的不同解释。传统 View 用分发、拦截和消费来协商；Compose 用 Modifier 和 pointer input 描述手势；但它们都运行在同一条系统输入链路之上。
