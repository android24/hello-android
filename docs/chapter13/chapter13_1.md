# 13.1 为什么要学习 WMS、Window 与窗口显示

第 12 章里，我们已经知道：一次 `startActivity()` 不是 App 自己创建页面，而是 ATMS 参与调度、任务栈参与决策、ActivityThread 接收生命周期分发。

现在 Activity 已经被创建了，新的问题来了：

```text
Activity 走到 onResume() 之后，界面为什么真的能出现在屏幕上？
```

这就是第 13 章要追的主线。

## 本章通关画面

完成第 13 章后，你应该能把一次页面显示讲成这样：

```text
Activity 创建
  -> PhoneWindow 准备窗口容器
      -> DecorView 成为页面根视图
          -> WindowManager.addView()
              -> ViewRootImpl 接管 View 树
                  -> Binder 请求 WindowManagerService
                      -> WMS 记录窗口、Token、层级与可见性
                          -> Choreographer 驱动 measure / layout / draw
                              -> 页面内容进入显示链路
```

本章不要求你一次读完整个 WMS 源码，但要先建立一条判断线：

```text
Activity 负责生命周期，Window 承载界面，WMS 负责系统级窗口管理。
```

## 本章剧情线

如果第 12 章是“页面启动申请表”，第 13 章就是页面拿到了入场许可之后，来到舞台管理处。

Activity 像演员，Window 像舞台框架，DecorView 像舞台上的主布景，ViewRootImpl 像现场调度员，WMS 则像剧场总控室。

你看到的是一页 UI，系统看到的是一个窗口：它属于哪个应用、能不能显示、显示在哪一层、是否有焦点、是否会挡住别的窗口、输入法该不该出现、旋转和尺寸变化时该如何调整。

## 本章探索任务

建议按下面路线推进：

```text
认识 WMS 与窗口显示
  -> 从 setContentView / Compose 找到 DecorView
      -> 理解 Window、PhoneWindow 与 ViewRootImpl
          -> 认识 WindowManager.addView 与 WMS
              -> 理解 Token、窗口层级与可见性
                  -> 串起 measure / layout / draw 与 Choreographer
                      -> 分析 Dialog、PopupWindow、输入法等特殊窗口
                          -> 完成窗口显示链路观察实验
```

## 本节定位

本节是第 13 章入口。

我们先回答：

- 为什么 Activity 启动之后还要学习窗口显示？
- WMS 到底管理什么？
- Window、DecorView、ViewRootImpl 分别站在哪里？
- 为什么很多 UI 问题不是单纯改布局就能解决？

## 学习目标

学完本节后，你应该能够：

- 理解 WMS 是 Android 显示链路的重要系统服务。
- 知道 Activity 和 Window 不是同一件事。
- 初步区分 Window、DecorView、ViewRootImpl、WMS 的角色。
- 知道第 13 章要观察哪些窗口现象。

## 第一部分：Activity 不是屏幕本身

初学时我们经常说“打开一个 Activity，就是显示一个页面”。

这句话能帮助入门，但不够精确。

Activity 更像一个生命周期与业务承载单元。它会接收 `onCreate()`、`onStart()`、`onResume()`，也会管理页面逻辑。但真正承载 UI 并进入系统窗口管理的，是 Window 相关链路。

可以先这样理解：

```text
Activity：我什么时候创建、暂停、销毁。
Window：我的内容放进哪个窗口。
DecorView：这个窗口里的根 View。
ViewRootImpl：这棵 View 树如何接入系统显示与事件调度。
WMS：系统如何管理所有窗口。
```

## 第二部分：为什么需要 WMS

如果每个 App 都能自己随便把内容画到屏幕上，系统会立刻失控。

比如：

- 谁在最上层？
- 谁能拿到焦点？
- 输入法应该贴着哪个输入框？
- Dialog 为什么能盖在 Activity 上？
- 悬浮窗为什么需要权限？
- 横竖屏切换时窗口尺寸如何变化？
- App 退到后台后窗口是否还能显示？

这些问题都不能只靠某个 Activity 自己决定。

WMS 的价值就在于：它站在系统视角管理窗口。

## 第三部分：窗口是 UI 的系统身份证

在应用代码里，我们写的是 Compose 或 XML。

但在系统服务眼里，最终要管理的是一个个 Window。

一个窗口通常需要包含：

- 它属于哪个应用。
- 它对应哪个 Activity Token。
- 它是什么类型。
- 它是否可见。
- 它处在哪个层级。
- 它是否可以接收输入。
- 它的尺寸、位置和显示区域是什么。

窗口不是简单的 View。View 是应用内的 UI 树，Window 是系统管理显示区域的单位。

## 第四部分：第 13 章要解决的问题

第 13 章重点解决这些问题：

- `setContentView()` 或 Compose 内容最后放到哪里？
- DecorView 为什么是页面根视图？
- ViewRootImpl 为什么经常出现在绘制、输入、异常堆栈里？
- `WindowManager.addView()` 为什么可能抛 `BadTokenException`？
- Dialog、PopupWindow、Toast、输入法为什么不是普通 View？
- 黑屏、白屏、窗口泄漏、遮挡异常该如何定位？

这些问题连接应用层开发和 Framework 显示链路。

## 本节小挑战

### 角色归位题

请把下面角色放到合适位置：

```text
Activity
Window
DecorView
ViewRootImpl
WindowManagerService
```

回答下面问题：

- 谁接收生命周期？
- 谁是窗口根视图？
- 谁把 View 树接入系统？
- 谁在 system_server 中管理窗口？

## 本节实践任务

### 基础任务

- 回顾第 12 章 demo 中 MainActivity 的生命周期日志。
- 写下你理解的“Activity 已经 onResume，但页面还需要显示链路”是什么意思。
- 查找项目里 `setContent` 或 `setContentView` 的入口。

### 进阶任务

- 在任意示例工程中打开 Layout Inspector。
- 找到页面根节点。
- 思考它和 DecorView / ViewRootImpl 的关系。

## 本节小结

第 13 章的核心不是背 WMS 的类名，而是理解：页面启动只是第一步，页面显示还需要 Window、DecorView、ViewRootImpl 和 WMS 协作。Activity 解决“谁在运行”，Window 解决“谁在显示”，WMS 解决“系统如何管理所有显示出来的东西”。
