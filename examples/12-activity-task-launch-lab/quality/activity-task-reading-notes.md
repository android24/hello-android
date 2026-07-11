# 第12章 Activity 启动源码阅读建议

## 从熟悉 API 进入

建议从你最熟悉的 API 开始：

```kotlin
startActivity(intent)
```

第一轮只追主线：

```text
Activity.startActivity()
  -> Instrumentation.execStartActivity()
      -> ActivityTaskManagerService
          -> ActivityStarter
              -> ActivityThread
```

不要一开始就追所有窗口、权限、多任务和兼容分支。

## 看任务栈先问三个问题

- 目标 Activity 是否已经存在？
- 目标是否在栈顶？
- Intent Flag 是否要求清理或复用？

这三个问题能帮助你理解 standard、singleTop 和 CLEAR_TOP。

## 读 ATMS 看四类对象

```text
ActivityTaskManagerService：系统服务入口
ActivityStarter：处理启动请求
ActivityRecord：系统侧 Activity 记录
Task：用户任务和返回栈相关结构
```

不同 Android 版本内部细节会变化，但这些概念能帮助你建立源码地图。

## 配合 demo 读

建议按下面顺序：

```text
运行 demo
  -> 触发 singleTop onNewIntent
      -> 写日志顺序
          -> 搜 ActivityStarter
              -> 搜 ActivityRecord
```

这样源码阅读就不是背类名，而是在解释你刚刚观察到的行为。
