# 16.1 为什么要学习 PMS、应用安装与包管理

前面几章我们已经追踪了 App 运行后的很多关键路径：

- Activity 如何被 AMS / ATMS 启动。
- Window 如何被 WMS 管理。
- 输入事件如何进入 View 树。
- UI 状态如何变成屏幕上的下一帧。

第 16 章换一个角度继续追问：

```text
系统为什么知道这个 App 存在？
系统为什么知道它有哪些 Activity、Service、权限和签名？
```

答案会进入 PMS：PackageManagerService。

## 本章通关画面

完成第 16 章后，你应该能把一次安装和一次组件查询讲成这样：

```text
APK 安装或系统扫描
  -> PMS 解析 AndroidManifest.xml
      -> 记录 package、version、component、permission、signature
          -> 建立包信息与组件索引
              -> 安装、升级、卸载、权限授权
                  -> Activity 启动 / Intent 解析 / 权限校验时查询 PMS
```

你会发现：PMS 不是“安装 App 的工具人”，它是 Android 认识 App 的户籍系统。

## 本章剧情线

如果第 12 章的 AMS / ATMS 像“调度中心”，第 13 章的 WMS 像“舞台管理处”，第 15 章的 SurfaceFlinger 像“最终导播台”，那第 16 章的 PMS 就像“城市户籍与通行证管理处”。

一个 App 想在系统里运行，不能只把 APK 文件放进手机。

系统还要知道：

- 这个包叫什么。
- 版本是多少。
- 有哪些 Activity、Service、Receiver、Provider。
- 请求了哪些权限。
- 声明了哪些 intent-filter。
- 签名是否可信。
- 升级时是否允许覆盖。
- 其他 App 能不能看见它或调用它。

这些问题都绕不开 PMS。

## 本章探索任务

```text
理解 APK 为什么需要安装
  -> 认识 PackageManagerService
      -> 解析 AndroidManifest 与组件声明
          -> 理解 Intent 解析与组件可见性
              -> 掌握签名、权限、sharedUserId 与安装校验
                  -> 分析安装、升级、卸载和包可见性问题
                      -> 完成包管理观察实验
```

## 本节定位

本节是第 16 章入口。

我们先回答：

- PMS 解决什么问题？
- 为什么 Android 不能只靠文件路径运行 APK？
- 为什么 Activity 启动、权限校验和组件查询都依赖包管理信息？
- 第 16 章 demo 应该如何观察包管理行为？

## 学习目标

学完本节后，你应该能够：

- 理解 PMS 是 Android Framework 的核心系统服务。
- 知道 APK 安装后会被解析成系统可查询的包信息。
- 初步区分包信息、组件信息、权限信息和签名信息。
- 知道第 16 章要解决哪些真实工程问题。

## 第一部分：APK 文件不是 App 的全部

一个 APK 只是文件。

系统要真正运行它，需要知道：

```text
包名
版本
入口 Activity
组件列表
权限声明
签名证书
安装路径
用户安装状态
```

这些信息不是运行时随便猜出来的，而是在安装、扫描和解析阶段被 PMS 记录下来。

## 第二部分：PMS 是包信息总账本

PMS 负责维护系统中已安装应用的信息。

它回答很多问题：

- 某个包是否安装？
- 某个 Activity 是否存在？
- 某个 Intent 能打开谁？
- 某个 App 是否持有权限？
- 某个 APK 是否能覆盖旧版本？
- 某个组件是否对外暴露？

所以 PMS 不只服务安装界面，也服务 AMS、权限系统、Launcher、Settings 和其他 App。

## 第三部分：为什么启动 Activity 也要查 PMS

当你调用：

```kotlin
startActivity(intent)
```

AMS / ATMS 不能凭空知道这个 Intent 对应哪个 Activity。

它需要查询包管理信息：

```text
Intent
  -> 查询 intent-filter / component
      -> 找到 ActivityInfo
          -> 检查 exported / permission / user state
              -> 才能继续启动
```

这就是第 12 章和第 16 章的连接点。

## 第四部分：为什么权限也离不开 PMS

权限不是简单字符串。

系统需要知道：

- 哪个 App 声明了权限。
- 哪个 App 请求了权限。
- 这个权限是 normal、dangerous、signature 还是其他级别。
- 用户是否授权。
- 签名是否满足要求。

PMS 会参与权限声明和包信息管理，权限管理模块则继续处理授权状态。

学习 PMS，可以让你更清楚权限问题不是“Manifest 写没写”这么简单。

## 第五部分：第 16 章要解决的问题

第 16 章重点解决：

- APK 安装时系统做了什么。
- AndroidManifest 如何变成 PackageInfo、ActivityInfo、ServiceInfo。
- Intent 解析为什么有时找不到组件。
- `exported`、permission、queries 和包可见性为什么重要。
- 签名和升级校验如何影响安装。
- 安装失败、权限异常、组件不可见应该如何排查。

## 本节小挑战

### 包管理开场题

请判断下面问题更应该从哪里查：

- `ActivityNotFoundException`。
- 安装失败：签名不一致。
- Android 11 后查询不到某个 App。
- 明明声明了 Service，外部 App 却无法启动。
- 用户授权了权限，但功能仍然提示无权限。

先不要急着修代码，先判断它可能是：组件声明、Intent 解析、签名校验、包可见性，还是权限授权状态。

## 本节实践任务

### 基础任务

- 找一个 AndroidManifest.xml。
- 标出 package、activity、service、receiver、provider、uses-permission。
- 写下哪些信息会被系统包管理服务记录。

### 进阶任务

- 使用 Android Studio 查看 APK Analyzer。
- 对比源码中的 Manifest 和最终合并后的 Manifest。
- 写下 manifest merge 后可能影响 PMS 解析的点。

## 本节小结

PMS 是 Android 认识 App 的入口。APK 安装后，系统会解析 Manifest、签名、组件、权限和版本信息，并把它们变成可查询、可校验、可调度的系统数据。理解 PMS 后，Activity 启动、权限校验、组件可见性、安装失败和升级失败都会有更清晰的排查入口。
