# 第一课：打开 Android 世界的大门

如果说一门技术课程是一段旅程，那么第一课的任务不是急着堆满知识点，而是把路铺稳：你要知道 Android 是什么，开发环境由哪些部分组成，一个 App 是怎样从代码变成手机上的图标，又是怎样被点击、启动、运行起来的。

本课是《从零开始的 Android 全栈开发课程》的起点。它会带你完成 Android 开发环境的准备，认识最小可运行工程，并建立一张清晰的学习地图。学完这一课，你不一定已经会写复杂 App，但你会真正知道自己站在哪里、下一步往哪里走。

## 本课定位

第一课属于课程路线中的“阶段一：开发环境与 Kotlin 基础”。它承担三个作用：

- 帮助零基础同学完成 Android 学习前的准备工作。
- 帮助有经验的开发者统一课程环境，减少后续构建、运行和调试上的干扰。
- 帮助所有学习者建立 Android 工程的第一印象：目录、构建、运行、调试、日志，以及学习路径。

这一课不追求一次讲完所有细节。我们会先把最小闭环跑通：安装工具，创建工程，运行 App，看懂关键目录，完成一次简单修改，再用几个问题把后续课程串起来。

## 学习目标

学完本课后，你应该能够：

- 说清楚 Android、Android Studio、Android SDK、Gradle、JDK 之间的关系。
- 独立安装并配置 Android Studio 与 Android SDK。
- 创建或打开一个最小 Android 工程。
- 使用模拟器或真机运行第一个 App。
- 看懂 Android 工程中最常见的几个目录和配置文件。
- 使用 Logcat 观察应用日志，完成一次基础调试。
- 理解本课程从基础、应用架构到 Android Framework 的整体学习顺序。

## 前置知识

本课默认你可以从零开始，不要求具备 Android 开发经验。

如果你完全没有编程基础，建议先对以下概念有一个朴素理解：

- 文件与文件夹
- 命令行或终端
- 程序、编译、运行
- 变量、函数、类这些编程中的基本词汇

如果这些词现在还比较陌生，也不用担心。第一阶段会继续补齐 Kotlin 和工程基础，重要的是先把开发环境跑起来。

## 环境准备

建议准备以下开发环境：

- 操作系统：macOS、Windows 或 Linux。
- 开发工具：Android Studio 稳定版。
- JDK：优先使用 Android Studio 内置 JDK。
- Android SDK：安装课程工程要求的 SDK Platform 与 Build Tools。
- 运行设备：Android 模拟器或 Android 真机。
- 版本管理：Git。

推荐机器配置：

- 内存：16GB 及以上更舒适，8GB 可以学习但可能会慢。
- 磁盘：至少预留 20GB 空间给 Android Studio、SDK、模拟器和课程工程。
- 网络：首次安装 SDK、Gradle 依赖时需要稳定网络。

## 第一课内容安排

### 1. 认识 Android 开发环境

在真正写代码之前，我们先拆开 Android 开发环境：

- Android Studio：写代码、调试、运行、管理工程的 IDE。
- Android SDK：Android 平台 API、构建工具、模拟器镜像等开发资源。
- JDK：运行 Gradle 和部分构建工具所需的 Java 环境。
- Gradle：Android 工程的构建系统，负责依赖、编译、打包、安装。
- Emulator / Device：运行 App 的模拟器或真实手机。

可以把它们理解成一条流水线：你在 Android Studio 中写代码，Gradle 根据配置把代码编译打包，Android SDK 提供平台能力，最终 App 被安装到模拟器或真机上运行。

### 2. 安装 Android Studio

本节需要完成：

- 下载并安装 Android Studio。
- 通过 Setup Wizard 安装推荐组件。
- 安装至少一个 Android SDK Platform。
- 创建一个 Android Virtual Device。
- 确认 Android Studio 能够正常打开工程。

检查点：

- Android Studio 可以启动。
- SDK Manager 中至少有一个可用的 Android SDK。
- Device Manager 中至少有一个模拟器，或者已经连接一台真机。

### 3. 创建第一个 Android 工程

建议使用最小模板创建工程，例如 Empty Activity。课程前期会先以简单工程为主，避免一开始就陷入复杂依赖。

创建时需要关注几个字段：

- Project name：工程名称。
- Package name：应用包名，通常形如 `com.example.helloandroid`。
- Save location：工程保存位置。
- Minimum SDK：应用支持的最低 Android 版本。
- Build configuration language：推荐使用 Kotlin DSL 或课程工程指定的配置。

第一次创建工程时，不要急着修改代码。先完成 Gradle Sync，再运行默认 App。能跑起来，就是第一课最重要的胜利。

### 4. 认识工程结构

一个基础 Android 工程通常包含以下内容：

```text
project-root/
  settings.gradle.kts
  build.gradle.kts
  gradle/
  gradlew
  gradlew.bat
  app/
    build.gradle.kts
    src/
      main/
        AndroidManifest.xml
        java/ 或 kotlin/
        res/
```

需要先记住这些核心文件：

- `settings.gradle.kts`：声明工程包含哪些模块。
- 根目录 `build.gradle.kts`：配置工程级插件和通用构建信息。
- `app/build.gradle.kts`：配置 App 模块的编译版本、依赖、插件和打包信息。
- `AndroidManifest.xml`：声明应用组件、权限、入口 Activity 等信息。
- `src/main/java` 或 `src/main/kotlin`：存放 Kotlin / Java 源码。
- `src/main/res`：存放布局、图片、字符串、颜色、主题等资源。

第一课不要求你完全掌握每一行配置，但要知道这些文件各自负责什么。后续课程会不断回到它们。

### 5. 运行第一个 App

运行 App 的基本流程：

- 选择运行设备：模拟器或真机。
- 点击 Run。
- 等待 Gradle 构建、安装、启动。
- 在设备上看到默认页面。

如果运行失败，优先检查：

- Gradle Sync 是否成功。
- SDK 是否安装完整。
- 模拟器是否启动正常。
- 真机是否开启 USB 调试。
- 报错信息中是否出现网络、依赖、JDK 或 SDK 相关问题。

### 6. 修改一次页面内容

完成第一次小修改，例如：

- 修改页面中的文字。
- 修改应用名称。
- 修改主题颜色。
- 添加一行日志。

这一小步的目的不是炫技，而是让你感受到完整开发循环：

```text
修改代码 -> 构建工程 -> 安装应用 -> 运行验证 -> 观察日志
```

从今天开始，每一节课都应该尽量形成这样的闭环。

### 7. 学会看 Logcat

Logcat 是 Android 开发中最常用的观察窗口。第一课只需要掌握三件事：

- 能打开 Logcat 面板。
- 能根据包名过滤当前 App 日志。
- 能分辨普通日志、警告和错误。

后续在学习生命周期、网络请求、数据库、性能优化和 Framework 调用链时，Logcat 都会反复出现。早一点熟悉它，会让你后面轻松很多。

## 本课实践任务

### 基础任务

- 安装 Android Studio。
- 创建一个 Empty Activity 工程。
- 启动模拟器或连接真机。
- 成功运行默认 App。
- 修改页面中的一段文字并重新运行。

### 进阶任务

- 找到 `AndroidManifest.xml`，观察应用入口 Activity。
- 找到 `app/build.gradle.kts`，记录 `compileSdk`、`minSdk` 和 `targetSdk`。
- 打开 Logcat，过滤当前应用日志。
- 尝试在代码中添加一条日志，并在 Logcat 中找到它。

### 思考题

- 为什么 Android 工程需要 Gradle？
- `compileSdk`、`minSdk`、`targetSdk` 分别大致代表什么？
- 为什么一个 App 需要 `AndroidManifest.xml`？
- 模拟器和真机调试分别适合什么场景？
- 如果一个 App 点击后白屏或闪退，你会先看哪里？

## 常见问题

### Gradle Sync 很慢怎么办？

首次同步会下载 Gradle、插件和依赖，耗时较长是正常现象。可以先确认网络是否稳定，再查看 Android Studio 底部的 Sync 日志。课程工程建议使用自带的 Gradle Wrapper，不要随意替换 Gradle 版本。

### 模拟器启动失败怎么办？

优先检查系统虚拟化能力、模拟器镜像是否安装完整、磁盘空间是否充足。如果机器配置较低，可以先使用真机调试。

### 真机无法识别怎么办？

检查 USB 调试是否开启、数据线是否支持数据传输、手机是否弹出授权确认框。Windows 用户可能还需要安装厂商 USB 驱动。

### 一开始要不要学 XML View？

本课程会以现代 Android 技术栈为主线，重点学习 Kotlin、Jetpack 与 Compose。但传统 View、资源系统、生命周期和 Framework 机制仍然重要。课程会在合适阶段解释它们之间的关系，而不是把它们割裂成两套世界。

## 本课小结

第一课的核心不是写出复杂功能，而是建立 Android 学习的第一条主干：工具如何安装，工程如何运行，代码如何变成 App，问题如何观察。

当你能独立运行第一个 App，并完成一次修改、一次构建、一次验证时，Android 的门就已经打开了。接下来，我们会进入 Kotlin 与 Android 基础，开始真正理解一段代码如何组织成一个可以交互的应用。

## 下一课预告

下一课将进入 Kotlin 基础与 Android 工程语法。我们会学习变量、函数、类、空安全、集合、扩展函数等内容，并把这些语法放进 Android 场景里理解，而不是把 Kotlin 当作孤立的语言练习。
