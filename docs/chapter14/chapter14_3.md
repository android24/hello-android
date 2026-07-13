# 14.3 MotionEvent、坐标体系与事件序列

事件进入 App 之后，应用层最常见的载体就是 `MotionEvent`。

这一节我们先不急着看 `dispatchTouchEvent()`，而是先看清楚事件本身。

## 本节剧情钩子

一次点击不是一个孤立事件。

它更像一段小故事：

```text
手指按下
  -> 可能移动
      -> 手指抬起
```

如果中途被父容器、系统或其他窗口打断，还可能出现 `CANCEL`。

`MotionEvent` 就是这段故事的记录单。

## 本节定位

本节解释 MotionEvent 的事件类型、坐标和事件序列。

## 学习目标

学完本节后，你应该能够：

- 理解 `ACTION_DOWN / MOVE / UP / CANCEL` 的基本含义。
- 知道一次手势通常由一组事件组成。
- 区分 raw 坐标和 View 局部坐标。
- 初步理解多指触控和 pointer 概念。

## 第一部分：常见 Action

最常见的触摸事件包括：

```text
ACTION_DOWN
ACTION_MOVE
ACTION_UP
ACTION_CANCEL
```

可以这样理解：

- `DOWN`：手指按下，一段触摸序列开始。
- `MOVE`：手指移动，可能出现很多次。
- `UP`：手指抬起，一段触摸序列正常结束。
- `CANCEL`：事件序列被取消，后续不要再按正常点击处理。

点击通常是：

```text
DOWN -> UP
```

滑动通常是：

```text
DOWN -> MOVE -> MOVE -> ... -> UP
```

## 第二部分：DOWN 很关键

`ACTION_DOWN` 是一段事件序列的起点。

很多分发逻辑会围绕 DOWN 做决定：

- 哪个 View 命中触摸区域。
- 父容器是否准备拦截。
- 子 View 是否有机会接收后续事件。
- 点击、长按、滑动的初始状态。

如果一个 View 没有处理 DOWN，后续 MOVE / UP 也未必会继续给它。

所以排查触摸问题时，不要只看 UP 或 click 回调，要先看 DOWN 去了哪里。

## 第三部分：CANCEL 不是失败日志

`ACTION_CANCEL` 经常让初学者困惑。

它不是简单的错误，而是系统告诉 View：

```text
这段触摸序列不要再按正常点击完成了。
```

可能原因：

- 父容器中途拦截。
- 其他窗口抢占。
- 手势被系统接管。
- View 被移除或状态变化。

收到 CANCEL 后，View 通常要清理按压态、停止追踪手势。

## 第四部分：坐标体系

MotionEvent 中常见坐标包括：

- `x / y`：相对于当前 View 的坐标。
- `rawX / rawY`：相对于屏幕的坐标。

理解坐标很重要。

例如：

- 判断手指是否在某个 View 内部，通常看 View 局部坐标。
- 判断跨窗口、全屏拖拽或系统层位置，可能需要 raw 坐标。
- 滑动冲突里，常要比较横向和纵向移动距离。

## 第五部分：多指触控与 pointer

多指触控里，一个 MotionEvent 可能包含多个 pointer。

常见概念：

- pointer count：当前手指数量。
- pointer id：某根手指的稳定标识。
- pointer index：当前事件数组里的位置。

入门阶段不用一开始深挖多指，但要知道：事件不是永远只有一个 x/y。

## 本节小挑战

### 事件序列判断题

请判断下面序列更像什么操作：

```text
DOWN -> UP
DOWN -> MOVE -> MOVE -> UP
DOWN -> MOVE -> CANCEL
```

## 本节实践任务

### 基础任务

- 打印 `MotionEvent.actionMasked`。
- 打印 `x / y` 和 `rawX / rawY`。
- 点击和滑动各做一次，对比日志。

### 进阶任务

- 尝试两根手指触摸屏幕。
- 打印 pointer count。
- 思考单指点击和多指手势为什么不能用同一套简单判断。

## 本节小结

MotionEvent 是输入事件在应用层的重要载体。一次交互通常不是一个事件，而是一段由 DOWN、MOVE、UP 或 CANCEL 组成的序列。理解事件序列和坐标体系，是看懂事件分发、滑动冲突和手势识别的前提。
