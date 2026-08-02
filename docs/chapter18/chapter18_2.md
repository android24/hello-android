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

## 第二部分：D8 负责把 class 变成 dex

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

## 第三部分：R8 负责 shrink、optimize、obfuscate

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

## 第四部分：为什么会有多个 Dex

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

## 第五部分：Main Dex 为什么重要

在低版本 Android 上，应用启动早期必须能找到一批关键类。

如果启动路径上的类没有进入主 dex，可能出现：

```text
ClassNotFoundException
NoClassDefFoundError
启动阶段崩溃
```

这就是 main dex list 曾经非常重要的原因。

虽然现代构建工具已经自动处理很多事情，但理解它能帮助你排查老项目、复杂项目和插件化项目的问题。

## 第六部分：R8 和反射的关系

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

## 第七部分：Dex 问题排查表

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

## 本节小结

Dex 是 Android 运行时代码的核心格式。D8 负责把 class 转成 dex，R8 负责压缩、优化和混淆，MultiDex 解决大项目 dex 拆分问题。理解这条链路后，`ClassNotFoundException`、反射失败、混淆问题、启动阶段类加载异常就不再只是“玄学崩溃”。
