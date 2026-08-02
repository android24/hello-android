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

## 学习目标

学完本节后，你应该能够：

- 区分 `Theme`、`Style`、`Attribute` 和普通资源值。
- 理解主题属性解析的大致流程。
- 能排查主题错乱、颜色不对、控件样式不生效的问题。
- 知道为什么 Material / AppCompat / Compose 都会重度使用主题。

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

## 第五部分：主题错乱的常见原因

| 现象 | 可能原因 | 排查入口 |
| --- | --- | --- |
| 按钮颜色不对 | Theme 属性未配置或被覆盖 | 查 `colorPrimary`、控件 style、Material theme |
| 深色模式文字看不清 | 硬编码颜色或缺少 night 资源 | 查 `values-night` 和固定色值 |
| Dialog 样式异常 | 使用了错误 Context | 确认使用 Activity Context 或主题包装 Context |
| 自定义 View 颜色不随主题变 | 直接写死颜色 | 改为读取 `?attr` |
| Compose 与 View 颜色不一致 | 两套主题未对齐 | 对照 Compose MaterialTheme 和 XML Theme |

主题问题最怕只盯某个控件。要从当前 Context 和 Theme 往下查。

## 第六部分：Compose 里的主题

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

### 进阶任务

- 写一个自定义 View，从 Theme 读取颜色绘制背景。
- 用 `ContextThemeWrapper` 切换主题，观察同一个 attr 返回不同值。
- 对比 XML Theme 和 Compose MaterialTheme 的颜色来源。

## 本节小结

`Style` 是属性集合，`Theme` 是作用范围更大的样式环境，`Attribute` 是可由主题填充的插槽。主题系统让控件不用硬编码所有视觉值，而是从当前上下文中读取颜色、字体和样式。理解 Theme / Style / Attribute 后，主题错乱、深色模式异常、自定义 View 不跟随主题变化等问题就不再神秘。
