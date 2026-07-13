# Input / ViewGroup 源码阅读笔记

## 阅读目标

第 14 章不是要求一次读完 InputFlinger，而是先带着应用侧现象找入口：

```text
触摸事件如何进入 App？
ViewRootImpl 如何把事件交给 View 树？
Activity、ViewGroup、View 如何分发、拦截和消费？
滑动冲突和 CANCEL 如何发生？
```

## 推荐阅读顺序

```text
InputReader
  -> InputDispatcher
      -> ViewRootImpl
          -> Activity.dispatchTouchEvent
              -> ViewGroup.dispatchTouchEvent
                  -> ViewGroup.onInterceptTouchEvent
                      -> View.dispatchTouchEvent
                          -> View.onTouchEvent
```

## 入口文件

```text
frameworks/base/services/core/java/com/android/server/input/InputManagerService.java
frameworks/native/services/inputflinger/reader/InputReader.cpp
frameworks/native/services/inputflinger/dispatcher/InputDispatcher.cpp
frameworks/base/core/java/android/view/ViewRootImpl.java
frameworks/base/core/java/android/view/View.java
frameworks/base/core/java/android/view/ViewGroup.java
frameworks/base/core/java/android/app/Activity.java
```

## 阅读问题

- InputDispatcher 如何选择目标窗口？
- ViewRootImpl 从哪里接收输入事件？
- Activity.dispatchTouchEvent 在应用层适合做什么？
- ViewGroup 为什么有 onInterceptTouchEvent？
- DOWN 没有被消费时，后续事件会怎样？
- 父容器中途拦截时，子 View 为什么可能收到 CANCEL？
- Compose pointerInput 如何和 Android 输入系统衔接？

## 观察记录

```text
现象：
应用侧日志：
可能源码入口：
我理解的链路：
仍不确定：
```
