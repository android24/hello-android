# 23.2 应用沙箱、UID、SELinux 与进程边界

上一节我们建立了第 23 章的安全地图。

这一节先看 Android 安全模型的地基：

```text
进程
UID
应用沙箱
文件权限
SELinux
```

权限弹窗是用户看得见的部分，沙箱和 UID 是系统一直在背后执行的部分。

## 本节先记住三句话

```text
沙箱不是目录藏得深，而是 uid、文件权限和 SELinux 一起工作。
多进程会改变运行位置，不会天然改变 App 的安全身份。
共享私有数据要改成受控入口，不能靠路径技巧绕过边界。
```

## 贯穿案例：学习报告为什么不能直接发路径

`Hello Android 学习中心`生成了一份学习报告：

```text
/data/user/0/com.helloandroid/files/report.txt
```

产品希望把这个路径发给另一个 App 打开。

这时你要先判断：

```text
另一个 App 的 uid 和当前 App 是否相同？
这个文件 owner 是否允许其他 uid 读取？
SELinux 是否允许这种访问？
如果不允许，是不是应该改成 FileProvider 或 SAF？
```

本节要让你明白：这个问题不是“路径对不对”，而是“访问者身份对不对”。

## 安全侦探问题

你只能看三条证据来判断私有文件为什么打不开。

你会先看哪三条？

```text
候选证据：
packageName
uid
dataDir
文件 owner / mode
SELinux avc 日志
FileProvider paths
```

建议答案不是唯一的，但必须能解释“谁在访问谁的文件”。

## 本节定位

本节负责回答：

- Android 为什么给每个 App 分配独立 UID？
- 应用沙箱到底保护什么？
- 多进程是否突破了沙箱？
- SELinux 和普通 Linux 文件权限是什么关系？
- 为什么私有目录不是跨应用共享方案？

## 学习目标

学完本节后，你应该能够：

- 理解 App 默认运行在自己的 Linux UID 下。
- 知道应用私有目录为什么其他 App 不能直接访问。
- 区分进程边界、UID 边界和组件通信边界。
- 理解 SELinux 是系统安全的强制访问控制层。
- 能从 `run-as`、`dumpsys package` 和文件路径里观察沙箱证据。

## 第一部分：UID 是 App 的系统身份

Android 安装 App 时，会为普通应用分配一个 Linux UID。

这个 UID 很重要。

它决定：

```text
这个 App 的进程以谁的身份运行
这个 App 默认能访问哪些文件
系统如何隔离不同 App
Binder 调用里系统如何识别调用方
```

可以粗略理解：

```text
packageName
  -> 面向人和包管理

uid
  -> 面向 Linux 内核和系统服务

signing certificate
  -> 面向安装、升级和信任关系
```

例如：

```text
/data/user/0/com.example.app/
```

这个目录不是“因为路径写得隐蔽所以安全”，而是因为文件所有者、权限和 SELinux 策略共同限制了其他 App 访问。

## 第二部分：应用沙箱保护的是默认边界

应用沙箱默认保护：

```text
App 私有文件
数据库
SharedPreferences / DataStore
私有缓存
native library 数据
进程内存
部分 IPC 入口
```

但沙箱不是说 App 永远不能和外部通信。

Android 提供了受控出入口：

```text
Activity / Service / BroadcastReceiver
ContentProvider
Binder
FileProvider
SAF Uri grant
MediaStore
系统权限 API
```

安全设计的重点是：

```text
默认隔离
  -> 必要时通过明确入口共享
      -> 每个入口都要有授权和边界
```

这和第 22 章的 FileProvider 正好连上：

```text
不要暴露 filesDir 路径
  -> 用 FileProvider 临时授权 content Uri
```

## 第三部分：多进程不等于多身份

一个 App 可以配置多进程：

```xml
android:process=":remote"
```

很多初学者会误以为：

```text
多进程
  -> 就有了新的安全身份
```

通常不是。

同一个 App 的普通多进程仍然属于同一个应用 UID。

它们共享：

```text
权限授予结果
应用私有目录访问能力
签名身份
AppOps 归属
```

多进程改变的是：

```text
内存空间
线程和 Looper
进程生命周期
Binder 通信成本
崩溃影响范围
```

它通常不改变：

```text
这个 App 在系统安全模型里的基本身份
```

第 19 章讲过进程模型，第 23 章要补上安全视角：

```text
processName 解释运行空间
uid 解释安全身份
permission / AppOps 解释能力边界
```

## 第四部分：SELinux 是强制访问控制

Linux 文件权限已经能隔离很多东西。

但 Android 还使用 SELinux 增加强制访问控制。

你可以粗略理解：

```text
Linux 权限
  -> 看用户、组和文件权限位

SELinux
  -> 继续看主体域、对象类型和访问策略
```

也就是说，即使某个进程看起来具备 Linux 层面的某些条件，SELinux 仍然可能拒绝访问。

这对普通 App 开发者意味着：

```text
不要指望通过路径技巧访问系统或其他 App 数据
不要把 root / debug 环境里的结果当作普通设备行为
不要把厂商系统差异简单归因成文件不存在
```

如果你在系统开发或 rooted 调试里看到：

```text
avc: denied
```

那通常是 SELinux 拒绝访问的证据。

## 第五部分：私有目录为什么不是共享方案

第 22 章已经讲过：

```text
filesDir 适合 App 私有数据
```

第 23 章从安全角度再解释一次：

```text
filesDir 的默认语义就是不让别的 App 直接读
```

所以如果你要分享：

```text
错误思路：
  -> 把 /data/user/0/.../files/report.txt 路径发出去

正确思路：
  -> FileProvider 生成 content Uri
  -> Intent 临时授权
  -> 接收方在授权范围内读
```

如果你要导出：

```text
错误思路：
  -> 把用户报告悄悄写进 filesDir

正确思路：
  -> SAF 让用户选择保存位置
```

共享必须改变入口，而不是绕过沙箱。

## 第六部分：沙箱证据怎么观察

推荐观察：

```bash
adb shell dumpsys package com.helloandroid.storage
adb shell run-as com.helloandroid.storage ls files
adb shell run-as com.helloandroid.storage ls cache
adb shell ps -A | grep com.helloandroid
```

观察点：

```text
packageName
uid
targetSdk
requested permissions
granted permissions
dataDir
processName
```

注意：

```text
run-as 只能用于 debuggable App
release App 通常不能随便 run-as
普通 App 不能直接 ls 其他 App 私有目录
```

这不是命令限制，而是安全模型正在工作。

## 第六部分补充：沙箱不是一个目录，而是一组判断

很多人第一次理解沙箱时，会把它想成：

```text
/data/user/0/<package> 这个目录不让别人读
```

这只说对了一小半。

Android 的沙箱更像一组连续判断：

```text
进程是谁
  -> Linux uid / gid
      -> 文件所有者和权限位
          -> SELinux domain / type
              -> Binder 调用方身份
                  -> Framework 服务里的权限和 AppOps 判断
```

也就是说，沙箱不是靠“目录藏得深”实现的，而是靠系统在多个层面不断确认：

```text
你是谁？
你想访问什么？
你是否被允许访问？
这次访问是否符合当前策略？
```

### DAC：Linux 文件权限

Linux 传统权限模型通常叫 DAC，Discretionary Access Control。

它主要看：

```text
文件属于哪个 uid / gid
当前进程属于哪个 uid / gid
权限位是否允许 read / write / execute
```

普通 App 访问自己的私有文件能成功，是因为：

```text
进程 uid == 文件所有者 uid
```

普通 App 访问其他 App 私有文件失败，是因为：

```text
进程 uid != 文件所有者 uid
权限位不允许其他用户读取
```

所以你在排查私有目录问题时，第一证据不是路径，而是：

```text
uid
dataDir
文件 owner
文件 mode
```

### MAC：SELinux 强制访问控制

SELinux 属于 MAC，Mandatory Access Control。

它不只看 uid，还看：

```text
主体 domain
对象 type
允许的 class 和 permission
```

可以粗略理解为：

```text
Linux DAC 说：这个用户有没有文件权限？
SELinux MAC 说：这个类型的进程，能不能对这个类型的对象做这类操作？
```

所以即使文件权限看起来“够开放”，SELinux 仍然可能拒绝。

典型证据是：

```text
avc: denied { read } for ...
```

读这类日志时不要只盯着路径，要看：

```text
scontext
  -> 谁在访问

tcontext
  -> 被访问对象是什么类型

tclass
  -> 访问的是 file、dir、binder、service_manager 还是 socket

permission
  -> read、write、find、call、open 等具体动作
```

这会帮你把问题从：

```text
系统不让我读文件
```

升级成：

```text
某个 domain 的进程，尝试对某个 type 的对象执行某个动作，被策略拒绝。
```

这才是系统工程师能继续往下查的表达。

### Binder：跨进程后身份怎么传

很多安全判断不是发生在 App 进程里，而是发生在系统服务里。

例如：

```text
App 调用系统 API
  -> Binder 进入 system_server
      -> 系统服务拿到 callingUid / callingPackage
          -> 查权限、查 AppOps、查用户状态
              -> 决定允许或拒绝
```

关键点是：

```text
系统服务不能只相信 App 传来的字符串。
```

因为字符串可以伪造，Binder 调用方身份不能随便伪造。

所以 Framework 里经常会看到类似判断：

```text
callingUid
callingPid
packageName 是否属于这个 uid
权限是否授予这个 uid / package
AppOps 是否允许这个 uid / package 执行这个 op
```

这也解释了为什么安全排查报告要记录：

```text
packageName
uid
processName
签名
permission
AppOps
```

它们不是表格字段，而是系统做判断时真正需要的证据。

## 第六部分补充二：沙箱问题的系统判断链

遇到沙箱、私有目录或跨进程访问问题时，可以按下面这条链路拆：

```text
第一层：包状态
  -> 这个 packageName 是否真的安装？
  -> 它属于哪个 userId？
  -> 它的 appId / uid 是多少？
  -> 它的 dataDir 在哪里？

第二层：进程状态
  -> 当前 pid 是多少？
  -> 当前进程 uid 是多少？
  -> processName 是否是主进程？
  -> 多进程是否仍然归属同一个 uid？

第三层：文件边界
  -> 目标文件 owner 是谁？
  -> 文件 mode 是否允许当前 uid 访问？
  -> 访问的是 files、cache、databases 还是外部共享目录？

第四层：SELinux
  -> 当前进程 domain 是什么？
  -> 目标对象 type 是什么？
  -> 策略是否允许这个 domain 对这个 type 做这个动作？

第五层：Framework 服务判断
  -> 是否通过 Binder 调用系统服务？
  -> 系统服务拿到的 callingUid 是谁？
  -> packageName 是否属于这个 uid？
  -> permission / AppOps 是否允许？
```

这条链能解释很多“看起来像文件问题，其实是身份问题”的事故。

例如：

```text
现象：App A 想读取 App B 的私有文件失败

不要先问：
  -> 路径是不是写错了？

应该先问：
  -> App A 的 uid 是多少？
  -> App B 私有目录的 owner 是谁？
  -> Linux DAC 是否允许？
  -> SELinux 是否允许？
  -> 是否应该改成 ContentProvider / FileProvider / SAF？
```

普通 App 之间共享数据，正确方向通常不是“突破沙箱”，而是：

```text
让拥有数据的一方主动暴露受控入口
  -> ContentProvider
  -> FileProvider
  -> SAF
  -> 系统 Picker
```

### uid、appId 和 userId 的关系

Android 是多用户系统。

同一个 App 在不同用户空间下，uid 可能不同。

可以粗略理解：

```text
uid
  -> userId + appId 组合出来的运行身份
```

所以排查时不要只写：

```text
uid=10123
```

还要意识到：

```text
这个 uid 属于哪个 Android user？
这个 package 在当前用户下是否 installed / enabled？
这个数据目录是不是当前用户空间的数据目录？
```

这在多用户、工作资料、平板多账号和企业设备里尤其重要。

### run-as 为什么不能代表线上环境

`run-as` 很适合学习沙箱，但它有边界。

它通常要求：

```text
App 是 debuggable
调用方具备调试能力
目标包允许 run-as
```

所以你可以用它观察：

```text
filesDir
cacheDir
databases
shared_prefs
```

但不要把它当作线上能力。

线上安全边界应该按普通 App 行为判断：

```text
不能 run-as
不能直接 ls 私有目录
不能直接读其他包数据
必须通过系统认可的共享入口
```

这也是 Demo 里要专门设计“debug 观察”和“线上边界”两个视角的原因。

## 第七部分：本节自测

请回答：

- packageName、UID、签名分别代表什么身份？
- 为什么其他 App 不能直接读取你的 `filesDir`？
- 多进程是否改变 App 的 UID？
- SELinux 和 Linux 文件权限有什么关系？
- 为什么 FileProvider 是共享私有文件的正确入口？
- `run-as` 为什么通常只适合 debug 包？
- 为什么 Binder 调用里不能只相信调用方传来的 packageName 字符串？
- `avc: denied` 日志里 scontext、tcontext、tclass 分别应该怎么看？
- uid、appId 和 userId 为什么会影响沙箱问题排查？
- 为什么私有文件共享应该改入口，而不是尝试绕过路径？

## 本节小结

Android 安全模型的地基是：

```text
每个 App 默认有自己的 UID
每个 UID 默认有自己的私有数据边界
跨边界访问必须通过系统认可的入口
SELinux 继续限制系统和进程之间的访问
```

第 23.2 节的关键结论是：

```text
沙箱不是为了阻止 App 做事，而是让共享必须变成明确授权。
```
