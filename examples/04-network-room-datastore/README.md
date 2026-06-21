# 示例工程：网络、Room 与 DataStore

## 对应章节

第4章 网络、Room 与 DataStore

## 工程目标

本工程将用于配合第 4 章学习真实数据处理能力：网络请求、本地数据库、轻量配置存储和离线缓存。

## 当前效果

运行后你会看到一个“课程列表缓存 Demo”：

- 页面启动后自动模拟一次网络刷新。
- 网络返回的课程数据会写入 Room。
- 页面会从 Room 读取缓存并展示。
- DataStore 会保存是否显示已完成课程，以及最近同步时间。
- 点击“模拟失败刷新”时，页面会展示错误提示，但仍保留本地缓存。

这个示例没有接入真实接口，而是用 `FakeLessonApi` 模拟远程数据。这样可以先把网络、Room、DataStore、Repository 和 UI 状态的完整链路跑通，再把假网络源替换成 Retrofit。

## 运行方式

1. 使用 Android Studio 打开 `examples/04-network-room-datastore`。
2. 等待 Gradle Sync 完成。
3. 选择模拟器或真机运行 `app`。
4. 点击“刷新课程”和“模拟失败刷新”，观察页面状态变化。
5. 切换“显示已完成课程”，重新打开 App，观察配置是否被保留。

## 工程结构

```text
app/src/main/java/com/helloandroid/networkroomdatastore/
  MainActivity.kt
  data/
    local/
      AppDatabase.kt
      LessonDao.kt
      LessonEntity.kt
    preferences/
      UserPreferences.kt
      UserPreferencesDataSource.kt
    remote/
      FakeLessonApi.kt
      LessonDto.kt
    repository/
      LessonRepository.kt
      LessonMappers.kt
  ui/
    LessonListScreen.kt
    LessonListUiModel.kt
    LessonListUiState.kt
    LessonListViewModel.kt
```

## 覆盖知识点

- 网络请求基础
- JSON 与数据模型
- Retrofit / OkHttp 的接口形态理解
- Room Entity / Dao / Database
- DataStore 配置存储
- 加载中 / 成功 / 失败 UI 状态
- 本地缓存与离线展示

## 练习任务

### 基础任务

- 修改 `FakeLessonApi` 中的课程数据。
- 给 `LessonDto` 增加一个字段，并完成 DTO -> Entity -> UI Model 的映射。
- 修改 `LessonDao` 的排序方式。
- 修改“显示已完成课程”的默认值。

### 进阶任务

- 将 `FakeLessonApi` 替换为 Retrofit 接口。
- 增加收藏字段，并保存到 Room。
- 给缓存增加过期策略。
- 添加空状态页面。
- 将 Repository 返回的数据进一步整理成更清晰的架构分层，为第 5 章做准备。
