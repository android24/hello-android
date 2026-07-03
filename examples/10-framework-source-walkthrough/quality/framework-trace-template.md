# Framework 调用链笔记模板

## 追踪问题

示例：

```text
Handler.post() 为什么能让 Runnable 回到主线程执行？
```

## 应用侧现象

- 点击了哪个按钮？
- 页面发生了什么变化？
- Logcat 输出了什么？
- 当前线程名是什么？

## 可能的源码入口

```text
Handler
MessageQueue
Looper
```

## 简化调用链

```text
Handler.post()
  -> Handler.sendMessageDelayed()
      -> MessageQueue.enqueueMessage()
          -> Looper.loop()
              -> Handler.dispatchMessage()
                  -> Runnable.run()
```

## 我已经理解的部分

- 

## 我还不确定的部分

- 

## 下一步要验证

- 
