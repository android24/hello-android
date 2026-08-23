# 22.4 MediaStore：图片、视频、音频为什么要交给系统索引

上一节我们讨论了 App 自己的数据。

这一节开始进入共享媒体：

```text
图片
视频
音频
```

这些数据通常不是 App 的私有财产，而是用户的媒体资产。

所以现代 Android 不鼓励 App 直接拿路径到处扫，而是通过 MediaStore、Photo Picker 和媒体权限来管理访问边界。

## 本节定位

本节负责回答：

- MediaStore 是什么？
- 为什么保存图片到相册不能只写一个文件路径？
- `IS_PENDING`、`RELATIVE_PATH`、`MIME_TYPE` 分别有什么作用？
- 读取媒体为什么要区分“选择器授权”和“媒体权限”？
- Android 13、Android 14 后媒体访问发生了什么变化？

## 学习目标

学完本节后，你应该能够：

- 理解 MediaStore 是共享媒体的系统索引表。
- 能描述一张图片从 App 写入到相册可见的大致过程。
- 知道保存媒体时为什么要处理 pending 状态。
- 区分 Photo Picker、READ_MEDIA_IMAGES、READ_MEDIA_VIDEO、READ_MEDIA_AUDIO 和部分照片授权。
- 能排查“图片保存成功但相册不可见”的常见原因。

## 第一部分：MediaStore 不是目录，而是索引

很多人第一次使用 MediaStore，会把它理解成：

```text
一个新的文件夹 API。
```

更准确地说：

```text
MediaStore 是系统维护的媒体数据库入口。
```

它记录的不只是文件在哪里，还包括：

```text
display name
mime type
relative path
size
date added
media type
uri
pending 状态
```

当你把图片写进共享媒体空间时，系统需要知道：

```text
这是不是图片？
应该放在哪个相册分类下？
写入是否完成？
其他 App 现在能不能看？
相册应用如何展示？
```

所以保存媒体不是：

```text
open file path -> write bytes -> done
```

而更像：

```text
向 MediaStore 插入一条媒体记录
  -> 拿到 content Uri
      -> 通过 ContentResolver 打开输出流
          -> 写入真实内容
              -> 标记写入完成
                  -> 系统和相册应用可以索引展示
```

再往 Framework 视角看，这条链路大致是：

```text
App
  -> ContentResolver.insert()
      -> Binder 调用到 MediaProvider
          -> MediaProvider 写入媒体数据库记录
              -> 返回 content Uri
                  -> App 通过 ContentResolver.openOutputStream()
                      -> 写入底层文件
                          -> MediaProvider 更新元数据
                              -> 其他 App 通过 MediaStore 查询到它
```

所以 MediaStore 不是“帮你拼路径”的工具，而是共享媒体的系统契约：

```text
数据库记录
  + 文件内容
  + MIME 类型
  + 相对位置
  + 写入完成状态
  + 权限边界
```

缺了其中任何一环，用户看到的结果都可能不符合预期。

## 第二部分：保存图片到相册的核心流程

典型流程：

```text
准备 ContentValues
  -> DISPLAY_NAME
  -> MIME_TYPE
  -> RELATIVE_PATH
  -> IS_PENDING = 1

ContentResolver.insert()
  -> 得到 content://media/...

openOutputStream(uri)
  -> 写入图片 bytes

更新 IS_PENDING = 0
  -> 表示写入完成
```

为什么需要 `IS_PENDING`？

因为写入媒体不是瞬间完成的。

如果没有 pending 语义，其他 App 可能在你只写了一半时就读到这个文件。

`IS_PENDING` 像资料室里的“正在装订”牌子：

```text
IS_PENDING = 1
  -> 这份文件还没写完，先别拿走

IS_PENDING = 0
  -> 写入完成，可以被展示和访问
```

一个更完整的写入状态机应该是：

```text
CREATE_ROW
  -> MediaStore 插入记录，IS_PENDING=1

OPEN_STREAM
  -> 打开输出流

WRITE_BYTES
  -> 分块写入图片内容

VERIFY
  -> 确认输出流关闭、大小合理、无异常

PUBLISH
  -> IS_PENDING=0，让媒体对其他应用可见

ROLLBACK
  -> 任一步失败时删除半成品 Uri 或保持不可见并记录错误
```

这套状态机比“保存图片”四个字重要得多。

因为线上最常见的问题不是完全写不进去，而是：

```text
插入记录成功
写入内容失败
页面提示成功
相册看不到
下次查询出现半成品
```

所以保存媒体时要把失败分支当作一等公民。

## 第三部分：RELATIVE_PATH 解决什么问题

`RELATIVE_PATH` 用来告诉系统：

```text
这个媒体文件应该出现在共享媒体空间的哪个相对目录下。
```

例如：

```text
Pictures/HelloAndroid/
Movies/HelloAndroid/
Music/HelloAndroid/
Download/HelloAndroid/
```

它不是让你回到绝对路径时代。

它表达的是：

```text
我希望这个用户媒体出现在某个用户可理解的位置。
```

常见错误：

```text
只设置 DISPLAY_NAME，不设置合理 MIME_TYPE
把图片写到 Download 里，却希望相册一定展示
写入失败后没有删除半成品记录
写完后忘记把 IS_PENDING 改回 0
```

## 第四部分：读取媒体：不要一上来申请全相册

读取媒体有两条常见路线：

```text
用户选一次
  -> Photo Picker / SAF

App 需要批量管理某类媒体
  -> 媒体权限 + MediaStore 查询
```

如果只是选择头像、选择封面、选择一张图片上传，优先考虑 Photo Picker。

它的语义是：

```text
用户主动选择某些媒体给你。
```

如果 App 是相册、剪辑、音频管理、媒体备份类工具，才更可能需要媒体读取权限。

从 Android 13 开始，媒体权限拆得更细：

```text
READ_MEDIA_IMAGES
READ_MEDIA_VIDEO
READ_MEDIA_AUDIO
```

从 Android 14 开始，用户还可以选择只授权部分照片和视频。

这意味着：

```text
拿到权限
  -> 不一定等于拿到整个相册

用户选择部分访问
  -> App 要能处理媒体集合变化
```

这里有一个非常重要的产品判断：

```text
读取媒体
  != 管理媒体库
```

选择头像是读取一次用户选择的图片。

相册备份是持续管理一类媒体集合。

两者在 UI、权限、隐私承诺和错误处理上都不同：

| 场景 | 推荐入口 | 用户心理预期 | 工程责任 |
| --- | --- | --- | --- |
| 换头像 | Photo Picker | 我选哪张，你用哪张 | 处理单个 Uri |
| 上传作业图片 | Photo Picker | 我选几张，你上传几张 | 复制或上传选中内容 |
| 图片管理 App | 媒体权限 + MediaStore | 你会浏览我的媒体库 | 处理权限、部分授权、增删变化 |
| 相册备份 | 媒体权限 + MediaStore | 你会持续访问媒体 | 处理后台限制、配额、隐私说明 |

不要让一个“头像功能”背上“相册管理”的权限成本。

## 第五部分：Photo Picker 的工程意义

Photo Picker 很适合这些场景：

```text
选择头像
选择课程封面
上传作业图片
选择一段视频作为素材
```

它的优点：

```text
用户主动选择
通常不需要申请整个媒体库权限
授权范围清晰
隐私压力更小
```

它的限制：

```text
不适合完整相册管理
不适合长期后台扫描媒体库
不等于拿到真实文件路径
应该通过 Uri 和 ContentResolver 读取
```

这会把很多旧代码逼出来：

```text
Uri -> path
path -> File
File -> decode
```

现代写法应该更接近：

```text
Uri
  -> ContentResolver.openInputStream(uri)
      -> decode / copy / upload
```

不要执着于“真实路径”。

在内容访问模型里，URI 才是钥匙。

## 第六部分：媒体保存事故怎么排查

### 事故一：保存成功但相册看不到

排查顺序：

```text
是否通过 MediaStore 插入？
MIME_TYPE 是否正确？
RELATIVE_PATH 是否合理？
IS_PENDING 是否从 1 更新到 0？
写入输出流是否成功关闭？
相册 App 是否刷新索引？
```

### 事故二：Android 13 读取图片失败

排查顺序：

```text
是否还在申请 READ_EXTERNAL_STORAGE？
是否应该申请 READ_MEDIA_IMAGES？
是否其实只需要 Photo Picker？
用户是否拒绝权限？
是否处理了空结果或部分授权？
```

### 事故三：拿 Uri 转路径后崩溃

排查顺序：

```text
这个 Uri 是否来自 Photo Picker / SAF？
是否一定有真实文件路径？
是否应该用 ContentResolver 读取？
是否错误假设了本地文件系统路径？
```

## 第七部分：本节自测

请回答：

- MediaStore 为什么不是简单文件夹 API？
- 保存图片时为什么要使用 `IS_PENDING`？
- `RELATIVE_PATH` 和绝对路径访问有什么区别？
- 选择头像为什么优先 Photo Picker，而不是申请整个相册权限？
- Android 13 的媒体权限为什么拆成图片、视频、音频？
- Android 14 的部分照片授权会给 App 带来什么适配要求？

## 本节小结

MediaStore 的核心不是“保存图片的 API”，而是共享媒体的系统账本。

```text
App 写入媒体
  -> 系统登记
      -> 写入内容
          -> 标记完成
              -> 相册、媒体应用和系统索引可以理解它
```

第 22.4 节的关键结论是：

```text
属于用户的媒体，要用用户和系统都能理解的方式保存与访问。
```
