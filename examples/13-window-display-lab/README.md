# 示例工程：Window 显示链路实验室

## 对应章节

第13章 WMS、Window、DecorView 与窗口显示机制

## 工程目标

本工程用于配合第 13 章，把 Activity 内容显示、Window、DecorView、ViewRootImpl、WMS、特殊窗口和一帧刷新放进一个可以运行、可以观察、可以复盘的小实验室。

它会围绕四个问题展开：

- Activity 已经启动后，内容挂到了哪个 Window / DecorView？
- Dialog、Popup、Toast 和输入法为什么不是普通页面 View？
- View 层级和 Window 层级应该如何区分？
- 一次颜色变化、文本变化和主线程忙碌，会如何影响刷新体验？

## 当前效果

运行后你会看到一个“第 13 章 Window 显示链路实验室”页面：

- `窗口观察分数` 用 100 分制提示当前实验进度。
- `窗口身份证` 展示 Activity、Window、DecorView、内容区尺寸和软键盘策略。
- `预期 vs 实际` 会在每次实验后显示你的判断和观察结果。
- `特殊窗口操作台` 提供 Dialog、Popup、Toast、输入法实验。
- `输入法与 Insets 观察` 用输入框观察软键盘对内容区域的影响。
- `一帧刷新实验` 提供颜色重绘、文本重布局、Choreographer 帧记录和主线程忙碌模拟。
- `窗口问题诊断卡` 把 BadToken、输入法遮挡、启动白屏、遮挡混淆变成排查清单。
- `窗口事件轨迹` 记录每次窗口实验与刷新事件。

这个 demo 不模拟 WMS 源码，也不能直接读取 system_server 内部窗口树；它从 App 侧观察 Window / DecorView / 弹层 / 输入法 / 刷新事件，帮助你反推系统窗口管理发生了什么。

## 探索玩法

建议把自己当成窗口侦探，按三段路线完成。

### 初级侦探：找到窗口身份证

先完成：

```text
刷新窗口信息
  -> 记录 Window 类型
      -> 记录 DecorView 类型
          -> 对比 DecorView 尺寸和内容区尺寸
```

通关判断：

```text
你能说清楚 Activity、Window、DecorView 和业务 UI 不是同一个东西。
```

### 中级侦探：区分 View 遮挡和 Window 遮挡

继续完成：

```text
显示 Dialog
  -> 显示 Popup
      -> 显示 Toast
          -> 判断它们和页面内部 View 层级的区别
```

通关判断：

```text
你能解释为什么 Dialog 盖住 Activity 不是普通 zIndex 问题。
```

### 高级侦探：追踪输入法、BadToken 和一帧刷新

最后完成：

```text
聚焦输入框并观察输入法
  -> 阅读窗口问题诊断卡
      -> 改变颜色
          -> 改变文本长度
              -> 记录下一帧 Choreographer
                  -> 模拟主线程忙碌
                      -> 写一份窗口显示报告
```

通关判断：

```text
你能把输入法遮挡、BadToken、白屏和刷新卡顿放回 Window / Insets / Token / Choreographer 这张地图里。
```

最小报告可以写成：

```text
操作：显示 Dialog
预期：Dialog 作为附属窗口盖在 Activity 主窗口之上。
实际：页面出现 AlertDialog，Activity 内容被遮挡，交互焦点转移到弹窗。
窗口推断：这是 Window 层级，不是同一个 Compose 层级里的 zIndex。
源码入口：PhoneWindow, WindowManager, ViewRootImpl, WindowManagerService
仍不确定：Dialog 的 Token 在系统侧如何和 ActivityRecord 关联？
```

## 运行方式

1. 使用 Android Studio 打开 `examples/13-window-display-lab`。
2. 等待 Gradle Sync 完成。
3. 运行 `app` 模块。
4. 打开 Logcat，搜索 `WindowDisplayLab`。
5. 依次点击实验按钮，观察页面、输入法、事件轨迹和日志变化。

如果工程里配置了 Gradle Wrapper，也可以参考：

```text
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
```

## 工程结构

```text
13-window-display-lab/
  app/
    src/main/java/com/helloandroid/window/
      WindowLabApplication.kt
      MainActivity.kt
      WindowLabState.kt
      WindowLabStore.kt
      WindowLabScreen.kt
  quality/
    window-display-report-template.md
    window-reading-notes.md
```

## 关键源码入口

- `MainActivity.kt`：读取 Activity 的 Window / DecorView 信息，接入 Toast、Choreographer 和主线程忙碌实验。
- `WindowLabState.kt`：定义窗口信息、实验状态、诊断卡和事件轨迹。
- `WindowLabStore.kt`：记录窗口实验、预期/实际对照和 Logcat 日志。
- `WindowLabScreen.kt`：展示窗口观察页面、特殊窗口操作台、输入法实验和一帧刷新实验。
- `quality/window-display-report-template.md`：窗口显示链路报告模板。
- `quality/window-reading-notes.md`：WMS / Window / ViewRootImpl 源码阅读建议。

## 推荐对照的 AOSP 入口

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

建议带着问题看：

```text
Activity 的内容什么时候进入 Window？
PhoneWindow 如何准备 DecorView？
WindowManager.addView 如何走向 WMS？
ViewRootImpl 为什么负责 traversal？
Dialog 为什么需要合法 Token？
Choreographer 如何驱动一帧刷新？
```

## 练习任务

### 基础任务

- 刷新窗口信息，记录 Window 和 DecorView 类型。
- 显示 Dialog，判断它是 View 层级还是 Window 层级。
- 显示 Popup 和 Toast，比较它们和 Dialog 的差异。
- 聚焦输入框，观察输入法出现后内容区域是否变化。
- 改变颜色和文本长度，观察重绘与重新布局的差异。
- 使用 `quality/window-display-report-template.md` 写一份短报告。

### 进阶任务

- 增加一个自定义 Dialog，并在 Activity 销毁前正确 dismiss。
- 给输入法实验补充 `imePadding` 开关，对比遮挡差异。
- 给刷新实验增加耗时计算，记录点击到下一帧的时间。
- 对照 AOSP 搜索 `PhoneWindow`、`ViewRootImpl`、`WindowManagerService`。

## 通关目标

完成本工程后，你应该能说清楚：

- Activity 和 Window 不是同一件事。
- DecorView 是 Activity 窗口里的根视图。
- Dialog、Popup、Toast、输入法属于不同窗口现象。
- View 层级和 Window 层级的区别。
- BadToken 与宿主生命周期、窗口 Token 的关系。
- Choreographer 和一帧刷新有什么关系。
- 白屏、遮挡、输入法异常、窗口泄漏应该如何分类排查。
