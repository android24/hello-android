# 4.2 Room 本地数据库

网络让 App 能获取远程数据，但真实应用不能完全依赖网络。用户可能在地铁里、飞机上、弱网环境中打开 App；服务端也可能暂时不可用。为了让应用更稳定、更快，很多数据需要保存在本地。

Room 是 Android 官方推荐的 SQLite 抽象层。它让我们用 Kotlin 类、接口和注解管理本地数据库，而不必直接手写大量 SQL 细节。

## 本节定位

本节属于 `04-network-room-datastore` 的第二部分。

4.1 解决远程数据从哪里来，4.2 解决数据如何落到本地。

本节重点包括：

- 为什么需要本地数据库。
- Entity、Dao、Database 三个核心概念。
- 如何定义本地表结构。
- 如何插入、查询和删除数据。
- 网络数据和本地数据如何衔接。

## 学习目标

学完本节后，你应该能够：

- 理解 Room 与 SQLite 的关系。
- 定义一个 `@Entity`。
- 定义一个 `@Dao`。
- 定义一个 `RoomDatabase`。
- 理解主键、表名和字段的基础含义。
- 初步设计课程数据的本地缓存表。
- 理解为什么数据库操作也不能阻塞主线程。

## 第一部分：为什么需要本地数据库

本地数据库常用于：

- 缓存网络数据。
- 保存用户收藏。
- 保存草稿。
- 保存离线内容。
- 保存历史记录。

例如课程 App 可以把课程列表缓存到本地：

```text
首次打开：从网络获取课程 -> 保存到 Room -> 显示页面
再次打开：先显示 Room 缓存 -> 后台刷新网络 -> 更新 Room -> 更新页面
```

这就是离线可用能力的基础。

## 第二部分：Room 的三个核心角色

Room 通常由三部分组成：

- Entity：描述数据库表。
- Dao：描述数据库操作。
- Database：数据库入口。

可以先这样理解：

```text
Entity 定义存什么
Dao 定义怎么读写
Database 提供访问入口
```

## 第三部分：Entity

Entity 用于定义一张表：

```kotlin
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val updatedAt: Long
)
```

这里的含义是：

- `lessons` 是表名。
- `id` 是主键。
- `title`、`description`、`updatedAt` 是字段。

Entity 应该更关注本地存储结构，不一定和网络 DTO 完全一样。

## 第四部分：Dao

Dao 用于定义数据库操作：

```kotlin
@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY id ASC")
    suspend fun getLessons(): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons")
    suspend fun clearLessons()
}
```

这里包含三个操作：

- 查询课程列表。
- 插入或替换课程列表。
- 清空课程表。

这些函数使用 `suspend`，说明数据库操作也应该放在协程中执行。

## 第五部分：Database

Database 是 Room 的入口：

```kotlin
@Database(
    entities = [LessonEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
}
```

真实项目中，数据库实例通常会做成单例，并通过依赖注入提供给 Repository。

第 4 章先理解基本结构即可，依赖注入会在后续章节展开。

## 第六部分：DTO、Entity 与 UI Model

一个常见数据流是：

```text
网络 DTO -> 本地 Entity -> UI Model -> Compose 页面
```

为什么不直接全程用一个类？

因为它们关注点不同：

- DTO 关注接口字段。
- Entity 关注本地表结构。
- UI Model 关注页面展示。

入门阶段可以先简化，但要知道大型项目中通常会做模型分层。

## 第七部分：缓存策略

最简单的缓存策略：

```text
1. 先从 Room 读取本地数据并展示。
2. 请求网络获取最新数据。
3. 将网络数据写入 Room。
4. 再从 Room 或内存状态更新页面。
```

这能带来两个好处：

- 没网时仍然有旧数据可看。
- 页面打开更快。

第 4 章示例工程可以先用假数据模拟这条链路，再逐步接入真实网络。

## 本节实践任务

### 基础任务

- 定义 `LessonEntity`。
- 定义 `LessonDao`。
- 定义 `AppDatabase`。
- 写出查询、插入、删除三个基础方法。

### Android 工程任务

- 在第 4 章示例工程中创建 `data/local` 包。
- 把课程假数据保存成 Entity。
- 添加“保存到本地”的按钮。
- 添加“读取本地缓存”的按钮。
- 在页面上显示读取结果。

### 进阶任务

- 给课程添加收藏字段 `isFavorite`。
- 实现收藏状态的本地保存。
- 思考收藏状态应该来自网络还是本地。
- 为数据库版本升级预留思路。

## 思考题

- 为什么 App 需要本地数据库？
- Room 和 SQLite 是什么关系？
- Entity 和 DTO 为什么不一定相同？
- 为什么数据库操作不能阻塞主线程？
- 什么数据适合存 Room，什么数据不适合？

## 常见问题

### Room 是不是一定要和网络一起用？

不是。Room 可以独立保存本地数据，例如收藏、草稿、历史记录。但在真实 App 中，Room 经常和网络缓存配合使用。

### Entity 可以直接给 UI 用吗？

小项目可以这样做，但大型项目建议转换成 UI Model，避免数据库结构直接影响页面。

### 数据库版本为什么重要？

当表结构变化时，需要升级数据库版本并处理迁移。否则用户已有数据可能丢失或导致崩溃。

## 本节小结

本节建立了 Room 的基础结构：Entity 定义表，Dao 定义读写，Database 提供入口。网络数据进入本地数据库后，App 就开始具备离线可用的基础。

下一节会学习 DataStore，用于保存轻量配置，例如是否首次打开、主题偏好、用户设置等。
