# 24.3 dumpsys：系统服务状态快照与问题定位

如果 logcat 像事件记录，那么 dumpsys 就像系统服务的现场快照。

它回答的问题是：

```text
此刻系统认为这个 App、任务、窗口、权限、内存、通知、后台调度处在什么状态？
```

很多事故不是代码没有执行，而是系统状态不允许它继续执行。

这时你需要 dumpsys。

## 本节先记住三句话

```text
dumpsys 不是日志，而是系统服务状态快照。
不同问题要找不同 service，不要一上来 dumpsys 全部。
dumpsys 输出要和时间点、复现动作、其他证据一起读。
```

## 贯穿案例：后台同步为什么没有跑

`Hello Android 学习中心`晚上应该同步学习进度。

用户第二天发现进度没更新。

你先看业务日志：

```text
CourseSync scheduled jobId=42
```

但没有看到：

```text
CourseSync run begin
```

下一步不要只看代码。

你要问系统：

```text
JobScheduler 里有没有这个任务？
任务约束是否满足？
App 是否处于受限 bucket？
设备是否处于 Doze？
进程当时是否存在？
```

这些答案很多都在 dumpsys 里。

## 现场侦探问题

一个通知不弹，你会先看哪三个 dumpsys？

参考答案：

```bash
adb shell dumpsys package <package>
adb shell cmd appops get <package> POST_NOTIFICATION
adb shell dumpsys notification
```

原因是：

```text
package 看权限声明和授权
appops 看实际放行
notification 看 channel、importance 和系统通知状态
```

## 学习目标

学完本节后，你应该能够：

- 理解 dumpsys 是系统服务 dump 出来的状态快照。
- 知道常见 service 对应的问题类型。
- 能用 dumpsys activity、package、window、jobscheduler、alarm、meminfo、gfxinfo 等命令取证。
- 能避免把 dumpsys 输出当成孤立结论。
- 能把 dumpsys 纳入事故报告。

## 第一部分：dumpsys 的基本用法

查看系统支持哪些服务：

```bash
adb shell dumpsys -l
```

查看某个服务：

```bash
adb shell dumpsys activity
adb shell dumpsys package <package>
adb shell dumpsys window
adb shell dumpsys jobscheduler
adb shell dumpsys alarm
adb shell dumpsys notification
adb shell dumpsys meminfo <package>
adb shell dumpsys gfxinfo <package>
```

大多数服务支持帮助：

```bash
adb shell dumpsys activity -h
adb shell dumpsys package -h
```

实际排查时，建议把输出保存下来：

```bash
adb shell dumpsys package com.helloandroid > package.txt
adb shell dumpsys jobscheduler > jobscheduler.txt
```

证据不要只停在终端里。

## 第二部分：常见问题和 service 对照表

| 问题 | 推荐 dumpsys | 想看什么 |
| --- | --- | --- |
| Activity 启动异常 | activity | task、activity record、launch state |
| 窗口遮挡 / BadToken | window | window token、focus、layer、visibility |
| 权限问题 | package | requested permissions、granted flags |
| 通知不展示 | notification | channel、importance、blocked state |
| 后台任务不跑 | jobscheduler | job state、constraints、standby bucket |
| 定时不准 | alarm | alarm 类型、触发时间、allow while idle |
| 内存压力 | meminfo / procstats | PSS、RSS、进程状态 |
| 掉帧 | gfxinfo | janky frames、framestats |
| 电量异常 | batterystats | wakelock、job、network、uid 消耗 |

dumpsys 的关键不是命令多，而是选择正确服务。

## 第三部分：dumpsys 背后的原理

`dumpsys` 不是魔法命令。

它的核心思路可以理解成：

```text
adb shell dumpsys <service>
  -> shell 进程请求 ServiceManager
      -> 找到对应系统服务的 Binder
          -> 调用服务暴露出来的 dump 入口
              -> 服务把自己当前维护的状态写到输出流
```

也就是说，`dumpsys activity` 看到的是 AMS / ATMS 维护的运行时状态，`dumpsys package` 看到的是 PMS 维护的包和权限状态，`dumpsys jobscheduler` 看到的是 JobSchedulerService 维护的任务调度状态。

这也是为什么 `dumpsys` 特别适合回答：

```text
系统服务现在到底怎么认为这件事？
```

但它也有天然边界：

```text
服务愿意 dump 什么，你才能看到什么。
当前状态是什么，你能看到。
状态怎样一步步变化过来，通常看不到完整过程。
```

所以 `dumpsys` 很适合做“系统口供”，但不能单独当作完整判决。

## 第四部分：如何读 dumpsys package

`dumpsys package <package>` 常用于：

```text
安装状态
targetSdk
versionCode
签名摘要
权限声明
权限授权
组件导出
intent-filter
包可见性
```

例如第 23 章安全事故：

```text
DebugActivity 是否 exported？
是否有 intent-filter？
是否加了 signature permission？
POST_NOTIFICATIONS 是否 granted？
```

这些都可以从 package 状态里拿到第一证据。

一个安全或安装问题，可以这样读：

```text
Package [com.helloandroid]
  userId=10234
  versionCode=24
  targetSdk=35

requested permissions:
  android.permission.POST_NOTIFICATIONS

runtime permissions:
  android.permission.POST_NOTIFICATIONS: granted=false

Activities:
  com.helloandroid.DebugActivity exported=true
```

这段输出可以支撑几个判断：

```text
系统认识这个包。
这个包确实声明了通知权限。
当前用户下通知权限没有授予。
DebugActivity 处于 exported 状态，需要继续检查是否有权限保护。
```

注意，这还不是最终结论。

如果问题是“授权了也不可用”，还要继续看 AppOps。

## 第五部分：如何读 dumpsys activity

`dumpsys activity` 常用于：

```text
当前前台 Activity
任务栈
进程状态
Service 运行状态
广播队列
后台限制线索
```

常见观察：

```bash
adb shell dumpsys activity activities
adb shell dumpsys activity processes
adb shell dumpsys activity services
adb shell dumpsys activity broadcasts
```

如果用户说“返回栈乱了”，你不能只看代码里的 Navigation。

你还要看系统里的 task 和 activity record。

例如：

```text
ACTIVITY MANAGER ACTIVITIES
  Stack #1:
    Task{42 #42 type=standard A=10234:com.helloandroid}
      Hist #0: ActivityRecord{... com.helloandroid/.CourseDetailActivity}
        state=RESUMED
        frontOfTask=true
```

这段证据能说明：

```text
系统当前前台页面是 CourseDetailActivity。
它处于 RESUMED 状态。
如果用户仍然看到白屏，问题更可能在窗口绘制、数据加载或渲染链路，而不是 Activity 没启动。
```

## 第六部分：如何读 jobscheduler 和 alarm

后台任务问题经常需要：

```bash
adb shell dumpsys jobscheduler
adb shell dumpsys alarm
adb shell dumpsys deviceidle
adb shell am get-standby-bucket <package>
```

重点不是找到任务名字，而是看：

```text
任务是否存在
约束是否满足
是否 ready
是否被 standby / doze 延迟
最近一次运行和失败原因
```

可以把输出读成一张调度问诊表：

```text
JOB #u0a234/12: com.helloandroid/.SyncWorker
  Required constraints: CONNECTIVITY CHARGING
  Satisfied constraints: CONNECTIVITY
  Unsatisfied constraints: CHARGING
  Ready: false
  Standby bucket: RARE
```

这里的结论不是“WorkManager 坏了”，而是：

```text
任务存在。
网络约束满足。
充电约束不满足。
任务当前不 ready。
App 还处在更受限制的 standby bucket。
```

这才是能写进报告里的证据。

第 21 章学的是后台机制。

第 24 章学的是怎么证明后台机制正在影响你的任务。

## 第七部分：dumpsys 的局限

dumpsys 是快照，不是完整电影。

它擅长回答：

```text
当前状态是什么？
系统服务记录了什么？
某个对象是否存在？
某个开关是否打开？
```

它不擅长回答：

```text
这个状态是如何一步步变成这样的？
线程在哪些时间运行？
一帧里谁耗时最长？
Binder 等待持续多久？
```

这些问题要交给 Perfetto 或 profiler。

## 本节自测

- dumpsys 和 logcat 最大区别是什么？
- 后台同步不跑时，为什么要看 jobscheduler / alarm / deviceidle？
- 为什么 dumpsys 输出不能脱离复现时间点？
- 权限问题为什么常常要同时看 package 和 appops？

## 本节总结

dumpsys 是系统服务状态快照。

当你不知道系统是否“认得这个任务、这个权限、这个窗口、这个进程”时，先问 dumpsys。

但请记住：

```text
dumpsys 给你状态。
Perfetto 给你时间线。
bugreport 给你完整现场。
```

它们组合起来，才是一条能站住的证据链。
