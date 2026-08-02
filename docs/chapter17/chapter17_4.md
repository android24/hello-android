# 17.4 Configuration 与资源限定符：多语言、密度、横竖屏和夜间模式

Android 资源系统最迷人的地方，不是它能读取资源，而是它能根据设备状态自动选择资源。

你可以准备多份资源：

```text
values/strings.xml
values-zh/strings.xml
values-night/colors.xml
drawable-mdpi/logo.png
drawable-xxhdpi/logo.png
layout/activity_main.xml
layout-land/activity_main.xml
```

代码里仍然只写：

```kotlin
R.string.title
R.drawable.logo
R.layout.activity_main
```

系统会根据当前 `Configuration` 选出最匹配的一份。

## 本节定位

本节负责回答：

- 什么是 `Configuration`？
- 资源目录限定符如何工作？
- 多语言、密度、横竖屏、夜间模式为什么能自动切换？
- 为什么某些设备上的资源适配会不符合预期？

## 学习目标

学完本节后，你应该能够：

- 理解资源限定符和 `Configuration` 的关系。
- 掌握常见限定符：语言、屏幕密度、夜间模式、横竖屏、尺寸。
- 能排查资源没有按预期生效的问题。
- 知道配置变化为什么会影响 Activity 重建和资源重新选择。

## 第一部分：Configuration 是当前环境快照

`Configuration` 记录当前设备和应用环境中的很多信息：

- locale：语言和区域。
- uiMode：夜间模式、设备类型。
- orientation：横竖屏。
- densityDpi：屏幕密度。
- screenWidthDp / screenHeightDp。
- fontScale：字体缩放。

资源系统会拿这些信息去匹配资源目录。

你可以把它理解成：

```text
当前设备是什么状态？
当前用户使用什么语言？
当前屏幕是什么形态？
当前主题是日间还是夜间？
```

## 第二部分：资源限定符像仓库标签

资源目录名中的后缀就是限定符：

```text
values-zh
values-en-rUS
values-night
drawable-xhdpi
layout-land
layout-sw600dp
```

它们告诉系统：

```text
这份资源适合什么配置？
```

系统会根据当前 `Configuration` 按规则选择最合适的候选。

如果没有完全匹配，就会逐步退回到默认资源。

## 第三部分：多语言资源

多语言通常这样组织：

```text
values/strings.xml
values-zh/strings.xml
values-en/strings.xml
```

读取时仍然是：

```kotlin
getString(R.string.title)
```

系统会根据 locale 选择：

```text
中文环境 -> values-zh
英文环境 -> values-en
兜底 -> values
```

排查多语言不生效时，先看：

- 是否有默认 `values/strings.xml`。
- 资源 key 是否一致。
- 当前 Context 的 locale 是否真的改变。
- 是否缓存了旧字符串。
- Compose 中是否依赖了能感知配置变化的读取方式。

## 第四部分：密度资源

图片资源常见目录：

```text
drawable-mdpi
drawable-hdpi
drawable-xhdpi
drawable-xxhdpi
drawable-xxxhdpi
```

系统会根据设备 density 选择合适图片。

如果图片发糊、过大或过小，可能是：

- 只提供了低密度图片。
- 把像素尺寸当成 dp 使用。
- 图片放错目录。
- vector 和 bitmap 使用场景混乱。
- Compose / ImageView 的缩放策略不合适。

密度适配的关键是：资源文件、dp/sp、布局约束要一起看。

## 第五部分：横竖屏和尺寸限定符

横竖屏常见目录：

```text
layout/
layout-land/
```

大屏适配常见目录：

```text
layout-sw600dp/
values-sw600dp/
```

它们可以让同一个页面在手机、平板、横屏场景下使用不同布局或尺寸。

但要小心：

- 不要为每个设备单独建目录。
- 优先用响应式布局解决通用问题。
- 限定符用于表达明确的体验差异。

## 第六部分：夜间模式

夜间模式常见目录：

```text
values/colors.xml
values-night/colors.xml
```

当系统进入深色模式时，资源系统会选择 night 版本。

主题和颜色经常一起参与：

```text
Theme
  -> colorPrimary / colorSurface / textColor
      -> values 或 values-night
          -> 控件最终颜色
```

如果深色模式颜色不对，先看：

- 是否提供了 night 资源。
- 是否硬编码颜色。
- 是否绕过主题直接使用固定色值。
- 自定义 View 是否从 Theme 读取属性。

## 第七部分：配置变化与 Activity 重建

语言、横竖屏、夜间模式等变化可能导致 Activity 重建。

大致链路是：

```text
Configuration 改变
  -> 系统判断 Activity 是否需要处理
      -> 默认重建 Activity
          -> 重新创建 Context / Resources / Theme
              -> 重新选择资源
```

如果你在 Manifest 中配置 `configChanges` 自己处理，就要非常谨慎。

自己处理意味着你也要负责：

- 更新 UI 文案。
- 更新主题颜色。
- 更新布局状态。
- 避免继续使用旧资源。

## 本节小挑战

### 限定符破案题

一个 App 在英文系统下仍然显示中文，你会怎么查？

建议顺序：

```text
确认系统语言
  -> 确认 App locale
      -> 检查 values-en 是否存在
          -> 检查资源 key 是否一致
              -> 检查是否缓存字符串
                  -> 检查 Context 是否正确
```

## 本节实践任务

### 基础任务

- 新增 `values-zh/strings.xml` 和 `values-en/strings.xml`。
- 切换系统语言，观察同一个 `R.string.xxx` 的返回值。
- 新增 `values-night/colors.xml`，切换深色模式观察颜色。

### 进阶任务

- 新增 `layout-land` 或 `values-land` 资源。
- 旋转设备观察资源是否切换。
- 打印 `resources.configuration`，对照当前资源选择结果。

## 本节小结

`Configuration` 是资源选择的环境快照，资源限定符是资源目录上的适配标签。多语言、密度、横竖屏、夜间模式和大屏适配都依赖这套机制。排查资源适配问题时，不要只看代码，要同时看当前配置、资源目录、默认兜底资源、Context 和是否存在缓存。
