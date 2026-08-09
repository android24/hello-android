# 20.4 Java Crash：异常如何杀死进程

ANR 是系统认为 App 没有及时响应。

Java Crash 则更直接：

```text
某个线程抛出未捕获异常
  -> 线程没有处理
      -> Runtime / ThreadGroup / UncaughtExceptionHandler 接管
          -> 记录崩溃日志
              -> 进程退出
```

很多人第一次接触稳定性，就是从 Java Crash 开始的：

```text
NullPointerException
IndexOutOfBoundsException
IllegalStateException
ClassCastException
ConcurrentModificationException
```

它们看起来比 ANR 简单，因为有明确栈。

但真正线上排查时，Java Crash 也可能很复杂：多线程、生命周期、异步回调、混淆、反射、动态加载、状态恢复都会让一个异常背后藏着更长的链路。

## 本节定位

本节负责解释：

- Java Crash 的基本分发链路是什么？
- 主线程 Crash 和后台线程 Crash 有什么差异？
- 为什么捕获异常不等于稳定？
- 混淆、异步和生命周期如何影响崩溃分析？
- 崩溃修复应该如何验证？

## 学习目标

学完本节后，你应该能够：

- 读懂 Java Crash 的异常类型、线程和调用栈。
- 解释未捕获异常为什么会导致进程退出。
- 知道全局异常捕获器的能力边界。
- 能把 Crash 分成必现、偶现、设备相关、版本相关和数据相关。
- 能写出一份可回归的 Crash 修复报告。

## 第一部分：Java Crash 的基本链路

一个典型 Java Crash 日志会包含：

```text
FATAL EXCEPTION: main
Process: com.example.app, PID: 12345
java.lang.NullPointerException
    at com.example.HomeActivity.render(HomeActivity.kt:42)
    at ...
```

你首先要提取：

```text
异常类型
线程名
进程名
pid
第一业务栈
设备和系统版本
App 版本
是否混淆
```

其中最重要的是“第一业务栈”。

不是每一行栈都同等重要。

很多顶部栈可能是系统调度或框架调用，真正需要修的是第一个进入你业务代码的位置。

### RuntimeInit 与崩溃收口

Android App 进程启动后，会经过运行时初始化和 `ActivityThread.main()` 进入主线程消息循环。

当 Java 层出现未捕获异常时，异常会沿着线程的未捕获异常处理链路向上走。

可以粗略理解成：

```text
线程执行代码
  -> 抛出异常
      -> 当前调用栈没有 catch
          -> Thread.dispatchUncaughtException
              -> UncaughtExceptionHandler
                  -> Android 运行时记录 FATAL EXCEPTION
                      -> 结束进程
```

Android 在进程初始化时会安装面向应用进程的异常处理器，用来统一处理未捕获异常：

```text
记录崩溃信息
  -> 通知系统侧应用崩溃
      -> 结束当前进程
          -> 让 AMS / 系统决定后续界面和进程状态
```

你不需要一开始背每个类名，但要理解一个原则：

```text
未捕获异常不是某个线程自己的小事故
  -> 它会被运行时收口
      -> 通常导致整个 App 进程退出
```

这也是为什么后台线程崩溃同样可能让 App 闪退。

## 第二部分：主线程 Crash 和后台线程 Crash

主线程 Crash 常见于：

- Activity / Fragment / Compose 渲染。
- 生命周期回调。
- 点击事件。
- Adapter / 列表绑定。
- LiveData / Flow / State 更新后渲染。

后台线程 Crash 常见于：

- 协程任务。
- 线程池任务。
- 数据库操作。
- 网络回调。
- 图片解码。
- 文件处理。

无论在哪个线程，如果异常没有被处理，都可能导致进程退出。

所以不要以为：

```text
后台线程崩了
  -> 只是那个线程没了
```

在 Android App 里，未捕获异常通常会进入全局异常处理并结束进程。

## 第三部分：全局异常捕获不是免死金牌

你可以设置：

```kotlin
Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
    // 记录日志、保存现场、上报
}
```

但它不应该被理解成：

```text
捕获所有异常
  -> App 可以继续安全运行
```

原因很简单：

- 发生未捕获异常时，进程状态可能已经不可信。
- 主线程崩溃后，UI 调度可能已经中断。
- 某些资源、锁、事务可能处于半完成状态。
- 强行吞掉异常可能制造更隐蔽的数据损坏。

全局异常捕获更适合做：

```text
记录现场
保存必要崩溃信息
触发上报
优雅退出或重启入口
```

不是用来把所有 bug 都藏起来。

### 为什么吞异常危险

强行吞掉未捕获异常，相当于告诉系统：

```text
这个进程没事，可以继续跑
```

但真实状态可能是：

```text
数据库事务只执行了一半
内存状态已经不一致
锁没有按预期释放
UI 树处于异常状态
后台任务丢失上下文
业务状态机进入非法分支
```

这会把“可见崩溃”变成“不可见数据损坏”。

所以全局异常处理器里最稳妥的动作是：

```text
保存现场
上报异常
尽可能保存用户关键草稿
让进程按可预期方式退出或重启入口
```

而不是继续假装一切正常。

## 第四部分：混淆后的 Crash 如何读

如果开启 R8 / ProGuard，线上栈可能变成：

```text
at a.b.c.a(:12)
```

这时需要 mapping 文件还原。

稳定性平台必须保证：

```text
每个线上版本
  -> 对应唯一 mapping
      -> 崩溃上报时带 versionCode / buildId
          -> 后台用正确 mapping 反混淆
```

否则你会看到一堆无意义栈名。

第 18 章讲过 R8 和代码加载，第 20 章要把它们放回稳定性治理：

```text
混淆不是问题
丢 mapping 才是问题
```

### Crash 分组与同栈不同因

稳定性平台经常按异常类型和栈进行聚合。

但要小心：

```text
同一个崩溃栈
  -> 不一定只有一个根因

不同崩溃栈
  -> 也可能来自同一个上游数据问题
```

例如同一个 `CourseDetailScreen.render()` NPE，可能来自：

- 接口字段缺失。
- 本地缓存迁移失败。
- 进程重建后参数丢失。
- 多进程写入了不完整状态。

所以 Crash 聚合只能帮助你找到热点，不能替代根因分析。

真正排查时还要看：

```text
用户路径
入参
前后台状态
进程是否重建
接口响应
本地数据版本
灰度开关
```

## 第五部分：异步 Crash 的隐藏链路

很多 Crash 的触发栈不等于根因栈。

例如：

```text
网络返回
  -> 更新 ViewModel 状态
      -> Compose 重组
          -> 读取空字段
              -> Crash
```

崩溃栈可能指向 UI 渲染，但根因是接口数据缺字段。

再比如：

```text
Activity 已销毁
  -> 异步回调返回
      -> 持有旧 View 引用
          -> IllegalStateException / NullPointerException
```

所以 Crash 排查要结合：

- 用户操作路径。
- 前后台状态。
- 生命周期状态。
- 网络数据。
- 本地缓存。
- Feature flag。
- 多线程时序。

## 第六部分：Crash 修复不要只修一行

看到 NPE 后直接加 `?.` 可能只是把崩溃变成静默错误。

修复要问：

```text
这个值为什么会为空？
为空是否合理？
如果合理，UI 应该展示什么？
如果不合理，数据链路哪里破了？
是否需要兜底、重试、降级或校验？
```

更可靠的修复报告应该包含：

```text
异常类型：
触发线程：
第一业务栈：
根因：
修复方案：
为什么不会引入新问题：
回归用例：
是否需要监控：
```

## 第七部分：常见 Java Crash 分类

| 类型 | 常见原因 | 修复方向 |
| --- | --- | --- |
| NullPointerException | 状态为空、接口字段缺失、生命周期已结束 | 明确空状态语义、兜底 UI、数据校验 |
| IndexOutOfBoundsException | 列表变更、异步更新、位置失效 | 使用稳定 id、检查边界、避免缓存旧 position |
| IllegalStateException | 生命周期状态不合法、重复提交、状态机错误 | 收敛状态流、增加状态约束 |
| ClassCastException | 类型假设错误、反射、混淆、动态加载 | 明确接口模型、减少强转 |
| ConcurrentModificationException | 遍历时修改集合、多线程共享状态 | 使用不可变快照或线程安全结构 |

## 本节小挑战

### Crash 诊断题

请分析下面日志：

```text
FATAL EXCEPTION: DefaultDispatcher-worker-2
Process: com.example.app, PID: 9988
java.lang.IllegalStateException: database is closed
    at com.example.CourseRepository.save(CourseRepository.kt:64)
    at com.example.SyncWorker.sync(SyncWorker.kt:38)
```

你需要回答：

- 这是主线程 Crash 还是后台线程 Crash？
- 第一业务栈在哪里？
- 根因可能是什么？
- 只 try-catch 是否足够？
- 回归验证应该怎么做？

## 本节实践任务

### 基础任务

- 制造一个 NPE，观察崩溃日志。
- 制造一个后台线程异常，观察是否导致进程退出。
- 给日志增加 processName、pid、threadName。

### 进阶任务

- 接入一个简单的全局异常捕获器，记录异常但不吞异常。
- 写一份 Crash 修复报告模板。
- 对比混淆前后 Crash stack 的可读性。

## 本节小结

Java Crash 看起来有明确栈，但不要停在“哪一行崩了”。

你要继续追问：

```text
哪个线程崩了？
状态为什么不合法？
数据从哪里来？
生命周期是否已经变化？
修复后如何证明不会再发生？
```

稳定性治理的价值，不是让崩溃消失在日志里，而是让每一次崩溃都能变成更可靠的工程约束。
