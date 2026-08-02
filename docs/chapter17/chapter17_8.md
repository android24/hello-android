# 17.8 综合实践：资源系统、主题与配置观察实验

第 17 章最后一节，我们把 `R` 文件、资源 ID、`AssetManager`、`Resources`、`Configuration`、Theme、资源限定符和资源问题诊断放进一个观察实验。

目标是：让你能从一次资源读取和一次配置变化，解释系统如何选出最终资源。

## 本节剧情钩子

现在你已经从包管理登记处走到了资源仓库。

你不再只问：

```text
系统为什么知道这个 App 存在？
```

而是继续追问：

```text
系统为什么知道这个字符串在哪里？
`R.string.xxx` 和 `resources.arsc` 到底凭什么能对上？
为什么同一个资源 ID 在中英文环境下返回不同文案？
为什么同一个图片在不同密度设备上表现不同？
为什么换了主题，按钮颜色也变了？
为什么混淆后资源还能找到，而 getIdentifier 有时会翻车？
为什么资源找不到会崩溃？
```

本节要做的，就是把这些问题串成一张资源系统地图。

你可以把自己当成资源仓库管理员。

第 16 章负责追踪“系统如何认识 App”，第 17 章负责追踪“系统如何认识 App 里的资源”。没有资源系统，界面就只剩代码骨架；有了资源系统，App 才能适配语言、密度、主题和屏幕环境。

## 本节定位

本节是第 17 章综合实践。

后续可以配套工程：

```text
examples/17-resource-system-lab/
```

这个工程可以围绕资源 ID、字符串切换、图片密度、Theme attr、Configuration、assets/raw 和资源诊断卡做成一个可观察实验室。

## 学习目标

学完本节后，你应该能够：

- 使用 `Resources` 读取 string、color、dimension、drawable。
- 打印并解释当前 `Configuration`。
- 观察 locale、night mode、orientation、density 对资源选择的影响。
- 从 Theme 中读取 attr。
- 区分 `assets` 和 `res/raw` 的读取方式。
- 写一份资源问题诊断报告。

## 第一部分：实践工程规划

第 17 章 demo 建议拆成这些可观察区域：

- `资源观察分数`：提示实验完成度。
- `资源身份证`：展示 packageName、resource id、resource name、resource type，并拆解 `0xPPTTEEEE`。
- `AAPT2 匹配卡`：展示源码资源名、R 常量、资源 ID、resources.arsc 条目的对应关系。
- `Configuration 面板`：展示 locale、densityDpi、uiMode、orientation、fontScale。
- `字符串多语言实验区`：对比默认、中文、英文资源。
- `图片密度实验区`：展示当前 density 与图片资源选择推断。
- `Theme attr 实验区`：读取 colorPrimary、colorSurface、textColor 等属性。
- `assets / raw 实验区`：对比路径读取和资源 ID 读取。
- `混淆与 shrink 观察卡`：对比 `R.xxx` 直接引用和 `getIdentifier` 字符串查找的风险。
- `依赖覆盖观察卡`：记录某个资源最终来自 main、debug、flavor 还是依赖库。
- `资源问题诊断卡`：整理 NotFound、多语言失败、主题错乱、图片模糊和包体积问题。
- `资源事件轨迹`：记录每次资源读取和配置观察结果。

它的目标不是模拟完整资源编译器，而是把 App 侧能观察到的资源系统证据整理出来。

## 第二部分：资源侦探通关路线

建议按三段路线完成：

```text
初级侦探：读取资源身份证
  -> 查询 R.string.app_name
      -> 打印资源 ID
          -> 拆解 package / type / entry
              -> 读取 resourceName / resourceType
                  -> 观察 getString 返回值

中级侦探：观察配置选择
  -> 打印 Configuration
      -> 切换语言或夜间模式
          -> 观察字符串和颜色变化
              -> 对比 Resources 返回值

高级侦探：解释主题和资源问题
  -> 读取 Theme attr
      -> 对比 assets / raw
          -> 对比 R 直接引用和 getIdentifier
              -> 分析图片密度
                  -> 阅读诊断卡
                      -> 写一份资源系统诊断报告
```

读者不是“背资源目录规则”，而是在复原系统如何为当前设备选择资源。

## 第三部分：手动实验路线

在配套工程创建之前，也可以先用任意项目做手动实验。

准备：

- 一个 `string` 资源。
- 一组 `values` / `values-en` / `values-night`。
- 一张 bitmap 或 vector 图片。
- 一个自定义 theme attr。
- 一个 `assets/config.json`。
- 一个 `res/raw/sample.txt`。
- 一个通过 `getIdentifier` 动态查找的资源名。

观察路线：

```text
打印 R 资源 ID
  -> 拆解 0xPPTTEEEE
      -> 读取 resourceName / resourceType
          -> 打印 Configuration
              -> 切换语言或夜间模式
                  -> 对比 R 引用与 getIdentifier
                      -> 读取 Theme attr
                          -> 对比 assets 与 raw
                              -> 写诊断报告
```

## 第四部分：资源 ID 观察

建议记录：

```text
R.string.app_name
resources.getResourceName(id)
resources.getResourceTypeName(id)
resources.getResourceEntryName(id)
resources.getString(id)
id package/type/entry 拆解
```

你要回答：

- 资源 ID 是不是资源内容本身？
- resourceName 里包含哪些信息？
- `R` 文件和 `resources.arsc` 是否来自同一次 link 结果？
- 如果 ID 类型和读取 API 不匹配会怎样？
- release 包中资源名是否总能保留？

## 第五部分：Configuration 观察

建议记录：

```text
Locale
orientation
densityDpi
fontScale
uiMode
screenWidthDp
screenHeightDp
```

观察：

- 当前语言影响了哪个 `values-*` 目录。
- 夜间模式影响了哪个 `values-night` 目录。
- 横竖屏影响了哪些 layout / values 资源。
- densityDpi 影响了图片选择和尺寸换算。

Configuration 是资源选择的现场天气。

## 第六部分：Theme attr 实验

建议读取：

```text
?attr/colorPrimary
?attr/colorSurface
?android:attr/textColorPrimary
```

观察：

- Activity Context 是否能读到。
- Application Context 是否读到相同值。
- `ContextThemeWrapper` 是否能改变结果。
- night 资源是否改变属性最终值。

Theme attr 是理解主题系统的关键入口。

## 第七部分：资源诊断卡

demo 可以把常见问题做成诊断卡：

| 现象 | 第一证据 | 可能原因 | 修复方向 |
| --- | --- | --- | --- |
| 资源找不到 | 资源 ID、资源名、APK 内容 | 资源不存在、类型不匹配、被 shrink | 检查 R、resources.arsc、模块依赖 |
| 多语言失败 | locale、values-xx、Context | 缺少语言资源、缓存旧字符串 | 补资源，更新 Context 和 UI 状态 |
| 主题错乱 | Theme attr、Context | 用错 Context、硬编码颜色 | 使用 Activity Context 和 attr |
| 图片模糊 | densityDpi、图片目录 | 低密度资源、缩放策略错误 | 补合适资源或使用 vector |
| 包体积过大 | APK Analyzer | 大图、重复、未使用资源 | 压缩、删除、开启 shrink |
| release 动态资源找不到 | getIdentifier 返回 0 | shrink、资源名混淆、包名错误 | 使用 R 引用、keep 规则和映射表 |
| 资源值被覆盖 | merged resources 来源 | app、variant、依赖库覆盖 | 查 source set 优先级和依赖树 |

## 第八部分：资源系统诊断报告

建议报告格式：

```text
操作：
目标资源 ID：
资源名称：
资源类型：
当前 Configuration：
Theme attr：
Resources 返回值：
R 与 resources.arsc 匹配证据：
getIdentifier 结果：
可能的资源目录：
是否经过 shrink / 资源名混淆：
资源最终来源：
我的结论：
仍不确定：
```

推荐 AOSP 入口：

```text
frameworks/base/core/java/android/content/res/Resources.java
frameworks/base/core/java/android/content/res/AssetManager.java
frameworks/base/core/java/android/content/res/Configuration.java
frameworks/base/core/java/android/content/res/ResourcesImpl.java
frameworks/base/core/java/android/content/res/ResourcesManager.java
frameworks/base/libs/androidfw/ResourceTypes.cpp
frameworks/base/tools/aapt2/
```

## 第九部分：本章通关检查

完成第 17 章后，请确认自己能回答：

- 为什么 Android 资源需要编译？
- `R` 文件和 `resources.arsc` 分别负责什么？
- `R.string.app_name` 和资源表条目是如何匹配的？
- 代码混淆、资源 shrink、资源名混淆分别会影响什么？
- `AssetManager` 和 `Resources` 的分工是什么？
- `Configuration` 如何影响资源选择？
- 多语言、夜间模式、密度和横竖屏资源如何生效？
- `Theme`、`Style`、`Attribute` 有什么关系？
- 多模块资源为什么会冲突？
- 资源覆盖优先级为什么会让最终资源和源码直觉不同？
- 资源找不到、主题错乱、图片模糊应该如何排查？

## 本节小挑战

### 资源仓库终局题

请为下面路径写一份资源系统诊断报告：

```text
新增 values-en 字符串
  -> 切换 App 语言后页面仍显示中文
      -> 重新创建 Activity 后恢复正常
```

你需要回答：

- 问题更像资源缺失，还是 Context / Configuration 没更新？
- 哪些字符串可能被缓存了？
- 哪些 UI 状态需要重新读取资源？
- 如果是 Compose 页面，状态和资源读取应该如何组织？

## 本节实践任务

### 基础任务

- 打印一个 string 资源的 ID、name、type、entry。
- 拆解一次资源 ID 的 package / type / entry。
- 打印当前 `resources.configuration`。
- 新增 `values-night`，观察颜色变化。
- 从 Theme 中读取一个 attr。
- 对比 `assets` 和 `res/raw` 的读取方式。
- 对比一次 `R.string.xxx` 和 `getIdentifier` 查找。

### 进阶任务

- 创建一个 `ContextThemeWrapper`，观察同一个 attr 的返回差异。
- 使用 `createConfigurationContext` 临时切换 locale，读取同一个 string。
- 使用 APK Analyzer 查看资源体积。
- 找出一个资源在 merged resources 中的最终来源。
- 写一份资源问题诊断报告。

## 本节小结

第 17 章把“系统如何读取资源”从源码目录推进到运行时资源选择。你不需要一次读完 AAPT2 和 ResourceTypes，但应该已经能把 `R`、`resources.arsc`、`AssetManager`、`Resources`、`Configuration`、Theme、资源合并和资源问题诊断放在同一张地图上。到这里，Framework 入门链路不只覆盖 App 如何被系统识别，也覆盖了 App 如何在不同设备环境下呈现合适的内容和视觉状态。
