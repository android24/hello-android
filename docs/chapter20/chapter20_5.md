# 20.5 Native Crash 与 tombstone：signal、JNI 和 so 崩溃

Java Crash 有 Java stack。

Native Crash 则会把你带到另一套证据系统：

```text
signal
  -> native backtrace
      -> tombstone
          -> so / addr / thread / register
              -> 符号化
                  -> 定位 native 代码
```

很多 Android App 表面上是 Kotlin / Java 写的，但依然可能发生 native 崩溃：

- 使用第三方 so。
- 使用音视频、地图、WebView、图片处理、加密库。
- 通过 JNI 调用 C / C++。
- 加载插件或动态库。
- 使用系统 native 能力。

第 18 章讲过 native so 如何加载；这一节讲 so 崩了以后如何看现场。

## 本节定位

本节负责解释：

- Native Crash 和 Java Crash 的差异。
- signal、tombstone、backtrace 是什么。
- JNI 常见错误为什么会导致进程崩溃。
- 为什么 native 崩溃需要符号表。
- 如何建立 native crash 的排查路线。

## 学习目标

学完本节后，你应该能够：

- 说清楚 `SIGSEGV`、`SIGABRT` 等 signal 的大致含义。
- 知道 tombstone 是 native 崩溃的重要证据。
- 能从 tombstone 中提取进程、线程、signal、崩溃地址和 so 信息。
- 知道没有符号表时 native backtrace 很难读。
- 能区分 JNI 使用错误、so ABI 问题和 native 内存问题。

## 第一部分：Native Crash 为什么更难

Java Crash 通常会告诉你：

```text
异常类型
类名
方法名
行号
```

Native Crash 可能只告诉你：

```text
signal 11 (SIGSEGV)
fault addr 0x0
backtrace:
  #00 pc 0000000000012344 /data/app/.../libfoo.so
```

如果没有符号表，你很难知道 `pc 00012344` 对应哪一行 C++。

所以 native 稳定性治理必须保留：

```text
版本
ABI
so 文件
符号表
build id
mapping / symbol 对应关系
```

这和 Java 混淆后的 mapping 很像。

区别是 Java 需要反混淆，native 需要符号化。

## 第二部分：常见 signal

| signal | 常见含义 | 典型原因 |
| --- | --- | --- |
| SIGSEGV | 非法内存访问 | 空指针、野指针、越界访问 |
| SIGABRT | 主动 abort | 断言失败、JNI 检查失败、运行时主动终止 |
| SIGBUS | 非法内存访问对齐或映射问题 | mmap 文件异常、地址对齐问题 |
| SIGILL | 非法指令 | ABI / CPU 指令不兼容、损坏代码 |
| SIGFPE | 算术异常 | 除零、浮点异常 |

看到 signal 不等于看到根因。

signal 只是告诉你“进程如何死的”，不是完整解释“为什么走到那里”。

### signal 与进程退出

Java 异常通常还能以对象形式被运行时捕获、打印和分发。

Native Crash 则更多是底层运行时或内核发现了严重错误：

```text
访问非法地址
执行非法指令
主动 abort
内存映射错误
```

这些错误往往意味着进程已经不能安全继续执行。

因此 native 崩溃的排查重点不是“能不能 catch 住”，而是：

```text
哪个线程触发 signal？
访问了哪个地址？
崩在哪个 so？
调用链从 Java / JNI 哪里进入？
是否有符号表还原 native 函数？
```

这也是 native crash 和普通 Java exception 最大的思维差异。

## 第三部分：tombstone 里看什么

一份 tombstone 通常包含：

```text
pid / tid / process name
signal
fault addr
thread name
registers
backtrace
memory near registers
open files
```

排查时先看：

```text
进程名
线程名
signal
崩溃 so
backtrace 顶部几帧
是否有 Java / JNI 桥接
设备 ABI
App 版本
```

如果崩在：

```text
libart.so
```

不一定代表 ART 有 bug，可能是 JNI 使用错误。

如果崩在：

```text
libfoo.so
```

就要找这个 so 的来源、版本、ABI、符号表和调用路径。

### debuggerd / tombstoned 的角色

Android native 崩溃发生后，系统会有专门的机制收集崩溃现场。

可以粗略理解为：

```text
native 线程触发 fatal signal
  -> 系统崩溃处理链路介入
      -> 收集寄存器、线程、内存、backtrace 等现场
          -> 生成 tombstone
              -> logcat / DropBox / crash 平台获得摘要或文件
```

在现代 Android 中，`debuggerd` 和 `tombstoned` 参与 native 崩溃现场的抓取和保存。

你不需要在入门阶段读完它们的所有源码，但要知道：

```text
tombstone 不是 App 自己随手打印的一段日志
  -> 它是系统为 native fatal signal 保存的事故现场
```

因此 tombstone 的价值很高，尤其是：

- 崩溃线程。
- signal 和 fault address。
- native backtrace。
- BuildId。
- ABI。
- 寄存器现场。

这些信息共同决定 native crash 能不能被定位。

## 第四部分：JNI 常见事故

JNI 是 Java / Kotlin 和 native 世界的桥。

桥很强大，也很容易出事故。

常见问题：

- 传入已经释放或无效的 native 指针。
- `jobject` 引用跨线程或跨生命周期错误使用。
- 本地引用没有释放，导致 local reference table overflow。
- JNI 方法签名不匹配。
- native 回调 Java 时线程没有正确 attach。
- 多线程访问 native 对象没有同步。
- so 加载顺序或 ABI 不匹配。

例如：

```text
Java 对象销毁
  -> native 仍持有指针
      -> 异步线程回调
          -> 访问已释放内存
              -> SIGSEGV
```

这类问题从 Java 代码看可能完全正常，但 native 现场已经损坏。

### JNI Check 失败和 SIGABRT

有些 native 崩溃不是野指针，而是运行时主动终止。

例如 JNI 使用违反规则：

```text
JNI 方法签名不匹配
使用无效 jobject
跨线程错误使用 JNIEnv
local reference 过多
调用 Java 方法时异常未处理
```

在某些情况下，运行时会直接 abort。

这类 tombstone 可能看到 `SIGABRT`，backtrace 里出现 ART / JNI 检查相关调用。

此时不要简单怀疑系统库。

更合理的方向是：

```text
检查 JNI 调用约定
检查线程 attach / detach
检查引用生命周期
检查 Java 方法签名
检查 native 回调 Java 的异常处理
```

## 第五部分：ABI 和 so 冲突

第 18 章讲过 ABI。

第 20 章要把 ABI 放进稳定性现场。

常见问题：

```text
某些设备启动崩溃
  -> 只发生在 arm64-v8a
      -> 某个 so 缺失或版本不匹配

某些功能打开崩溃
  -> 动态加载 so
      -> UnsatisfiedLinkError 或 native crash

多个依赖带同名 so
  -> 打包选择了不兼容版本
      -> 运行时崩溃
```

这类问题需要结合：

- APK Analyzer。
- `lib/` 目录。
- Gradle dependency。
- 设备 ABI。
- `System.loadLibrary` 日志。
- tombstone。

## 第六部分：符号化为什么重要

没有符号化的 backtrace：

```text
#00 pc 0000000000012344 libfoo.so
#01 pc 0000000000013456 libfoo.so
```

符号化后可能变成：

```text
#00 FooDecoder::decodeFrame() FooDecoder.cpp:88
#01 PlayerEngine::render() PlayerEngine.cpp:132
```

这就是从“看见地址”到“看见代码”的差异。

线上 native crash 平台必须保证：

```text
每个版本的 so
  -> 对应 build id
      -> 对应符号表
          -> crash 上报后能自动符号化
```

否则 native crash 只能靠猜。

## 第七部分：Native Crash 修复报告

一份合格报告至少包含：

```text
signal：
process / pid：
thread / tid：
崩溃 so：
ABI：
设备 / 系统版本：
backtrace：
是否已符号化：
Java 调用入口：
native 根因：
修复方案：
回归设备：
是否需要灰度：
```

Native Crash 修复尤其需要关注回归设备。

因为很多问题只在特定 ABI、CPU、系统版本或厂商 ROM 出现。

## 第八部分：一段 tombstone 样例

下面是一段教学用的简化 tombstone：

```text
pid: 24680, tid: 24731, name: ImageDecoder  >>> com.example.course <<<
signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x0
ABI: arm64
backtrace:
  #00 pc 00000000000124a0  /data/app/.../libcourse_image.so (ImageDecoder::decodeFrame+64)
  #01 pc 0000000000011988  /data/app/.../libcourse_image.so (ImagePipeline::decode+120)
  #02 pc 000000000036bb44  /apex/com.android.art/lib64/libart.so (art_quick_generic_jni_trampoline+148)
```

逐行读：

| 线索 | 说明 |
| --- | --- |
| `pid / tid / name` | 崩溃发生在 `ImageDecoder` 线程，不是 main。 |
| `signal 11 (SIGSEGV)` | 非法内存访问。 |
| `fault addr 0x0` | 很像空指针或空地址访问。 |
| `ABI: arm64` | 需要确认该 ABI 下 so 和符号表是否匹配。 |
| `libcourse_image.so` | 第一关注对象是业务或第三方图片 so。 |
| `ImageDecoder::decodeFrame+64` | 已符号化后能看到 native 函数名，定位价值很高。 |
| `art_quick_generic_jni_trampoline` | 说明 Java / JNI 调用进入了 native 链路。 |

这段 tombstone 的结论可以写成：

```text
ImageDecoder 线程在 libcourse_image.so 的 decodeFrame 中发生 SIGSEGV，fault addr 为 0x0，疑似 native 空指针访问。需要结合输入图片、JNI 入口、so 版本、arm64 符号表和最近图片解码改动继续定位。
```

不要写成：

```text
libart 崩了。
```

因为 `libart.so` 出现在 JNI 桥接附近，并不代表 ART 是根因。

## 本节小挑战

### tombstone 阅读题

看到下面信息，你会怎么判断？

```text
signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x0
backtrace:
  #00 pc 0000000000008844 /data/app/.../libimage_filter.so
thread: RenderWorker
```

你需要回答：

- 这是 Java Crash 还是 Native Crash？
- signal 大致说明什么？
- 第一关注的 so 是哪个？
- 为什么需要符号表？
- 还需要补哪些信息？

## 本节实践任务

### 基础任务

- 用 APK Analyzer 查看一个 App 的 `lib/` 目录。
- 记录不同 ABI 下有哪些 so。
- 找一份 tombstone 样例，标出 signal、thread、backtrace。

### 进阶任务

- 学习一次 native crash 符号化流程。
- 整理团队项目依赖的所有 native so 来源。
- 为 native crash 报告增加 ABI、so version、build id 字段。

## 本节小结

Native Crash 的难点不是“没有栈”，而是证据体系不同。

你要从：

```text
signal
  -> tombstone
      -> so
          -> ABI
              -> 符号表
                  -> Java / JNI 调用入口
```

一步步还原现场。

如果 Java Crash 是读异常，Native Crash 就是读事故黑匣子。
