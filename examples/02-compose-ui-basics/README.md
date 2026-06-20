# 示例工程：Compose UI 基础

## 对应章节

第2章 Compose UI 基础

## 工程目标

本工程用于配合第 2 章学习 Jetpack Compose 的基础 UI 能力。它会从一个课程首页开始，练习 Composable 拆分、Modifier 修饰、基础布局、状态与点击事件。

## 覆盖知识点

- `@Composable`
- `setContent`
- `Modifier`
- `Column` / `Row` / `Box`
- `Text` / `Button`
- `remember` / `mutableStateOf`
- 基础状态与点击事件
- 字符串和颜色资源

## 工程结构

```text
02-compose-ui-basics/
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/com/helloandroid/composebasics/
        MainActivity.kt
        CourseModels.kt
      res/
        values/
          strings.xml
          colors.xml
          themes.xml
```

## 运行方式

1. 使用 Android Studio 打开 `examples/02-compose-ui-basics` 目录。
2. 等待 Gradle Sync 完成。
3. 选择模拟器或真机。
4. 运行 `app` 模块。

## 练习任务

### 基础任务

- 修改课程标题和副标题。
- 调整 `Column`、`Row`、`Box` 的排列方式。
- 给页面元素添加或删除 `Modifier.padding`。
- 点击“开始学习”按钮，观察页面状态变化。

### 进阶任务

- 新增一个课程章节卡片。
- 将按钮点击次数显示在页面上。
- 给章节卡片增加不同颜色。
- 将 `CourseHomeScreen` 拆分出更多小 Composable。

## 章节衔接

完成本工程后，可以进入第 3 章 `Activity、生命周期与导航`。第 3 章会在 Compose 页面基础上继续学习多页面跳转、生命周期日志和 Intent 参数传递。
