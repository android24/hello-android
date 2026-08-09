# 18.5 Native 库加载：System.loadLibrary、JNI、ABI 与 so 冲突

Android App 不只有 Kotlin / Java 代码。

很多项目还会包含 native 代码：

```text
libxxx.so
JNI
NDK
C / C++
```

本节负责把 native 库加载放进第 18 章的代码加载地图。

## 本节定位

本节负责回答：

- so 文件是什么？
- `System.loadLibrary()` 做了什么？
- ABI 为什么会影响安装包和运行时加载？
- `UnsatisfiedLinkError` 常见原因有哪些？
- 插件化和动态加载为什么也要处理 so？

## 学习目标

学完本节后，你应该能够：

- 区分 Dex 代码加载和 so 加载。
- 说清楚 ABI 的意义。
- 初步排查 `UnsatisfiedLinkError`。
- 说清楚 `System.loadLibrary("foo")` 到 `libfoo.so` 的查找链路。
- 理解 JNI 方法名、混淆和 keep 的关系。
- 知道 so 冲突和重复打包为什么会出问题。

## 第一部分：so 是 native 世界的代码

Dex 里放的是 Android 运行时能加载的托管代码。

so 里放的是 native 机器码。

APK 中常见结构：

```text
lib/
  arm64-v8a/
    libfoo.so
  armeabi-v7a/
    libfoo.so
  x86_64/
    libfoo.so
```

不同目录对应不同 ABI。

## 第二部分：ABI 是设备 CPU 架构约定

ABI 决定 native 库的二进制兼容性。

常见 ABI：

| ABI | 说明 |
| --- | --- |
| arm64-v8a | 64 位 ARM，现代设备常见 |
| armeabi-v7a | 32 位 ARM，老设备常见 |
| x86 / x86_64 | 模拟器或特定设备 |

如果设备需要 `arm64-v8a`，但 APK 里只有不兼容的 so，就可能加载失败。

## 第三部分：System.loadLibrary 做了什么

代码：

```kotlin
System.loadLibrary("foo")
```

运行时会尝试加载：

```text
libfoo.so
```

它会在 native library path 中查找对应 ABI 的 so 文件。

如果找不到，就会抛出：

```text
java.lang.UnsatisfiedLinkError
```

再往下拆，这条链路大致是：

```text
System.loadLibrary("foo")
  -> 把 foo 转成平台库名 libfoo.so
      -> Runtime 尝试加载 native 库
          -> 结合当前 ClassLoader 查找 native library path
              -> 在对应 ABI 目录里寻找 libfoo.so
                  -> 找到后交给系统 linker 加载
                      -> 解析依赖 so 和 native 符号
```

所以 `loadLibrary` 不是在整个手机里随便搜索文件。它会依赖当前进程、当前应用、当前 ClassLoader 对应的 native 库路径。

普通安装包里，常见位置可以理解为：

```text
APK lib/arm64-v8a/libfoo.so
  -> 安装 / 分发后进入应用可访问的 native library 目录
      -> System.loadLibrary("foo") 从 nativeLibraryDir 查找
```

插件化或动态下载场景会更麻烦：

```text
plugin.apk
  -> 解压 lib/arm64-v8a/libfoo.so
      -> 放到宿主可访问目录
          -> 让插件 ClassLoader 或加载逻辑知道这个 nativeLibraryPath
              -> 再调用 System.load / System.loadLibrary
```

因此排查 so 问题时，不要只问“代码里有没有调用 `loadLibrary`”，还要问：

- 最终包里有没有对应 ABI 的 so？
- 安装到设备后 native library path 指向哪里？
- 当前调用发生在哪个 ClassLoader 上下文里？
- 这个 so 依赖的其他 so 是否也能被找到？

## 第四部分：JNI 方法如何匹配

JNI 连接 Java/Kotlin 和 native 方法。

一种方式是使用命名约定：

```kotlin
external fun hello(): String
```

native 侧需要有匹配方法。

另一种方式是动态注册 JNI 方法。

混淆后，如果 native 侧依赖 Java 类名或方法名，可能需要 keep 规则。

这里也要区分两种匹配方式。

### 静态注册

静态注册依赖 JNI 命名约定。粗略形式是：

```text
Java_包名_类名_方法名
```

如果 Kotlin / Java 侧的类名、包名、方法名发生变化，native 侧仍然按旧名字找，就可能出现：

```text
java.lang.UnsatisfiedLinkError: No implementation found for ...
```

这种问题常见于：

- release 混淆后类名或方法名变化。
- native 方法签名改了，但 C / C++ 侧没同步。
- Kotlin companion / object / overload 生成的 JVM 签名和预期不一致。

这时 keep 规则的作用不是“保护 so 文件”，而是保护 native 侧要按名字匹配的 Java / Kotlin 入口。

### 动态注册

动态注册通常会在 `JNI_OnLoad` 里把 Java 方法和 native 函数绑定起来。

粗略流程是：

```text
System.loadLibrary
  -> so 被加载
      -> JNI_OnLoad 执行
          -> RegisterNatives
              -> 把 Java 方法签名绑定到 native 函数指针
```

动态注册更灵活，也更常见于复杂 SDK。它减少了对长 JNI 函数名的依赖，但仍然可能受这些因素影响：

- 注册时查找的 Java 类名被混淆。
- 方法签名和 native 注册表不一致。
- `JNI_OnLoad` 没执行成功。
- so 的依赖库先加载失败。

所以看到 `UnsatisfiedLinkError` 时，要分清楚它是在说：

```text
找不到 so 文件
```

还是在说：

```text
so 找到了，但找不到 native 方法
```

这两个问题的证据完全不同。

## 第五部分：UnsatisfiedLinkError 常见原因

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 找不到 so | 崩溃日志中的 lib 名 | APK 未打包、ABI 不匹配 | 查 APK lib 目录和 abiFilters |
| 找不到 native 方法 | 方法签名 | JNI 名称不匹配、混淆 | 检查 keep 和动态注册 |
| 某些设备失败 | 设备 ABI | 缺少对应 ABI so | 补 ABI 或调整打包策略 |
| 依赖 so 冲突 | 重复 so 文件 | 多个 AAR 带同名 so | packagingOptions / 统一版本 |
| 插件 so 加载失败 | 插件路径 | nativeLibraryPath 未处理 | 插件框架处理 so 解压和加载路径 |

可以把 `UnsatisfiedLinkError` 分成三层：

```text
文件层：libfoo.so 不存在或 ABI 不匹配
  -> 依赖层：libfoo.so 找到了，但它依赖的 libbar.so 找不到
      -> 符号层：so 加载了，但 JNI 方法或 native 符号匹配失败
```

很多人只查第一层，于是会困在“明明 APK 里有 so，为什么还是崩”的问题里。第二层和第三层才是 native 排查里更容易漏掉的部分。

## 第六部分：so 与包体积

每增加一个 ABI，就可能多带一份 native 库。

如果一个 so 很大，多 ABI 会显著增加包体积。

常见策略：

- 只保留目标 ABI。
- 使用 App Bundle 按设备分发。
- 检查三方 SDK 是否带了重复 so。
- 删除不需要的架构。

## 第七部分：插件化里的 so

插件如果带 native 库，宿主还要处理：

```text
下载插件 APK
  -> 解压对应 ABI 的 so
      -> 配置 nativeLibraryPath
          -> loadLibrary / load
```

所以插件化不仅是 DexClassLoader。

它还要处理：

- Dex。
- Resources。
- Manifest / 组件生命周期。
- so。
- 签名和安全。

这也是插件化复杂的原因之一。

再进一步看，插件 so 的难点不是“复制文件”本身，而是闭环：

```text
插件选择正确 ABI
  -> 解压到宿主私有目录
      -> 保证目录权限和生命周期
          -> 配置 nativeLibraryPath 或使用绝对路径 load
              -> 处理 so 之间的依赖关系
                  -> 保证插件卸载 / 更新 / 回滚时不残留错误版本
```

一旦插件 A 和插件 B 都带了同名 so，或者插件 so 与宿主 so 版本不同，就会出现更微妙的问题：

- 先加载的版本占据进程。
- 后加载的插件以为自己加载了新版本，实际命中了旧版本。
- Java 层 API 版本和 native 层实现版本不匹配。
- 某些设备 ABI 下才暴露问题。

所以大型插件框架通常会把 native 库管理当成独立模块，而不是顺手放在 Dex 加载逻辑里。

## 本节小挑战

### so 加载失败题

某些 arm64 手机上崩溃：

```text
java.lang.UnsatisfiedLinkError: couldn't find "libimage_filter.so"
```

你会查什么？

建议回答：

- APK 中是否包含 `lib/arm64-v8a/libimage_filter.so`。
- Gradle 是否配置了 `abiFilters`。
- AAB 分发是否漏了 ABI。
- 三方库是否只提供 32 位。
- 插件或动态下载路径是否正确。
- 如果 so 存在，继续查它依赖的其他 so 是否存在。
- 如果 so 已加载，继续查 JNI 方法签名或动态注册是否成功。

## 本节实践任务

### 基础任务

- 用 APK Analyzer 查看 `lib/` 目录。
- 记录项目支持哪些 ABI。
- 搜索 `System.loadLibrary`。
- 记录 `applicationInfo.nativeLibraryDir`。
- 区分一次崩溃是“找不到 so”还是“找不到 native 方法”。

### 进阶任务

- 找一个带 native so 的三方库。
- 查看它的 AAR 中包含哪些 ABI。
- 思考包体积和兼容性的取舍。
- 整理一条 `System.loadLibrary -> nativeLibraryDir -> linker -> JNI_OnLoad` 的加载链路。
- 尝试解释静态注册和动态注册在混淆场景下分别需要保护什么。

## 本节小结

Dex 解决托管代码加载，so 解决 native 代码加载。`System.loadLibrary` 会根据库名和 ABI 查找对应 so。`UnsatisfiedLinkError` 通常来自 so 未打包、ABI 不匹配、JNI 签名错误、混淆或重复依赖。理解 native 加载后，插件化、包体积和跨设备崩溃排查都会更完整。
