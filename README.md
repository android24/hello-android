#《从零开始的 Android 全栈开发课程》

## 🎯 项目定位
《从零开始的 Android 全栈开发课程》是一套面向 Android 学习者的系统化开源课程。它不满足于让你“照着写出一个页面”，而是希望带你真正走完一条从入门到进阶、从应用到架构、从业务代码到 Android Framework 的成长路径。

课程以 Kotlin 与现代 Android 技术栈为主线，覆盖 Jetpack Compose、架构设计、网络与本地数据、协程与 Flow、依赖注入、模块化、测试、性能优化、稳定性治理，以及 Android Framework 核心机制。每一门课程都将尽量保持“一个 README + 一个可运行代码工程”的组织方式：README 负责讲清楚为什么学、学什么、怎么学；代码工程负责把知识落到真实项目里。

这不是一本零散知识点合集，而是一条面向工程实践的路线。你会先学会搭建一个应用，再学会让它变得清晰、稳定、可维护，最后继续向下理解：一次启动、一次点击、一次页面跳转、一次渲染，究竟是怎样穿过 Framework、系统服务和底层机制，最终变成用户手中的 Android 体验。

## 项目受众
本课程非常适合以下同学：

🏁 0 基础 / 无编程经验但想进入 Android 开发的人
从编程语言、开发工具、项目结构开始铺路，不要求你一开始就理解复杂工程。

🔰 有一点编程经验但从未系统学习 Android 的人
帮助你快速建立 Android 的知识地图，避免只会复制代码，却不知道代码运行在哪里。

🎓 已经能写 Android，但想补齐 Jetpack、Compose、架构与工程能力的人
课程会从“功能可用”继续推进到“结构清晰、边界明确、质量可控”。

🚀 想冲击中高级、资深 Android 工程师的人
课程后半程会进入性能、稳定性、源码阅读、Binder、AMS、WMS、PMS、View 绘制等 Framework 主题，帮助你把经验沉淀成体系。

🧭 想用一个完整项目沉淀简历作品的人
课程会通过阶段项目串联知识点，让每一步学习都能留下可展示、可复盘、可继续演进的代码。

## 目录
*这里写你的项目目录，已完成的部分用添加上跳转链接*

- [总路线图](#总路线图)
  - [阶段一：开发环境与 Kotlin 基础](#阶段一开发环境与-kotlin-基础)
  - [阶段二：Android 应用基础](#阶段二android-应用基础)
  - [阶段三：现代 UI 与 Jetpack Compose](#阶段三现代-ui-与-jetpack-compose)
  - [阶段四：数据、网络与异步编程](#阶段四数据网络与异步编程)
  - [阶段五：应用架构与工程化](#阶段五应用架构与工程化)
  - [阶段六：质量、性能与发布](#阶段六质量性能与发布)
  - [阶段七：Android Framework 与源码进阶](#阶段七android-framework-与源码进阶)
- [学习顺序](#学习顺序)
- [环境要求](#环境要求)

### 总路线图

这门课程按照“先会写，再写好，最后懂系统”的顺序展开。前半程建立应用开发能力，中段训练真实工程能力，后半程进入 Framework 与源码视角。

### 阶段一：开发环境与 Kotlin 基础

- Android Studio 安装、SDK 配置、模拟器与真机调试
- Gradle、项目结构、常见目录与构建流程
- Kotlin 基础语法、空安全、集合、函数、类与扩展
- 第一个 Android 工程：从 Hello World 到理解 App 如何启动

### 阶段二：Android 应用基础

- Activity、生命周期、Intent、资源系统与权限
- 日志、调试、断点、异常分析
- 基础控件、状态保存、页面跳转与参数传递
- 阶段项目：Todo / 记账 / 个人资料 App

### 阶段三：现代 UI 与 Jetpack Compose

- Compose 基础、Composable、Modifier、State
- 列表、表单、主题、动画与响应式布局
- Navigation、ViewModel 与 UI 状态管理
- 阶段项目：用 Compose 重构基础 App

### 阶段四：数据、网络与异步编程

- Retrofit / OkHttp 网络请求
- Room 本地数据库与 DataStore 配置存储
- Kotlin Coroutines、Flow、错误处理与取消
- WorkManager 与后台任务
- 阶段项目：新闻 / 笔记 / 阅读类 App

### 阶段五：应用架构与工程化

- MVVM、UDF、Repository、UseCase
- Hilt 依赖注入、分层架构与模块化
- 多环境配置、构建变体、资源隔离
- 组件边界、接口设计、可维护性与可测试性
- 阶段项目：把单体 App 演进成清晰的工程结构

### 阶段六：质量、性能与发布

- 单元测试、UI 测试、Mock 与测试替身
- 启动优化、内存优化、卡顿分析、包体积优化
- 崩溃治理、日志体系、埋点与可观测性
- R8 / ProGuard、签名、打包与发布流程
- 阶段项目：让 App 从“能跑”走向“可交付”

### 阶段七：Android Framework 与源码进阶

- Android 系统架构、AOSP 代码结构与源码阅读方法
- Binder 通信机制与系统服务
- SystemServer、AMS / ATMS、WMS、PMS
- View 绘制流程、Input 事件分发、Choreographer
- SurfaceFlinger、渲染链路与应用显示原理
- 阶段项目：从一次点击追踪到 Framework 调用链

### 学习顺序

建议按照目录顺序学习，不要急着跳到 Framework。Android 的底层知识并不是孤立存在的，它最好从真实业务问题里长出来：当你写过页面，才会真正关心 View 如何绘制；当你处理过页面跳转，才会理解 AMS / ATMS 的价值；当你遇到卡顿和内存问题，Framework 的调用链才不再是抽象名词。

推荐节奏如下：

- 第 1 步：先完成环境搭建，确保可以独立创建、运行、调试一个 Android 工程。
- 第 2 步：掌握 Kotlin 与 Android 基础，能写出简单但完整的页面和交互。
- 第 3 步：进入 Compose 与 Jetpack，建立现代 Android UI 开发方式。
- 第 4 步：加入网络、本地数据、协程和 Flow，让 App 具备真实业务能力。
- 第 5 步：重构项目架构，学习分层、依赖注入、模块化和测试。
- 第 6 步：补齐性能、稳定性、发布与工程协作能力。
- 第 7 步：回到系统底层，从应用行为反向理解 Framework。

每一章建议都按照“阅读 README -> 运行代码工程 -> 修改一个小功能 -> 完成进阶任务 -> 复盘关键问题”的方式学习。真正的掌握，不是看懂一遍，而是能在自己的项目里重新写出来。

### 环境要求

为了保证课程体验一致，建议使用以下开发环境：

- 操作系统：macOS、Windows 或 Linux，推荐使用内存 16GB 及以上的开发机器。
- IDE：Android Studio 稳定版，建议使用当前官方稳定版本或更新版本。
- JDK：JDK 17 及以上，优先使用 Android Studio 内置 JDK。
- Android Gradle Plugin：建议使用课程工程中声明的版本，不建议自行随意升级。
- Gradle：使用项目自带的 Gradle Wrapper，即通过 `./gradlew` 或 `gradlew.bat` 执行构建。
- Android SDK：安装课程工程要求的 `compileSdk`、`minSdk` 以及对应 Build Tools。
- 运行设备：Android 模拟器或 Android 8.0 及以上真机，推荐准备一台真机用于权限、性能和系统行为调试。
- Git：用于拉取课程代码、提交练习记录和对比不同阶段的实现。

首次运行建议：

- 使用 Android Studio 打开对应章节目录下的代码工程。
- 等待 Gradle Sync 完成。
- 选择模拟器或真机运行 `app` 模块。
- 如果构建失败，优先检查 JDK、SDK、Gradle Sync 日志和网络代理配置。

### 示例工程

课程配套示例工程统一放在 [examples](examples/) 目录下。每个大章节对应一个独立示例工程，建议按照“阅读章节文档 -> 运行示例工程 -> 完成练习任务 -> 自己做一次改造”的方式学习。

当前已创建：

- [第1章 Kotlin 与 Android 基础示例工程](examples/01-kotlin-and-android-basics/)
- [第2章 Compose UI 基础示例工程](examples/02-compose-ui-basics/)
- [第3章 Activity、生命周期与导航示例工程](examples/03-activity-lifecycle-navigation/)

## 贡献者名单

| 姓名 | 职责 | 简介 |
| :----| :---- | :---- |
| 龚福均 | 项目负责人 | 一个有十年Android开发经验的从业者 |
| 虚位以待 | 第1章贡献者 | 小明的朋友 |
| 虚位以待 | 第2章贡献者 | 小明的朋友 |

## 参与贡献

- 如果你发现了一些问题，可以提Issue进行反馈，如果提完没有人回复你可以联系[保姆团队](https://github.com/datawhalechina/DOPMC/blob/main/OP.md)的同学进行反馈跟进~
- 如果你想参与贡献本项目，可以提Pull request，如果提完没有人回复你可以联系[保姆团队](https://github.com/datawhalechina/DOPMC/blob/main/OP.md)的同学进行反馈跟进~
- 如果你对 Datawhale 很感兴趣并想要发起一个新的项目，请按照[Datawhale开源项目指南](https://github.com/datawhalechina/DOPMC/blob/main/GUIDE.md)进行操作即可~

## 关注我们

<div align=center>
<p>扫描下方二维码关注公众号：Datawhale</p>
<img src="https://raw.githubusercontent.com/datawhalechina/pumpkin-book/master/res/qrcode.jpeg" width = "180" height = "180">
</div>

## LICENSE

<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="知识共享许可协议" style="border-width:0" src="https://img.shields.io/badge/license-CC%20BY--NC--SA%204.0-lightgrey" /></a><br />本作品采用<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">知识共享署名-非商业性使用-相同方式共享 4.0 国际许可协议</a>进行许可。

*注：默认使用CC 4.0协议，也可根据自身项目情况选用其他协议*
