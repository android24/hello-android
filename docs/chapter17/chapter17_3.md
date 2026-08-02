# 17.3 AssetManager 与 Resources：运行时如何加载资源

上一节我们看到资源如何在构建期变成 `R` 文件和 `resources.arsc`。

本节进入运行时：

```text
APK 已经安装
  -> 代码拿到资源 ID
      -> 系统如何找到资源表？
          -> 如何读取具体字符串、图片、尺寸和 XML？
```

这条链路的核心角色是 `AssetManager` 和 `Resources`。

## 本节定位

本节负责回答：

- `AssetManager` 管什么？
- `Resources` 管什么？
- 为什么 `Context.getResources()` 这么常用？
- App、系统资源、第三方库资源如何一起被访问？

## 学习目标

学完本节后，你应该能够：

- 理解 `AssetManager` 与 `Resources` 的分工。
- 知道资源路径、资源表和配置上下文如何一起工作。
- 能解释 `getString`、`getDrawable`、`getDimension` 背后的大致流程。
- 初步理解动态资源加载为什么会碰到 AssetManager。

## 第一部分：AssetManager 像资源仓库管理员

`AssetManager` 负责管理资源来源。

它关心：

- 当前 App 的 APK 路径。
- framework-res.apk 等系统资源路径。
- 资源表加载。
- `assets/` 目录中的原始文件。
- `raw/`、XML、二进制资源等底层读取。

你可以把它理解成：

```text
资源从哪里来？
资源表在哪里？
这个 APK 的资源路径如何加入查询范围？
```

很多动态换肤、插件化、资源热修复方案都会接触 `AssetManager`，因为它们要把新的资源路径加入运行时资源搜索范围。

## 第二部分：Resources 像资源解释器

`Resources` 面向应用代码。

你常用的是：

```kotlin
resources.getString(...)
resources.getDrawable(...)
resources.getDimension(...)
resources.getColor(...)
```

它负责把资源 ID 翻译成当前上下文下的值。

它关心：

- 当前语言。
- 当前区域。
- 当前屏幕密度。
- 当前横竖屏。
- 当前夜间模式。
- 当前字体缩放。
- 当前资源表和资源文件。

所以 `Resources` 不只是从表里拿值，它还要结合 `Configuration` 做选择。

## 第三部分：Context 为什么能拿到 Resources

你经常写：

```kotlin
context.resources
context.getString(R.string.app_name)
```

这是因为 `Context` 表示一段运行环境。

它连接了：

```text
App 信息
  -> Resources
      -> Theme
          -> ClassLoader
              -> PackageManager
                  -> 系统服务
```

不同 `Context` 可能有不同资源视角：

- `Application` Context。
- `Activity` Context。
- `ContextThemeWrapper`。
- `createConfigurationContext` 创建的新配置 Context。
- `createPackageContext` 创建的其他包 Context。

这也是为什么有些主题属性在 Application Context 里读不到，却在 Activity Context 里能读到。

## 第四部分：一次 getString 的大致流程

当你调用：

```kotlin
context.getString(R.string.app_name)
```

可以先这样理解：

```text
Context
  -> Resources
      -> AssetManager
          -> resources.arsc
              -> 根据资源 ID 找到 string/app_name
                  -> 根据 locale 选择最匹配值
                      -> 返回字符串
```

如果对应资源不存在，或者资源类型不匹配，就可能出现：

```text
Resources.NotFoundException
```

## 第五部分：一次 getDrawable 的大致流程

图片资源更复杂，因为它可能受密度影响：

```text
R.drawable.logo
  -> 找到候选资源
      -> 匹配 drawable-mdpi / hdpi / xhdpi / xxhdpi
          -> 选择最适合当前 density 的文件
              -> 解码 bitmap 或解析 vector
                  -> 返回 Drawable
```

这就是为什么同一个资源名可以对应多个不同目录下的文件。

资源系统会尽量帮你选，但前提是你提供了合理的资源目录和尺寸策略。

## 第六部分：assets 与 res/raw 的区别

`assets/` 和 `res/raw/` 都可以放原始文件，但使用方式不同。

| 位置 | 访问方式 | 特点 |
| --- | --- | --- |
| `assets/` | `assets.open("file.json")` | 更像文件系统，按路径读取 |
| `res/raw/` | `resources.openRawResource(R.raw.file)` | 有资源 ID，参与资源编译和索引 |

如果你希望通过 `R` 引用，放 `res/raw` 更合适。

如果你希望保持目录结构、按文件路径读取，放 `assets` 更自然。

## 本节小挑战

### Context 资源题

请解释下面几个对象拿到的 `Resources` 是否一定完全相同：

- `applicationContext.resources`
- `activity.resources`
- `ContextThemeWrapper(activity, R.style.SomeTheme).resources`
- `activity.createConfigurationContext(newConfig).resources`

重点不是背答案，而是判断：它们的资源表、配置、主题是否可能不同。

## 本节实践任务

### 基础任务

- 在 Activity 中打印 `resources.configuration`。
- 读取一个 string、一个 color、一个 dimension。
- 放一个 `assets/config.json`，使用 `assets.open` 读取。

### 进阶任务

- 使用 `createConfigurationContext` 临时切换 locale。
- 对比同一个 `R.string.xxx` 在不同 Configuration 下的返回值。
- 尝试用 Activity Context 和 Application Context 读取主题属性，观察差异。

## 本节小结

`AssetManager` 负责资源来源和资源表加载，`Resources` 负责把资源 ID 解释成当前配置下的值，`Context` 则把运行环境、资源、主题和包信息连接起来。理解这三者后，你就能解释为什么同一个资源 ID 在不同语言、密度、夜间模式和 Context 下可能返回不同结果。
