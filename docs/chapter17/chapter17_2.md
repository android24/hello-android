# 17.2 从 res 到 R 文件：AAPT2、资源 ID 与 resources.arsc

本节开始进入第 17 章最核心的一条链路：

```text
res 目录里的资源
  -> AAPT2 编译
      -> link 阶段分配资源 ID
          -> 生成 R 文件
              -> 生成 resources.arsc
                  -> 运行时通过 Resources 查表
```

这节不能只停留在“R 文件会自动生成”。真正要讲清楚的是：

```text
R.string.app_name
```

究竟如何和 APK 里的资源表匹配起来。

## 本节定位

本节负责回答：

- AAPT2 到底做了什么？
- `R` 文件为什么能代表资源？
- 资源 ID 为什么是一个整数？
- `R.string.app_name` 和 `resources.arsc` 之间如何匹配？
- 代码混淆、资源 shrink、资源名混淆后，系统为什么还能找到资源，或者为什么会找不到？
- 为什么资源命名、重复资源、类型错误会在编译期暴露？

## 学习目标

学完本节后，你应该能够：

- 理解 AAPT2 的 compile 和 link 两个阶段。
- 知道资源 ID 的 `0xPPTTEEEE` 结构。
- 解释 `R` 文件和 `resources.arsc` 为什么必须来自同一次资源链接结果。
- 能说明运行时 `Resources` 如何通过资源 ID 查表。
- 能区分代码混淆、资源 shrink、资源名混淆对资源查找的不同影响。

## 第一部分：资源编译不是复制文件

你在工程里看到的是：

```text
res/values/strings.xml
res/drawable/icon.xml
res/layout/activity_main.xml
```

代码里看到的是：

```kotlin
R.string.app_name
R.drawable.icon
R.layout.activity_main
```

它们之间不是靠运行时“按文件名搜索”连起来的，而是靠构建期的资源编译和链接。

Android 构建资源时，大致可以拆成两步。

### AAPT2 compile

`compile` 阶段更关注单个资源文件：

```text
扫描 res 文件
  -> 校验文件名和 XML 结构
      -> 识别资源类型与资源名
          -> 编译 XML / value / drawable 等资源
              -> 生成中间产物
                  -> 记录资源符号
```

例如：

```xml
<string name="app_name">Hello Android</string>
```

会被识别成：

```text
type = string
name = app_name
value = Hello Android
```

此时它已经不再只是普通 XML 文本，而是可以参与后续资源链接的符号。

### AAPT2 link

`link` 阶段才会把所有资源放到一起：

```text
app 资源
  + library 资源
      + flavor 资源
          + buildType 资源
              + Manifest 引用
                  -> 合并符号
                      -> 解决引用
                          -> 分配最终资源 ID
                              -> 生成 R 文件
                                  -> 生成 resources.arsc
```

很多资源问题发生在 link 阶段，因为这时才知道最终 App 里到底有哪些资源、哪些重复、哪些覆盖、哪些引用找不到。

## 第二部分：资源的身份不是文件路径

资源系统关心的核心身份通常是：

```text
package + type + entry
```

例如：

```text
com.example.app:string/app_name
com.example.app:drawable/icon
android:color/transparent
```

源码里的目录只是输入形式：

```text
res/values/strings.xml
res/drawable/icon.xml
```

编译后，资源会进入资源表。运行时真正用于快速查找的是资源 ID。

## 第三部分：R 文件是代码侧索引

`R` 文件会把资源名字变成代码能引用的常量。

例如：

```kotlin
R.string.app_name
R.color.primary
R.drawable.ic_launcher
```

可以先理解成：

```text
资源类型 + 资源名字 -> 一个整数 ID
```

更具体一点：

```text
R.string.app_name = 0x7f100001
```

代码最终带到运行时的关键不是字符串 `"app_name"`，而是这个整数 ID。

所以常规资源读取不是：

```text
按照名字 app_name 搜文件
```

而是：

```text
拿 0x7f100001 去资源表中查 package / type / entry
```

## 第四部分：资源 ID 的结构

Android 资源 ID 通常可以按这个直觉理解：

```text
0xPPTTEEEE

PP   = package id
TT   = type id
EEEE = entry id
```

含义是：

- package id：资源属于哪个资源包。
- type id：资源类型，比如 string、drawable、layout。
- entry id：某个类型下的具体条目。

常见直觉：

```text
android 系统资源 package id 常见是 0x01
应用自己的资源 package id 常见是 0x7f
```

例如：

```text
0x7f100001
  -> 0x7f：当前 App 的资源包
  -> 0x10：某个资源类型
  -> 0x0001：该类型下某个条目
```

不要死背 `0x10` 一定代表什么类型。类型编号来自本次资源链接结果。你真正要掌握的是：资源 ID 本身已经携带了查表所需的索引信息。

## 第五部分：R 和 resources.arsc 如何匹配

这是本节最重要的原理。

假设源码里有：

```xml
<!-- res/values/strings.xml -->
<string name="app_name">Hello Android</string>
```

AAPT2 link 阶段给它分配最终资源 ID：

```text
string/app_name -> 0x7f100001
```

然后同一份链接结果会写到两个地方。

代码侧：

```text
R.string.app_name = 0x7f100001
```

资源表侧：

```text
resources.arsc
  -> package 0x7f
      -> type string
          -> entry app_name
              -> id 0x7f100001
              -> value "Hello Android"
              -> config default / zh / en / night ...
```

所以 `R` 文件和 `resources.arsc` 的匹配，不是运行时临时通过名字猜出来的，而是在构建期由同一次 link 结果确定的。

核心关系是：

```text
代码里的 int ID
  == resources.arsc 里的 package/type/entry 索引
```

当你调用：

```kotlin
resources.getString(R.string.app_name)
```

运行时大致会走：

```text
0x7f100001
  -> 解析 package id = 0x7f
      -> 找到当前 App 资源表
          -> 解析 type id
              -> 确认目标类型是 string
                  -> 解析 entry id
                      -> 找到 app_name 这条 entry
                          -> 根据 Configuration 选择最匹配 value
                              -> 返回字符串
```

如果你拿 string ID 去读取 drawable，或者拿错误 package 的 ID 去当前资源表查，就可能出现类型错误或 `Resources.NotFoundException`。

## 第六部分：为什么同一个 ID 可以返回不同语言

同一个 entry 可以有多个配置版本。

例如：

```text
string/app_name
  -> default: "Hello Android"
  -> zh: "你好 Android"
  -> en: "Hello Android"

color/page_background
  -> default: #FFFFFF
  -> night: #111111
```

它们通常共享同一个逻辑资源 ID。

变化的是：

```text
当前 Configuration
```

所以：

```kotlin
R.string.app_name
```

在中文和英文环境下 ID 可以不变，但返回值不同。

资源系统不是给每种语言生成一套代码常量，而是在资源表里为同一个条目保存多个配置版本，运行时按当前语言、夜间模式、密度、横竖屏等配置选择最合适的版本。

## 第七部分：混淆后为什么还能找到资源

这里必须区分三件事。

### 代码混淆

R8 / ProGuard 主要处理 class、method、field 等代码符号。

常规资源读取依赖的是：

```text
int resourceId
```

不是 Java / Kotlin 字段名本身。

也就是说，代码混淆后类名、字段名可能变化，但只要编译后的代码里使用的资源 ID 仍然和 `resources.arsc` 一致，运行时就能找到资源。

可以这样理解：

```text
门牌号没变，路牌名字缩短了也能进门。
```

### 资源 shrink

资源 shrink 是另一回事。

它会分析哪些资源看起来没有被使用，然后从最终包里移除。

风险在于动态引用：

```kotlin
resources.getIdentifier("some_name", "string", packageName)
```

这类代码不是直接引用 `R.string.some_name`，静态分析可能看不出它使用了哪个资源。如果资源被 shrink 移除，运行时就可能找不到。

这时需要 keep 规则，或者减少字符串式资源查找。

### 资源名混淆

有些工具会做资源名混淆，例如把：

```text
res/drawable/course_card_background.xml
```

改成更短的路径或名字。

只要工具同时更新：

```text
resources.arsc
XML 引用
文件路径
资源映射表
```

并保持资源 ID 或引用关系一致，常规 `R.xxx.xxx` 读取仍然可以工作。

但下面这些能力会变脆：

- `getIdentifier("course_card_background", "drawable", packageName)`。
- `getResourceName(id)` 返回的人类可读名称。
- 线上崩溃日志中根据资源名排查。
- WebView、远端配置、插件协议中硬编码的资源名字。

所以资源混淆真正改变的是“名字可读性和字符串查找稳定性”，不是常规 ID 查表路径。

## 第八部分：为什么资源编译会失败

常见资源编译错误包括：

| 现象 | 可能原因 | 排查入口 |
| --- | --- | --- |
| resource not found | 引用了不存在的资源 | 检查资源名称、模块依赖、命名空间 |
| duplicate resources | 同类型同名资源重复 | 检查 main/res、flavor、library 合并结果 |
| invalid file name | 文件名不符合规则 | 资源名只能使用小写字母、数字和下划线 |
| XML 解析失败 | 标签、属性或命名空间错误 | 打开具体 XML 文件定位行号 |
| style 属性不存在 | 主题或依赖版本不匹配 | 检查 Material / AppCompat / SDK 版本 |
| release 资源找不到 | shrink 或资源名混淆影响动态引用 | 检查 keep 规则、mapping、APK 内容 |

资源编译错误不是坏消息。它是在 App 运行前帮你拦下一批问题。

## 第九部分：多模块下的 R 文件

现代 Android 工程常常是多模块。

你会遇到：

```text
app 模块的 R
feature 模块的 R
library 模块的 R
```

如果开启了：

```properties
android.nonTransitiveRClass=true
```

那么模块只能直接访问自己声明的资源，不能随意跨模块访问依赖模块的资源。

这能让资源边界更清晰，也能减少“一个模块偷偷依赖另一个模块资源”的情况。

这里也要强调一个原理：`namespace` 主要影响源码中生成的 `R` 类位置，最终 APK 里的资源仍然会经过 app link 阶段统一处理。多模块中的资源不是各自带着独立小账本运行，而是最终被链接进应用的资源表。

## 本节小挑战

### R 文件追踪题

选择一个资源：

```text
R.string.app_name
```

请追踪它：

```text
源文件在哪里？
生成后的 R 常量在哪里被引用？
APK 里能否找到对应资源表？
它的资源 ID 是多少？
ID 的 package / type / entry 分别是什么？
如果删掉源文件，编译会报什么？
如果改名但不改代码，会报什么？
如果通过 getIdentifier 字符串查找，资源 shrink 后会发生什么？
```

## 本节实践任务

### 基础任务

- 新增一个 `string` 资源，并在代码里引用它。
- 故意写错资源名，观察编译错误。
- 新增一个同名资源，观察是否会冲突。
- 使用 `resources.getResourceName(id)`、`getResourceTypeName(id)`、`getResourceEntryName(id)` 反查资源 ID。

### 进阶任务

- 打开 `build/generated` 目录，寻找生成的 R 相关文件。
- 使用 APK Analyzer 查看 `resources.arsc`。
- 在多模块工程里观察 `nonTransitiveRClass` 对资源访问的影响。
- 对比直接使用 `R.string.xxx` 和 `getIdentifier` 的差异。

## 本节小结

AAPT2 把 `res` 目录里的资源编译成资源符号，再在 link 阶段分配最终资源 ID，并把同一份结果写入 `R` 文件和 `resources.arsc`。代码里的 `R.string.app_name` 本质是一个整数 ID，运行时通过 `0xPPTTEEEE` 解析 package、type、entry，再到资源表里查找最终值。代码混淆通常不影响这条 ID 查表路径；资源 shrink 和资源名混淆影响的是资源是否还在包里、名字是否还能被字符串方式查到。理解这一步后，资源编译、混淆、查找和线上资源问题都会有清晰得多的原理支撑。
