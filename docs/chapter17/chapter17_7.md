# 17.7 资源体验问题：NotFound、主题错乱、多语言失败、图片模糊与包体积

学资源系统，不只是为了知道 `R` 文件怎么来，更是为了排查真实问题。

资源问题常常看起来像 UI 问题、适配问题、构建问题，甚至线上崩溃：

```text
Resources.NotFoundException
按钮颜色突然不对
英文环境显示中文
图片在某些设备上发糊
APK 体积越来越大
debug 正常，release 资源找不到
```

本节把这些问题整理成诊断路线。

## 本节定位

本节是第 17 章的问题诊断章节。

它负责把前面学到的：

```text
R
resources.arsc
AssetManager
Resources
Configuration
Theme
资源合并
```

转化成实际排查能力。

## 学习目标

学完本节后，你应该能够：

- 按资源 ID、资源表、配置、主题、合并、包体积对问题分类。
- 能排查 `Resources.NotFoundException`。
- 能解释混淆、资源 shrink、资源名查找对线上问题的影响。
- 能分析多语言、夜间模式、图片密度和主题异常。
- 能初步定位资源包体积问题。

## 第一部分：Resources.NotFoundException

典型现象：

```text
android.content.res.Resources$NotFoundException
```

常见原因：

- 资源 ID 不存在。
- 资源类型不匹配。
- library 资源没有被正确依赖。
- 混淆、shrink、动态加载导致资源不在当前表里。
- 使用了错误包的资源 ID。

排查顺序：

```text
崩溃堆栈
  -> 资源 ID / 资源名
      -> 资源类型是否正确
          -> 当前模块是否能访问该资源
              -> APK 里是否真的包含它
                  -> 是否被资源 shrink 移除
```

这里要特别注意：代码混淆本身通常不是资源找不到的直接原因。真正危险的常常是：

```text
资源 shrink 移除了资源
getIdentifier 依赖字符串名字
资源名混淆改变了可读 entry name
动态加载只加载了部分资源表
```

如果代码直接使用：

```kotlin
R.string.title
```

运行时走的是资源 ID 查表。

如果代码使用：

```kotlin
resources.getIdentifier("title", "string", packageName)
```

运行时先要靠字符串找到资源 ID。资源名混淆、shrink、包名错误或拼写错误都会让这条路径更脆弱。

## 第二部分：多语言失败

典型现象：

```text
系统切到英文，页面仍显示中文。
```

常见原因：

- 没有提供 `values-en`。
- 默认文案写死在代码里。
- 资源 key 在不同语言文件中不一致。
- App 内语言切换后仍使用旧 Context。
- 文案在 ViewModel 或单例中被缓存。

排查顺序：

```text
确认当前 locale
  -> 检查对应 values-xx
      -> 检查 key 是否一致
          -> 检查是否硬编码
              -> 检查 Context / Configuration 是否更新
                  -> 检查是否缓存旧字符串
```

## 第三部分：夜间模式和主题错乱

典型现象：

```text
深色模式下文字看不清。
Dialog 颜色和页面不一致。
自定义 View 不跟随主题变化。
```

常见原因：

- 硬编码颜色。
- 缺少 `values-night`。
- 使用 Application Context 创建 UI。
- XML Theme 和 Compose MaterialTheme 没有对齐。
- 自定义 View 没有从 Theme 读取 attr。

排查顺序：

```text
是否硬编码颜色
  -> 是否有 night 资源
      -> 当前 Context 是否带正确 Theme
          -> 是否从 attr 读取
              -> Compose / View 主题是否一致
```

## 第四部分：图片模糊、过大或失真

典型现象：

```text
某些设备图片发糊。
图标显示太大或太小。
同一张图在不同屏幕表现不一致。
```

常见原因：

- 只提供低密度 bitmap。
- 图片放错 `drawable-*dpi` 目录。
- 没有使用 vector drawable。
- ImageView / Compose Image 缩放策略不合理。
- 把 px 当成 dp 使用。

排查顺序：

```text
资源目录
  -> 图片真实像素
      -> 设备 density
          -> 控件尺寸约束
              -> scaleType / contentScale
                  -> 是否更适合 vector
```

## 第五部分：资源包体积膨胀

资源也会拖慢交付。

常见体积来源：

- 多套大图。
- 未压缩图片。
- 重复资源。
- 未使用资源。
- 多语言资源过多。
- raw / assets 中放了大文件。

排查入口：

- APK Analyzer。
- Android Studio App Size 分析。
- resource shrink。
- 图片压缩工具。
- 资源命名和引用扫描。

包体积优化不是“删图比赛”，而是确认每个资源是否真的被业务需要。

## 第六部分：混淆、shrink 与字符串式资源查找

很多线上资源问题发生在 release 包，因为 release 往往会开启：

```text
代码混淆
资源 shrink
资源压缩
资源名混淆或资源重排
```

要分清它们的影响：

| 机制 | 主要改变 | 常规 R 引用是否容易受影响 | `getIdentifier` 是否容易受影响 |
| --- | --- | --- | --- |
| 代码混淆 | class / method / field 名称 | 通常不影响，核心仍是 int ID | 间接影响较小 |
| 资源 shrink | 移除未使用资源 | 如果误删会影响 | 很容易误判动态资源未使用 |
| 资源名混淆 | entry 名称和文件路径可读性 | 保持映射正确时通常不影响 | 容易影响字符串按名查找 |
| 动态资源加载 | 资源表来源变化 | 取决于是否加载完整资源表 | 更容易受包名、名字、路径影响 |

这就是为什么大型项目里要谨慎使用：

```kotlin
getIdentifier(name, type, packageName)
```

它绕开了编译期 `R` 引用的安全网，把问题推迟到运行时。

更稳的策略是：

- 优先直接使用 `R.xxx.xxx`。
- 动态资源名必须有 keep 规则和映射表。
- release 包必须验证资源 shrink 结果。
- 线上日志里同时记录资源 ID、资源名、包名和版本。

## 第七部分：依赖冲突导致的运行时错觉

资源依赖冲突不一定都表现为构建失败，有时表现为“运行时拿到的不是你以为的那份资源”。

典型现象：

```text
某个库升级后按钮颜色变了。
debug 包文案正确，release 包文案不对。
一个 feature 改了资源，另一个 feature 页面也变了。
```

这类问题要回到最终资源表看：

```text
当前资源 ID
  -> resourceName
      -> merged resources 来源
          -> source set / dependency
              -> 是否被 app 或 variant 覆盖
```

不要只在当前模块搜资源名。最终 APK 里的资源可能来自依赖库、flavor、buildType 或 app 覆盖。

## 第八部分：资源问题诊断表

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| `NotFoundException` | 崩溃栈和资源 ID | 资源不存在、类型不匹配、被 shrink | 查资源表、模块依赖和 APK 内容 |
| 多语言失败 | locale 和 strings 目录 | 缺少语言资源、Context 未更新 | 补资源、重建界面、避免缓存旧文案 |
| 深色模式错乱 | 当前 Theme 和 night 资源 | 硬编码颜色、主题不一致 | 使用 attr，补 `values-night` |
| 图片模糊 | density 和图片目录 | 低密度图片、目录错误 | 补高密度或 vector，修正尺寸策略 |
| 包体积过大 | APK Analyzer | 大图、重复、未使用资源 | 压缩、删除、开启 shrink、资源治理 |
| release 缺资源 | debug/release APK 对比 | shrink、资源名混淆、动态引用 | 加 keep 规则，减少字符串式查找 |
| 资源值被覆盖 | merged resources 来源 | variant、app、依赖库覆盖 | 查 source set 优先级和依赖树 |

## 本节小挑战

### 资源问题分类题

请把下面问题归类：

- 某个页面线上崩溃 `Resources.NotFoundException`。
- 德语环境某个按钮仍显示英文。
- 深色模式下输入框边框不可见。
- xxhdpi 设备上图标发糊。
- APK 中 assets 目录有 30MB 测试文件。
- debug 能找到资源，release 通过 `getIdentifier` 找不到。
- 升级一个 UI 库后，全局按钮颜色发生变化。

分别判断第一证据应该从哪里找。

## 本节实践任务

### 基础任务

- 制造一次资源名写错的编译错误。
- 制造一次读取错误类型资源的问题。
- 切换深色模式观察主题变化。

### 进阶任务

- 使用 APK Analyzer 查找最大资源文件。
- 删除一个未使用图片，观察包体积变化。
- 在 release 配置中观察资源 shrink 对动态资源名的影响。
- 查一次 merged resources，找出某个颜色最终来自哪个模块。
- 写一份资源问题诊断报告。

## 本节小结

资源问题往往跨越构建期、运行时、UI 适配和工程治理。排查时不要只看控件表现，要把问题翻译成资源 ID、资源表、Configuration、Theme、资源合并和 APK 内容证据。能把这些证据串起来，才算真正掌握资源系统。
