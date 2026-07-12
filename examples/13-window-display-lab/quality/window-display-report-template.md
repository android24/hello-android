# Window 显示链路报告模板

## 追踪问题

示例：

```text
为什么 Dialog 能盖在 Activity 内容之上？
```

## 操作路径

```text
打开 MainActivity -> 显示 Dialog -> 关闭 Dialog
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

## 观察到的窗口

```text
Activity 主窗口：
Dialog / Popup / Toast / 输入法：
是否改变焦点：
是否改变可见区域：
```

## DecorView / 内容根节点

```text
Window：
DecorView：
内容根节点：
DecorView 尺寸：
内容区尺寸：
```

## 窗口层级推断

示例：

```text
上层：Dialog
中层：Activity 主窗口
下层：系统背景 / Launcher
```

## 绘制或刷新现象

```text
是否改变尺寸：
是否重新布局：
是否只是重绘：
是否出现主线程忙碌：
是否记录到 Choreographer frame：
```

## 可能的源码入口

```text
Activity
PhoneWindow
Window
WindowManager
ViewRootImpl
Choreographer
WindowManagerService
WindowState
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
窗口：
DecorView：
层级：
刷新：
源码入口：
结论：
仍不确定：
```
