# WMS / Window / ViewRootImpl 源码阅读笔记

## 阅读目标

第 13 章不是要求一次读完 WMS，而是先带着应用侧现象找入口：

```text
Activity 内容如何进入 Window？
DecorView 如何接入 ViewRootImpl？
WindowManager 如何和 WMS 协作？
一帧刷新如何被 Choreographer 调度？
```

## 推荐阅读顺序

```text
Activity.setContentView / setContent
  -> PhoneWindow.setContentView
      -> DecorView
          -> WindowManager.addView
              -> ViewRootImpl.setView
                  -> WindowManagerService.addWindow
                      -> Choreographer / performTraversals
```

## 入口文件

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

## 阅读问题

- Activity 什么时候创建或持有 Window？
- PhoneWindow 如何准备 DecorView？
- DecorView 和业务 content 区域是什么关系？
- WindowManager.addView 为什么会进入 ViewRootImpl？
- ViewRootImpl.performTraversals 做了什么？
- WMS 为什么需要 Token？
- Dialog、输入法、Toast 这类窗口在规则上有什么不同？

## 观察记录

```text
现象：
应用侧日志：
可能源码入口：
我理解的链路：
仍不确定：
```
