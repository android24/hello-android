# Activity 启动链路报告模板

## 追踪问题

示例：

```text
为什么再次启动栈顶 SingleTopActivity 会触发 onNewIntent？
```

## 操作路径

```text
Main -> SingleTop -> 再次启动 SingleTop
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

## 实际日志

- 

## 生命周期顺序

```text

```

## 返回栈推断

```text

```

## 任务栈推断图

示例：

```text
栈顶
SingleTopActivity    singleTop 观察页
DetailActivity #1    standard 新实例
MainActivity         任务栈根页面
栈底
```

## 可能的源码入口

```text
Activity.startActivity()
Instrumentation.execStartActivity()
ActivityTaskManagerService
ActivityStarter
ActivityRecord
Task
ActivityThread
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
生命周期：
返回栈：
任务栈图：
源码入口：
结论：
仍不确定：
```
