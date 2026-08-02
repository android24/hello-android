# 17.5 Theme、Style 与 Attribute：界面气质如何被资源系统塑形

资源系统不只负责字符串和图片，也负责 App 的视觉气质。

你看到的按钮颜色、页面背景、文字颜色、状态栏颜色，很多都不是控件自己决定的，而是来自：

```text
Theme
  -> Style
      -> Attribute
          -> Resource value
```

本节我们把主题系统拆开。

## 本节定位

本节负责回答：

- `style` 和 `theme` 有什么区别？
- `attr` 是什么？
- 为什么 Activity Context 和 Application Context 读取主题属性结果不同？
- 为什么自定义 View 应该从 Theme 读取颜色，而不是硬编码？
- 动态资源替换应该从哪些入口做，哪些方案不适合普通业务代码？

## 学习目标

学完本节后，你应该能够：

- 区分 `Theme`、`Style`、`Attribute` 和普通资源值。
- 理解主题属性解析的大致流程。
- 能排查主题错乱、颜色不对、控件样式不生效的问题。
- 知道为什么 Material / AppCompat / Compose 都会重度使用主题。
- 能区分 Theme 切换、资源提供器、配置 Context、AssetManager 插件化和 RRO 的适用边界。

## 第一部分：Style 是一组属性集合

`style` 可以理解为一组属性集合：

```xml
<style name="CardTitleText">
    <item name="android:textSize">18sp</item>
    <item name="android:textStyle">bold</item>
</style>
```

它可以被某个 View 使用：

```xml
<TextView
    style="@style/CardTitleText" />
```

这类 style 更像局部样式。

## 第二部分：Theme 是全局样式环境

`theme` 也是 style，但它的作用范围更大。

它通常挂在 Application 或 Activity 上：

```xml
<application
    android:theme="@style/AppTheme" />
```

Theme 会影响：

- Activity 窗口。
- 默认字体和颜色。
- 控件默认样式。
- 状态栏和导航栏。
- Dialog、PopupWindow、Menu 等 UI 元素。

你可以把 Theme 理解成当前界面的“天气系统”：很多控件不直接问你要颜色，而是从天气里感知默认颜色。

## 第三部分：Attribute 是可被主题填充的插槽

`attr` 是属性名。

例如：

```xml
?attr/colorPrimary
?attr/colorSurface
?android:attr/textColorPrimary
```

它不是具体颜色，而是一个插槽。

真正的值来自当前 Theme：

```text
?attr/colorPrimary
  -> 当前 Theme
      -> 找到 colorPrimary 的具体资源
          -> 返回颜色值
```

这就是为什么同一个控件在不同主题下可以自动换颜色。

## 第四部分：一次主题属性解析

当你读取：

```kotlin
?attr/colorPrimary
```

大致可以理解成：

```text
Context
  -> Theme
      -> 查找 attr
          -> 找到引用的 resource
              -> Resources 解析最终值
```

如果 Context 没有正确主题，就可能读不到属性。

这就是为什么：

- `Activity` Context 往往能读到界面主题。
- `Application` Context 可能缺少某些 Activity 主题属性。
- `ContextThemeWrapper` 可以临时包一层主题。

## 第五部分：动态资源替换的几种方式

动态资源替换不是“运行时修改 R 文件”。

`R` 文件里的资源 ID 是构建期产物，运行时真正能变化的是：

```text
同一个资源 ID 在不同 Configuration 下选哪个 value
同一个 attr 在不同 Theme 下解析到哪个资源
同一个业务槽位在你的 ResourceProvider 中映射到哪个资源 ID
AssetManager 当前加载了哪些资源路径
系统 RRO 覆盖了哪些目标资源
```

所以动态资源替换要先分清层级。

### 方式一：切换 Theme

最常见、最安全的方式是让界面读 `?attr`，然后切换 Theme。

```text
Button 背景
  -> ?attr/resourceLabPanelColor
      -> Theme A: @color/resource_lab_surface
      -> Theme B: @color/resource_lab_accent
```

优点：

- 不破坏资源 ID。
- 不依赖隐藏 API。
- 适合换肤、品牌色、深色模式、局部样式切换。

代价：

- 控件必须从 Theme attr 读取值。
- 硬编码颜色不会自动变。
- 切换后可能需要 Activity recreate 或触发 UI 重组。

### 方式二：业务 ResourceProvider

如果替换的是业务文案、图标、卡片颜色，可以建立一个资源提供器。

```kotlin
data class SkinResources(
    val title: Int,
    val panelColor: Int,
    val icon: Int
)
```

运行时切换的是映射关系：

```text
default skin
  -> title = R.string.default_title
  -> panelColor = R.color.default_panel

festival skin
  -> title = R.string.festival_title
  -> panelColor = R.color.festival_panel
```

优点：

- 简单、可测试、可回滚。
- 资源仍然来自编译期 R，安全稳定。
- Compose 和 View 都容易接入。

代价：

- 所有可替换资源都要经过统一入口。
- 不能替换未知 APK 里的任意资源。

### 方式三：Configuration Context

多语言、字体缩放、夜间模式这类更适合走 `Configuration`。

```text
createConfigurationContext(newConfig)
  -> 新 Resources
      -> 同一个 R.string.title
          -> 按新的 locale 选择 value
```

这不是换资源 ID，而是换资源选择环境。

适合：

- App 内语言切换。
- 局部 locale 预览。
- 特定配置下的资源观察实验。

要注意：

- 不要长期混用旧 Context 和新 Context。
- 文案不要永久缓存到单例。
- UI 需要重新读取资源。

### 方式四：AssetManager 加载外部资源路径

插件化和部分换肤方案会尝试给 `AssetManager` 添加新的资源路径。

```text
host AssetManager
  + skin.apk resources
      -> 构造新的 Resources
          -> 用名称或映射表读取外部资源
```

这类方案更接近框架/基础设施能力，不适合普通业务随手使用。

风险包括：

- 资源 ID 不同包之间不稳定。
- 隐藏 API 和系统版本兼容问题。
- 资源名混淆后字符串查找容易失效。
- 宿主和皮肤包的版本映射要严格管理。

如果项目没有插件化或独立皮肤包需求，优先不要走这条路。

### 方式五：Runtime Resource Overlay

RRO 是系统层资源覆盖机制，常见于系统主题、厂商定制和 AOSP 层能力。

它的思路是：

```text
overlay package
  -> 声明覆盖 target package 的某些资源
      -> 系统资源管理层应用覆盖关系
```

普通 App 通常不会把 RRO 当作业务换肤方案。它更适合系统应用、设备定制、Framework 资源覆盖场景。

## 第六部分：动态资源替换的选择建议

| 场景 | 推荐方案 | 原因 |
| --- | --- | --- |
| 深色模式 | `values-night` + Theme | 系统 Configuration 原生支持 |
| 品牌色换肤 | Theme attr / Compose Theme | 控件读取 attr 后可统一变化 |
| 业务活动皮肤 | ResourceProvider | 映射清晰，避免隐藏 API |
| App 内语言切换 | Configuration Context | 同一资源 ID 按 locale 选值 |
| 插件皮肤包 | 独立资源包 + AssetManager 方案 | 需要基础设施和版本映射 |
| 系统级主题覆盖 | RRO | 属于系统层资源覆盖能力 |

一句话：

```text
能用 Theme 和 ResourceProvider，就不要急着碰 AssetManager 外部路径。
能用 Configuration，就不要手动缓存一堆语言字符串。
需要插件资源包时，必须设计资源名、版本和映射协议。
```

## 第七部分：主题错乱的常见原因

| 现象 | 可能原因 | 排查入口 |
| --- | --- | --- |
| 按钮颜色不对 | Theme 属性未配置或被覆盖 | 查 `colorPrimary`、控件 style、Material theme |
| 深色模式文字看不清 | 硬编码颜色或缺少 night 资源 | 查 `values-night` 和固定色值 |
| Dialog 样式异常 | 使用了错误 Context | 确认使用 Activity Context 或主题包装 Context |
| 自定义 View 颜色不随主题变 | 直接写死颜色 | 改为读取 `?attr` |
| Compose 与 View 颜色不一致 | 两套主题未对齐 | 对照 Compose MaterialTheme 和 XML Theme |

主题问题最怕只盯某个控件。要从当前 Context 和 Theme 往下查。

## 第八部分：Compose 里的主题

Compose 也有主题：

```kotlin
MaterialTheme(
    colorScheme = ...,
    typography = ...,
    shapes = ...
) {
    App()
}
```

Compose 主题和 XML Theme 不是同一个对象，但它们都承担类似职责：

- 提供颜色。
- 提供字体。
- 提供形状。
- 给子组件统一上下文。

真实项目里，经常需要把 XML Theme 和 Compose MaterialTheme 对齐，否则混合页面会出现视觉割裂。

## 本节小挑战

### 主题侦探题

一个自定义 View 在深色模式下背景仍然是白色，你会怎么查？

建议顺序：

```text
是否硬编码白色？
  -> 是否读取了 Theme attr？
      -> values-night 是否提供对应颜色？
          -> 当前 Context 是否带正确 Theme？
              -> View 是否在配置变化后重新取值？
```

## 本节实践任务

### 基础任务

- 定义一个 `colorPrimary`。
- 在 XML 或代码中读取 `?attr/colorPrimary`。
- 切换 night 资源，观察颜色变化。
- 设计一个 `ResourceProvider`，让同一个业务槽位在两个资源 ID 之间切换。

### 进阶任务

- 写一个自定义 View，从 Theme 读取颜色绘制背景。
- 用 `ContextThemeWrapper` 切换主题，观察同一个 attr 返回不同值。
- 对比 XML Theme 和 Compose MaterialTheme 的颜色来源。
- 为 App 内语言切换创建一个 `ConfigurationContext`，观察同一个 `R.string.xxx` 的返回值。

## 本节小结

`Style` 是属性集合，`Theme` 是作用范围更大的样式环境，`Attribute` 是可由主题填充的插槽。动态资源替换不是修改 `R`，而是改变 Theme、Configuration、业务资源映射或资源加载路径。普通业务优先使用 Theme attr、ResourceProvider 和 Configuration Context；AssetManager 外部资源路径和 RRO 更偏框架、插件化或系统层能力。理解这些边界后，主题错乱、深色模式异常、自定义 View 不跟随主题变化、动态换肤失效等问题就不再神秘。
