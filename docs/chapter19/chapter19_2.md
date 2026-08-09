# 19.2 Linux 进程、UID、应用沙箱与 SELinux

学习 Android 进程模型，不能只停在 Java / Kotlin 层。

Android 本质上运行在 Linux 内核之上。App 进程首先是 Linux 进程，然后才是 Android App 进程。

本节先把身份问题讲清楚：

```text
一个 App
  -> 一个或多个 Linux 进程
      -> 一个 uid / appId
          -> 一个应用沙箱
              -> 一组权限和 SELinux 约束
```

## 本节定位

本节负责回答：

- Linux 进程和 Android App 进程是什么关系？
- pid、uid、appId 分别是什么？
- 为什么不同 App 不能随便访问彼此文件？
- Android 应用沙箱如何建立边界？
- SELinux 在 Android 安全模型里承担什么边界？

## 学习目标

学完本节后，你应该能够：

- 区分 pid、uid、packageName 和 processName。
- 理解应用沙箱为什么不只是 Java API 约束。
- 能解释同一 App 多进程为什么通常共享 uid。
- 知道权限、文件系统和 SELinux 是多层边界。

## 第一部分：进程先是 Linux 进程

在设备上执行：

```text
adb shell ps -A
```

你会看到很多进程：

```text
system_server
zygote64
surfaceflinger
com.example.app
com.example.app:remote
```

其中 `com.example.app` 不是一种特殊魔法，它也是 Linux 进程。

它有：

- pid：进程号。
- ppid：父进程号。
- uid：运行身份。
- cmdline：进程名。
- 内存、线程、文件描述符等资源。

Android Framework 是在 Linux 进程机制之上建立了 App 组件、Binder、权限和生命周期调度。

## 第二部分：pid、uid、appId、processName

这几个概念很容易混：

| 名称 | 说明 | 是否稳定 |
| --- | --- | --- |
| pid | 当前运行进程 ID | 每次进程重启都可能变化 |
| uid | Linux 用户身份 | App 安装后通常稳定 |
| appId | Android 为应用分配的应用身份部分 | 通常和 uid 相关 |
| packageName | 应用包名 | 由 Manifest / Gradle 决定 |
| processName | 进程名 | 默认等于包名，也可用 `android:process` 修改 |

一个 App 冷启动后，pid 可能是：

```text
24567
```

被系统杀掉再启动，pid 可能变成：

```text
25110
```

但它仍然是同一个 App。

所以不要把 pid 当成应用身份。pid 是“这次生命编号”，uid 才更接近系统权限身份。

再往下看，Android 的 uid 还和多用户有关。

同一个应用在不同用户空间里，可能对应不同 uid。可以拆成两层身份来看：

```text
uid
  -> userId 部分
  -> appId 部分
```

例如你在设备上看到：

```text
u0_a123
```

可以先读成：

```text
u0
  -> 第 0 个用户空间

a123
  -> 这个用户空间里的某个应用身份
```

这解释了为什么 Android 支持多用户后，包名相同也不一定代表同一个运行身份。

排查系统问题时，最好把这几项一起记录：

```text
packageName
processName
pid
uid
userId
```

只记录包名，很多时候不够。

## 第三部分：应用沙箱是什么

Android 会让不同 App 运行在不同 uid 下。

这带来一个非常重要的结果：

```text
App A uid = u0_a123
App B uid = u0_a124
```

App A 的私有目录：

```text
/data/data/com.example.a/
```

App B 默认不能直接读写。

这不是因为某个 Java 方法判断了包名，而是 Linux 文件权限和进程 uid 已经把它们隔开了。

应用沙箱可以理解成：

```text
进程身份
  -> 文件权限
      -> 私有数据目录
          -> 组件权限
              -> SELinux 域限制
```

多层一起工作，才让 App 之间默认互不信任。

更接近系统视角的链路是：

```text
PackageManager 安装 APK
  -> 分配 appId / uid
      -> 创建 /data/user/<userId>/<packageName> 私有目录
          -> 设置目录 owner / group / mode
              -> 进程以这个 uid 运行
                  -> 访问文件时由 Linux 权限先拦截
                      -> 访问系统能力时由 Binder 调用方身份继续校验
                          -> 更底层再叠加 SELinux 策略
```

所以沙箱不是一个对象，也不是一个开关。

它是一组从安装、文件系统、进程身份到系统服务检查的连续约束。

这也是为什么普通 App 就算通过反射找到某些类，也不等于拥有系统权限。代码能访问 API，不代表进程身份被允许做这件事。

## 第四部分：同一 App 多进程通常共享 uid

如果你声明：

```xml
<service
    android:name=".RemoteService"
    android:process=":remote" />
```

系统可能创建：

```text
com.example.app
com.example.app:remote
```

这两个进程 pid 不同，但通常属于同一个 uid。

它们共享应用身份，却不共享内存。

所以：

```text
同 uid
  -> 可以共享一部分权限和私有文件访问能力

不同进程
  -> 单例、堆内存、线程、ClassLoader 实例都不是同一份
```

这句话很关键。

多进程不是“多个线程”，而是多个独立运行空间。

## 第五部分：权限不是单层开关

Android 权限通常会以 API 形式出现：

```kotlin
checkSelfPermission(Manifest.permission.CAMERA)
```

但权限背后不止 Java API。

大致可以分成：

```text
Manifest 声明
  -> 安装 / 运行时授权状态
      -> AppOps / 系统策略
          -> Binder 调用方 uid 检查
              -> SELinux 和底层访问控制
```

例如一个系统服务收到 Binder 请求时，它看到的不只是“调用了哪个方法”，还可以知道调用方 uid / pid，从而判断是否允许访问。

这也是 Binder 和进程身份紧密相连的原因。

以相机为例，表面上你写的是：

```text
cameraManager.openCamera(...)
```

但系统真正关心的是：

```text
调用方 uid / pid 是谁
  -> 是否声明了权限
      -> 用户是否授权
          -> AppOps 是否允许
              -> camera service 是否接受这个调用方
                  -> SELinux 是否允许底层访问
```

所以“权限明明申请了还是失败”时，不要只盯 Manifest。

你要同时看：

- 运行时授权。
- AppOps 状态。
- 当前进程身份。
- 目标系统服务的校验逻辑。
- 是否出现 SELinux deny。

## 第六部分：SELinux 承担什么边界

SELinux 是 Android 安全边界的重要组成部分。

你可以先这样理解：

```text
Linux 文件权限回答：
  这个 uid 能不能访问这个文件？

SELinux 回答：
  这个安全域里的进程，能不能对这个对象做这类操作？
```

它不是普通 App 开发每天都要直接操作的东西，但对 Framework 学习很重要。

很多系统层问题会表现成：

```text
avc: denied
```

这通常意味着 SELinux 策略拒绝了某个访问。

课程里不要求你立刻能写完整 SELinux policy，但你需要知道：Android 的安全边界不是一层 if，而是 uid、权限、Binder、文件系统和 SELinux 共同构成的。

一个典型 deny 日志可能长这样：

```text
avc: denied { read } for name="xxx"
  scontext=u:r:untrusted_app:s0
  tcontext=u:object_r:system_file:s0
  tclass=file
```

先不用急着背每个字段，但要知道大概意思：

| 字段 | 含义 |
| --- | --- |
| `scontext` | 谁在访问，源安全上下文 |
| `tcontext` | 被访问对象是什么，目标安全上下文 |
| `tclass` | 对象类型，比如 file、service、dir |
| `{ read }` | 请求的操作 |

这类问题如果发生在系统开发里，通常不能靠 Java 层 try-catch 解决，而要回到权限、进程域和 SELinux policy。

## 第七部分：进程身份如何影响排查

遇到问题时，可以先问：

```text
当前代码运行在哪个进程？
pid 是多少？
uid 是多少？
processName 是什么？
调用方是谁？
被调用方是谁？
```

尤其是这些场景：

- 多进程初始化重复。
- RemoteService 读不到主进程缓存。
- ContentProvider 过早初始化。
- 权限明明申请了但系统服务仍拒绝。
- 系统 ROM / Framework 开发中遇到 `avc: denied`。

不要只看类名和日志 tag。

进程身份本身就是第一证据。

## 本节小挑战

### 身份识别题

下面两个进程：

```text
com.example.app
com.example.app:push
```

请回答：

- 它们是不是同一个 pid？
- 它们是否可能共享 uid？
- 它们是否共享同一个单例对象？
- 它们是否都能访问 App 私有目录？
- 日志和初始化逻辑应该如何区分它们？

## 本节实践任务

### 基础任务

- 使用 `adb shell ps -A | grep 包名` 查看 pid 和进程名。
- 使用 `adb shell run-as 包名 id` 查看当前应用身份。
- 使用 `adb shell cat /proc/<pid>/status` 查看 uid、线程数和进程状态。
- 在 App 中打印 `Process.myPid()` 和当前进程名。

### 进阶任务

- 给一个 Service 配置 `android:process=":remote"`。
- 对比主进程和 remote 进程里的单例地址、pid、进程名。
- 思考哪些初始化不应该在所有进程里执行。

## 本节小结

Android App 进程首先是 Linux 进程。pid 表示一次运行生命，uid 表示系统权限身份，processName 表示进程名字。应用沙箱依赖 uid、文件权限、Binder 调用方身份、Android 权限和 SELinux 等多层机制。理解这些边界后，多进程、权限拒绝、私有文件访问和系统服务调用才不会只停留在 API 表面。
