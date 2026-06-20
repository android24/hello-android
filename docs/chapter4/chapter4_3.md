# 4.3 DataStore 配置存储

Room 适合保存结构化数据，例如课程列表、收藏记录和历史记录。但并不是所有本地数据都需要数据库。很多时候，我们只想保存一些轻量配置：是否首次打开、是否开启深色模式、最近一次同步时间、用户选择的排序方式。

这些数据更适合使用 DataStore。DataStore 是 Android 用于替代 SharedPreferences 的现代化数据存储方案，支持异步读取、类型安全思路和 Flow 数据流。

## 本节定位

本节属于 `04-network-room-datastore` 的第三部分。

4.1 解决远程数据，4.2 解决结构化本地数据，4.3 解决轻量配置存储。

本节重点包括：

- 什么数据适合放在 DataStore。
- DataStore 与 SharedPreferences 的区别。
- Preferences DataStore 的基本使用方式。
- 如何保存和读取用户偏好。
- 如何将配置状态展示到 Compose 页面。

## 学习目标

学完本节后，你应该能够：

- 判断哪些数据适合使用 DataStore。
- 理解 Preferences DataStore 的基本概念。
- 定义配置 key。
- 保存一个布尔值或字符串配置。
- 读取配置并转换为 UI 状态。
- 理解 DataStore 为什么适合与 Flow 配合。

## 第一部分：什么数据适合 DataStore

DataStore 适合保存轻量配置，例如：

- 是否首次打开 App。
- 是否启用深色模式。
- 用户选择的课程排序方式。
- 最近一次同步时间。
- 是否只在 Wi-Fi 下同步。

不适合 DataStore 的数据：

- 大量课程列表。
- 复杂关系数据。
- 需要条件查询的数据。
- 需要事务管理的数据。

这些更适合 Room。

## 第二部分：DataStore 与 Room 的分工

可以这样划分：

```text
Room：保存结构化业务数据
DataStore：保存轻量用户配置
```

例如课程 App：

- 课程列表：Room。
- 收藏记录：Room。
- 是否首次打开：DataStore。
- 上次同步时间：DataStore。
- 用户选择的主题模式：DataStore。

清晰分工能减少后续维护成本。

## 第三部分：Preferences DataStore

Preferences DataStore 使用 key-value 形式保存数据。

例如定义 key：

```kotlin
val SHOW_COMPLETED_KEY = booleanPreferencesKey("show_completed")
```

保存配置：

```kotlin
suspend fun setShowCompleted(enabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[SHOW_COMPLETED_KEY] = enabled
    }
}
```

读取配置：

```kotlin
val showCompletedFlow: Flow<Boolean> = dataStore.data
    .map { preferences ->
        preferences[SHOW_COMPLETED_KEY] ?: false
    }
```

这里会看到 Flow。第 6 章会系统学习 Flow，本章先理解它表示“会随时间变化的数据流”。

## 第四部分：配置如何影响 UI

假设用户可以选择是否显示已完成课程：

```text
DataStore 保存 showCompleted
页面读取 showCompleted
列表根据 showCompleted 过滤课程
用户切换开关
写入 DataStore
页面自动更新
```

这和第 2 章的状态思想是一致的，只是状态来源从内存变成了本地存储。

## 第五部分：DataStore 使用注意点

使用 DataStore 时要注意：

- 读写是异步的。
- 不要在主线程阻塞等待结果。
- key 名称要稳定，避免随意修改。
- 配置项不宜过多、过杂。
- 复杂结构数据不要硬塞进 Preferences DataStore。

如果需要强类型结构，可以了解 Proto DataStore；入门阶段先掌握 Preferences DataStore 即可。

## 本节实践任务

### 基础任务

- 列出课程 App 中适合 DataStore 的配置项。
- 定义一个布尔配置，例如 `showCompleted`。
- 定义一个字符串配置，例如 `sortType`。
- 思考它们如何影响 UI。

### Android 工程任务

- 在第 4 章示例工程中创建 `data/preferences` 包。
- 定义一个 `UserPreferences` 数据类。
- 定义保存和读取配置的方法。
- 在页面中展示当前配置值。
- 添加按钮切换配置。

### 进阶任务

- 保存最近一次同步时间。
- 在页面上显示“上次同步时间”。
- 添加“仅显示未完成课程”开关。
- 思考配置变化后列表是否应该重新过滤。

## 思考题

- DataStore 和 Room 的边界是什么？
- 为什么 SharedPreferences 在现代项目中不再是首选？
- 什么配置适合用布尔值，什么配置适合用字符串或枚举？
- 配置变化为什么也可以看作 UI 状态变化？
- 如果配置项越来越多，应该如何组织？

## 常见问题

### DataStore 能替代 Room 吗？

不能。DataStore 适合轻量配置，Room 适合结构化数据。两者解决的问题不同。

### DataStore 是否一定要配合 Flow？

DataStore 的读取天然以 Flow 形式暴露。你可以先把它理解成“配置变化时会自动发出新值”。

### 能不能把整个用户对象存进 DataStore？

不建议。复杂对象更适合数据库或专门的缓存策略。DataStore 更适合简单、稳定、轻量的配置。

## 本节小结

本节补齐了第 4 章的数据存储版图：网络负责远程数据，Room 负责结构化本地数据，DataStore 负责轻量配置。

下一节会把它们组合起来，完成一个简单的离线可用课程列表项目。
