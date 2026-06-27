# 7.6 Gradle、多环境配置与构建变体

真实 Android 工程不只有一套运行环境。

开发时可能连测试服务器，预发时连灰度环境，正式发布时连线上环境。不同环境可能有不同包名、不同接口地址、不同日志开关、不同签名配置。

这就需要 Gradle 和构建变体。

这一节要处理的是一个真实到有点紧张的问题：你以为自己装的是测试包，结果它连的是正式接口；你以为只是开了调试日志，结果 release 包也把日志带出去了。多环境配置就是为了让这些差异有规矩、有位置、有自动化保障。

## 本节定位

本节进入工程化配置。

前面我们解决代码如何分层、对象如何装配、模块如何拆分。本节解决：同一套代码如何构建出不同版本。

## 学习目标

学完本节后，你应该能够：

- 理解 Gradle 在 Android 工程中的作用。
- 理解 buildTypes 和 productFlavors。
- 使用 BuildConfig 管理环境变量。
- 知道 debug、release 的差异。
- 理解多环境配置和资源隔离。

## 第一部分：Gradle 负责什么

Gradle 不只是点运行时出现的那个进度条。

它负责：

- 编译代码。
- 管理依赖。
- 打包 APK / AAB。
- 处理资源。
- 配置不同构建类型。
- 执行测试、混淆、签名等任务。

可以把 Gradle 看成工程的施工队长：它不写业务逻辑，但决定工程如何被建起来。

## 第二部分：buildTypes

常见 buildTypes：

```kotlin
android {
    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
        }
    }
}
```

debug 适合开发调试，release 适合正式发布。

差异可能包括：

- 是否可调试。
- 是否开启混淆。
- 是否压缩资源。
- 使用什么签名。
- 日志是否输出。

## 第三部分：productFlavors

productFlavors 适合区分产品环境或渠道：

```kotlin
flavorDimensions += "env"

productFlavors {
    create("dev") {
        dimension = "env"
    }
    create("prod") {
        dimension = "env"
    }
}
```

这样可以得到：

```text
devDebug
devRelease
prodDebug
prodRelease
```

不同变体可以使用不同接口地址、包名后缀或资源。

## 第四部分：BuildConfig

可以通过 BuildConfig 暴露环境变量：

```kotlin
buildConfigField(
    "String",
    "BASE_URL",
    "\"https://api.example.com\""
)
```

代码中使用：

```kotlin
val baseUrl = BuildConfig.BASE_URL
```

这样网络层可以根据构建变体拿到不同配置。

## 第五部分：资源隔离

不同环境也可以放不同资源：

```text
src/dev/res/values/strings.xml
src/prod/res/values/strings.xml
```

例如：

- dev 显示“课程 App 测试版”。
- prod 显示“课程 App”。

这能帮助测试人员一眼区分当前安装的是哪个环境。

## 第六部分：多环境不要写死在代码里

不建议这样：

```kotlin
val baseUrl = "https://test.example.com"
```

更好的方式是让构建配置提供环境信息。

否则切环境会变成手动改代码，很容易把测试地址带到线上。

手动改环境最危险的地方，不是麻烦，而是它太容易“刚好忘了改回来”。Gradle 配置的意义，就是把这种靠记忆完成的动作，变成构建系统自动完成的动作。

## 本节小挑战

请设计课程 App 的两个环境：

- dev：测试接口、显示测试版名称、日志全开。
- prod：正式接口、正式名称、关闭调试日志。

思考这些差异应该放在代码里，还是 Gradle 配置里。

## 本节实践任务

### 基础任务

- 添加 `debug` 和 `release` 配置。
- 添加 `dev` 和 `prod` flavor。
- 为不同 flavor 设置不同 `BASE_URL`。
- 在页面上显示当前环境名称。

### 进阶任务

- 为 dev/prod 准备不同 app name。
- 思考多渠道包如何扩展。
- 了解 signingConfig 与 release 打包关系。

## 本节小结

Gradle 和构建变体让同一套代码可以服务不同环境。工程化不是只写 Kotlin，真正可交付的 App 还需要清晰的构建配置、环境隔离和发布策略。
