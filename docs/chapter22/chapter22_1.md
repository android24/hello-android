# 22.1 为什么要学习存储系统、Scoped Storage 与数据访问机制

第 21 章我们看见了后台任务的边界：

```text
App 想在后台做事
  -> 系统会先问：用户是否感知、是否耗电、是否合规
```

第 22 章继续追问另一个真实工程里同样高频的问题：

```text
App 想读写一个文件
  -> 这个文件属于谁？
      -> 用户是否授权？
          -> 系统是否需要索引？
              -> 其他 App 是否应该看见？
                  -> 卸载 App 后它是否应该保留？
```

很多初学者学习 Android 存储时，会把问题简化成一句话：

```text
文件应该放到哪个路径？
```

但在现代 Android 里，这个问题已经不够用了。

真正要问的是：

```text
这份数据是 App 私有数据、缓存、用户媒体、用户文档，还是跨应用共享内容？
```

路径只是结果。数据归属、用户授权、系统索引、生命周期和隐私边界，才是存储设计的原因。

## 本章通关画面

学完第 22 章后，你应该能画出这张存储地图：

```text
业务想读写数据
  -> 判断数据归属
      -> App 私有数据 / App 缓存 / 共享媒体 / 用户文档 / 跨应用数据
          -> 选择存储入口
              -> filesDir / cacheDir / externalFilesDir
              -> MediaStore
              -> Photo Picker
              -> Storage Access Framework
              -> ContentProvider
                  -> 判断权限与 URI 授权
                      -> 设计迁移、备份、清理、导入导出和排障方案
```

如果第 21 章像后台调度台，第 22 章就是资料室。

资料室里不是所有柜子都能随便打开：

```text
自己的抽屉
  -> 你可以直接放

公共相册
  -> 要遵守系统索引和媒体权限

用户文件柜
  -> 要让用户自己选门

别人的资料夹
  -> 需要对方通过 ContentProvider 给你钥匙
```

这个比喻有点朴素，但它很接近 Android 存储设计的核心：不要把用户的数据当成 App 的私产。

## 本章要解决的真实问题

真实项目里，存储问题通常长这样：

```text
为什么 Android 10 以后写不了外部存储？
为什么相册里看不到刚保存的图片？
为什么拿到的文件路径在另一个手机上崩了？
为什么选择文件后下次打开又没有权限？
为什么用户卸载 App 后下载内容没了？
为什么备份恢复后数据库和文件对不上？
为什么只想选一张图片，却要申请整个相册权限？
为什么清缓存时把用户重要文件删了？
```

这些问题看起来都叫“读写文件”，但答案可能完全不同。

```text
App 自己用的数据
  -> App-specific storage

可以丢的临时数据
  -> Cache

用户照片、视频、音频
  -> MediaStore / Photo Picker

用户主动选择的文档
  -> Storage Access Framework

跨应用共享的数据
  -> ContentProvider + URI permission

结构化业务数据
  -> Room / DataStore / 文件持久化组合
```

## 本章核心判断：五问法

面对任何存储需求，先问五句话：

```text
第一问：这份数据属于 App，还是属于用户？
第二问：卸载 App 后是否应该保留？
第三问：其他 App 是否应该看见？
第四问：系统是否需要把它索引为图片、视频、音频或下载文件？
第五问：用户是否需要亲自选择访问范围？
```

例如：

| 业务场景 | 数据归属 | 卸载后保留 | 推荐入口 |
| --- | --- | --- | --- |
| 登录态、课程配置 | App | 否 | DataStore / filesDir |
| 课程数据库 | App | 否或按备份策略 | Room / filesDir |
| 图片编辑草稿 | App | 通常否 | filesDir / externalFilesDir |
| 导出的学习报告 PDF | 用户 | 是 | SAF 创建文档 / MediaStore Downloads |
| 保存到相册的海报 | 用户媒体 | 是 | MediaStore Images |
| 选择头像图片 | 用户媒体 | 不复制也可 | Photo Picker / SAF |
| 临时压缩包 | 临时数据 | 否 | cacheDir |

存储设计的第一步，不是拼路径，而是判断数据归属。

## 本章核心模型：三层存储判断

第 22 章后面会反复使用一个三层模型：

```text
第一层：业务层
  -> 这份数据对用户意味着什么？

第二层：系统层
  -> Android 用哪个入口管理这份数据？

第三层：证据层
  -> 出问题时能从哪里证明它的状态？
```

举个例子。

“保存课程海报”在业务层看起来只是：

```text
生成一张图片
保存起来
```

但系统层会继续追问：

```text
这是用户媒体吗？
是否应该被相册看到？
是否要进入 MediaStore 索引？
MIME_TYPE 是否正确？
写入是否完成？
其他 App 是否可以读取？
```

到了证据层，排查时不能只看：

```text
bitmap 是否生成成功
```

还要看：

```text
MediaStore 是否插入记录
content Uri 是什么
IS_PENDING 是否归零
RELATIVE_PATH 是否合理
输出流是否关闭
相册查询是否能查到
```

这就是第 22 章要从“会写代码”推进到“会解释系统行为”的地方。

本章所有场景都可以套进这个模型：

| 业务问题 | 系统入口 | 第一证据 |
| --- | --- | --- |
| 保存草稿 | filesDir / Room | 文件、数据库索引、清理策略 |
| 清缓存 | cacheDir | 缓存大小、清理日志、核心数据是否受影响 |
| 保存海报 | MediaStore | content Uri、MIME_TYPE、IS_PENDING |
| 选择头像 | Photo Picker | 返回 Uri、读取结果、授权范围 |
| 导入课程包 | SAF | Uri、复制日志、校验结果 |
| 导出报告 | SAF / Downloads | 用户选择位置、输出流结果 |
| 分享文件 | FileProvider | 授权 Uri、目标 App 读取结果 |
| 旧数据迁移 | 迁移器 | 迁移状态、失败清单、回滚点 |

## 本章探索任务

```text
理解 Android 存储为什么收紧
  -> 区分 App 私有目录、缓存目录、外部 App 专属目录和共享媒体
      -> 掌握 Scoped Storage 的核心边界
          -> 理解 MediaStore 如何让媒体进入系统索引
              -> 理解 SAF 如何让用户主动授权文档
                  -> 理解 URI 权限、ContentResolver 和 ContentProvider
                      -> 学会排查图片不可见、文件丢失、权限失效和迁移失败
                          -> 做一个存储访问观察实验室
```

## 官方参考入口

第 22 章涉及的系统行为会随 Android 版本和 `targetSdk` 变化。建议学习时同时参考 Android 官方文档：

- [Data and file storage overview](https://developer.android.com/training/data-storage)
- [Access media files from shared storage](https://developer.android.com/training/data-storage/shared/media)
- [Access documents and other files from shared storage](https://developer.android.com/training/data-storage/shared/documents-files)
- [Grant partial access to photos and videos](https://developer.android.com/about/versions/14/changes/partial-photo-video-access)

## 本节定位

本节是第 22 章入口。

它负责回答：

- 第 22 章为什么不能只学文件 API？
- Android 为什么从“路径访问”转向“范围访问”？
- Scoped Storage、MediaStore、SAF 和 ContentProvider 分别解决什么问题？
- 本章应该按照什么顺序学习？

## 学习目标

学完本节后，你应该能够：

- 知道现代 Android 存储的核心不是路径，而是数据归属和授权边界。
- 区分 App 私有数据、缓存、共享媒体、用户文档和跨应用共享内容。
- 初步知道 filesDir、cacheDir、MediaStore、Photo Picker、SAF、ContentProvider 的适用场景。
- 明白 Scoped Storage 不是“系统刁难”，而是隐私、整理、索引和跨应用边界的治理。
- 建立一个原则：先判断数据归属，再选择存储入口。

## 第一部分：为什么“随便读写 SD 卡”的时代结束了

早期 Android 里，很多 App 会这样写文件：

```text
/sdcard/xxx/
/sdcard/Download/
/sdcard/DCIM/
Environment.getExternalStorageDirectory()
```

这种方式简单、直接、好理解。

问题也很明显：

```text
一个 App 可以扫很多不属于自己的文件
一个 App 卸载后可能留下大量垃圾目录
用户很难知道哪个 App 创建了哪些文件
图片、视频、下载文件可能绕开系统索引
权限一旦授予，访问范围过大
不同厂商、不同 Android 版本表现不一致
```

从工程角度看，这不是自由，而是失控。

于是 Android 开始把存储拆成更明确的边界：

```text
App 自己的数据
  -> 直接管理

共享媒体
  -> 通过 MediaStore 管理

用户选中的文档
  -> 通过 SAF 管理

跨应用共享
  -> 通过 ContentProvider 和 URI 授权
```

这就是 Scoped Storage 背后的核心思想。

## 第二部分：第 22 章和前面章节的关系

第 4 章已经讲过数据、网络与本地缓存。

那时重点是：

```text
App 如何把数据存起来？
```

第 16 章讲过 PMS、权限、包管理和组件注册。

它回答的是：

```text
系统如何知道 App 声明了什么能力？
```

第 17 章讲过资源系统。

它回答的是：

```text
App 内置资源如何被编译、查找和加载？
```

第 22 章要把问题换成系统视角：

```text
运行时产生的数据，系统如何决定谁能读、谁能写、谁能展示、谁能长期保留？
```

这也是从“应用开发”走向“系统工程”的关键一步。

## 第三部分：本章学习顺序

建议按下面顺序推进：

```text
22.1 先建立存储地图
22.2 看懂 Android 存储分区与 Scoped Storage
22.3 掌握 App 私有目录、缓存、Room、DataStore 与备份
22.4 掌握 MediaStore 与共享媒体写入读取
22.5 掌握 SAF、文档选择、目录授权与 URI 权限
22.6 梳理存储权限演进与版本适配
22.7 训练存储事故排查：文件丢失、权限失效、媒体不可见
22.8 做综合实践：存储访问观察实验室
```

不要急着背 API。

先把这张表记住：

| 你要做什么 | 优先考虑 |
| --- | --- |
| 保存 App 自己的数据 | App-specific storage |
| 保存临时文件 | Cache |
| 保存图片到相册 | MediaStore |
| 让用户选一张照片 | Photo Picker |
| 让用户选择任意文档 | SAF |
| 长期访问用户选择的目录 | SAF tree URI + 持久授权 |
| 给其他 App 暴露数据 | ContentProvider / FileProvider |
| 管理结构化业务数据 | Room / DataStore |

## 本节小结

第 22 章要训练的不是“会写文件”，而是“知道文件在系统里的身份”。

```text
路径
  -> 只是入口

归属
  -> 决定生命周期

授权
  -> 决定谁能访问

索引
  -> 决定系统和其他 App 能否看见

迁移与备份
  -> 决定数据能否长期可靠
```

当你能先问“这份数据属于谁”，再决定用哪个 API，第 22 章就已经打开了。
