# 第17章附录：主题替换、动态换肤与资源覆盖方案

第 17 章正文已经讲清楚了 `R`、`resources.arsc`、`AssetManager`、`Resources`、`Configuration` 和 Theme 的主线。这个附录再往前走一步：如果产品说“春节换一套皮肤”“会员主题要独立配置”“海外版本要切语言和品牌色”，Android 工程里到底应该换什么？

动态换肤不是一场魔术。它真正替换的不是 `R` 文件，而是资源读取链路上的某个决策点。

```text
业务状态
  -> 选择 Theme / Style / ResourceProvider / Configuration / 外部资源路径
      -> Resources 根据资源 ID 和资源表取值
          -> UI 重新读取并刷新
```

只要抓住这条链路，动态换肤就不再神秘。

## 一、先把误会讲清楚：R 文件不会在运行时变

`R.color.xxx`、`R.string.xxx`、`R.drawable.xxx` 在编译后就是整数常量。它们来自 AAPT2 link 阶段，同时和 `resources.arsc` 中的资源表条目保持匹配。

所以运行时所谓“换肤”，通常不是修改 `R`：

- 不会把 `R.color.primary` 的 int 常量改成另一个值。
- 不会临时重写 APK 里的 `resources.arsc`。
- 不会让已经编译好的代码突然生成新的 `R.xxx`。

真正发生的是：

- 同一个 attr 在不同 Theme 中解析出不同颜色。
- 同一个业务槽位映射到不同资源 ID。
- 同一个资源 ID 在不同 Configuration 下返回不同值。
- `AssetManager` 加载了额外资源路径，再通过名称或映射找到资源。
- 系统层通过 Runtime Resource Overlay 覆盖目标包资源。

把“改 R”换成“改资源选择策略”，这件事就清楚了一半。

## 二、方案一：Theme / Style / Attribute，最适合做应用主题

这是最推荐、最 Android 的主题替换方式。

你把可变的颜色、字体、圆角、间距抽象成 attr：

```xml
<attr name="resourceLabPanelColor" format="color" />
<attr name="resourceLabSignalColor" format="color" />
```

再在不同 Theme 中给 attr 赋值：

```xml
<style name="Theme.App.Default">
    <item name="resourceLabPanelColor">@color/panel_default</item>
    <item name="resourceLabSignalColor">@color/signal_default</item>
</style>

<style name="Theme.App.Festival">
    <item name="resourceLabPanelColor">@color/panel_festival</item>
    <item name="resourceLabSignalColor">@color/signal_festival</item>
</style>
```

运行时切换 Theme，本质是让同一个 attr 进入不同的解析环境：

```kotlin
val themedContext = ContextThemeWrapper(context, R.style.Theme_App_Festival)
val value = TypedValue()
themedContext.theme.resolveAttribute(R.attr.resourceLabPanelColor, value, true)
```

适合：

- 深色 / 浅色主题。
- 节日主题。
- 会员主题。
- 多品牌 App 的色彩和视觉语气。

注意：

- UI 要尽量使用 attr，不要到处硬编码 `@color/xxx`。
- View 体系切换后通常需要 `recreate()` 或重新 inflate。
- Compose 中要把当前主题状态放进可观察状态，再驱动 `MaterialTheme` 或自定义 `CompositionLocal`。
- 使用 Application Context 读取 UI attr 很容易读不到 Activity 主题覆盖。

Theme 方案像给 UI 换灯光：布景还在，但气氛已经变了。

## 三、方案二：ResourceProvider，用稳定业务槽位承接动态资源

如果换肤不只是颜色，还包括文案、图标、插画、运营模块素材，就可以把资源选择收拢到一个 Provider。

```kotlin
data class HomeSkinResources(
    val titleRes: Int,
    val panelColorRes: Int,
    val iconRes: Int
)

interface HomeSkinProvider {
    fun current(): HomeSkinResources
}
```

业务 UI 不直接关心“现在是森林皮肤还是节日皮肤”，只关心稳定槽位：

```kotlin
val skin = skinProvider.current()
Text(text = stringResource(skin.titleRes))
Icon(painter = painterResource(skin.iconRes), contentDescription = null)
```

适合：

- 运营皮肤。
- 会员皮肤。
- 可远程配置但资源仍内置在 APK 的场景。
- Compose 与 View 混合工程中的统一资源入口。

注意：

- Provider 返回的最好仍然是编译期资源 ID，而不是到处拼字符串。
- 切换皮肤后要让 UI 重新读取 Provider。
- 如果使用 `getIdentifier`，要明确 keep 规则、资源名映射和 shrink 策略。
- 不要让业务页面到处散落 `if (skin == xxx)`。

ResourceProvider 像资源仓库里的分拣台：货架不乱动，但每个业务槽位知道今天该拿哪一件。

## 四、方案三：ConfigurationContext，适合语言与环境切换

语言、字体缩放、夜间模式、屏幕尺寸等属于 Configuration 选择。Android 资源系统会根据当前 Configuration 在多个候选资源中挑一个最匹配的。

比如临时读取英文资源：

```kotlin
val config = Configuration(context.resources.configuration)
config.setLocale(Locale.ENGLISH)
val englishContext = context.createConfigurationContext(config)
val title = englishContext.getString(R.string.app_name)
```

适合：

- App 内语言切换。
- 按业务环境读取某一套限定符资源。
- 验证 `values-en`、`values-night`、`layout-land` 是否生效。

注意：

- ConfigurationContext 只影响用它读取的资源。
- 已缓存的字符串、颜色和 drawable 不会自动刷新。
- Activity、Fragment、Compose 状态都需要重新读取资源。
- App 内语言切换要考虑 Android 13+ per-app language 与历史兼容方案。

Configuration 像资源系统的天气预报：同一张地图，在不同天气下会选择不同路线。

## 五、方案四：外部皮肤包，适合插件化但成本最高

传统动态换肤里经常会听到“外部皮肤包”。它的典型思路是：

```text
下载或安装一个皮肤 APK
  -> 通过 AssetManager 加载皮肤资源路径
      -> 按资源名或映射表寻找对应资源
          -> UI 重新读取外部资源
```

这类方案的难点不在“能不能加载”，而在“能不能长期稳定维护”：

- 外部包和主包的资源 ID 不一定一致。
- 资源名如果被 shrink 或混淆，`getIdentifier` 可能失败。
- `AssetManager.addAssetPath` 历史上常涉及隐藏 API 或兼容问题。
- 皮肤包版本必须和主包资源槽位保持协议。
- drawable、selector、style、font、dimension 的兼容成本很高。

适合：

- 大型 App 的插件化皮肤体系。
- 必须在线下发完整视觉包的业务。
- 有能力维护资源协议、灰度、回滚和兼容矩阵的团队。

不适合：

- 初期项目为了“看起来高级”而过早引入。
- 只是换几个颜色、图标和文案的普通主题需求。

外部皮肤包像给仓库临时接入一座外部仓库：能力很强，但每条货道都要有编号、合同和验货流程。

## 六、方案五：RRO，系统层资源覆盖

Runtime Resource Overlay 是 Android 系统层的资源覆盖能力，常见于系统主题、厂商定制、设备形态适配等场景。

它的核心不是 App 自己随手换肤，而是系统在资源解析阶段把 overlay 包覆盖到目标包上。

适合：

- 系统应用。
- ROM / 设备厂商定制。
- Framework 或系统组件主题覆盖。

普通业务 App 通常不应该把 RRO 当成换肤首选方案。它更接近系统工程能力，而不是普通应用内主题开关。

## 七、怎么选：一张工程决策表

| 场景 | 推荐方案 | 原因 |
| --- | --- | --- |
| 深色 / 浅色主题 | Theme + attr | 系统支持完整，和 View / Compose 都能配合 |
| 节日色彩、会员色彩 | Theme + attr 或 ResourceProvider | 颜色走 attr，素材走 Provider |
| 文案、图标、插画成套切换 | ResourceProvider | 稳定槽位清晰，利于测试和灰度 |
| App 内语言切换 | ConfigurationContext / per-app language | 本质是 Configuration 选择 |
| 在线下发完整皮肤包 | 外部皮肤包 | 能力强，但要承担资源协议成本 |
| 系统主题覆盖 | RRO | 系统层覆盖目标包资源 |

优先级建议：

```text
Theme attr
  -> ResourceProvider
      -> ConfigurationContext
          -> 外部皮肤包
              -> RRO
```

越靠前，越适合普通 App；越靠后，工程成本和系统依赖越高。

## 八、常见事故排查

### 切换后只有部分 UI 生效

通常是因为有些 UI 走了 Theme / Provider，有些 UI 直接写死了颜色或资源 ID。

第一证据：

```text
检查页面资源入口是否统一
检查是否缓存了旧字符串、旧颜色、旧 drawable
检查 Compose state 或 View invalidate / recreate 是否触发
```

### Application Context 读到的主题值不对

UI attr 应该使用带 Theme 的 Context。Application Context 更适合应用级资源，不适合读取 Activity 主题覆盖。

第一证据：

```text
对比 Activity Context
对比 Application Context
对比 ContextThemeWrapper
```

### getIdentifier 在 release 找不到

常见原因是资源 shrink、资源名混淆、包名错误或皮肤包协议不一致。

第一证据：

```text
debug / release 对比
资源 keep 规则
APK Analyzer 中的最终资源
动态资源名映射表
```

### 外部皮肤包升级后错位

常见原因是主包和皮肤包版本协议不一致。

第一证据：

```text
皮肤包版本
资源名映射
缺失资源兜底
灰度和回滚记录
```

## 九、附录 Demo 怎么玩

配套工程：

```text
examples/17-resource-system-lab/
```

建议按这个顺序观察：

```text
Theme attr 实验区
  -> 动态资源替换实验区
      -> 动态换肤附录实验区
          -> 资源事件轨迹
              -> 资源系统诊断报告
```

在 `动态资源替换实验区` 中点击“切换资源槽位”，观察 title、panel、signal 对应的资源 ID 和返回值如何变化。

再进入 `动态换肤附录实验区`，对照每一种方案：

- 它替换的是 Theme、Provider、Configuration，还是外部资源路径？
- 它适合普通业务 App，还是系统 / 插件化工程？
- 它的第一风险是什么？
- 如果线上出问题，第一证据应该去哪里找？

## 十、附录小挑战

请为下面需求选择方案：

```text
业务希望首页支持普通、森林、春节三套视觉；
颜色、标题、按钮文案、顶部插画都要变；
资源先内置在 APK，未来可能远程下发；
要求 release 稳定，不能因为 shrink 导致找不到资源。
```

推荐答案：

```text
颜色：Theme attr
文案 / 插画 / 图标：ResourceProvider 返回稳定资源 ID
远程配置：只下发皮肤 key，不下发任意资源名
release 稳定性：避免散落 getIdentifier，必要动态查找必须配 keep 规则和映射表
UI 刷新：切换 key 后触发 View recreate 或 Compose 状态更新
```

## 附录小结

动态换肤的关键不是“运行时改资源”，而是设计一个稳定、可解释、可诊断的资源选择层。Theme 负责气质，Configuration 负责环境，ResourceProvider 负责业务槽位，外部皮肤包负责扩展能力，RRO 负责系统覆盖。

当你能解释“我到底替换了资源读取链路上的哪一层”，动态换肤就从玄学变成了工程。
