# 示例工程：Kotlin 与 Android 基础

## 对应章节

第1章 Kotlin 与 Android 基础

## 工程目标

本工程用于配合第 1 章学习 Android 开发环境、Kotlin 基础、工程结构和资源系统。它不是为了展示复杂功能，而是为了帮助你跑通一个最小 Android App，并看懂一个基础工程由哪些部分组成。

## 覆盖知识点

- Android 工程结构
- `AndroidManifest.xml`
- `MainActivity`
- Kotlin 变量、函数、数据类
- 字符串资源
- 颜色资源
- 基础资源目录
- Logcat 输出

## 工程结构

```text
01-kotlin-and-android-basics/
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/com/helloandroid/basics/
        MainActivity.kt
        CourseInfo.kt
      res/
        values/
          strings.xml
          colors.xml
          themes.xml
```

## 运行方式

1. 使用 Android Studio 打开 `examples/01-kotlin-and-android-basics` 目录。
2. 等待 Gradle Sync 完成。
3. 选择模拟器或真机。
4. 运行 `app` 模块。

如果本机 Android Studio 提示升级 Gradle、Android Gradle Plugin 或 SDK，可先保留当前版本完成首次运行；后续再统一升级课程工程配置。

## 练习任务

### 基础任务

- 修改 `strings.xml` 中的 `app_name`。
- 修改 `CourseInfo.kt` 中的课程阶段文案。
- 在 `MainActivity.kt` 中新增一条日志。
- 运行 App，并在 Logcat 中找到日志输出。

### 进阶任务

- 新增一个 `StudentInfo` data class。
- 写一个函数，根据学生姓名生成欢迎语。
- 把欢迎语显示到页面上。
- 新增一个颜色资源，并在页面中使用。

## 章节衔接

完成本工程后，可以进入第 2 章 `Compose UI 基础`。第 2 章会在此基础上继续学习 Composable、Modifier、布局、状态和事件。
