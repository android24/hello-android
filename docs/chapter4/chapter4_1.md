# 4.1 网络请求基础

前三章让我们完成了一个可交互 App 的基本外壳：能显示页面，能响应点击，能在 Activity 之间跳转。到了第 4 章，App 要开始接触真实数据。

真实应用很少只依赖写死在代码里的内容。新闻、课程、用户信息、订单、评论、配置开关，往往都来自服务端。本节先从网络请求的基础概念讲起，帮助你理解 Android App 如何从远程接口获取数据，并把结果展示到页面上。

## 本节定位

本节属于 `04-network-room-datastore` 的第一部分。

第 4 章的主线是：

- 先理解网络请求与接口数据。
- 再学习本地数据库 Room。
- 然后学习轻量配置存储 DataStore。
- 最后把网络、本地数据库和配置存储组合成一个离线可用的小项目。

本节只解决第一步：App 如何向服务端请求数据，以及请求结果如何进入 UI。

## 学习目标

学完本节后，你应该能够：

- 理解客户端、服务端、接口和 JSON 的基本关系。
- 知道 GET、POST 等 HTTP 方法的基础含义。
- 理解 Retrofit / OkHttp 在 Android 网络层中的位置。
- 定义一个简单的数据模型。
- 理解网络请求为什么需要异步执行。
- 将网络请求结果转换为页面状态。
- 初步理解加载中、成功、失败三种 UI 状态。

## 第一部分：App 为什么需要网络

一个只展示本地固定内容的 App 很容易写，但真实 App 需要不断变化的数据。

例如课程 App 可能需要：

- 从服务端获取课程列表。
- 获取某一节课的详情。
- 提交学习进度。
- 拉取最新公告。
- 根据用户身份展示不同内容。

这些能力都需要网络请求。

可以先这样理解：

```text
Android App -> HTTP 请求 -> 服务端接口 -> JSON 数据 -> App 页面
```

网络层的任务，就是把远程数据安全、清晰地带回 App。

## 第二部分：接口与 JSON

服务端通常会提供接口，例如：

```text
GET https://example.com/api/lessons
```

返回数据可能是 JSON：

```json
[
  {
    "id": 1,
    "title": "Jetpack Compose 入门",
    "description": "理解声明式 UI 的基础思想"
  },
  {
    "id": 2,
    "title": "状态与事件",
    "description": "让页面响应用户操作"
  }
]
```

在 Kotlin 中，我们通常会定义对应的数据类：

```kotlin
data class LessonDto(
    val id: Long,
    val title: String,
    val description: String
)
```

数据类是接口数据进入 App 的第一层结构。

## 第三部分：HTTP 方法

常见 HTTP 方法包括：

- `GET`：获取数据，例如获取课程列表。
- `POST`：提交数据，例如提交学习记录。
- `PUT`：整体更新资源。
- `PATCH`：局部更新资源。
- `DELETE`：删除资源。

入门阶段最常见的是 `GET` 和 `POST`。

第 4 章示例可以先从 `GET` 开始：请求一组课程数据，并展示在列表中。

## 第四部分：Retrofit 与 OkHttp

在 Android 中，常见网络组合是：

- OkHttp：负责底层 HTTP 请求。
- Retrofit：把接口声明转换成 Kotlin 调用方式。
- Kotlinx Serialization / Moshi / Gson：负责 JSON 解析。

使用 Retrofit 时，我们通常会先定义接口：

```kotlin
interface LessonApi {
    @GET("lessons")
    suspend fun getLessons(): List<LessonDto>
}
```

这里的 `suspend` 表示这是一个挂起函数，需要在协程中调用。协程会在后续章节专门展开，本章先记住：网络请求不能阻塞主线程。

## 第五部分：为什么网络请求不能在主线程执行

Android 主线程负责 UI 绘制和用户交互。如果在主线程执行耗时网络请求，页面会卡住，甚至触发 ANR。

所以网络请求必须异步执行：

```text
主线程：显示页面、响应点击
后台任务：请求网络、解析数据
主线程：拿到结果后更新 UI 状态
```

现代 Android 中，通常用协程处理异步任务。第 6 章会系统学习协程和 Flow；在第 4 章中，我们先用简单方式理解它们的使用场景。

## 第六部分：UI 状态

网络请求可能有三种典型状态：

- 加载中：正在请求数据。
- 成功：拿到了数据。
- 失败：请求失败或数据异常。

可以用一个简单 sealed interface 表示：

```kotlin
sealed interface LessonUiState {
    data object Loading : LessonUiState
    data class Success(val lessons: List<LessonDto>) : LessonUiState
    data class Error(val message: String) : LessonUiState
}
```

然后 UI 根据状态展示不同内容：

```kotlin
when (uiState) {
    LessonUiState.Loading -> Text("加载中")
    is LessonUiState.Success -> LessonList(uiState.lessons)
    is LessonUiState.Error -> Text(uiState.message)
}
```

这延续了第 2 章的思想：状态决定 UI。

## 本节实践任务

### 基础任务

- 找一个公开 JSON 示例接口。
- 观察接口返回的数据结构。
- 根据 JSON 定义 Kotlin data class。
- 思考哪些字段可以为空。

### Android 工程任务

- 在第 4 章示例工程中定义 `LessonDto`。
- 定义 `LessonUiState`。
- 先用假数据模拟网络成功状态。
- 在页面上分别展示加载中、成功、失败三种状态。

### 进阶任务

- 尝试接入 Retrofit。
- 添加一个简单的错误提示。
- 将网络返回的数据映射为 UI 使用的数据模型。
- 思考 DTO 和 UI Model 是否应该直接混用。

## 思考题

- 为什么真实 App 通常需要网络层？
- JSON 数据为什么要映射成 Kotlin data class？
- 为什么网络请求不能在主线程执行？
- 加载中、成功、失败这三种状态为什么重要？
- DTO 和页面状态有什么区别？

## 常见问题

### 一开始必须接真实接口吗？

不必须。可以先用假数据模拟接口返回，先把 UI 状态和数据结构跑通，再接真实网络。

### Retrofit 是必须的吗？

不是唯一选择，但它是 Android 中非常常见的网络接口声明工具。课程会优先用常见工程实践，避免一开始就造轮子。

### 网络层应该直接更新 UI 吗？

不建议。更好的方式是网络层返回数据，状态层转换成 UI 状态，页面根据状态展示内容。

## 本节小结

本节完成了网络请求的第一层地图：App 通过 HTTP 请求服务端接口，拿到 JSON 数据，转换成 Kotlin 数据结构，再变成 UI 状态展示在页面上。

下一节会进入 Room 本地数据库。它解决的问题是：当没有网络，或者需要缓存数据时，App 如何把数据可靠地保存在本地。
