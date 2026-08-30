# 23.7 安全体验问题：权限拒绝、组件暴露、日志泄露与合规风险

前面几节讲的是安全机制。

这一节进入真实事故。

安全问题很少以“我不懂 Android 安全模型”的形式出现。

它通常这样出现：

```text
用户说已经授权，功能还是不能用。
测试说通知权限开了，但通知不弹。
安全扫描说 Activity exported 有风险。
线上日志里出现 token。
测试包覆盖不了线上包。
导出的诊断报告包含手机号。
第三方 SDK 申请了一堆权限。
```

这些问题看起来很杂，但都可以按同一条链路排查：

```text
身份
  -> 边界
      -> 授权
          -> 实际操作
              -> 数据保护
                  -> 证据复盘
```

## 本节先记住三句话

```text
安全事故不要先猜权限，要先画入口和数据流。
所有能进入 App、带入参数、触发动作、带出数据的地方，都是安全入口。
好的修复不仅要关门，还要留下回归证据。
```

## 贯穿案例：内部调试面板为什么被外部打开

`Hello Android 学习中心`有一个内部调试页面：

```text
DebugActivity
```

测试发现另一个 App 可以通过隐式 Intent 拉起它。

这时你要问：

```text
DebugActivity 是否 exported=true？
是否配置了 intent-filter？
是否要求 permission？
permission 是否是 signature 级别？
是否校验调用方 uid / package / signature？
进入页面后是否能触发敏感动作？
```

本节要让你形成一个意识：组件不是普通页面，它也是外部世界进入 App 的门。

## 安全侦探问题

安全扫描只给你一句话：

```text
存在 exported Activity 风险。
```

你会继续追问哪四件事？

提示：

```text
谁能进入？
能带什么参数？
能触发什么动作？
会不会带出数据？
```

## 本节定位

本节负责回答：

- 安全事故如何建立证据链？
- 权限拒绝、AppOps 拦截和用户设置关闭如何区分？
- exported 组件为什么可能变成入口风险？
- 日志和导出文件为什么会泄露数据？
- 如何把安全问题写成可复盘报告？

## 学习目标

学完本节后，你应该能够：

- 用“身份 -> 权限 -> AppOps -> 组件边界 -> 数据保护”的方式排查安全问题。
- 区分权限未授权、AppOps 拦截、渠道配置、组件误暴露和数据泄露。
- 能设计 exported 组件、Provider、FileProvider 的基本安全检查。
- 能为日志、导出、分享设计脱敏和用户确认。
- 建立安全事故复盘模板。

## 本节阅读导航

这一节不是按 API 分类，而是按事故现场来读：

```text
先画入口地图
  -> 谁能进来，能带什么参数，能触发什么动作

再查授权链路
  -> permission、AppOps、用户设置、前后台状态是否一致

再查数据出口
  -> 日志、FileProvider、SAF、崩溃报告是否带出敏感信息

最后写复盘报告
  -> 第一证据、根因、修复、回归和监控都要落下
```

读完这一节，不要满足于一句“安全扫描发现风险”。你应该能继续追问：

```text
风险入口在哪里？
调用方是谁？
系统哪一层放行了？
敏感数据是否被带出？
修复后用什么命令证明？
```

## 第一部分：安全事故六步排查法

遇到安全问题，不要先猜权限。

先按六步走：

```text
第一步：确认调用方身份
  -> packageName、uid、签名、进程、安装来源

第二步：确认访问对象
  -> 系统能力、用户数据、组件入口、Uri、密钥、日志

第三步：确认声明和授权
  -> Manifest、runtime permission、Uri grant、signature permission

第四步：确认实际放行
  -> AppOps、前后台状态、targetSdk、系统设置、渠道策略

第五步：确认组件边界
  -> exported、intent-filter、permission、Provider authority、FileProvider paths

第六步：确认数据保护
  -> 加密、备份、日志脱敏、导出提示、删除和撤销
```

这套顺序可以避免把所有问题都粗暴归因成：

```text
权限问题。
```

## 第二部分：事故一：权限授权了，功能仍不可用

现象：

```text
用户说已经授权照片或通知。
页面仍然提示不可用。
```

排查：

```text
Manifest 是否声明？
runtime permission 是否 granted？
AppOps mode 是 allow、ignore 还是 foreground？
用户是否只授权部分照片？
通知渠道是否关闭？
targetSdk 是否触发新规则？
```

修复方向：

```text
权限状态展示要更精确
区分未授权、部分授权、系统设置关闭和渠道关闭
提供重新选择、设置页引导或降级入口
```

不要写：

```text
请开启权限
```

更好的是：

```text
你只选择了部分照片，可继续选择更多照片，或使用当前已选择照片。
```

### 事故一补充：前台定位可用，后台定位失败

还有一种很容易误判的事故：

```text
用户打开学习打卡页时可以定位。
锁屏一段时间后，后台签到失败。
```

这时不要只看：

```text
ACCESS_FINE_LOCATION 是否 granted。
```

因为前台定位和后台定位不是同一个判断场景。

继续拆：

```text
ACCESS_FINE_LOCATION 是否授权？
ACCESS_BACKGROUND_LOCATION 是否需要且已授权？
AppOps FINE_LOCATION 是否是 foreground？
调用发生时 App 是否真的在前台？
后台任务是否被系统限制？
产品是否真的需要静默后台定位？
```

`foreground` mode 的意思不是“永远允许”，而是：

```text
前台可以
后台重新判断
```

所以修复方向通常不是偷偷绕过系统，而是：

```text
把定位动作前台化
提供用户可理解的打卡入口
降低后台定位依赖
必要时申请后台定位并给出清晰理由
失败时保留可回归证据
```

## 第三部分：事故二：组件误暴露

现象：

```text
安全扫描提示 Activity / Service / Receiver exported 风险。
```

第一判断：

```text
这个组件是否真的需要被外部 App 调用？
```

排查：

```text
android:exported 是否为 true？
是否有 intent-filter？
是否设置 permission？
是否校验调用来源？
是否处理恶意参数？
是否返回敏感数据？
```

风险：

```text
外部 App 启动内部页面
伪造 Intent 参数
触发敏感操作
读取 Provider 数据
滥用 Service
```

修复方向：

```text
默认 exported=false
确实需要外部访问时设置明确 permission
校验输入参数
最小化返回数据
敏感操作要求用户确认
```

组件不是“只是一个入口”，它是安全边界上的门。

## 第四部分：事故三：FileProvider 暴露范围过大

现象：

```text
分享日志功能使用 FileProvider。
安全扫描提示 paths 配置过宽。
```

排查：

```text
file_paths.xml 是否暴露整个 filesDir？
分享目录里是否可能有 token、数据库、授权文件？
是否只分享脱敏后的临时文件？
Intent 是否只授予临时读权限？
MIME_TYPE 是否正确？
```

修复方向：

```text
把可分享文件放进单独 share/ 子目录
FileProvider paths 只暴露 share/
分享前生成脱敏副本
分享后定期清理
不要暴露数据库、token、原始日志
```

第 22 章教你用 FileProvider，第 23 章提醒你：

```text
FileProvider 也要最小暴露。
```

## 第五部分：事故四：日志泄露敏感数据

现象：

```text
用户上传诊断日志后，里面包含 token、手机号或完整 Uri。
```

排查：

```text
日志采集点在哪里？
debug 日志是否进了 release？
脱敏规则是否覆盖请求、响应、异常、Uri、文件路径？
导出前是否让用户确认？
日志是否有过期清理？
```

修复方向：

```text
建立统一日志网关
敏感字段默认脱敏
release 降低日志级别
导出前二次脱敏
用户确认后再分享
日志文件定期清理
```

日志是证据，不该变成泄露源。

## 第六部分：事故五：签名或渠道异常

现象：

```text
测试包无法覆盖线上包。
用户安装的包签名和官方不一致。
某渠道包多了未知权限。
```

排查：

```text
packageName 是否一致？
签名证书指纹是否匹配？
versionCode 是否递增？
构建变体是否正确？
渠道是否可信？
是否被二次打包？
第三方 SDK 是否引入额外权限？
```

修复方向：

```text
发布前校验签名和 versionCode
保留构建产物和 commit 对应关系
限制 release keystore 权限
审查渠道包差异
对第三方 SDK 做权限和组件审计
```

## 第七部分：安全事故报告模板

建议报告格式：

```text
问题标题：
用户现象：
Android 版本：
targetSdk：
设备 / 厂商：
packageName：
uid：
processName：
签名证书指纹：
安装来源：
访问对象：
Manifest 声明：
runtime permission：
AppOps：
前后台状态：
组件名：
exported：
intent-filter：
Provider authority：
Uri：
Uri grant：
FileProvider paths：
是否含敏感数据：
是否加密：
是否备份：
是否日志脱敏：
是否用户确认导出：
第一证据：
系统证据：
根因判断：
修复方案：
回归用例：
监控指标：
```

这份报告要回答的不是：

```text
用户有没有点同意。
```

而是：

```text
这个调用方在这个系统状态下，凭什么能或不能访问这份能力和数据？
```

## 第七部分补充：安全事故要从“入口地图”开始

安全事故排查不要从猜测开始，要先画入口地图。

对一个 App 来说，常见入口包括：

```text
Activity
Service
BroadcastReceiver
ContentProvider
FileProvider
deep link
notification pending intent
shared Uri
backup / restore data
exported file
log upload
third-party SDK callback
```

每个入口都要问四个问题：

```text
谁能进来？
进来后能带什么参数？
进来后能触发什么动作？
动作结果会不会返回或泄露敏感数据？
```

### exported 不是唯一条件

`exported=true` 很重要，但它不是唯一风险。

你还要继续看：

```text
是否有 intent-filter
是否能被 deep link 匹配
是否要求 permission
permission 是否是 dangerous / signature
是否校验 calling uid / package / signature
是否处理恶意参数
是否有用户确认
是否输出敏感结果
```

比如一个 Activity 即使只是展示页面，如果它能接收外部传入的文件 Uri、课程 id 或调试开关，也可能变成风险入口。

### Provider 要看 authority、path 和授权粒度

ContentProvider / FileProvider 的风险不只在 exported。

还要看：

```text
authority 是否可预测
path 是否过宽
是否允许 grantUriPermissions
是否只授予临时权限
是否区分 read / write
是否校验调用方
是否返回了超出业务需要的数据
```

尤其是 FileProvider：

```text
files-path path="."
```

这类配置看起来省事，本质上是在把整个私有文件柜挂到分享入口后面。

正确做法是：

```text
只暴露 share/ 或 export/ 子目录
分享前生成脱敏副本
分享后清理临时文件
Intent 只加需要的 FLAG_GRANT_READ_URI_PERMISSION
```

### PendingIntent 也要看身份和可变性

通知、桌面小组件、系统回调常常会用 PendingIntent。

它的特殊之处在于：

```text
别人触发这个 PendingIntent 时，可能以创建者预先授权的身份执行某些动作。
```

所以要注意：

```text
是否使用 immutable / mutable
是否允许外部修改 Intent 内容
是否包含敏感 extras
是否指向 exported 组件
是否触发敏感操作前要求用户确认
```

当前 `examples/23-security-permission-lab/` 已经把这部分做成 `PendingIntent 授权令牌实验`：你可以对比显式 immutable、mutable 和隐式入口，观察一个未来动作为什么也需要边界判断。

安全事故不是只有权限弹窗，所有能进入 App、带入参数、触发动作、带出数据的地方，都是入口。

## 第七部分补充二：组件入口是如何被系统解析和授权的

组件安全也可以按系统链路拆。

### Activity / Service 的显式和隐式入口

显式 Intent 大致是：

```text
调用方指定 package / component
  -> 系统查目标组件是否存在
      -> 检查目标组件 exported
          -> 检查组件声明的 permission
              -> 检查调用方是否满足权限
                  -> 启动或拒绝
```

隐式 Intent 多一步匹配：

```text
调用方发出 action / data / category
  -> IntentResolver 匹配 intent-filter
      -> 找到候选组件
          -> 继续检查 exported 和 permission
              -> 交给 ActivityTaskManagerService / ActivityManagerService 调度
```

所以 `intent-filter` 不是普通配置。

它经常意味着：

```text
这个组件可能被外部世界发现。
```

如果这个组件只给 App 内部使用，通常就不应该挂公开的 filter。

### Receiver 的入口风险

BroadcastReceiver 要看：

```text
静态注册还是动态注册
是否 exported
接收的 action 是否公开
是否要求发送方权限
是否校验 Intent 内容
是否触发敏感操作
```

风险常见于：

```text
外部 App 伪造广播
  -> 触发同步、删除、上传、调试开关
```

修复方向：

```text
内部广播改成显式或应用内机制
需要外部广播时增加 permission
敏感动作要求用户确认或服务端二次校验
```

### Provider 和 Uri grant 的判断链

ContentProvider / FileProvider 更像“数据入口”。

粗略链路是：

```text
调用方拿到 content Uri
  -> 根据 authority 找到 Provider
      -> 检查 Provider exported / grantUriPermissions
          -> 检查 readPermission / writePermission
              -> 检查临时 Uri grant
                  -> Provider 内部再校验 path、参数和返回列
```

Uri grant 的意义是：

```text
不给对方整个 Provider 权限
只给某个 Uri 的临时 read / write 能力
```

所以安全设计要尽量把授权粒度压到：

```text
某个文件
某次分享
某个方向：read 或 write
某段生命周期：Activity 栈、任务或持久授权
```

这也是为什么 FileProvider paths 要收窄。

系统可以帮你做 Uri 授权，但不能替你判断：

```text
这个目录里到底有没有不该分享的文件。
```

### PendingIntent 的真实身份

PendingIntent 容易被低估。

它可以理解为：

```text
一个由创建方交给系统保存的未来动作令牌
```

触发时，系统会按创建方预先定义的身份和 Intent 去执行。

所以安全检查要问：

```text
这个 PendingIntent 是谁创建的？
是否 mutable？
外部能不能改 extras / data / action？
目标组件是否 exported？
触发后是否直接执行敏感动作？
```

经验规则：

```text
不需要外部修改就用 immutable
需要 mutable 时尽量限制可修改字段
敏感动作不要只靠 PendingIntent 触发，进入页面后再确认
```

组件边界的本质不是“有没有 exported”，而是：

```text
谁能找到入口？
谁能进入入口？
谁能带参数？
谁能触发动作？
谁能拿到结果？
```

## 第八部分：本节自测

请回答：

- 安全事故为什么不能只看 runtime permission？
- exported 组件为什么可能变成攻击入口？
- FileProvider paths 为什么不能配置得太宽？
- 日志脱敏应该覆盖哪些环节？
- 签名不一致为什么是安装和供应链问题？
- 安全事故报告为什么要记录 uid、签名和 AppOps？
- 安全入口地图应该至少包含哪些入口？
- 为什么 PendingIntent 也可能成为安全边界的一部分？
- 隐式 Intent 为什么会让 intent-filter 成为外部可发现入口？
- Uri grant 和 Provider exported 的区别是什么？
- PendingIntent mutable 为什么需要谨慎使用？

## 本节小结

安全体验问题不是单点 bug。

```text
身份错
  -> 可能是包、签名、安装来源问题

授权错
  -> 可能是 permission、AppOps、Uri grant 问题

边界错
  -> 可能是 exported、Provider、FileProvider 问题

保护错
  -> 可能是日志、导出、备份和加密问题
```

第 23.7 节的关键结论是：

```text
安全问题要用证据链排查，不能用“用户应该已经授权了”来猜。
```
