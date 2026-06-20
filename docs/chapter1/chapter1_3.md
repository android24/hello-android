# 1.3 Android 工程结构与资源基础

前两节，我们已经完成了开发环境准备，并学习了 Kotlin 在 Android 工程中的基础写法。现在需要补齐一个非常重要的工程基础：Android 的资源系统。

Android 的页面并不只是“在 Kotlin 代码里写几个组件”。一个成熟的 App 会把可复用的文字、颜色、图片、图标、尺寸、主题等内容放进资源系统中统一管理。这样做的意义很大：它让界面更容易维护，也让多语言、多主题、多屏幕适配变得可能。

本节会带你认识 Android 工程中的 `res` 目录，并理解资源文件如何与 Kotlin 代码、Manifest 和后续 Compose 页面连接起来。

## 本节定位

本节仍然属于 `01-kotlin-and-android-basics`。它不是 Compose UI 正课，而是进入 Compose 之前必须理解的 Android 工程基础。

本节重点解决六个问题：

- Android 工程中的 `res` 目录有什么作用？
- 为什么文字、颜色、图片不应该全部写死在代码里？
- `strings.xml`、`colors.xml`、`themes.xml` 分别负责什么？
- Drawable、Mipmap、图片资源和应用图标有什么区别？
- 后续 Compose 页面如何使用这些资源？
- 资源命名、复用和维护为什么重要？

学完本节后，你会更清楚地理解：Android 工程不是只有 Kotlin 代码，它还依赖一套被系统识别和管理的资源体系。

## 学习目标

学完本节后，你应该能够：

- 理解 `app/src/main/res` 目录的基本结构。
- 使用字符串资源管理页面文案。
- 使用颜色资源和主题管理基础视觉风格。
- 区分 `drawable`、`mipmap`、`values` 等常见资源目录。
- 在 Compose 中读取字符串、颜色和图片资源。
- 修改应用名称和应用图标相关资源。
- 创建一个简单的课程欢迎页，展示标题、说明、按钮和图片。
- 初步理解资源命名、复用和维护的重要性。

## 前置知识

开始本节前，建议你已经完成：

- 1.1：可以运行 Android 工程。
- 1.2：理解 Kotlin 基础语法。

如果你还没有创建可运行工程，可以先使用 1.1 中的默认工程继续练习。

## 第一部分：认识 res 目录

Android 工程中的资源通常位于：

```text
app/src/main/res/
```

常见目录包括：

```text
res/
  drawable/
  mipmap-hdpi/
  mipmap-mdpi/
  mipmap-xhdpi/
  values/
    colors.xml
    strings.xml
    themes.xml
```

这些目录的含义大致如下：

- `values`：存放字符串、颜色、尺寸、样式、主题等值资源。
- `drawable`：存放图片、矢量图、形状背景等可绘制资源。
- `mipmap`：主要存放应用图标，不同目录对应不同屏幕密度。
- `font`：可选目录，用于存放字体资源。
- `raw`：可选目录，用于存放原始文件，例如音频或配置文件。

资源系统的核心价值是：把界面中可复用、可替换、可适配的内容从代码里抽出来，让它们由 Android 工程统一管理。

## 第二部分：字符串资源

字符串资源通常写在：

```text
app/src/main/res/values/strings.xml
```

示例：

```xml
<resources>
    <string name="app_name">Hello Android</string>
    <string name="home_title">从零开始学 Android</string>
    <string name="home_subtitle">一步一步，从基础走向 Framework</string>
</resources>
```

为什么不建议把所有文字都直接写在 Kotlin 代码里？

- 方便统一维护。
- 方便多语言适配。
- 方便设计、产品和开发协作。
- 避免同一句文案散落在多个文件中。

在 Compose 中可以这样读取字符串资源：

```kotlin
Text(text = stringResource(id = R.string.home_title))
```

这里的 `R.string.home_title` 来自资源编译生成的 `R` 类。你可以先把 `R` 理解为 Android 为资源生成的一张索引表。

## 第三部分：颜色资源

颜色资源通常写在：

```text
app/src/main/res/values/colors.xml
```

示例：

```xml
<resources>
    <color name="brand_blue">#2563EB</color>
    <color name="text_primary">#111827</color>
    <color name="text_secondary">#6B7280</color>
    <color name="page_background">#F9FAFB</color>
</resources>
```

在传统 View 中，颜色资源经常通过 XML 或代码引用。在 Compose 中，也可以通过资源读取：

```kotlin
val titleColor = colorResource(id = R.color.text_primary)

Text(
    text = stringResource(id = R.string.home_title),
    color = titleColor
)
```

课程前期你不需要追求复杂设计系统，但要养成一个好习惯：常用颜色尽量集中定义，不要在页面里到处散落 `#FFFFFF`、`#000000` 这样的硬编码。

## 第四部分：主题与样式

主题资源通常位于：

```text
app/src/main/res/values/themes.xml
```

主题决定了一个应用的整体视觉基调，例如状态栏颜色、默认字体、Material 组件样式、深色模式行为等。

一个主题可能类似这样：

```xml
<resources>
    <style name="Theme.HelloAndroid" parent="android:style/Theme.Material.Light.NoActionBar">
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:navigationBarColor">@color/page_background</item>
    </style>
</resources>
```

在 `AndroidManifest.xml` 中，应用或 Activity 可以引用主题：

```xml
<application
    android:theme="@style/Theme.HelloAndroid">
</application>
```

如果你使用的是 Compose 项目，通常还会有 Kotlin 侧的主题封装，例如 `Theme.kt`。这说明现代 Android UI 里，资源 XML 与 Kotlin 主题代码会共同参与界面风格管理。

## 第五部分：图片与 Drawable

图片资源可以放在 `drawable` 目录中，例如：

```text
res/drawable/course_cover.png
res/drawable/ic_android.xml
```

Android 支持多种可绘制资源：

- PNG / JPG / WebP：位图图片。
- Vector Drawable：矢量图，通常是 XML 文件。
- Shape Drawable：用 XML 描述形状、圆角、边框、背景。

在 Compose 中显示图片资源：

```kotlin
Image(
    painter = painterResource(id = R.drawable.course_cover),
    contentDescription = stringResource(id = R.string.course_cover_desc)
)
```

注意 `contentDescription` 很重要，它用于无障碍访问，也能帮助页面语义更清晰。装饰性图片可以根据场景设置为 `null`，但有实际信息的图片应该提供描述。

## 第六部分：Mipmap 与应用图标

`mipmap` 目录通常用于应用图标：

```text
res/mipmap-mdpi/
res/mipmap-hdpi/
res/mipmap-xhdpi/
res/mipmap-xxhdpi/
res/mipmap-xxxhdpi/
```

不同目录对应不同屏幕密度。Android 会根据设备屏幕情况选择合适资源。

应用图标通常在 `AndroidManifest.xml` 中声明：

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round">
</application>
```

本节不要求你立即掌握所有图标适配规则，但要知道：图标不是随便放一张图就结束，它需要考虑不同屏幕密度、圆形图标、前景背景图层等问题。

## 第七部分：尺寸、密度与适配

Android 设备种类很多，屏幕尺寸和像素密度差异很大。因此 Android 使用一些适配单位：

- `dp`：与屏幕密度无关的尺寸单位，常用于布局尺寸。
- `sp`：用于文字大小，会受到用户字体设置影响。
- `px`：真实像素，日常布局中较少直接使用。

在 Compose 中也会看到这些单位：

```kotlin
Text(
    text = "Hello Android",
    fontSize = 20.sp
)

Spacer(modifier = Modifier.height(16.dp))
```

初学阶段先记住：

- 控件尺寸、间距优先用 `dp`。
- 文字大小使用 `sp`。
- 不要按某一台手机的像素尺寸硬写界面。

## 第八部分：创建课程欢迎页

本节建议完成一个简单页面，作为课程项目的首页雏形。

页面包含：

- 课程标题。
- 一段课程说明。
- 当前学习阶段。
- 一个“开始学习”按钮。
- 一张课程相关图片或图标。

资源建议：

```xml
<string name="home_title">从零开始的 Android 全栈开发课程</string>
<string name="home_subtitle">从基础、架构到 Framework，建立完整 Android 工程能力。</string>
<string name="start_learning">开始学习</string>
<string name="course_stage">当前阶段：Android 应用基础</string>
```

Compose 示例结构：

```kotlin
@Composable
fun CourseHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.home_title),
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = stringResource(id = R.string.home_subtitle))

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { }) {
            Text(text = stringResource(id = R.string.start_learning))
        }
    }
}
```

这段代码不追求视觉复杂，而是让你把资源、Compose、页面结构和 Kotlin 代码串起来。

## 本节实践任务

### 基础任务

- 找到 `app/src/main/res` 目录。
- 打开 `strings.xml`，修改应用名称。
- 新增课程首页所需的标题、说明和按钮文字。
- 找到 `colors.xml`，新增几个基础颜色。
- 在页面中读取并展示字符串资源。

### Android 工程任务

- 创建一个 `CourseHomeScreen`。
- 使用 `Column`、`Text`、`Spacer` 和 `Button` 组织页面。
- 使用 `stringResource` 读取页面文案。
- 使用 `colorResource` 或主题颜色设置文字颜色。
- 运行 App，确认页面展示正常。

### 进阶任务

- 添加一张图片资源，并在页面中展示。
- 为图片设置合适的 `contentDescription`。
- 修改应用图标或应用名称，观察桌面图标和标题变化。
- 尝试调整 `dp` 和 `sp`，观察不同尺寸对页面的影响。
- 尝试开启系统深色模式，观察页面颜色是否合适。

## 思考题

- 为什么页面文案建议放进 `strings.xml`？
- `drawable` 和 `mipmap` 分别适合存放什么资源？
- `dp` 和 `sp` 的区别是什么？
- 为什么图片资源需要考虑无障碍描述？
- Compose 中使用资源和直接写字符串、颜色相比，有什么好处？
- 如果未来要支持中英文切换，资源系统会如何帮助我们？

## 常见问题

### 所有颜色都必须写进 colors.xml 吗？

不是绝对必须。小范围临时代码可以直接写颜色，但项目中反复使用、代表品牌或主题语义的颜色，应该集中管理。随着课程推进，我们会逐步学习更系统的主题设计方式。

### Compose 项目还需要 XML 资源吗？

需要。Compose 负责声明 UI，但 Android 工程中的字符串、图片、图标、主题、权限声明等仍然依赖资源系统和 Manifest。现代 Android 开发不是完全抛弃 XML，而是让不同工具承担更合适的职责。

### 为什么我新增资源后 R 找不到？

可能是资源文件 XML 格式错误、命名不符合规则、Gradle Sync 尚未完成，或者导入了错误的 `R`。资源命名建议使用小写字母、数字和下划线。

### 应用名称在哪里改？

通常在 `strings.xml` 中修改 `app_name`，然后 `AndroidManifest.xml` 中通过 `android:label="@string/app_name"` 引用它。

## 本节小结

本节让你认识了 Android 工程背后的资源系统。你已经知道文字、颜色、图片、主题、图标和尺寸不是零散地写在代码里，而是由 `res` 目录统一组织，并通过 `R` 类被 Kotlin 或 XML 引用。

从这里开始，你写出的页面会逐渐像一个真正的 App：有文案、有视觉风格、有资源管理，也有可维护的结构。后续进入 Compose UI、状态管理和页面导航时，这些资源基础会一直陪着你。

## 下一章预告

下一章将正式进入 `02-compose-ui-basics`。我们会学习 Composable、Modifier、布局组件、状态与事件，让页面从静态展示走向真正可交互。
