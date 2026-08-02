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

## 第四部分：JNI 方法如何匹配

JNI 连接 Java/Kotlin 和 native 方法。

一种方式是使用命名约定：

```kotlin
external fun hello(): String
```

native 侧需要有匹配方法。

另一种方式是动态注册 JNI 方法。

混淆后，如果 native 侧依赖 Java 类名或方法名，可能需要 keep 规则。

## 第五部分：UnsatisfiedLinkError 常见原因

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 找不到 so | 崩溃日志中的 lib 名 | APK 未打包、ABI 不匹配 | 查 APK lib 目录和 abiFilters |
| 找不到 native 方法 | 方法签名 | JNI 名称不匹配、混淆 | 检查 keep 和动态注册 |
| 某些设备失败 | 设备 ABI | 缺少对应 ABI so | 补 ABI 或调整打包策略 |
| 依赖 so 冲突 | 重复 so 文件 | 多个 AAR 带同名 so | packagingOptions / 统一版本 |
| 插件 so 加载失败 | 插件路径 | nativeLibraryPath 未处理 | 插件框架处理 so 解压和加载路径 |

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

## 本节实践任务

### 基础任务

- 用 APK Analyzer 查看 `lib/` 目录。
- 记录项目支持哪些 ABI。
- 搜索 `System.loadLibrary`。

### 进阶任务

- 找一个带 native so 的三方库。
- 查看它的 AAR 中包含哪些 ABI。
- 思考包体积和兼容性的取舍。

## 本节小结

Dex 解决托管代码加载，so 解决 native 代码加载。`System.loadLibrary` 会根据库名和 ABI 查找对应 so。`UnsatisfiedLinkError` 通常来自 so 未打包、ABI 不匹配、JNI 签名错误、混淆或重复依赖。理解 native 加载后，插件化、包体积和跨设备崩溃排查都会更完整。
