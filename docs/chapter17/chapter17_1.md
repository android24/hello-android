# 17.1 为什么要学习资源系统、AssetManager 与 Resources

第 16 章我们追踪了系统如何认识一个 App：

```text
APK
  -> Manifest
      -> package / component / permission / signature
          -> PMS 建立包信息
```

第 17 章继续追问另一个同样基础的问题：

```text
系统为什么知道 R.string.app_name 对应哪段文字？
为什么同一个 drawable 在不同分辨率设备上会加载不同文件？
为什么切换深色模式、语言、横竖屏后，资源会发生变化？
```

答案会进入 Android 资源系统：`R`、`resources.arsc`、`AssetManager`、`Resources`、`Theme` 与 `Configuration`。

## 本章通关画面

完成第 17 章后，你应该能把一次资源读取讲成这样：

```text
源码中的 res 文件
  -> AAPT2 编译资源
      -> 生成 R 文件和 resources.arsc
          -> APK 安装后资源路径被系统记录
              -> AssetManager 加载资源表和资源文件
                  -> Resources 根据 Configuration 选择最合适的资源
                      -> Theme 叠加样式属性
                          -> View / Compose / 业务代码拿到最终值
```

你会发现：资源系统不是“取字符串的小工具”，它是 Android 适配多设备、多语言、多主题、多屏幕密度的重要底座。

## 本章剧情线

如果第 16 章的 PMS 像“户籍系统”，负责登记一个 App 的身份，那么第 17 章的资源系统就像“物资仓库和调度员”。

App 不是只有 Kotlin 代码。它还带着大量资源：

- 字符串。
- 图片。
- 颜色。
- 尺寸。
- 布局。
- 动画。
- 样式。
- 主题。
- 多语言、多夜间模式、多屏幕密度、多横竖屏版本。

代码里看起来只是：

```kotlin
getString(R.string.app_name)
```

但系统背后要回答：

- `R.string.app_name` 是谁生成的？
- 资源 ID 为什么是一个整数？
- 当前语言应该选哪个 `strings.xml`？
- 当前密度应该选哪个图片目录？
- 主题里的颜色为什么能被 View 读到？
- 为什么资源找不到会崩溃？
- 为什么动态换肤、插件化和资源混淆都绕不开 AssetManager？

这些问题都属于第 17 章。

## 本章探索任务

```text
理解 res 为什么需要编译
  -> 认识 AAPT2、R 文件和 resources.arsc 的匹配关系
      -> 理解 AssetManager 如何加载 APK 资源
          -> 掌握 Resources 如何根据 Configuration 选资源
              -> 拆解 Theme、Style、Attribute 的关系
                  -> 分析语言、夜间模式、密度、横竖屏适配
                      -> 理解资源混淆、shrink 和依赖冲突
                          -> 排查资源找不到、主题错乱和适配异常
                              -> 完成资源系统观察实验
```

## 本节定位

本节是第 17 章入口。

我们先回答：

- 为什么资源不是普通文件读取？
- 为什么 Android 要把资源编译成资源表？
- 为什么 `R` 文件和 `resources.arsc` 能通过资源 ID 匹配？
- 为什么 `AssetManager`、`Resources` 和 `Theme` 是一条运行时链路？
- 为什么资源混淆、shrink 和依赖冲突会影响线上排查？
- 第 17 章 demo 应该观察什么？

## 学习目标

学完本节后，你应该能够：

- 理解 Android 资源系统解决的核心问题。
- 知道资源从源码到运行时大致经历哪些阶段。
- 初步区分 `R`、`resources.arsc`、`AssetManager`、`Resources` 和 `Theme`。
- 能说清楚 `R.string.xxx`、资源 ID 和 `resources.arsc` 的匹配关系。
- 初步理解资源混淆、资源 shrink、`getIdentifier` 和依赖冲突的风险。
- 知道资源问题常见排查入口。

## 第一部分：资源不是普通文件

普通文件读取通常像这样：

```text
给我一个路径
  -> 打开文件
      -> 读字节
```

Android 资源读取更像这样：

```text
给我一个资源 ID
  -> 找到资源表条目
      -> 根据当前 Configuration 选择最匹配版本
          -> 解析类型、密度、语言、主题属性
              -> 返回最终值
```

资源系统真正解决的不是“读文件”，而是“在当前设备和上下文里，选出最合适的那一份资源”。

## 第二部分：为什么需要 R 文件

你在代码中使用：

```kotlin
R.string.app_name
R.drawable.ic_launcher
R.color.primary
```

这些不是文件路径，而是编译期生成的资源 ID。

资源 ID 的价值是：

- 让代码不用关心真实文件路径。
- 让资源可以被编译、压缩、索引和校验。
- 让系统运行时可以快速查表。
- 让多语言、多密度、多主题资源可以共享同一个逻辑名字。

简单说，`R` 文件是代码进入资源世界的门牌号。

## 第三部分：为什么需要 resources.arsc

APK 里除了 `.dex`、Manifest 和资源文件，还会包含资源表：

```text
resources.arsc
```

它记录了很多关键信息：

- 资源 ID。
- 资源类型。
- 资源名称。
- 资源值。
- 不同配置下的候选资源。
- package / type / entry 的映射关系。

如果说 `R` 文件是代码侧的门牌号，那么 `resources.arsc` 就是运行时的仓库账本。

## 第四部分：AssetManager 与 Resources 的分工

可以先这样理解：

```text
AssetManager
  -> 管理 APK 资源路径
  -> 加载资源表
  -> 读取 asset / raw / compiled resource

Resources
  -> 面向应用代码提供 getString / getDrawable / getDimension
  -> 根据 Configuration 选择资源
  -> 处理显示密度、语言、区域、夜间模式等上下文

Theme
  -> 在 Resources 之上叠加 style / attribute
  -> 决定控件默认颜色、字体、背景和行为
```

三者经常一起出现，但职责并不相同。

## 第五部分：第 17 章要解决的问题

第 17 章重点解决：

- `R` 文件和资源 ID 是怎么来的。
- `resources.arsc` 为什么是资源系统核心。
- `R` 常量和资源表条目如何通过 `0xPPTTEEEE` 匹配。
- `AssetManager` 和 `Resources` 如何加载资源。
- `Configuration` 如何影响语言、夜间模式、密度和横竖屏。
- `Theme`、`Style`、`Attribute` 为什么容易让人绕晕。
- 代码混淆、资源 shrink、资源名混淆和动态查找分别影响什么。
- 多模块依赖冲突为什么会发生，哪些重复是覆盖，哪些重复是错误。
- 资源找不到、主题错乱、适配异常应该如何排查。

## 本节小挑战

### 资源系统开场题

请判断下面问题更应该从哪里查：

- `Resources.NotFoundException`。
- 同一张图片在某些设备上变糊。
- 切换深色模式后颜色不对。
- 多语言文案没有生效。
- 自定义 View 读取不到主题属性。
- release 包资源名变化导致线上排查困难。

先不要急着改代码，先判断它可能是：资源 ID、资源表、资源目录限定符、Configuration、Theme，还是打包混淆问题。

## 本节实践任务

### 基础任务

- 打开任意 Android 工程的 `res` 目录。
- 找出 `values`、`drawable`、`mipmap`、`layout`、`xml` 分别存放什么。
- 在代码里找到三个 `R.xxx.xxx` 的使用位置。

### 进阶任务

- 使用 APK Analyzer 打开一个 APK。
- 找到 `resources.arsc`。
- 对比源码里的资源文件和 APK 里的资源结构。

## 本节小结

资源系统是 Android 适配能力的基础。`R` 文件让代码能通过整数 ID 访问资源，`resources.arsc` 记录运行时资源表，`AssetManager` 负责加载资源路径和资源表，`Resources` 根据当前配置选择最合适的资源，`Theme` 则把样式属性叠加到界面上。理解这条链路后，资源找不到、主题错乱、多语言失败、图片适配异常都会有清晰的排查方向。
