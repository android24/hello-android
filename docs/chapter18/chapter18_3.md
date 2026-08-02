# 18.3 ClassLoader：PathClassLoader、DexClassLoader 与类查找路径

上一节我们知道，Android App 的代码最终会进入 Dex。

本节继续问：

```text
Dex 已经在 APK 里了
  -> 运行时是谁把类找出来？
```

答案是 ClassLoader。

## 本节定位

本节负责回答：

- ClassLoader 是什么？
- Android 常见 ClassLoader 有哪些？
- `PathClassLoader` 和 `DexClassLoader` 有什么区别？
- 为什么同一个类名被不同 ClassLoader 加载后可能不是同一个类型？
- 插件化和热修复为什么都绕不开 ClassLoader？

## 学习目标

学完本节后，你应该能够：

- 打印并解释当前类的 ClassLoader。
- 区分系统类、应用类、动态 dex 类的加载路径。
- 理解双亲委派在 Android 中的意义。
- 能解释 ClassLoader 隔离为什么会导致类型转换失败。
- 知道插件化常见的加载策略。

## 第一部分：ClassLoader 是类的查找器

类加载不是凭空发生的。

当运行时需要某个类：

```text
com.example.HomeActivity
```

ClassLoader 会沿着自己的路径去查找这个类所在的 Dex。

你可以先把它理解成：

```text
类名
  -> ClassLoader
      -> dex path
          -> class definition
```

## 第二部分：Android 常见 ClassLoader

常见角色如下：

| ClassLoader | 典型用途 |
| --- | --- |
| BootClassLoader | 加载 framework / boot classpath 中的核心类 |
| PathClassLoader | 加载已安装 App 的 apk / dex |
| DexClassLoader | 加载外部 dex / jar / apk |
| InMemoryDexClassLoader | 从内存字节加载 dex，较新系统可用 |

普通 App 运行时，应用类通常由 `PathClassLoader` 加载。

动态加载外部 dex 时，历史上常见 `DexClassLoader`。

## 第三部分：打印 ClassLoader

你可以在 Activity 里打印：

```kotlin
Log.d("ClassLoader", "activity=${this::class.java.classLoader}")
Log.d("ClassLoader", "string=${String::class.java.classLoader}")
Log.d("ClassLoader", "app=${applicationContext::class.java.classLoader}")
```

你会看到应用类和系统类的加载器不同。

这说明：并不是所有类都来自同一个地方。

## 第四部分：双亲委派

传统 Java ClassLoader 有双亲委派模型：

```text
当前 ClassLoader 收到类加载请求
  -> 先交给 parent
      -> parent 找不到
          -> 当前 ClassLoader 再尝试自己找
```

这样可以避免应用随便替换系统核心类。

Android 中具体实现和普通 JVM 不完全一样，但“先看父加载器，再看自己路径”的思想仍然很重要。

## 第五部分：同名类不一定是同一个类型

如果两个不同 ClassLoader 分别加载了同一个类名：

```text
loaderA -> com.example.PluginApi
loaderB -> com.example.PluginApi
```

它们在运行时可能被视为两个不同类型。

于是你可能遇到：

```text
ClassCastException
```

明明类名一样，却不能强转。

原因是类型身份不只由类名决定，也和加载它的 ClassLoader 有关。

```text
类型身份 = 类名 + ClassLoader
```

这是插件化里非常重要的一句话。

## 第六部分：PathClassLoader 与 DexClassLoader

`PathClassLoader` 常用于加载已安装应用路径中的代码。

`DexClassLoader` 常用于加载外部 dex / jar / apk。

典型动态加载结构：

```text
宿主 App
  -> DexClassLoader(plugin.apk)
      -> loadClass("com.example.plugin.Entry")
          -> 反射创建插件入口
```

但只加载类还不够。插件页面如果要显示，还会涉及：

- 插件资源如何加载。
- 插件组件是否注册。
- 插件生命周期由谁托管。
- 插件和宿主依赖如何共享。
- 插件 so 如何加载。

## 第七部分：热修复为什么要抢加载顺序

很多热修复方案的核心思路之一是：

```text
把补丁 dex 放到原 dex 前面
  -> ClassLoader 查找类时先命中补丁类
      -> 达到替换旧类的效果
```

这就要求你理解 ClassLoader 的查找路径顺序。

如果类已经被加载过，后面再替换就很困难。

所以热修复不是简单“下载一个 dex”，而是在和类加载时机赛跑。

## 第八部分：ClassLoader 问题排查

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 类找不到 | 类名、dex 路径、ClassLoader | dex 没加载或路径错误 | 打印 classLoader 和 dexElements |
| 强转失败 | 类名、两个对象的 classLoader | 同名类由不同加载器加载 | 抽公共 API 到宿主父加载器 |
| 插件资源错乱 | 类能加载但资源不对 | 只处理代码，没处理资源 | 配套 AssetManager / Resources |
| 热修复不生效 | 类是否已加载 | 补丁加入太晚 | 提前加载补丁或调整方案 |

## 本节小挑战

### 同名类陷阱题

为什么下面两个对象类名一样，却可能强转失败？

```text
pluginApiFromHost.class.name == pluginApiFromPlugin.class.name
```

你需要回答：

- 它们是否由同一个 ClassLoader 加载？
- 公共接口应该放在宿主还是插件？
- 为什么插件化常把 API 合同放在宿主或公共模块？

## 本节实践任务

### 基础任务

- 在 Activity 中打印当前类的 ClassLoader。
- 打印 `String`、`Activity`、业务类的 ClassLoader。
- 搜索项目中是否使用 `Class.forName`。

### 进阶任务

- 阅读 `BaseDexClassLoader`、`DexPathList` 的源码。
- 画出一个插件类从 plugin.apk 被加载的流程。
- 思考插件资源和插件类为什么要一起处理。

## 本节小结

ClassLoader 决定类从哪里被找到，也决定类型身份的一部分。普通应用主要使用 `PathClassLoader`，动态加载常见 `DexClassLoader`。插件化和热修复的关键，不只是能不能加载一个类，而是加载路径、加载顺序、类型隔离、资源加载和生命周期托管能不能一起闭环。
