# 17.6 资源合并、依赖模块与资源冲突

真实 Android 工程很少只有一个模块。

你可能会有：

```text
app
feature-home
feature-profile
core-ui
core-design
third-party libraries
```

每个模块都可能有自己的资源。最终打包时，这些资源需要被合并、链接、编号，变成一个 APK 可以使用的资源集合。

本节关注资源合并和冲突。

这节也不能只停留在“资源要加前缀”。真正要讲清楚的是：

```text
冲突为什么发生？
哪些重复是合法覆盖？
哪些重复会构建失败？
为什么依赖库资源会影响最终 App？
为什么 namespace 不能天然解决所有资源冲突？
```

## 本节定位

本节负责回答：

- 多模块资源如何进入最终 APK？
- 为什么会出现资源冲突？
- 哪些冲突是合法覆盖，哪些冲突是构建错误？
- library 资源、app 资源、flavor 资源的优先级是什么思路？
- `namespace`、`applicationId` 和资源包名有什么区别？
- 为什么 `nonTransitiveRClass` 能改善资源边界？

## 学习目标

学完本节后，你应该能够：

- 理解 app 模块、library 模块资源合并的大致过程。
- 能排查重复资源、资源覆盖、依赖缺失等问题。
- 能解释资源冲突发生在资源身份、合并优先级还是 R 访问边界。
- 知道资源命名规范为什么重要。
- 理解多模块工程中资源边界的意义。

## 第一部分：资源合并发生在最终链接前

多个模块会贡献资源：

```text
app/res
  + feature/res
      + core-ui/res
          + third-party/res
              -> merged resources
                  -> AAPT2 link
                      -> final R
                          -> resources.arsc
                              -> APK
```

最终 App 运行时看到的是链接后的资源集合。

这带来便利，也带来风险：不同模块之间的资源名可能冲突。

资源合并里的“同一个资源”通常由这些信息识别：

```text
resource package
resource type
resource name
source set / variant 规则
```

例如：

```text
string/title
color/primary
drawable/ic_back
layout/item_course
```

如果两个来源都声明了 `string/title`，AAPT2 link 阶段必须判断：这是同一个资源的合法覆盖，还是两个模块无意间撞名。

## 第二部分：冲突的第一类根因是资源身份撞车

资源名通常比较短：

```text
title
button_text
primary_color
ic_back
item_layout
```

如果多个模块都这样命名，就容易撞车。

例如：

```text
feature-home/res/values/strings.xml
  -> string/title

feature-profile/res/values/strings.xml
  -> string/title
```

如果它们最终都进入 app 的资源链接范围，而又不是明确的 variant 覆盖关系，就可能出现 duplicate resource。

资源冲突的本质不是“文件夹多了”，而是多个来源在最终资源表里争抢同一个身份：

```text
type + name
```

这就是为什么命名规范要强调业务前缀。

## 第三部分：冲突的第二类根因是覆盖优先级不符合预期

有些同名资源不是错误，而是设计好的覆盖。

例如：

```text
src/main/res/values/strings.xml
src/debug/res/values/strings.xml
src/free/res/values/strings.xml
```

debug、flavor、main 之间本来就有变体覆盖关系。

可以建立一个直觉：

```text
越靠近最终 app、越具体的 variant，优先级越高。
```

大致可以理解成：

```text
library 默认资源
  -> app main 资源
      -> productFlavor 资源
          -> buildType 资源
              -> variant 组合后的最终资源
```

这不是让你死背每个构建插件版本的细节，而是让你形成排查方向：

```text
页面展示的资源不对
  -> 先查最终 merged resources
      -> 再反推它来自哪个 source set 或依赖库
```

覆盖能解决定制问题，也会制造“我明明改了资源但没生效”的错觉。

## 第四部分：冲突的第三类根因是依赖库资源过于通用

两个 AAR 都可能声明：

```text
color/primary
drawable/ic_close
string/app_name
layout/dialog_confirm
```

library 资源最终会参与 app link。库作者如果没有做前缀隔离，就可能和业务模块或其他库撞名。

这也是为什么成熟库通常会使用资源前缀：

```text
material_*
abc_*
design_*
```

它们不是为了显得专业，而是在避免资源身份撞车。

依赖冲突常见于：

- 两个库带了同名资源。
- app 模块想覆盖库资源，但覆盖范围比预期更大。
- 老库没有资源前缀。
- 不同版本的同一个库同时进入依赖图。
- transitive dependency 带进来一套你没有意识到的资源。

所以排查资源冲突时不能只看当前模块，要看完整依赖图。

## 第五部分：依赖模块与资源冲突

依赖模块带来的资源冲突，通常比本地模块冲突更隐蔽。

因为你在当前模块里看不到那份资源，但它会通过依赖关系进入最终 App：

```text
app
  -> feature-home
      -> core-ui
          -> ui-widget-aar
              -> res/values/colors.xml
              -> res/drawable/ic_close.xml
```

最终 link 时，这些资源都可能进入同一张资源表。

### 直接依赖冲突

直接依赖比较容易理解：

```text
app 依赖 lib-a
app 依赖 lib-b

lib-a -> color/primary
lib-b -> color/primary
```

如果两个库都把资源暴露进最终资源合并范围，就可能出现同名冲突，或者某个资源被更高优先级来源覆盖。

这种问题的第一证据通常是：

```text
Duplicate resources
```

或者页面表现为：

```text
我以为用的是 lib-a 的颜色，实际却变成了 app 或另一个库覆盖后的颜色。
```

### 传递依赖冲突

更常见的是传递依赖：

```text
app
  -> feature-feed
      -> image-picker:1.0
          -> res/drawable/ic_close.xml

app
  -> feature-profile
      -> dialog-kit:2.0
          -> res/drawable/ic_close.xml
```

当前 app 的 `build.gradle` 里可能根本没有直接声明 `image-picker` 或 `dialog-kit`，但它们通过 feature 或三方库传进来了。

所以只搜当前工程源码是不够的。你还需要看：

```text
Gradle dependency tree
AAR 内容
merged resources
APK Analyzer
```

### 同一个库的多版本冲突

还有一种情况是同一个库的不同版本进入依赖图。

例如：

```text
feature-a -> design-kit:1.2
feature-b -> design-kit:1.5
```

Gradle 通常会做版本选择，最终只保留一个版本。但这会带来另一个问题：

```text
feature-a 编写时以为资源来自 1.2
最终打包时实际使用了 1.5
```

如果 1.5 改了资源名、删除了资源、调整了 style 默认值，就可能出现：

- 编译期找不到资源。
- 运行时样式变化。
- 主题属性默认值变化。
- release 包里资源 shrink 结果不同。

这类问题表面上像 UI 变了，根上是依赖解析后的资源版本变了。

### app 覆盖 library 资源

App 模块可以覆盖 library 里的某些资源。

这有时是设计能力：

```text
library 定义 string/ok = "OK"
app 定义 string/ok = "确认"
```

但风险是覆盖范围可能比你想象的大。

如果一个 library 内部多个页面都引用 `string/ok`，app 覆盖后会影响整个库，不只是你当前看到的按钮。

所以覆盖 library 资源时要问：

```text
这个资源是不是库作者允许覆盖的扩展点？
这个名字是否足够具体？
覆盖后影响的是一个页面，还是整个库？
是否有 public.txt 或文档说明？
```

如果库没有明确把资源当成扩展点，就不要依赖“刚好能覆盖”作为长期方案。

### api 与 implementation 对资源可见性的影响

Gradle 的 `api` / `implementation` 主要影响编译依赖暴露边界，但资源也会跟着模块依赖关系进入构建图。

直觉上可以这样判断：

```text
当前模块能不能在源码里引用某个依赖模块的 R？
最终 app 会不会把这个依赖模块的资源打进包？
```

这是两个相关但不完全相同的问题。

有时你不能在某个 feature 源码里直接访问深层依赖的 R，但最终 APK 里仍然包含那份资源，因为它被某条运行时依赖带进来了。

这也是为什么排查资源问题要分两层：

```text
源码访问层
  -> R 类、namespace、api/implementation、nonTransitiveRClass

最终打包层
  -> merged resources、AAPT2 link、resources.arsc、APK Analyzer
```

### resourcePrefix 的价值

Library 模块可以通过约定或配置要求资源前缀，例如：

```kotlin
android {
    resourcePrefix = "feed_"
}
```

它的价值是提前把风险暴露出来：

```text
feed_title
feed_ic_close
feed_dialog_confirm
```

这样库资源进入最终 App 后，也更不容易和 app 或其他库撞名。

resourcePrefix 不是运行时隔离机制，它是构建期命名约束。它不能替你解决所有冲突，但能显著减少依赖模块资源身份撞车。

### 依赖资源冲突排查路线

遇到依赖资源冲突，建议按下面路线：

```text
确定资源名和类型
  -> 确认当前模块是否直接声明
      -> 查 merged resources 最终来源
          -> 查 Gradle dependency tree
              -> 打开可疑 AAR 看 res 目录
                  -> 判断是 duplicate、override、version drift 还是 shrink
```

记录时建议写成：

```text
资源：color/primary
当前值来自：app/src/main/res/values/colors.xml
候选来源：core-ui、design-kit、third-party-widget
是否有覆盖关系：app 覆盖 library
是否来自传递依赖：是 / 否
是否存在多版本库：是 / 否
解决方案：改名前缀 / 升级统一版本 / 排除传递依赖 / 明确公共资源模块
```

## 第六部分：命名规范是在补业务命名空间

建议按模块或业务前缀命名：

```text
home_title
profile_avatar_placeholder
course_card_background
design_color_primary
```

Android 资源系统本身不会自动理解：

```text
home:title
profile:title
```

如果你都叫：

```text
string/title
```

最终就可能撞到同一个资源身份上。

所以资源前缀不是“啰嗦”，而是在替资源系统补上业务语义。

这样做的价值是：

- 降低冲突概率。
- 让资源来源更清晰。
- 方便删除和迁移。
- 方便排查 APK 中的资源。
- 降低 app 覆盖 library 资源时误伤其他资源的概率。

资源命名规范属于工程治理，不只是代码风格。

## 第七部分：namespace、applicationId 与资源表不是一回事

现代 Android Gradle Plugin 要求配置：

```kotlin
android {
    namespace = "com.example.feature.home"
}
```

它影响生成的 R 类和代码命名空间。

而：

```kotlin
applicationId = "com.example.app"
```

是最终安装到设备上的包名。

可以先这样区分：

| 概念 | 主要作用 |
| --- | --- |
| `namespace` | 编译期代码和 R 类命名空间 |
| `applicationId` | 运行时安装包名 |
| 资源表 package id / package name | 运行时资源查找索引 |
| Manifest 合并结果 | 参与最终包信息、组件和权限声明 |

更深入一点：

```text
namespace
  -> 决定源码中 R / BuildConfig 等生成类的位置
  -> 主要服务编译期代码访问

applicationId
  -> 决定设备上安装包名
  -> 主要服务 PMS、安装、权限和运行时包身份

resources.arsc 中的资源包
  -> 决定资源表如何被索引
  -> 主要服务 AssetManager / Resources 查找资源
```

它们有关联，但不是一回事。

把 `namespace` 改了，不等于安装包名就变了；把 `applicationId` 改了，也不等于所有 library 的源码 R 命名空间都变了；多个模块的资源也不会因为 namespace 不同就天然避免最终资源合并中的同名问题。

## 第八部分：nonTransitiveRClass 解决的是访问边界

当开启：

```properties
android.nonTransitiveRClass=true
```

模块的 R 类只包含本模块自己的资源。

好处是：

- 模块边界更清晰。
- 避免随意引用依赖模块资源。
- 减少 R 类膨胀。
- 更容易发现资源依赖问题。

代价是：

- 迁移旧工程时会暴露很多跨模块资源引用。
- 需要更清楚地设计公共资源模块。

举个例子：

```text
core-design 定义 design_color_primary
feature-home 依赖 core-design
```

如果 `feature-home` 想使用这个颜色，应该明确依赖 `core-design`。

但如果 `feature-home` 随手引用了 `app` 模块里的 `R.color.app_primary`，这就是反向依赖。不开启严格边界时它可能暂时混过去，开启 `nonTransitiveRClass` 后会在编译期暴露。

这类错误不是资源系统变严格了，而是原来的架构边界本来就不清楚。

注意：`nonTransitiveRClass` 主要解决源码访问边界，不等于完全解决最终 APK 资源合并冲突。合并冲突仍然要回到资源身份和覆盖规则中排查。

## 第九部分：资源合并排查思路

遇到资源合并问题，可以先分类。

| 类型 | 典型现象 | 根因 |
| --- | --- | --- |
| Duplicate | 构建时报重复资源 | 同类型同名资源进入同一链接范围，且无合法覆盖关系 |
| Override | 页面资源不是你以为的值 | 更高优先级 source set 或 app 资源覆盖了低优先级资源 |
| Missing | 某模块找不到资源 | 依赖没声明、R 非传递、资源被移除 |
| Transitive | 当前模块没声明但最终包里出现资源 | 传递依赖 AAR 带入资源 |
| Version drift | 升级后资源值或样式变化 | 同一依赖最终解析到不同版本 |
| Wrong package | 运行时资源 ID 找不到 | 使用了错误包、动态加载资源表不完整 |
| Shrink | debug 正常 release 缺资源 | 资源 shrink 误判动态引用未使用 |

具体排查顺序：

```text
错误日志中的资源名
  -> 资源类型
      -> 来源模块
          -> 是否属于合法 variant 覆盖
              -> 是否来自 transitive dependency
                  -> 最终 merged resources 里保留了谁
                      -> 是否存在同库多版本
                          -> R 引用是否跨模块
```

常见证据入口：

- Gradle 构建日志。
- Merged Manifest / Merged Resources。
- APK Analyzer。
- `build/intermediates` 下的资源中间产物。
- IDE 的资源引用跳转。
- 依赖树和 AAR 内容。

## 本节小挑战

### 资源冲突诊断题

如果构建报：

```text
Duplicate resources
```

你会怎么查？

建议记录：

```text
重复资源名：
资源类型：
出现在哪些模块：
它们是否属于合法 variant 覆盖关系：
哪个模块应该拥有它：
是否需要改名前缀：
是否属于 flavor 覆盖：
最终 merged resources 里保留了谁：
是否来自传递依赖：
是否存在同一个库的多版本：
```

## 本节实践任务

### 基础任务

- 在两个模块中创建同名 string，观察是否冲突。
- 在 debug 和 main 中创建同名资源，观察谁覆盖谁。
- 给资源增加模块前缀，重新构建。
- 查找当前工程是否开启 `android.nonTransitiveRClass`。
- 给一个 library 模块配置 `resourcePrefix`，观察不符合前缀的资源是否被提示。

### 进阶任务

- 建一个 `core-design` 模块存放公共颜色和尺寸。
- 让 feature 模块只引用公共模块暴露的资源。
- 用 APK Analyzer 对比资源合并后的结构。
- 查看 `build/intermediates` 中 merged resources，确认最终资源来自哪个 source set。
- 检查依赖树，找出哪些 AAR 向最终 APK 贡献了资源。
- 模拟两个 feature 依赖不同版本 design-kit，观察最终解析版本和资源变化。

## 本节小结

多模块资源合并让 App 可以复用设计资源和组件资源，但冲突不是因为“资源系统不聪明”，而是因为多个来源在最终链接时进入同一张资源表。冲突的根因通常是资源身份撞车、覆盖优先级不符合预期、依赖库命名过宽、传递依赖带入未知资源、同一库多版本导致资源漂移，或者源码 R 边界和最终资源表边界不一致。`namespace` 负责编译命名空间，`applicationId` 负责运行时包名，`resourcePrefix` 能降低 library 资源撞名概率，`nonTransitiveRClass` 能帮助模块建立更清晰的资源访问边界。资源治理是大型 Android 工程稳定性的基础之一。
