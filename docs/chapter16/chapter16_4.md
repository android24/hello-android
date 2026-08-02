# 16.4 Intent 解析、组件匹配与包可见性

上一节我们知道组件会被 PMS 注册。

这一节继续看：

```text
系统如何从一个 Intent 找到目标组件？
```

这背后是 Intent 解析、组件匹配和包可见性。

## 本节剧情钩子

你写了这样的代码：

```kotlin
val intent = Intent(Intent.ACTION_VIEW, uri)
startActivity(intent)
```

有些手机能打开，有些手机报错。

或者 Android 11 之后，你用 PackageManager 查询某个 App，结果查不到。

这时问题不一定是对方没安装，也可能是解析规则或包可见性发生了变化。

## 本节定位

本节讲清楚 Intent 解析和包可见性，为排查 ActivityNotFound、隐式跳转失败和查询不到应用打基础。

## 学习目标

学完本节后，你应该能够：

- 区分显式 Intent 和隐式 Intent。
- 理解 action、category、data 如何参与匹配。
- 知道 PackageManager 查询为什么可能返回空。
- 初步理解 Android 11 之后的包可见性限制。

## 第一部分：显式 Intent

显式 Intent 直接指定目标组件。

例如：

```kotlin
Intent(this, DetailActivity::class.java)
```

或者：

```kotlin
intent.setClassName("com.example", "com.example.DetailActivity")
```

系统不需要复杂匹配规则，只需要确认：

- 目标包存在。
- 目标组件存在。
- 调用者是否有权限。
- 组件是否允许访问。

显式 Intent 更确定，但也更依赖具体组件名。

## 第二部分：隐式 Intent

隐式 Intent 不指定具体组件，而是描述意图。

例如：

```kotlin
Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
```

系统会用 Intent 中的：

- action。
- category。
- data scheme / host / mimeType。

去匹配已安装应用的 intent-filter。

这就是为什么浏览器、相册、分享面板能被系统找到。

## 第三部分：匹配不是只看 action

很多隐式跳转失败，是因为只看了 action，忽略了 category 或 data。

匹配通常要同时考虑：

```text
action 是否匹配
category 是否满足
data / mimeType 是否匹配
```

例如 Activity 要能响应普通 `startActivity`，通常需要包含：

```xml
<category android:name="android.intent.category.DEFAULT" />
```

少了它，就可能查询得到但启动失败，或者根本不进入候选。

## 第四部分：包可见性

Android 11 开始，应用不能随意查询设备上所有安装包。

如果你想查询某些包或 Intent，需要在 Manifest 中声明 queries。

例如：

```xml
<queries>
    <package android:name="com.example.target" />
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:scheme="https" />
    </intent>
</queries>
```

这不是组件是否存在的问题，而是调用方“能不能看见”的问题。

## 第五部分：ActivityNotFoundException 的排查

遇到 ActivityNotFoundException，可以按下面顺序查：

```text
Intent 是否正确
  -> action / category / data 是否匹配
      -> 目标 App 是否安装
          -> 调用方是否有 queries 可见性
              -> 目标组件 exported / permission 是否允许访问
```

不要只加 try-catch。

try-catch 可以防崩溃，但不能解释为什么找不到。

## 第六部分：PackageManager 查询结果要谨慎

常见查询包括：

- `queryIntentActivities()`。
- `resolveActivity()`。
- `getPackageInfo()`。
- `getInstalledPackages()`。

这些 API 的结果受多个因素影响：

- 包是否安装。
- 用户状态。
- 组件 enabled 状态。
- 包可见性。
- 查询 flag。
- 系统版本。

所以“查不到”不是一个结论，只是一个线索。

## 本节小挑战

### Intent 侦探题

请为下面问题写出第一排查路径：

- 分享 Intent 没有候选 App。
- Android 10 正常，Android 11 查询不到微信。
- 隐式打开网页失败。
- 显式启动其他 App Activity 失败。
- resolveActivity 返回 null，但手机上明明装了目标 App。

## 本节实践任务

### 基础任务

- 写一个隐式 Intent。
- 使用 PackageManager 查询候选 Activity。
- 打印候选包名和 Activity 名。

### 进阶任务

- 在 Android 11+ 设备上测试 queries。
- 对比声明 queries 前后的查询结果。
- 写下包可见性对组件查询的影响。

## 本节小结

Intent 解析是 PMS 和组件系统的重要交汇点。显式 Intent 依赖具体组件，隐式 Intent 依赖 action、category、data 和 intent-filter。Android 11 后，包可见性又让“能不能查询到”变成新的排查维度。遇到 ActivityNotFound 或查询为空，要从 Intent、组件声明、可见性、权限和用户状态一起看。
