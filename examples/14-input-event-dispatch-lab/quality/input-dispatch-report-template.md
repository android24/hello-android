# 输入事件分发报告模板

## 追踪问题

示例：

```text
为什么父容器拦截 MOVE 后，子 View 收到了 CANCEL？
```

## 操作路径

```text
打开页面 -> 开启父容器 MOVE 拦截 -> 在 Child View 上滑动
```

## 预期行为

-

## 实际结果对照

```text
本次操作：
预期：
实际：
结论：
```

## 事件序列

```text
DOWN:
MOVE:
CANCEL:
UP:
```

## 分发链路

```text
Activity.dispatchTouchEvent:
Parent.dispatchTouchEvent:
Parent.onInterceptTouchEvent:
Parent.onTouchEvent:
Child.dispatchTouchEvent:
Child.onTouchEvent:
```

## 是否发生拦截

```text
拦截发生在：
拦截前事件归属：
拦截后事件归属：
子 View 是否收到 CANCEL：
```

## Compose 手势观察

```text
clickable:
pointerInput tap:
pointerInput drag:
和传统 View 分发的关系：
```

## 可能的源码入口

```text
InputManagerService
InputReader
InputDispatcher
ViewRootImpl
Activity
ViewGroup
View
```

## 我已经理解的部分

-

## 仍不确定的问题

-

## 10 行通关报告

```text
操作：
预期：
实际：
事件序列：
Activity：
Parent：
Child：
Compose：
源码入口：
结论：
```
