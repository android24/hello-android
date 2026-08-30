# 第23章示例工程：安全模型观察实验室

这个工程对应课程第 23 章：Android 安全模型、权限、签名、AppOps 与数据保护。

它不是一个“权限弹窗 demo”，而是一套安全判断可视化实验室。你可以观察 App 的 packageName、uid、pid、targetSdk、签名指纹、权限声明、runtime 授权、AppOps 命令、FileProvider 边界、Keystore 密钥生命周期、日志脱敏和安全事故剧本，并把一次安全问题写成可复盘的诊断报告。

## 学习目标

运行本工程后，你应该能回答：

- 为什么 App 的安全身份不只是 packageName，还包括 uid、签名和当前用户空间？
- Manifest 声明、runtime permission、AppOps 和系统设置分别解决什么问题？
- 为什么 `checkSelfPermission()` 返回 granted 后，通知、定位或媒体访问仍可能不可用？
- 签名校验为什么同时包含 APK 完整性校验和升级身份校验？
- `signature permission` 为什么信任的是签名身份，而不是权限字符串本身？
- Keystore 为什么保护密钥，而不是用来保存所有明文数据？
- 备份恢复后为什么可能出现“密文还在，key 不在”？
- FileProvider 为什么要收窄 paths，并只分享脱敏后的临时文件？
- exported、intent-filter、Uri grant、PendingIntent 为什么都是安全入口的一部分？
- 一个安全事故如何从现象走向系统模块、第一证据、根因、修复和回归？

## 工程结构

```text
23-security-permission-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/res/xml/file_paths.xml
    src/main/res/xml/backup_rules.xml
    src/main/res/xml/data_extraction_rules.xml
    src/main/java/com/helloandroid/security/
      MainActivity.kt
      SecurityLabApplication.kt
      SecurityLabScreen.kt
      SecurityLabState.kt
      SecurityLabStore.kt
      DebugPanelActivity.kt
      SafeInternalActivity.kt
  quality/
    security-diagnosis-report-template.md
```

## 运行方式

用 Android Studio 打开本目录：

```text
examples/23-security-permission-lab/
```

等待 Gradle Sync 完成后，运行 `app`。

如果你习惯命令行，并且本机有 Gradle Wrapper 或全局 Gradle，也可以执行：

```bash
./gradlew :app:assembleDebug
```

当前仓库环境没有统一的根 Gradle Wrapper，建议优先用 Android Studio 打开示例工程。

## 实验区域

### 任务板

页面顶部把本章拆成 16 个观察点：

```text
运行身份
沙箱路径
权限声明
runtime 权限
AppOps
签名
签名实测
Keystore
FileProvider
PendingIntent
Photo Picker
日志脱敏
事故剧本
诊断答题
自由报告
诊断报告
```

不要一上来乱点按钮。先看任务板，再做第一个未完成的观察点。

任务板会显示：

```text
已完成 x / 16
当前建议：下一步要做的观察点
```

每个关键实验区标题右侧也会标记 `待完成 / 已完成`，帮助你在长页面里知道自己卡在哪一层。

### 推荐剧情模式

第一次运行时，可以按一条安全事故线走完：

```text
选择事故剧本
  -> 刷新运行身份
      -> 检查权限状态
          -> 判断 AppOps mode
              -> 观察 PendingIntent、Photo Picker、FileProvider 边界
                  -> 运行 Keystore 和日志脱敏实验
                      -> 提交诊断答题
                          -> 导出安全诊断报告
```

这样玩一遍，读者会比单独记 API 更容易形成排查直觉。

页面底部的诊断报告默认收起。完成答题后，再展开报告或通过 SAF 导出，这样阅读节奏会更轻。

### 身份与沙箱实验

工程会展示：

```text
packageName
uid / pid
processName
targetSdk
buildProfile
installer
dataDir
filesDir
cacheDir
signature SHA-256
```

它用来说明：

```text
App 身份不是路径，而是 uid、包状态、签名和系统服务共同确认的结果。
```

推荐观察：

```bash
adb shell dumpsys package com.helloandroid.security
adb shell ps -A | grep com.helloandroid.security
adb shell run-as com.helloandroid.security ls files
```

### 权限状态实验

页面会展示相机、通知、定位、照片媒体等权限的：

```text
Manifest 是否声明
runtime 是否 granted
推荐 AppOps 命令
事故提示
```

可以点击：

```text
请求相机权限
请求通知权限
```

然后刷新运行现场。

这一块用来说明：

```text
Manifest 声明不等于用户授权。
runtime granted 不等于最终可用。
```

### AppOps 裁决实验

页面会让你选择：

```text
op：POST_NOTIFICATION / CAMERA / FINE_LOCATION / READ_MEDIA_IMAGES
mode：allow / ignore / foreground / deny / default
```

每次选择后，Demo 会生成一条判断结论和取证命令：

```bash
adb shell cmd appops get com.helloandroid.security
adb shell cmd appops get com.helloandroid.security POST_NOTIFICATION
adb shell cmd appops get com.helloandroid.security CAMERA
adb shell cmd appops get com.helloandroid.security FINE_LOCATION
```

你也可以把命令输出粘贴回 AppOps 实验区，Demo 会尝试解析：

```text
POST_NOTIFICATION: ignore
CAMERA: foreground
allow
ignore
foreground
deny
default
```

这样就形成了更完整的链路：

```text
页面生成命令
  -> adb 取证
      -> 粘贴输出
          -> Demo 解析 mode
              -> 得到裁决解释
```

App 内不强行读取所有 AppOps 状态，而是引导你用系统命令取证。

这是更接近真实工作的方式：

```text
App 内观察
  -> 系统命令取证
      -> 回到 App 写诊断结论
```

请特别观察：

```text
allow 代表系统服务通常继续执行。
ignore 常常表现为静默失败或空结果。
foreground 代表前台可用，后台仍可能被拦。
default 不是“无权限”，而是回到系统默认策略。
```

### 相册访问与 Photo Picker 实验

点击“打开 Photo Picker”后，选择一张图片。

你会看到一个 `content://` 形式的 Uri。这个实验用来说明：

```text
Photo Picker
  -> 用户主动挑选某个媒体项
  -> App 不需要拿到整个相册读取权限

READ_MEDIA_IMAGES / 部分照片授权
  -> 适合更持续的媒体访问
  -> 要继续考虑 Android 版本、授权范围和用户选择
```

不要把 `content://` 当成文件路径。它背后是 Provider、Uri grant 和系统授权生命周期。

### 签名升级身份实验

工程会展示当前安装包签名证书 SHA-256。

推荐用 debug / release 两个构建产物继续验证：

```bash
apksigner verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

它用来说明：

```text
签名有效
  -> 证明这个 APK 内容和签名匹配

可信升级
  -> 还要和已安装 App 的签名身份或 signing lineage 匹配
```

所以测试包覆盖不了线上包时，不要只说“安装失败”，要继续看包名、证书指纹、versionCode、variant 和渠道。

Demo 里还提供三个升级场景：

```text
同签名升级
签名轮换 lineage
包名相同但签名不同
```

你要训练的是这句判断：

```text
APK 自己签得对，只能说明它内容完整；能不能覆盖安装，还要看它和已安装 App 的签名身份是否连续。
```

你还可以把 debug / release 两个 APK 的 `apksigner` 输出粘贴到页面中。Demo 会解析证书 SHA-256，并给出对比结论：

```text
两个证书相同
  -> 可以继续检查 versionCode、渠道和安装来源

两个证书不同
  -> 即使 packageName 相同，也不是可信升级关系
```

### PendingIntent 授权令牌实验

页面提供三个选择：

```text
显式 + Immutable
Mutable
隐式入口
```

它们分别对应：

```text
显式 + Immutable
  -> 推荐默认姿势：目标明确，授权内容不被接收方改写

Mutable
  -> 只有 RemoteInput 等明确需要接收方补充内容时才使用

隐式入口
  -> 匹配面更大，要小心 action、data、extras 和目标组件
```

这块的核心不是记住 flag，而是理解：

```text
PendingIntent 不是普通 Intent，而是一张带着创建方身份的授权令牌。
```

### Keystore 密钥生命周期实验

你可以依次点击：

```text
生成密钥并加密
解密 token
删除 key 后解密
```

第三步会模拟：

```text
密文还在
  -> key 不在
      -> 解密失败
```

这对应备份恢复、设备安全状态变化、密钥失效等真实事故。

你需要观察：

```text
alias
cipherText
decryptedText
status
lastError
```

### 日志脱敏与 FileProvider 边界

页面内置一条危险日志：

```text
Authorization=Bearer ...
phone=13800138000
uri=content://...
```

点击“生成脱敏日志”后，会输出安全版本。

点击“创建并分享报告”后，工程会：

```text
在 filesDir/share/ 下生成报告
通过 FileProvider 转成 content Uri
添加 FLAG_GRANT_READ_URI_PERMISSION
打开系统分享面板
```

诊断报告区还提供 SAF 导出：

```text
点击“导出报告到用户选择的位置”
  -> 系统文件创建器出现
      -> 用户选择保存位置
          -> App 只向这个 Uri 写入报告
```

对应配置：

```xml
<files-path
    name="safe_share"
    path="share/" />
```

它用来说明：FileProvider 是安全入口，但 paths 配置过宽仍然会扩大暴露面。

FileProvider 和 SAF 的侧重点不同：

```text
FileProvider
  -> App 把自己的临时文件授权给别人读

SAF
  -> 用户明确选择一个文档位置，让 App 写入或读取这个 Uri
```

### 组件边界实验

Manifest 中包含两个入口：

```text
DebugPanelActivity
  -> exported=true
  -> intent-filter
  -> signature permission

SafeInternalActivity
  -> exported=false
  -> 内部 action：com.helloandroid.security.OPEN_INTERNAL
```

推荐观察：

```bash
adb shell dumpsys package com.helloandroid.security | grep -A 20 DebugPanelActivity
adb shell am start -a com.helloandroid.security.DEBUG_PANEL
```

这个实验用来说明：

```text
exported、intent-filter、permission 和调用方签名要一起看。
```

其中 `SafeInternalActivity` 故意保留了一个 action，但仍然 `exported=false`。这能帮助你区分两件事：

```text
intent-filter 负责“能不能匹配到”
exported / permission / PendingIntent 负责“外部能不能真的进来”
```

### 安全事故剧本

内置 6 个事故：

```text
剧本 A：学习提醒不弹
剧本 B：拍照上传失败
剧本 C：换机后 token 解不开
剧本 D：调试页被外部拉起
剧本 E：分享报告暴露过宽
剧本 F：前台定位可用，后台定位失败
```

每个剧本都包含：

```text
用户现象
第一证据
隐藏陷阱
错误直觉
正确诊断
下一步动作
```

点击“选择这个案子”后，任务板会点亮事故剧本。不要只写“权限问题”，要写出系统在哪一层允许或拒绝，以及第一证据应该去哪里拿。

### 安全诊断答题区

选择事故剧本后，进入答题区完成三段判断：

```text
问题类型
第一证据
修复动作
```

三个选项都选完后才能提交。提交后，Demo 会给出 `0/3` 到 `3/3` 的反馈。

选择题下面还有一个自由文本诊断报告输入框。建议至少写出四句话：

```text
现象是什么？
第一证据是什么？
根因是什么？
修复和回归怎么做？
```

Demo 会按 `0/4` 到 `4/4` 给出轻量反馈。这个不是为了替代老师批改，而是训练你把系统判断说完整。

这块是精品 Demo 的关键，因为它把“看懂答案”变成了“自己做一次判断”：

```text
用户现象
  -> 选择问题类型
      -> 找第一证据
          -> 给修复动作
              -> 对照标准诊断
                  -> 写入诊断报告
```

如果只拿到 0 或 1 分，不要急着改答案。先回到事故剧本，重新看“隐藏陷阱”和“第一证据”。

## 通关标准

完成本 Demo 后，你至少要能写出这样的判断链：

```text
现象：
调用方：
访问对象：
判断模块：
判断输入：
系统结果：
第一证据：
根因：
修复：
回归：
```

推荐使用：

```text
quality/security-diagnosis-report-template.md
```

## 后续可继续挑战

- 增加一个真实安装脚本，自动生成 debug / release 两个 APK，并观察 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`。
- 把自由文本报告升级成更细的 rubric，例如证据、根因、修复、回归、监控各 1 分。

真正的安全能力，不是记住权限名，而是能回答：

```text
系统为什么允许或拒绝这个 App 做这件事？
```
