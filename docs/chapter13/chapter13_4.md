# 13.4 WindowManager、WMS、Token 与窗口层级

有了 DecorView 和 ViewRootImpl，页面还没有彻底进入系统窗口秩序。

接下来它需要通过 WindowManager 把窗口交给系统服务管理。

这一节我们进入 WMS 的核心视角：窗口不是孤立显示的，它要有身份、归属、层级和可见性。

## 本节剧情钩子

把手机屏幕想成一栋大楼。

Activity 窗口、Dialog、输入法、Toast、系统状态栏、导航栏、悬浮窗，都想占一个位置。

如果没有楼层规则，谁都可以站到最前面，结果一定混乱。

WMS 就是这栋楼的物业总控：它要知道谁有门禁、谁在哪一层、谁能被看见、谁能拿到焦点。

## 本节定位

本节建立 WindowManager 到 WMS 的主线，并解释 Token、窗口类型和层级概念。

## 学习目标

学完本节后，你应该能够：

- 知道 `WindowManager.addView()` 会把窗口请求交给系统。
- 理解 WMS 管理的是系统级窗口集合。
- 初步理解 Token 用来描述窗口归属与合法性。
- 知道窗口类型和层级会影响遮挡关系。

## 第一部分：WindowManager 是应用侧入口

应用侧常见入口是：

```kotlin
windowManager.addView(view, layoutParams)
```

Activity 的 DecorView 也会在合适时机被加入 WindowManager。

简化链路：

```text
App 进程
  -> WindowManager
      -> ViewRootImpl
          -> Binder
              -> WindowManagerService
```

WindowManager 是应用侧接口，WMS 是 system_server 中真正管理窗口的系统服务。

## 第二部分：WMS 管理什么

WMS 不是负责写业务布局的。

它更关心系统级窗口秩序：

- 窗口是否允许添加。
- 窗口属于哪个 Activity 或应用。
- 窗口类型是什么。
- 窗口在什么层级。
- 窗口是否可见。
- 是否需要重新布局。
- 哪个窗口拥有输入焦点。
- 输入法应该依附哪个窗口。

它看见的不是“按钮和列表”，而是“窗口和窗口关系”。

## 第三部分：Token 是窗口身份证

很多窗口异常都和 Token 有关。

可以先这样理解：

```text
Token 是系统用来确认窗口归属和合法性的凭证。
```

Activity 窗口通常和 ActivityRecord / WindowToken 等系统侧对象相关。Dialog 这类附属窗口也需要依附在合适的宿主窗口上。

如果宿主 Activity 已经销毁，你还尝试显示 Dialog，就可能遇到：

```text
WindowManager.BadTokenException
```

这不是 Dialog 的文案错了，而是系统判断：这个窗口没有合法归属，不能添加。

## 第四部分：窗口类型决定规则

不同窗口类型有不同规则。

例如：

- 应用主窗口。
- Dialog 这类应用附属窗口。
- Toast 或提示类窗口。
- 输入法窗口。
- 系统状态栏、导航栏。
- 悬浮窗。

它们的权限、层级、焦点能力、显示时机都不同。

这也是为什么“一个 View 能不能盖在最上面”不是简单设置 `zIndex` 就能解决。应用内部 View 的层级和系统窗口层级是两回事。

## 第五部分：Z-Order 与遮挡关系

窗口有层级，View 也有层级。

要区分：

```text
View 层级：同一个窗口内部，谁盖住谁。
Window 层级：多个窗口之间，谁盖住谁。
```

例如：

- 一个 Button 盖住另一个 Text，是 View 层级问题。
- Dialog 盖住 Activity，是 Window 层级问题。
- 输入法盖住输入框，是窗口与 Insets 协作问题。
- 系统悬浮窗盖住 App，是权限和窗口类型问题。

## 本节小挑战

### 谁管遮挡

请判断下面问题更偏 View 层级还是 Window 层级：

- Card 盖住了 Text。
- Dialog 盖住了 Activity。
- 输入法弹起后遮住输入框。
- 悬浮窗显示在其他 App 上方。
- Toast 出现在当前页面上。

## 本节实践任务

### 基础任务

- 在示例工程里弹出一个 Dialog。
- 观察 Dialog 和 Activity 内容的遮挡关系。
- 写下这是 View 层级还是 Window 层级。

### 进阶任务

- 查找一次 `BadTokenException` 的典型触发场景。
- 用“宿主是否还有效”解释它。

## 本节小结

WindowManager 是应用侧入口，WMS 是系统侧窗口管理者。WMS 关心窗口身份、Token、类型、层级、可见性和焦点。理解这些概念后，Dialog、输入法、悬浮窗、遮挡异常和窗口泄漏就不再是零散问题，而是同一套窗口秩序的不同表现。
