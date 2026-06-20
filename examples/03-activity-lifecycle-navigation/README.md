# 示例工程：Activity、生命周期与导航

## 对应章节

第3章 Activity、生命周期与导航

## 工程目标

本工程用于配合第 3 章学习 Android Activity 组件模型、生命周期日志、Intent 页面跳转、参数传递和返回栈。它会从一个课程章节列表页开始，点击课程项进入详情页，并在 Logcat 中观察两个 Activity 的生命周期变化。

## 覆盖知识点

- `Activity`
- `AndroidManifest.xml`
- 生命周期回调
- Logcat 观察生命周期
- Intent 显式跳转
- Intent 参数传递
- 返回键与任务栈
- Compose 页面承载多 Activity 示例

## 工程结构

```text
03-activity-lifecycle-navigation/
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/com/helloandroid/navigation/
        MainActivity.kt
        DetailActivity.kt
        LessonModels.kt
        NavigationContract.kt
      res/
        values/
          strings.xml
          colors.xml
          themes.xml
```

## 运行方式

1. 使用 Android Studio 打开 `examples/03-activity-lifecycle-navigation` 目录。
2. 等待 Gradle Sync 完成。
3. 选择模拟器或真机。
4. 运行 `app` 模块。
5. 打开 Logcat，过滤 `MainActivityLifecycle` 或 `DetailActivityLifecycle`。

## 练习任务

### 基础任务

- 运行 App，观察 `MainActivity` 的生命周期日志。
- 点击任意课程卡片，跳转到 `DetailActivity`。
- 在详情页确认标题、描述和章节编号已经通过 Intent 传递。
- 按返回键，观察两个 Activity 的生命周期变化。

### 进阶任务

- 给 Intent 增加一个新的参数，例如难度等级。
- 在 `DetailActivity` 中读取这个参数并显示。
- 修改 `DetailActivity` 的返回按钮文案。
- 尝试按 Home 键、重新进入 App，观察生命周期日志。
- 尝试旋转屏幕，观察 Activity 是否重新创建。

## 章节衔接

完成本工程后，可以继续学习 Navigation、返回栈、启动模式和更复杂的页面流转。后续也可以将显式 Intent 示例升级为 Navigation Compose 示例。
