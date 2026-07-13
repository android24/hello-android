# 14.5 ViewGroup 事件分发：dispatchTouchEvent、onInterceptTouchEvent 与 onTouchEvent

事件进入 View 树后，最重要的分发规则开始登场。

这一节是第 14 章的核心：ViewGroup 如何决定事件给自己，还是给子 View。

## 本节剧情钩子

把 ViewGroup 想成一个带孩子出门的家长。

手里来了一张任务卡：

```text
这次触摸谁来处理？
```

家长可以先看看情况：如果只是点子 View，那交给孩子；如果发现这是一个纵向滑动列表，那家长可能说“这事我来”。

这就是分发和拦截。

## 本节定位

本节讲清楚 ViewGroup 事件分发的三个关键方法。

## 学习目标

学完本节后，你应该能够：

- 理解 `dispatchTouchEvent()`、`onInterceptTouchEvent()`、`onTouchEvent()` 的职责。
- 知道 ViewGroup 为什么有拦截能力。
- 理解事件消费对后续事件的影响。
- 能用日志分析一条事件分发路径。

## 第一部分：dispatchTouchEvent 是分发入口

View 和 ViewGroup 都有 `dispatchTouchEvent()`。

它负责：

```text
我收到事件后，要不要继续分发？
```

对于 ViewGroup 来说，它通常要判断：

- 是否自己处理。
- 是否拦截。
- 是否交给某个子 View。
- 子 View 是否消费。

## 第二部分：onInterceptTouchEvent 只属于 ViewGroup

ViewGroup 有一个特殊方法：

```kotlin
override fun onInterceptTouchEvent(event: MotionEvent): Boolean
```

它负责：

```text
我要不要拦截事件，不让子 View 继续处理？
```

典型场景：

- ScrollView 判断纵向滑动。
- RecyclerView 判断列表滚动。
- ViewPager 判断横向滑动。
- 父容器判断是否接管手势。

如果父容器决定拦截，子 View 可能收到 `CANCEL`。

## 第三部分：onTouchEvent 是自己处理

`onTouchEvent()` 负责：

```text
如果事件最终给到我，我要不要消费它？
```

对于普通 View，例如 Button，它会根据 DOWN / UP 判断点击。

对于自定义 View，你可以在这里处理：

- 按下态。
- 拖拽。
- 缩放。
- 自定义手势。

## 第四部分：消费很重要

事件方法通常返回 Boolean。

可以先这样理解：

- `true`：我消费了。
- `false`：我不处理，交回上层或走其他路径。

如果一个 View 在 DOWN 阶段没有消费事件，它后续未必还能收到 MOVE / UP。

所以排查点击不触发时，要问：

```text
DOWN 有没有被正确消费？
```

## 第五部分：简化分发模型

入门阶段可以先记这条线：

```text
ViewGroup.dispatchTouchEvent
  -> ViewGroup.onInterceptTouchEvent
      -> child.dispatchTouchEvent
          -> child.onTouchEvent
```

如果子 View 不处理，事件可能回到父容器的 `onTouchEvent()`。

真实源码更复杂，但这条线足够开始分析大多数问题。

## 第六部分：日志是最好的地图

分析事件分发时，不要只靠想象。

建议在这些位置打日志：

```text
Activity.dispatchTouchEvent
ParentViewGroup.dispatchTouchEvent
ParentViewGroup.onInterceptTouchEvent
ChildView.dispatchTouchEvent
ChildView.onTouchEvent
```

然后用一次点击和一次滑动对比日志。

## 本节小挑战

### 三方法归位题

请解释：

- 谁负责分发？
- 谁负责拦截？
- 谁负责最终处理？

再回答：为什么普通 View 没有 `onInterceptTouchEvent()`？

## 本节实践任务

### 基础任务

- 自定义一个简单 ViewGroup。
- 在三个方法里打印日志。
- 点击子 View，观察日志顺序。

### 进阶任务

- 在 MOVE 超过一定距离后让父容器拦截。
- 观察子 View 是否收到 CANCEL。
- 写下这和滑动冲突的关系。

## 本节小结

ViewGroup 事件分发的核心是：先分发，再判断是否拦截，最后决定谁消费。`dispatchTouchEvent()` 负责分发，`onInterceptTouchEvent()` 负责拦截，`onTouchEvent()` 负责自己处理。理解这三个方法，滑动冲突和点击无响应就有了基本排查工具。
