# 24.4 bugreport、DropBox、ANR trace 与 tombstone：完整事故包怎么读

logcat 和 dumpsys 适合快速取证。

但遇到复杂事故，单独几条命令往往不够。

这时你需要 bugreport。

bugreport 的价值是：

```text
把设备在某个时刻的大量系统现场打包保存下来。
```

它里面可能包含：

```text
logcat
dumpsys
dumpstate
ANR trace
tombstone
DropBox 线索
系统属性
电量、内存、进程、包、窗口、任务等状态
```

## 本节先记住三句话

```text
bugreport 是事故现场包，不是单一日志文件。
拿 bugreport 前要记录复现步骤和时间点，否则很难读。
bugreport 可能包含隐私和敏感信息，分享前必须控制范围。
```

## 贯穿案例：线上偶发 ANR 无法复现

用户说：

```text
昨晚 22:13 左右，打开课程详情页后 App 卡住。
```

你本地复现不了。

这时单看代码价值有限。

你需要：

```text
用户操作时间
发生问题后的 bugreport
ANR traces
logcat 前后窗口
系统内存和进程状态
可能的 tombstone 或 DropBox 记录
```

如果只有一句“偶发 ANR”，那不是证据，是求救信号。

## 现场侦探问题

bugreport 很大，你会先搜什么？

建议顺序：

```text
包名
时间点
ANR
FATAL EXCEPTION
tombstone
DropBox
Input dispatching timed out
am_anr
```

先把事故窗口圈出来，再深入每个模块。

## 学习目标

学完本节后，你应该能够：

- 知道 bugreport 适合保存复杂事故现场。
- 能用 `adb bugreport` 采集报告。
- 能理解 bugreport 里 logcat、dumpsys、dumpstate 的关系。
- 能把 ANR trace、tombstone、DropBox 放入同一份事故分析。
- 知道 bugreport 的隐私风险和分享边界。

## 第一部分：如何采集 bugreport

命令行采集：

```bash
adb bugreport bugreports/
```

如果连接多个设备，先确认设备：

```bash
adb devices
adb -s <serial> bugreport bugreports/
```

也可以从设备开发者选项里选择：

```text
Take bug report
```

模拟器也可以从扩展控制面板导出 bug report。

## 第二部分：bugreport 里有什么

bugreport 通常是一个 zip。

里面最重要的是：

```text
bugreport-*.txt
```

这个文本里会包含大量分段，例如：

```text
SYSTEM LOG
EVENT LOG
RADIO LOG
DUMPSYS
DUMPSTATE
ANR
TOMBSTONES
SYSTEM PROPERTIES
```

不同 Android 版本和厂商会有差异。

不要背固定行号，要学会用关键字、包名和时间点搜索。

## 第三部分：bugreport 是怎么被拼出来的

bugreport 可以理解成一次系统级现场采集。

它不是某一个服务单独生成的文件，而是由系统工具把多类信息收集到一起：

```text
dumpstate
  -> 采集系统属性、内核状态、进程信息、文件系统状态

logcat
  -> 采集 main / system / events / crash / radio 等日志 buffer

dumpsys
  -> 请求各系统服务 dump 当前状态

DropBox / traces / tombstones
  -> 收集重要异常事件和崩溃现场
```

因此，bugreport 的正确读法不是从第一行读到最后一行。

更像是在一个事故档案库里检索：

```text
先搜包名
再搜时间点
再搜 ANR / crash / tombstone / dropbox
再搜相关 service 的 dumpsys 段落
最后把线索按时间顺序重新排列
```

这也是为什么没有时间点的 bugreport 很难读：档案很多，但你不知道案发现场在哪个抽屉。

## 第四部分：ANR trace 怎么放进证据链

ANR 证据通常要看：

```text
发生时间
ANR 类型
主线程堆栈
Binder 线程状态
锁等待
CPU / I/O / 内存压力
系统服务是否卡住
```

常见关键词：

```text
Input dispatching timed out
Broadcast of Intent
executing service
ContentProvider not responding
```

不要看到主线程堆栈就立刻下结论。

如果主线程停在：

```text
BinderProxy.transact
```

你还要继续看：

```text
远端是谁？
system_server 是否忙？
Binder 线程池是否被占满？
Perfetto 里有没有 Binder wait？
```

第 20 章讲 ANR 原理，第 24 章讲如何把 ANR 证据和其他工具拼起来。

可以把 ANR trace 摘成这样的报告语言：

```text
发生时间：2026-08-30 21:16:04
ANR 类型：Input dispatching timed out
主线程：停在 BinderProxy.transact
Binder 线索：等待 system_server 返回
下一步证据：查看 Perfetto 中同一时间段 system_server 线程状态
```

这样写，比只贴一段堆栈更有价值。

## 第五部分：tombstone 怎么读

tombstone 主要用于 Native Crash。

重点看：

```text
signal
fault address
pid / tid
thread name
backtrace
memory map
ABI
so 名称
```

常见判断：

```text
SIGSEGV
  -> 空指针、野指针、越界访问

SIGABRT
  -> abort、assert、JNI 检查失败

UnsatisfiedLinkError
  -> 不一定生成 tombstone，更多是加载失败
```

如果没有符号表，tombstone 只能给你地址和 so 名称。

真正定位还需要：

```text
符号化
构建产物
mapping / native symbols
版本号和 ABI
```

一个 tombstone 可以先读成三层：

```text
第一层：谁崩了
  -> package、pid、tid、thread name、ABI

第二层：怎么崩的
  -> signal、fault address、abort message

第三层：崩在哪里
  -> backtrace、so 名称、函数名或地址、memory map
```

如果 backtrace 只有地址，没有函数名，报告里要明确写：

```text
当前证据只能定位到 so 和地址范围。
需要对应版本的 native symbols 才能继续符号化。
```

## 第六部分：DropBox 是什么

Android 系统会把一些重要事件放入 DropBox。

它可以帮助你找到：

```text
system_server crash
ANR
watchdog
tombstone
低内存或系统异常线索
```

常见命令：

```bash
adb shell dumpsys dropbox
```

DropBox 不是业务埋点系统。

它更像系统保存的事故摘要入口。

## 第七部分：bugreport 的隐私和协作边界

bugreport 可能包含：

```text
包名
设备信息
网络信息
路径
日志
部分用户操作线索
崩溃堆栈
厂商信息
```

分享前要确认：

```text
是否需要脱敏
是否只分享相关片段
是否能上传到外部平台
是否符合团队和公司安全要求
```

第 23 章讲日志脱敏，第 24 章要把 bugreport 也纳入这个意识。

## 本节自测

- bugreport 为什么比单独 logcat 更适合复杂事故？
- 读 bugreport 为什么要先找时间点和包名？
- ANR trace 里看到主线程 Binder 调用，下一步该看什么？
- tombstone 为什么通常还需要符号表？

## 本节总结

bugreport 是完整事故包。

它不是让你从头读到尾，而是让你按线索搜索：

```text
时间点
  -> 包名
      -> logcat
          -> dumpsys
              -> ANR / tombstone / DropBox
                  -> 根因候选
                      -> 其他工具验证
```

会读 bugreport，意味着你已经开始具备真实系统事故分析能力。
