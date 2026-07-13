# 14.4 Activity、Window、DecorView 的事件入口

上一节我们看清了 MotionEvent。

这一节开始看事件进入 App 后的第一段路：它如何从 ViewRootImpl 走到 Activity、Window 和 DecorView。

## 本节剧情钩子

你以为事件一进 App 就冲向按钮。

其实它还要先经过“前台接待”：

```text
ViewRootImpl 签收事件
  -> DecorView 作为窗口根节点接住事件
      -> Activity 有机会先看一眼
          -> 再交给具体 View 树
```

如果说第 13 章的 DecorView 是窗口布景根节点，那么第 14 章的 DecorView 也是输入事件进入 View 树的重要门口。

## 本节定位

本节解释 Activity、Window、DecorView 在输入分发中的位置。

## 学习目标

学完本节后，你应该能够：

- 知道 `Activity.dispatchTouchEvent()` 是应用层常见入口。
- 理解 Window / DecorView 会参与事件传递。
- 明白事件进入 View 树前，Activity 可以做统一观察或处理。
- 知道为什么全局点击统计常放在 Activity 层。

## 第一部分：ViewRootImpl 接收后继续分发

ViewRootImpl 从系统输入链路接收事件后，会把事件交给 View 树。

简化理解：

```text
ViewRootImpl
  -> DecorView
      -> Activity / Window callback
          -> ViewGroup / View
```

不同版本细节可能变化，但主线是：事件要从窗口根节点进入应用 UI 层级。

## 第二部分：Activity.dispatchTouchEvent

Activity 中可以重写：

```kotlin
override fun dispatchTouchEvent(event: MotionEvent): Boolean {
    return super.dispatchTouchEvent(event)
}
```

它常用于：

- 打印全局触摸日志。
- 统计用户点击。
- 点击空白处隐藏键盘。
- 在不破坏子 View 的前提下观察事件。

但要谨慎：如果在这里直接返回 `true`，可能会吞掉所有子 View 的事件。

## 第三部分：Window 和 Callback

Activity 和 Window 之间存在回调关系。

入门阶段可以先这样理解：

```text
Activity 是窗口回调的重要承载者，
Window / DecorView 会把部分事件交回 Activity 处理。
```

这也是为什么 Activity 能在较高层观察输入事件。

## 第四部分：DecorView 是 View 树入口

DecorView 是窗口里的根 View。

输入事件进入应用 UI 时，需要从根节点开始往下找目标。

例如：

```text
DecorView
  -> content parent
      -> root layout
          -> nested layout
              -> button
```

事件不是凭空跳到 Button，而是从根往下分发。

## 第五部分：全局处理要有边界

Activity 层处理事件很方便，但也很危险。

适合做：

- 日志观察。
- 埋点统计。
- 点击空白处收起键盘。
- 特定页面的全局手势。

不适合随便做：

- 无条件消费所有事件。
- 在 Activity 层硬编码子 View 的复杂手势。
- 破坏 ViewGroup 和子 View 的分发规则。

全局入口应该像安检，不应该像黑洞。

## 本节小挑战

### 高层入口题

如果你想统计一个页面所有触摸事件，下面哪个位置更适合先观察？

- Button 的 click 回调。
- Activity.dispatchTouchEvent。
- Repository。
- Room Dao。

为什么？

## 本节实践任务

### 基础任务

- 在 Activity 中重写 `dispatchTouchEvent()`。
- 打印事件 action。
- 保持 `return super.dispatchTouchEvent(event)`。

### 进阶任务

- 尝试在 Activity 中返回 `true`。
- 观察子 View 点击是否还能触发。
- 恢复正确写法，并写下原因。

## 本节小结

事件进入 App 后，不会直接跳到具体按钮，而是通过 ViewRootImpl、DecorView、Window / Activity 回调进入 View 树。Activity.dispatchTouchEvent 是很好的观察入口，但不能随意吞掉事件。理解这段入口链路，后面看 ViewGroup 分发会更稳。
