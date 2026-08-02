# 18.1 为什么要学习 ClassLoader、Dex、ART 与动态加载

第 16 章我们看见系统如何认识一个 APK：安装、解析 Manifest、登记组件、校验签名和权限。

第 17 章我们看见系统如何读取 APK 里的资源：`R`、`resources.arsc`、`AssetManager`、`Resources`、`Configuration` 和 Theme 一起决定最终界面。

第 18 章继续追问另一个关键问题：

```text
系统知道这个 App 存在
  -> 系统知道这个 App 有哪些资源
      -> 那系统又是如何加载这个 App 的代码？
```

这会进入 `Dex`、`ClassLoader`、Dalvik、`ART`、`JNI`、动态加载、插件化、热修复和代码加载问题排查。

## 本章通关画面

学完第 18 章后，你应该能画出这条链路：

```text
APK / split APK / AAB
  -> classes.dex / classes2.dex
      -> PathClassLoader / DexClassLoader
          -> Dalvik / ART 加载类
              -> 解释执行 / JIT / AOT
                  -> native so 加载
                      -> 动态加载 / 插件化 / 热修复边界
```

如果第 17 章像打开资源仓库，第 18 章就是走进代码引擎室。

资源决定 App 看起来是什么样，Dex 和 ClassLoader 决定 App 的代码如何被找到，Dalvik 和 ART 则代表 Android 代码执行环境从早期虚拟机到现代运行时的演进。

## 本章剧情线

很多线上问题，表面看起来只是一个崩溃：

```text
ClassNotFoundException
NoClassDefFoundError
VerifyError
UnsatisfiedLinkError
NoSuchMethodError
冷启动变慢
热修复不生效
插件页面打不开
ABI 包体积异常
```

但它们背后经常不是“少写一行代码”，而是代码加载链路出了问题：

- 类在编译期存在，运行时为什么找不到？
- 同一个类在不同 ClassLoader 里为什么不是同一个类型？
- MultiDex 为什么会影响低版本启动？
- R8 混淆后，反射和动态加载为什么容易翻车？
- so 文件为什么在某些设备上加载失败？
- 插件化为什么不能只把一个 APK 文件下载下来就完事？
- 热修复为什么要理解类加载顺序？
- Dalvik 和 ART 有什么差异？
- ART 的解释执行、JIT、AOT 和 Profile 为什么会影响启动性能？

这一章会把这些问题放回代码加载链路里解释。

## 本章探索任务

```text
认识 APK 中的 classes.dex
  -> 理解 Dex 与 Java/Kotlin 字节码的关系
      -> 区分 PathClassLoader、DexClassLoader、BootClassLoader
          -> 观察 ClassLoader 双亲委派和 Android 的加载路径
              -> 对比 Dalvik 与 ART
                  -> 认识解释执行、JIT、AOT 和 Profile
                  -> 理解 native so 加载和 ABI
                      -> 分析动态加载、插件化和热修复的工程边界
                          -> 整理 ClassNotFound、NoSuchMethod、UnsatisfiedLinkError 排查路线
```

## 本节定位

本节是第 18 章入口。

它负责回答：

- 为什么 Framework 学习不能停在 Activity、Window、Input 和 Resources？
- Dex、ClassLoader、Dalvik / ART 在 App 运行时分别负责什么？
- 为什么动态加载、插件化和热修复都绕不开 ClassLoader？
- 第 18 章的学习顺序是什么？

## 学习目标

学完本节后，你应该能够：

- 说清楚第 18 章和第 16、17 章的关系。
- 初步区分 APK、Dex、ClassLoader、Dalvik、ART、JNI 和 so。
- 能把常见代码加载问题放入正确的排查方向。
- 知道本章不是教你立刻造插件化框架，而是先建立代码加载地图。

## 第一部分：代码不是直接从源码运行

Android 项目里的 Kotlin / Java 源码不会原样进入设备运行。

大致过程是：

```text
Kotlin / Java 源码
  -> JVM class 字节码
      -> D8 / R8
          -> classes.dex
              -> APK
                  -> Dalvik / ART 加载和执行
```

也就是说，运行时真正被 Android 运行时处理的核心产物是 Dex。

源码是给人看的，Dex 是给 Android 运行时加载的。

## 第二部分：ClassLoader 是代码世界的资源索引

第 17 章里，`Resources` 通过资源 ID 去资源表中找值。

第 18 章里，`ClassLoader` 通过类名去 Dex 路径中找类。

你可以先粗略类比：

| 资源系统 | 代码加载 |
| --- | --- |
| `R.string.title` | `com.example.HomeActivity` |
| `resources.arsc` | `classes.dex` |
| `Resources` | `ClassLoader` |
| 资源路径 | dex path |
| 资源找不到 | ClassNotFound |

这不是完全等价，但它能帮助你建立第一张地图：运行时很多东西都不是“随便搜文件”，而是沿着固定表和固定路径查找。

## 第三部分：从 Dalvik 到 ART：代码执行环境的换代

早期 Android 使用 Dalvik 虚拟机。Android 4.4 开始提供 ART 预览，Android 5.0 起 ART 成为默认运行时。

它们都面向 Dex，但执行策略不同：

| 对比项 | Dalvik | ART |
| --- | --- | --- |
| 时代定位 | 早期 Android 运行时 | 现代 Android 运行时 |
| 核心思路 | 以解释执行和 JIT 为主 | 解释执行、JIT、AOT 和 Profile 组合 |
| 安装后优化 | 相对较轻 | 会结合 dexopt、oat / vdex、Profile 做更多优化 |
| 启动性能 | 更依赖运行时逐步热起来 | 可通过 AOT / Profile 提前优化关键路径 |
| 工程影响 | MultiDex、方法数、运行时性能问题更突出 | Baseline Profile、启动路径优化更重要 |

ART 负责加载类、校验字节码、管理对象、执行方法、垃圾回收和运行时优化。

它既要让代码能跑，又要让代码跑得快。

所以你会遇到这些词：

```text
interpretation
JIT
AOT
Profile Guided Optimization
oat
vdex
baseline profile
```

它们都在回答一个问题：一段 Dex 代码，什么时候解释执行，什么时候编译优化，什么时候复用历史运行数据。理解 Dalvik 和 ART 的差异，可以帮助你读懂很多老文章、老项目和热修复方案里的历史包袱。

## 第四部分：动态加载是能力，也是风险

动态加载听起来很诱人：

```text
下载一个插件
  -> 加载插件 Dex
      -> 读取插件资源
          -> 打开插件页面
```

但真正工程化时，它会遇到很多边界：

- 插件类由哪个 ClassLoader 加载？
- 插件资源由哪个 AssetManager 加载？
- 插件 Activity 是否需要在 Manifest 注册？
- 宿主和插件是否共享依赖？
- 同名类冲突怎么处理？
- 插件 so 如何加载？
- R8 混淆后反射入口是否还能找到？
- 安全和签名如何保证？

所以本章讲动态加载，不是鼓励一上来造框架，而是让你知道这类方案为什么复杂。

## 本节小挑战

### 代码加载开场题

请解释下面两句话的差异：

```text
系统找不到资源：Resources.NotFoundException
系统找不到类：ClassNotFoundException
```

你需要回答：

- 一个查的是资源表，另一个查的是什么？
- 资源名混淆和代码混淆分别可能影响谁？
- 为什么插件化同时要处理资源加载和代码加载？
- 为什么很多早期热修复或 MultiDex 文章会频繁提到 Dalvik？

## 本节实践任务

### 基础任务

- 打开一个 APK，观察里面的 `classes.dex`。
- 用 Android Studio APK Analyzer 查看 dex、res、lib 目录。
- 在代码里打印 `this::class.java.classLoader`。
- 对比 App 类、系统类的 ClassLoader 输出。

### 进阶任务

- 搜索项目中所有反射入口。
- 思考它们在 R8 混淆后是否安全。
- 记录一次 `ClassNotFoundException` 或 `NoSuchMethodError` 的可能原因。
- 查阅一篇早期 Dalvik / MultiDex 文章，标出哪些内容在现代 ART 下已经变化。

## 本节小结

第 18 章负责回答“App 代码如何被加载和执行”。它和第 16、17 章一起补齐 APK 运行时三件大事：系统如何认识 App，系统如何读取资源，系统如何加载代码。理解 Dex、ClassLoader、Dalvik、ART 和动态加载后，插件化、热修复、MultiDex、so 加载失败、启动优化和混淆问题都会有更清晰的原理入口。
