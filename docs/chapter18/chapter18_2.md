# 18.2 从源码到 Dex：classes.dex、D8、R8 与 MultiDex

本节进入第 18 章的第一条核心链路：

```text
Kotlin / Java 源码
  -> class 字节码
      -> D8 / R8
          -> classes.dex
              -> APK
```

如果第 17 章的核心产物是 `resources.arsc`，那么第 18 章的核心产物就是 `classes.dex`。

## 本节定位

本节负责回答：

- Dex 是什么？
- D8 和 R8 分别做什么？
- 为什么 APK 里会有 `classes.dex`、`classes2.dex`？
- MultiDex 为什么曾经是启动性能和兼容性的关键问题？
- R8 优化和混淆会给反射、动态加载带来什么风险？

## 学习目标

学完本节后，你应该能够：

- 解释源码、class、dex、apk 之间的关系。
- 区分 D8 和 R8 的职责。
- 理解 MultiDex 产生的原因。
- 能说清楚 R8 的 shrink、optimize、obfuscate 分别影响什么。
- 初步判断反射和动态加载为什么需要 keep 规则。

## 第一部分：Dex 是 Android 的代码格式

Java / Kotlin 编译后通常先得到 JVM class 字节码。

Android 设备并不直接把这些 `.class` 当作最终运行格式，而是把它们转换成 Dex：

```text
Foo.kt / Foo.java
  -> Foo.class
      -> classes.dex
```

Dex 更适合 Android 运行时使用。它把多个类组织在一个或多个 Dex 文件里，并使用适合移动设备的指令格式和常量池结构。

你可以先记住：

```text
class 是 JVM 世界的字节码单元
dex 是 Android 世界的运行时代码单元
```

更进一步说，Dex 不是把 `.class` 简单打包到一起。

它会把多个 class 中重复出现的信息集中整理，比如类型、字符串、方法签名、字段引用等。这样做的目的，是让移动设备在内存、存储和加载效率之间取得更合适的平衡。

你可以把 Dex 粗略看成一张运行时代码账本：

```text
string_ids      -> 字符串表
type_ids        -> 类型表
proto_ids       -> 方法原型表
field_ids       -> 字段引用表
method_ids      -> 方法引用表
class_defs      -> 类定义表
code_item       -> 方法代码
```

当运行时要找一个类或方法时，并不是把源码文件翻出来读，而是沿着这些表去定位。

这和第 17 章的资源系统很像：

```text
resources.arsc 是资源运行时账本
classes.dex 是代码运行时账本
```

理解这一点，后面 `ClassNotFoundException`、`NoSuchMethodError`、MultiDex、R8 shrink 就都有了落点。

## 第二部分：为什么 Android 不直接用 JVM class

普通 JVM 的 class 文件通常是“一个类一个文件”。

Android App 往往有大量类。如果运行时频繁处理大量独立 class 文件，移动设备的 I/O、内存和启动成本会比较难控制。

Dex 的设计更适合 Android：

- 把很多类合并到一个文件中。
- 共享常量和引用表，减少重复信息。
- 使用寄存器式指令模型，适配 Dalvik / ART 的执行方式。
- 方便安装阶段或后台阶段做 dexopt、校验和编译优化。

所以 Dex 不是“JVM class 的压缩包”，而是 Android 运行时真正理解的代码格式。

## 第三部分：D8 负责把 class 变成 dex

D8 是 Android 的 dex 编译器。

它负责把编译器产出的 `.class` 文件转换成 `.dex` 文件。

大致过程：

```text
Kotlin compiler / Java compiler
  -> .class
      -> D8
          -> classes.dex
```

当你没有开启混淆压缩时，D8 仍然会参与 Dex 生成。

## 第四部分：R8 负责 shrink、optimize、obfuscate

R8 可以理解为更进一步的代码处理器。

它通常负责：

| 能力 | 作用 |
| --- | --- |
| shrink | 删除未使用代码 |
| optimize | 优化代码结构 |
| obfuscate | 混淆类名、方法名、字段名 |
| desugar | 处理部分语言特性兼容 |

R8 可以让包更小、代码更难被直接阅读，也可能让反射、序列化、动态加载、JNI 方法名和三方 SDK 入口出现问题。

所以 R8 不是“开了就完事”，而是要配合 keep 规则。

## 第五部分：R8 为什么会误伤反射和动态入口

R8 的判断依赖“可分析的调用关系”。

直接调用是清楚的：

```kotlin
HomeRoute().open()
```

R8 可以从调用图里看到 `HomeRoute` 被使用。

字符串反射就不同：

```kotlin
Class.forName("com.example.HomeRoute")
```

这对 R8 来说更像一段普通字符串。除非工具能识别这个模式，或者你写了 keep 规则，否则它可能认为这个类没有被直接使用。

于是 release 里可能出现：

```text
debug:
  com.example.HomeRoute 存在

release:
  类被改名，或者被 shrink 删除
  Class.forName("com.example.HomeRoute") 找不到
```

这就是很多“debug 正常，release 崩溃”的根因。

keep 规则不是形式主义，它是在告诉 R8：

```text
这个类虽然在静态调用图里不明显
但运行时会通过字符串、注解、JNI、序列化或框架协议访问
不要删除或改坏它
```

## 第六部分：为什么会有多个 Dex

早期 Android 有一个著名限制：单个 Dex 中的方法引用数量有上限。

当项目变大，方法数超过限制时，就需要 MultiDex：

```text
classes.dex
classes2.dex
classes3.dex
```

现在工具链已经成熟很多，但 MultiDex 仍然值得理解，因为它会影响：

- 启动阶段主 dex 里有哪些类。
- 低版本设备如何安装额外 dex。
- 反射类是否在合适的 dex 中。
- 冷启动时类加载是否变慢。

## 第七部分：Main Dex 为什么重要

在低版本 Android 上，应用启动早期必须能找到一批关键类。

如果启动路径上的类没有进入主 dex，可能出现：

```text
ClassNotFoundException
NoClassDefFoundError
启动阶段崩溃
```

这就是 main dex list 曾经非常重要的原因。

虽然现代构建工具已经自动处理很多事情，但理解它能帮助你排查老项目、复杂项目和插件化项目的问题。

更深一层看，main dex 问题不是“文件名必须叫 classes.dex”这么简单，而是启动阶段 ClassLoader 能否在足够早的时机找到必须初始化的类。

启动阶段常见关键类包括：

- `Application`。
- 启动 Activity。
- ContentProvider。
- MultiDex 安装逻辑本身。
- 早期初始化框架。
- 反射路由入口。

如果这些类在低版本设备上落到 secondary dex，而 secondary dex 又还没完成安装和加载，就会出现启动阶段找不到类。

这也是为什么 MultiDex 曾经特别考验启动路径设计。

## 第八部分：R8 和反射的关系

普通直接调用：

```kotlin
val user = User()
user.name
```

R8 能看见调用关系，通常可以安全分析。

反射调用：

```kotlin
Class.forName("com.example.User")
```

R8 不一定能可靠理解字符串里的类名用途。混淆或 shrink 后，这类入口可能失效。

所以反射、JSON 序列化、路由、插件入口、JNI 方法名、三方 SDK 回调常常需要 keep 规则。

## 第九部分：从构建产物反推问题

遇到代码加载问题时，不要只看源码。

你要看最终产物：

```text
源码里有没有
  -> 编译后的 class 有没有
      -> dex 里有没有
          -> release 混淆后叫什么
              -> 运行时 ClassLoader 路径里有没有
```

源码存在，只能说明“你写过它”。

Dex 存在，才能说明“运行时有机会加载它”。

ClassLoader 路径正确，才能说明“运行时真的找得到它”。

## 第十部分：Dex 问题排查表

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| `ClassNotFoundException` | 类名、dex 内容、ClassLoader | 类未打包、加载路径错误、混淆 | 查 APK、mapping、keep、加载路径 |
| `NoClassDefFoundError` | 崩溃类和触发路径 | 依赖缺失、运行时找不到间接类 | 查依赖和最终 APK |
| `NoSuchMethodError` | 方法签名、依赖版本 | 编译期和运行时版本不一致 | 统一依赖版本 |
| 反射失败 | 字符串类名、mapping | 类名被混淆或被 shrink | 添加 keep 规则 |
| 启动慢 | dex 数量、类加载耗时 | 类过多、主路径加载复杂 | 减少启动类、使用 baseline profile |

## 本节小挑战

### Dex 拆包题

如果 release 包里出现：

```text
java.lang.ClassNotFoundException: com.example.router.HomeRoute
```

但 debug 包正常，你会先查什么？

建议回答：

- release 是否开启 R8。
- `HomeRoute` 是否只通过反射访问。
- mapping 中类名是否被改写。
- APK 中是否还能找到对应类。
- keep 规则是否覆盖路由入口。
- 运行时 ClassLoader 是否真的加载了包含它的 dex。

## 本节实践任务

### 基础任务

- 用 APK Analyzer 查看 `classes.dex`。
- 对比 debug 和 release APK 中 dex 大小。
- 找出项目中一个反射入口。
- 写出它可能需要的 keep 规则。

### 进阶任务

- 尝试开启 minify，观察 mapping 文件。
- 查找 `Class.forName`、`ServiceLoader`、JSON 序列化模型。
- 解释它们为什么可能受混淆影响。
- 画出“源码存在 -> dex 存在 -> ClassLoader 可见”的三段证据链。

## 本节小结

Dex 是 Android 运行时代码的核心格式。它不是 class 文件的简单压缩包，而是一套面向 Android 运行时的代码账本。D8 负责把 class 转成 dex，R8 负责压缩、优化和混淆，MultiDex 解决大项目 dex 拆分问题。理解“源码存在、dex 存在、ClassLoader 可见”这三层证据后，`ClassNotFoundException`、反射失败、混淆问题、启动阶段类加载异常就不再只是“玄学崩溃”。
