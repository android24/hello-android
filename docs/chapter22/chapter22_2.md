# 22.2 Android 存储分区与 Scoped Storage：系统为什么不让 App 随便翻柜子

上一节我们建立了第 22 章的总地图：

```text
数据归属
  -> 授权范围
      -> 存储入口
          -> 系统索引
              -> 迁移、备份与排障
```

这一节开始进入第一块核心：Android 存储空间到底如何分区，Scoped Storage 又到底限制了什么。

## 本节定位

本节负责回答：

- 内部存储、外部存储、共享存储分别是什么？
- App-specific storage 和 shared storage 有什么区别？
- Scoped Storage 到底限制的是路径，还是数据边界？
- 为什么同一段读写代码在不同 Android 版本上表现不同？

## 学习目标

学完本节后，你应该能够：

- 说清楚 internal storage、external storage、shared storage 的关系。
- 区分 App 私有目录、App 外部专属目录、共享媒体目录和用户文档目录。
- 理解 Scoped Storage 的核心目标：让 App 只默认访问自己应该访问的范围。
- 能根据业务场景选择合适存储位置，而不是盲目申请大权限。

## 第一部分：先别被 internal / external 搞晕

Android 里的 internal 和 external 很容易误导人。

很多人以为：

```text
internal = 手机内置存储
external = SD 卡
```

这个理解只对了一点点。

在现代 Android 里，更实用的理解是：

| 名称 | 常见 API | 默认访问范围 | 适合数据 |
| --- | --- | --- | --- |
| App 内部私有目录 | `filesDir` / `cacheDir` | 只有本 App | 数据库、配置、私有文件、缓存 |
| App 外部专属目录 | `getExternalFilesDir()` / `externalCacheDir` | 本 App 直接访问 | 较大但仍属于 App 的文件 |
| 共享媒体集合 | `MediaStore` | 按媒体类型和权限访问 | 图片、视频、音频 |
| 用户文档空间 | SAF / DocumentsUI | 用户选择后访问 | PDF、zip、txt、任意文档 |
| 跨应用共享入口 | ContentProvider / FileProvider | 通过 URI 授权访问 | 给其他 App 临时或受控访问 |

更关键的不是它在物理磁盘哪里，而是：

```text
谁拥有它？
谁能访问它？
系统是否知道它？
卸载 App 后它会怎样？
```

再往下一层看，Android 存储不是只有一块“磁盘”。

可以粗略理解成：

```text
Linux 文件系统
  -> /data/user/0/<package>              App 内部私有数据
  -> /storage/emulated/0/Android/data    App 外部专属目录
  -> /storage/emulated/0/Pictures        用户共享媒体目录
  -> /storage/emulated/0/Download        用户下载目录

Android 访问层
  -> Context 提供 App 私有目录
  -> MediaProvider 管理媒体索引
  -> DocumentsProvider 暴露文档树
  -> ContentResolver 统一访问 content Uri

权限与策略层
  -> runtime permission
  -> AppOps
  -> Uri permission
  -> targetSdk 行为切换
```

这张图的意义是：你写的不是“一个文件路径”，而是在穿过几层系统规则。

例如，同样是读取一张图片：

```text
App 私有图片
  -> 直接读 filesDir 里的文件

相册图片
  -> 通过 MediaStore 查询或 Photo Picker 获取 Uri

用户从云盘选择的图片
  -> 通过 DocumentsProvider 返回的 Uri 读取
```

它们最后都可能变成一串 bytes，但入口、权限、生命周期完全不同。

## 第二部分：App-specific storage：自己的抽屉

App-specific storage 可以理解为 App 自己的抽屉。

常见入口：

```kotlin
context.filesDir
context.cacheDir
context.getExternalFilesDir(null)
context.externalCacheDir
```

它的特点：

```text
默认属于当前 App
通常不需要额外存储权限
卸载 App 后会被清理
适合保存 App 自己维护的数据
```

例如课程 App 可以把这些东西放在 App 私有空间：

```text
用户登录态
课程数据库
草稿
未提交学习记录
离线课程索引
临时解压文件
图片编辑中间产物
```

这里有一个重要原则：

```text
如果数据只是 App 自己运行所需，不要急着放进公共目录。
```

公共目录不是“更保险”，反而意味着更多权限、更多清理责任和更多隐私风险。

## 第三部分：Shared storage：公共展示柜

Shared storage 是用户和其他 App 可能共同看到的空间。

常见内容：

```text
图片
视频
音频
下载文件
文档
```

这类数据的特点是：

```text
它通常属于用户，而不是属于某个 App
卸载 App 后通常应该保留
系统或其他 App 可能需要索引和展示它
访问需要更明确的授权边界
```

例如：

```text
保存一张课程海报到相册
导出一份学习报告 PDF
让用户选择一个 zip 导入课程包
播放用户本地选择的视频
```

这些场景不要用“直接路径思维”处理。

应该先问：

```text
这是媒体吗？
  -> MediaStore / Photo Picker

这是用户文档吗？
  -> SAF

这是给其他 App 分享吗？
  -> ContentProvider / FileProvider
```

## 第四部分：Scoped Storage 限制的不是能力，而是越界

Scoped Storage 常常被误解成：

```text
Android 不让 App 读写文件了。
```

更准确的说法是：

```text
Android 不再鼓励 App 用一个大权限随便访问共享存储里的所有文件。
```

Scoped Storage 做了几件事：

```text
App 默认可以访问自己的专属目录
访问共享媒体要走 MediaStore 和媒体权限
访问用户文档要让用户通过 SAF 选择
跨应用共享要通过 URI 授权
旧式外部存储路径访问被逐步收紧
```

这和第 21 章的后台限制很像。

系统不是说：

```text
你不能做后台任务。
```

而是在说：

```text
你要说明任务为什么需要后台执行。
```

第 22 章也是一样。

系统不是说：

```text
你不能读写文件。
```

而是在说：

```text
你要说明这份数据为什么属于你，或者用户是否授权你访问。
```

## 第四部分补充：Scoped Storage 真正改变了哪几件事

Scoped Storage 不是单点变化，而是把旧式外部存储访问拆成了几条更明确的通道。

### 1. 从 File path 转向 content Uri

旧模式喜欢这样思考：

```text
/sdcard/DCIM/a.jpg
  -> File
      -> InputStream
```

新模式更强调：

```text
content://media/...
content://com.android.providers.downloads.documents/...
  -> ContentResolver
      -> InputStream / OutputStream
```

路径回答的是“它在哪里”。

Uri 回答的是：

```text
谁提供它？
谁授权访问？
访问语义是什么？
能读还是能写？
授权是否能持久化？
```

### 2. 从全局扫描转向按集合访问

旧代码经常会递归扫描目录：

```text
扫描 /sdcard/
  -> 找所有 jpg
      -> 自己维护列表
```

现代 Android 更希望你表达：

```text
我要读取图片集合
  -> MediaStore.Images

我要读取视频集合
  -> MediaStore.Video

我要读取用户选中的文档
  -> SAF Uri
```

这不是为了让代码更绕，而是为了让系统知道：

```text
你访问的是哪类用户数据
你是否具备对应授权
这个访问是否符合用户预期
```

### 3. 从“我能访问”转向“我被授权访问”

Scoped Storage 把存储访问变成授权问题。

```text
App 私有目录
  -> 包名和 UID 形成默认授权

MediaStore
  -> 媒体类型权限 / 用户选择范围形成授权

SAF
  -> 用户在系统选择器中的选择形成授权

FileProvider
  -> 分享方临时授予接收方授权
```

这也是为什么存储排查不能只看 `File.exists()`。

真正要看的是：

```text
我有没有访问这份数据的授权？
这个授权来自权限、Uri，还是 App 私有目录？
授权是否还有效？
```

## 第五部分：常见目录选择错误

### 错误一：把用户导出的文件放进私有目录

现象：

```text
用户导出学习报告。
文件生成成功。
用户用文件管理器找不到。
卸载 App 后文件没了。
```

原因：

```text
导出文件属于用户，不应该默认只放在 App 私有目录。
```

更合理的做法：

```text
用 SAF 让用户选择保存位置
或用 MediaStore Downloads 写入下载集合
```

### 错误二：把 App 缓存放进公共 Download

现象：

```text
用户文件管理器里出现一堆奇怪缓存文件。
卸载 App 后垃圾文件还在。
```

原因：

```text
缓存属于 App，不属于用户文档。
```

更合理的做法：

```text
cacheDir / externalCacheDir
```

### 错误三：保存图片后相册看不到

现象：

```text
图片文件确实存在。
系统相册里没有显示。
```

原因可能是：

```text
没有通过 MediaStore 插入
没有正确设置 MIME_TYPE / RELATIVE_PATH
写入未完成时没有更新 IS_PENDING
扫描或索引状态没有完成
```

更合理的做法：

```text
通过 MediaStore 写入共享媒体集合
写入前设置 IS_PENDING=1
写入完成后设置 IS_PENDING=0
```

## 第六部分：版本差异为什么让人头疼

Android 存储策略经历过多轮变化。

你不需要死记每一个细节，但要知道方向：

```text
早期：大权限 + 路径访问
Android 10：Scoped Storage 开始成为主线
Android 11：共享存储访问进一步收紧
Android 13：媒体权限按图片、视频、音频拆分
Android 14：用户可以只授权部分照片和视频
```

这意味着存储代码经常需要按版本分支。

但版本分支不应该散落在业务代码里。

更好的方式是建立一个存储网关：

```text
CourseStorageGateway
  -> savePrivateDraft()
  -> savePosterToMediaStore()
  -> pickUserDocument()
  -> exportReport()
  -> persistUriPermission()
```

业务层只表达意图：

```text
保存草稿
导出报告
选择头像
保存海报
```

存储层负责处理版本、权限、URI 和异常。

## 第七部分：本节自测

请回答：

- `filesDir` 和 `getExternalFilesDir()` 的共同点是什么？
- 为什么导出的用户报告不应该只放在 `filesDir`？
- Scoped Storage 主要解决什么问题？
- 保存到相册为什么推荐使用 MediaStore？
- 用户选择一个 PDF 导入时，为什么 SAF 比直接路径更合理？
- 卸载 App 后哪些数据应该消失，哪些数据应该保留？

## 本节小结

第 22.2 节的核心不是目录名字，而是边界：

```text
App 私有
  -> App 自己负责

共享媒体
  -> MediaStore 管理

用户文档
  -> SAF 授权

跨应用访问
  -> URI 权限控制
```

当你能先判断数据边界，再决定存储 API，Scoped Storage 就不再像一堵墙，而更像一套清晰的资料室规则。
