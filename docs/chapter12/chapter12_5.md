# 12.5 launchMode 与 Intent Flag

同样是启动 Activity，不同 launchMode 和 Intent Flag 可能带来完全不同的返回栈结果。

这一节聚焦最容易在项目里踩坑的启动规则。

## 本节剧情钩子

你以为自己只是“打开一个页面”。

但你交给系统的申请表上可能写着不同要求：

```text
普通打开
如果栈顶已有就复用
清理目标之上的页面
放到新任务里
```

系统会根据这些要求调整任务栈。

## 本节定位

本节理解常见 launchMode 和 Intent Flag 的效果。

## 学习目标

学完本节后，你应该能够：

- 理解 standard 与 singleTop 的基本区别。
- 知道 `onNewIntent()` 什么时候可能触发。
- 理解 `FLAG_ACTIVITY_CLEAR_TOP` 的常见用途。
- 能用 demo 验证不同启动方式的日志差异。

## 第一部分：standard

`standard` 是默认启动模式。

每次启动通常都会创建新的 Activity 实例。

简化理解：

```text
Main
  -> Detail
      -> Detail
```

连续启动两次详情页，可能得到两个实例。

## 第二部分：singleTop

`singleTop` 的关键是：

```text
如果目标 Activity 已经在栈顶，就复用它。
```

复用时通常不会重新走 `onCreate()`，而是触发 `onNewIntent()`。

如果目标不在栈顶，仍可能创建新实例。

## 第三部分：CLEAR_TOP

`FLAG_ACTIVITY_CLEAR_TOP` 常用于回到栈中已有页面，并清理它上面的页面。

例如：

```text
Main -> Detail -> Settings
```

如果用 `CLEAR_TOP` 启动 Main，系统可能清理 Detail 和 Settings，让 Main 回到前台。

具体行为还会受到 launchMode、Flag 组合影响。

## 第四部分：Flag 不要随便叠

Intent Flag 很强大，但也容易制造难懂的返回栈。

常见问题：

- 页面重复创建。
- 返回键路径异常。
- 任务切换体验奇怪。
- 深链和通知跳转行为不一致。
- 多 Flag 叠加后难以预测。

使用 Flag 前，要先写出你期望的返回栈。

## 第五部分：demo 中怎么观察

第 12 章 demo 提供：

- 普通启动详情页。
- 启动 `singleTop` 观察页。
- 在 `singleTop` 页面里再次启动自己。
- 使用 `CLEAR_TOP` 回到首页。

你可以观察：

- 哪些操作触发 `onCreate()`。
- 哪些操作触发 `onNewIntent()`。
- 返回键路径如何变化。

## 本节小挑战

### 返回栈预测题

请预测：

```text
Main -> SingleTop -> 再次启动 SingleTop
```

第二次启动会走 `onCreate()` 还是 `onNewIntent()`？

先写预测，再用 demo 验证。

## 本节实践任务

### 基础任务

- 打开 `singleTop` 观察页。
- 在观察页里再次启动自己。
- 记录 `onNewIntent()` 是否出现。

### 进阶任务

- 从详情页使用 `CLEAR_TOP` 回首页。
- 观察中间页面是否被销毁。
- 画出操作前后的返回栈。

## 本节小结

launchMode 和 Intent Flag 是任务栈调度的入口参数。它们影响实例复用、生命周期回调和返回路径。想用好它们，不能只背定义，要用日志验证每一次启动后栈发生了什么。
