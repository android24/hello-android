# 第11章系统服务阅读建议

## 从熟悉 API 进入

不要一开始就试图读完整个 SystemServer。

建议先选择一个熟悉 API：

```text
getSystemService(NotificationManager::class.java)
```

然后反向追：

```text
Context
  -> SystemServiceRegistry
      -> Manager
          -> 远程服务代理
              -> system_server 中的服务
```

## 读 SystemServer 看四件事

阅读 `SystemServer.java` 时，先看：

- 服务在哪个阶段启动。
- 服务对象如何创建。
- 服务如何注册。
- 服务依赖哪些其他系统能力。

第一轮不要追所有服务细节。

## 读 Binder 看五个角色

```text
Client
Proxy
Binder Driver
Stub
Server
```

第 11 章只要求你能把这五个角色放回一次调用里。

## 配合 demo 读

建议按下面顺序：

```text
绑定 RemoteEchoService
  -> 发送 Messenger 消息
      -> 观察 remotePid
          -> 写 Binder 调用链报告
              -> 再去看 Binder / ServiceManager / SystemServer 源码
```

这样读源码时，类名不会只是抽象名词，而是能对应到你刚刚观察过的现象。
