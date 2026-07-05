# Binder 调用链报告模板

## 追踪问题

示例：

```text
Messenger 消息为什么能送到 :binder 进程中的 RemoteEchoService？
```

## 任务卡编号

示例：

```text
任务三：发送跨进程消息
```

## 应用侧现象

- 点击了哪个按钮？
- Binder 观察分数是多少？
- 本地 PID 是多少？
- 远程 PID 是多少？
- 远程处理线程是什么？
- 往返耗时是多少？

## 本地入口

示例：

```text
MainActivity.sendBinderMessage()
```

## 远程入口

示例：

```text
RemoteEchoService.IncomingHandler.handleMessage()
```

## 简化调用链

```text
MainActivity
  -> Messenger.send()
      -> Binder
          -> RemoteEchoService
              -> replyTo.send()
                  -> MainActivity.ReplyHandler
```

## 我已经理解的部分

- 

## 风险与注意点

- 

## 仍不确定的问题

- 

## 10 行通关报告

```text
问题：
现象：
本地入口：
远程入口：
链路：
风险：
仍不确定：
```
