# 18.8 综合实践：代码加载、ClassLoader 与运行时观察实验

第 18 章最后一节，我们把 Dex、ClassLoader、Dalvik、ART、R8、MultiDex、so 和动态加载放进一个综合观察实验。

目标是：让你能从一次类加载和一次崩溃日志，解释系统如何找到代码、执行代码，以及为什么会失败。

## 本节剧情钩子

现在你已经知道：

```text
系统如何认识 App
系统如何读取 App 资源
```

第 18 章继续追问：

```text
系统如何加载 App 代码？
为什么同一个类名不一定是同一个类型？
为什么 debug 正常 release 崩？
为什么插件不只是一个 apk 文件？
为什么 so 在某些设备上找不到？
```

本节要把这些问题做成一个可观察实验室。

## 本节定位

本节是第 18 章综合实践。

后续可以配套工程：

```text
examples/18-code-loading-lab/
```

这个工程可以围绕 ClassLoader 打印、Dex 路径观察、反射查找、R8 keep 风险、模拟插件入口、so 加载信息和崩溃诊断卡做成一个可运行实验。

## 学习目标

学完本节后，你应该能够：

- 打印并解释当前类的 ClassLoader。
- 区分系统类和应用类的加载器。
- 观察 `Class.forName` 成功和失败的差异。
- 解释 R8 混淆和 keep 规则对反射的影响。
- 理解插件化最小模型与完整工程边界。
- 识别 native so 加载问题的第一证据。
- 写一份代码加载问题诊断报告。

## 第一部分：实践工程规划

第 18 章 demo 可以拆成这些可观察区域：

- `代码加载分数`：提示实验完成度。
- `ClassLoader 身份卡`：展示 Activity、Application、业务类、系统类的 ClassLoader。
- `Dex 路径观察卡`：展示 APK 路径、native library path、dexElements 概念。
- `R 直接调用 vs 反射查找`：对比普通调用和 `Class.forName`。
- `R8 / keep 风险卡`：模拟 release 中反射入口被混淆或 shrink 的风险。
- `MultiDex 观察卡`：说明 classes.dex、classes2.dex 和启动类边界。
- `Dalvik vs ART 对比卡`：对比早期虚拟机和现代运行时在 JIT、AOT、Profile、安装和启动上的取舍。
- `ART 运行时卡`：解释解释执行、JIT、AOT、Profile 和启动性能。
- `native so 观察卡`：展示 ABI、`System.loadLibrary`、so 缺失风险。
- `动态加载边界卡`：模拟插件入口类、插件资源、插件 so、插件生命周期四个边界。
- `代码加载诊断卡`：整理 ClassNotFound、NoSuchMethod、VerifyError、UnsatisfiedLinkError。
- `事件轨迹`：记录每次类加载、反射、诊断动作。

## 第二部分：代码加载通关路线

建议按三段完成：

```text
初级侦探：认识 ClassLoader
  -> 打印 Activity ClassLoader
      -> 打印系统类 ClassLoader
          -> 对比业务类和系统类
              -> 解释 PathClassLoader

中级侦探：观察反射和 R8 风险
  -> 普通调用一个类
      -> Class.forName 同一个类
          -> Class.forName 一个不存在的类
              -> 写出 keep 规则

高级侦探：解释插件化和 so
  -> 模拟插件入口
      -> 观察插件代码、资源、so、生命周期边界
          -> 阅读 native so 观察卡
              -> 阅读代码加载诊断卡
                  -> 写代码加载诊断报告
```

## 第三部分：手动实验路线

在配套工程创建之前，也可以先用任意项目做手动实验。

准备：

- 一个普通 Kotlin 类。
- 一个只通过反射访问的类。
- 一段 `Class.forName`。
- 一个不存在的类名。
- 一个 `System.loadLibrary` 搜索点。
- 一个 release minify 配置。
- 一份 keep 规则。

观察路线：

```text
打印 classLoader
  -> 打印 APK sourceDir
      -> 普通调用业务类
          -> 反射调用业务类
              -> 反射调用不存在类
                  -> 开启 release minify
                      -> 对比 mapping
                          -> 写诊断报告
```

## 第四部分：ClassLoader 观察

建议记录：

```text
Activity classLoader
Application classLoader
业务类 classLoader
String classLoader
Activity classLoader
packageCodePath
nativeLibraryDir
```

你要回答：

- 应用类和系统类是否来自同一个 ClassLoader？
- PathClassLoader 主要加载什么？
- 为什么类型身份和 ClassLoader 有关？
- 插件化为什么要设计公共 API 边界？

## 第五部分：反射与 R8 观察

建议记录：

```text
direct call result
Class.forName existing result
Class.forName missing result
release minify enabled
mapping file
keep rule
```

你要回答：

- 直接调用为什么比字符串式查找更容易被工具链理解？
- R8 为什么可能删除或改名反射入口？
- keep 规则应该保护类名、成员，还是注解入口？
- debug 正常 release 崩溃时先查什么？

## 第六部分：ART 与启动观察

建议记录：

```text
Dalvik 时代关键词
ART 时代关键词
第一次启动耗时
第二次启动耗时
启动路径类数量
Application 初始化任务
是否使用 Baseline Profile
```

你要回答：

- Dalvik 和 ART 都执行 Dex，核心差异是什么？
- 为什么很多老文章会强调 odex、dexopt、main dex 和热修复加载顺序？
- 第二次启动为什么可能更快？
- 类加载和 JIT / Profile 有什么关系？
- 哪些类不应该过早进入启动路径？

## 第七部分：native so 观察

建议记录：

```text
supported ABIs
nativeLibraryDir
APK lib 目录
System.loadLibrary 调用点
目标 so 是否存在
```

你要回答：

- `System.loadLibrary("foo")` 会找哪个文件？
- `UnsatisfiedLinkError` 的第一证据是什么？
- ABI 不匹配为什么只在部分设备崩溃？

## 第八部分：代码加载诊断报告

建议报告格式：

```text
操作：
目标类 / 方法 / so：
当前 ClassLoader：
dex / apk 路径：
是否通过反射：
是否开启 R8：
mapping 证据：
keep 规则：
依赖版本：
设备 ABI：
nativeLibraryDir：
我的结论：
仍不确定：
```

推荐 AOSP / Android 入口：

```text
libcore/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
libcore/dalvik/src/main/java/dalvik/system/DexPathList.java
art/
dalvik/system/DexClassLoader
dalvik/system/PathClassLoader
```

## 第九部分：本章通关检查

完成第 18 章后，请确认自己能回答：

- Dex 和 class 字节码有什么关系？
- D8 和 R8 分别负责什么？
- shrink、optimize、obfuscate 分别会影响什么？
- PathClassLoader 和 DexClassLoader 有什么区别？
- 为什么同名类由不同 ClassLoader 加载后可能不能强转？
- ART 的解释执行、JIT、AOT 和 Profile 大致解决什么问题？
- Dalvik 和 ART 的主要差异是什么？
- 为什么现代 ART 不是单纯 AOT？
- Baseline Profile 为什么能影响启动性能？
- `System.loadLibrary` 如何查找 so？
- `UnsatisfiedLinkError` 常见原因有哪些？
- 动态加载、插件化和热修复分别适合什么场景？
- 为什么插件化不只是 DexClassLoader？
- debug 正常 release 崩溃时如何排查？

## 本节小挑战

### 代码引擎室终局题

请为下面问题写一份诊断报告：

```text
debug 包正常
release 包启动后崩溃
崩溃信息：ClassNotFoundException: com.demo.generated.RouteTable
这个类只通过字符串反射访问
```

你需要回答：

- 问题更像代码缺失，还是混淆 / shrink？
- 如何确认 APK 中是否还有这个类？
- mapping 文件应该看什么？
- keep 规则应该如何设计？
- 如果这是路由生成类，构建任务是否在 release 中执行？

## 本节实践任务

### 基础任务

- 打印 Activity、Application、业务类、系统类 ClassLoader。
- 使用 `Class.forName` 查找一个存在类和一个不存在类。
- 查看 APK 中的 `classes.dex`。
- 查看 APK 中的 `lib/` 目录。
- 写一段 Dalvik vs ART 的对比说明。
- 写一份代码加载诊断报告。

### 进阶任务

- 开启 release minify，观察 mapping 文件。
- 给反射入口写 keep 规则。
- 查一次 dependencyInsight。
- 阅读 `BaseDexClassLoader` 和 `DexPathList`。
- 画出一个插件化最小结构图。
- 查阅一篇 Dalvik / ART 过渡时期的 MultiDex 或热修复文章，标注哪些结论今天仍然成立。

## 本节小结

第 18 章把“系统如何加载代码”从 APK 产物推进到运行时执行。你不需要一次读完 ART 源码，但应该能把 Dex、D8、R8、ClassLoader、Dalvik、ART、Profile、JNI、so、动态加载、插件化和代码加载问题诊断放在同一张地图上。到这里，Framework 阶段从一次点击、一次页面显示、一次资源读取，继续推进到一次类加载和一次代码执行。
