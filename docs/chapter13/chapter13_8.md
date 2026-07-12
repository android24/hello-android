# 13.8 综合实践：窗口显示链路观察实验

第 13 章最后一节，我们把 Window、DecorView、ViewRootImpl、WMS、特殊窗口和绘制刷新放进一个观察实验。

目标是：让你能从一次页面显示，解释 UI 如何进入窗口、如何被系统管理、如何完成首帧。

## 本节剧情钩子

现在你已经从 Activity 启动窗口走到了显示控制台。

你不再只看“页面有没有打开”，而是继续追问：

```text
内容挂到哪里？
窗口是谁管理？
Dialog 为什么能盖住页面？
输入法为什么会改变可见区域？
首帧为什么有时会慢？
```

本节要做的，就是把这些问题串成一张窗口显示地图。

## 本节定位

本节是第 13 章综合实践。

本节会使用配套工程：

```text
examples/13-window-display-lab/
```

它围绕窗口显示、特殊窗口和绘制刷新做成一个可观察实验室。

## 学习目标

学完本节后，你应该能够：

- 观察 Activity 的 Window 和 DecorView。
- 区分业务 UI、DecorView 和特殊窗口。
- 解释 Dialog、PopupWindow、输入法的窗口特征。
- 用日志描述一次页面首帧显示链路。
- 写一份窗口问题排查报告。

## 第一部分：实践工程入口

第 13 章 demo 已经把窗口显示链路拆成这些可观察区域：

- `窗口观察分数`：提示你已经完成哪些实验。
- `窗口身份证`：展示 Activity、Window、DecorView、内容区尺寸和软键盘策略。
- `预期 vs 实际`：每次操作后对照你的判断和观察证据。
- `特殊窗口操作台`：触发 Dialog、PopupWindow、Toast 和输入法。
- `输入法与 Insets 观察`：观察软键盘如何影响内容区域。
- `一帧刷新实验`：对比颜色重绘、文本重布局、Choreographer 帧记录和主线程忙碌。
- `窗口问题诊断卡`：把 BadToken、输入法遮挡、启动白屏、遮挡混淆变成排查清单。
- `窗口事件轨迹`：记录每次窗口实验和刷新事件。

它的目标不是模拟完整 WMS，而是把窗口现象变成可点击、可观察、可复盘的实验。

## 第二部分：DecorView 观察任务

进入页面后，先记录：

```text
Activity 名称
Window 实现类
DecorView 类型
内容根节点类型
当前屏幕尺寸
当前 Insets 信息
```

你要回答：

- 业务 UI 是否直接等于 DecorView？
- Compose 页面是否仍然挂在窗口内容区？
- 状态栏和导航栏是否影响内容布局？

## 第三部分：特殊窗口实验

依次触发：

- Dialog。
- PopupWindow。
- Toast。
- 输入法。

观察：

- 它们是否盖在 Activity 内容上。
- 是否会抢焦点。
- 是否依赖宿主 Activity。
- 页面退出时是否需要手动释放。
- 输入法出现后内容区域是否变化。

这组实验能帮助你把“弹层问题”从 UI 直觉升级为窗口视角。

## 第四部分：绘制刷新实验

设计两个按钮：

```text
改变颜色
改变文本长度
```

观察它们更像触发：

- 只重绘。
- 重新测量布局。
- 重新绘制整棵树的一部分。

再加一个“主线程忙碌”按钮，模拟首帧或点击后卡顿，让读者看到主线程耗时和刷新节拍的关系。

## 第五部分：窗口问题诊断卡

demo 已经把常见问题做成诊断卡：

```text
问题：Dialog 偶发崩溃
可能原因：宿主 Activity 已销毁，Token 无效
观察证据：BadTokenException / lifecycle 日志
修复方向：显示前检查生命周期，销毁时 dismiss
```

再比如：

```text
问题：输入法遮挡输入框
可能原因：Insets 未处理或窗口 resize 策略不合适
观察证据：键盘出现后可见区域变化
修复方向：处理 WindowInsets，调整滚动容器
```

这种卡片能让读者把 Framework 概念和真实故障联系起来。

## 第六部分：窗口侦探通关路线

为了让实验更像一次排查任务，你可以把自己当成“窗口侦探”，按下面路线通关。

### 初级侦探：找到窗口身份证

先完成：

```text
刷新窗口信息
  -> 记录 Window 类型
      -> 记录 DecorView 类型
          -> 对比 DecorView 尺寸和内容区尺寸
```

你要得到的结论是：

```text
Activity 不是屏幕本身，业务 UI 是挂进 Window / DecorView 内容区的。
```

### 中级侦探：区分 View 遮挡和 Window 遮挡

继续完成：

```text
显示 Dialog
  -> 显示 PopupWindow
      -> 显示 Toast
          -> 判断它们和页面内部 View 层级的区别
```

你要得到的结论是：

```text
Dialog、Popup、Toast 不是简单的 Compose 层级问题，它们背后有窗口类型、宿主和层级规则。
```

### 高级侦探：解释输入法、BadToken 和一帧刷新

最后完成：

```text
聚焦输入框
  -> 观察输入法改变内容区域
      -> 阅读 BadToken 诊断卡
          -> 改变颜色和文本长度
              -> 记录 Choreographer frame
                  -> 模拟主线程忙碌
```

你要得到的结论是：

```text
窗口问题不只来自布局，还可能来自 Token、Insets、生命周期、主线程和刷新节拍。
```

完成三段侦探路线后，再写窗口显示报告，会比直接抄概念更扎实。

## 第七部分：窗口显示报告

建议报告格式：

```text
操作：
观察到的窗口：
DecorView / 内容根节点：
特殊窗口行为：
绘制或刷新现象：
可能的 Framework 入口：
我的结论：
仍不确定：
```

推荐 AOSP 入口：

```text
frameworks/base/core/java/android/app/Activity.java
frameworks/base/core/java/com/android/internal/policy/PhoneWindow.java
frameworks/base/core/java/android/view/Window.java
frameworks/base/core/java/android/view/WindowManager.java
frameworks/base/core/java/android/view/ViewRootImpl.java
frameworks/base/core/java/android/view/Choreographer.java
frameworks/base/services/core/java/com/android/server/wm/WindowManagerService.java
frameworks/base/services/core/java/com/android/server/wm/WindowState.java
```

## 第八部分：本章通关检查

完成第 13 章后，请确认自己能回答：

- Activity 和 Window 是什么关系？
- DecorView 为什么是窗口根视图？
- ViewRootImpl 为什么重要？
- WindowManager 和 WMS 分别在哪里？
- Token 为什么会影响 Dialog 显示？
- View 层级和 Window 层级有什么区别？
- 输入法为什么不是普通布局的一部分？
- Choreographer 和一帧刷新有什么关系？
- 白屏、遮挡、窗口泄漏应该如何分类排查？

## 本节小挑战

### 窗口侦探终局题

请为下面路径写一份窗口显示报告：

```text
打开 MainActivity
  -> 显示 Dialog
      -> 点击输入框弹出输入法
          -> 关闭 Dialog
              -> 返回上一页
```

要求写出你认为出现过哪些窗口，以及它们之间的层级关系。

## 本节实践任务

### 基础任务

- 打印 Activity 的 Window 和 DecorView 信息。
- 弹出一个 Dialog 并观察遮挡关系。
- 弹出输入法并观察内容区域变化。
- 写一份 10 行以内的窗口显示报告。

### 进阶任务

- 设计一个可能触发 `BadTokenException` 的错误场景，但不要在正式代码里保留危险实现。
- 对照源码搜索 `PhoneWindow`、`ViewRootImpl`、`WindowManagerService`。
- 画出从 `setContent` 到首帧绘制的简化链路。

## 本节小结

第 13 章把“页面显示”从 UI 代码推进到窗口系统。你不需要一次读完 WMS，但应该已经能把 Activity、Window、DecorView、ViewRootImpl、WMS、特殊窗口和一帧刷新放在同一张地图上。下一步继续深入时，View 绘制、Input 分发、SurfaceFlinger 和渲染链路就会有更稳的起点。
