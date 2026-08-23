# 22.6 存储权限演进：从大权限到照片选择器与部分授权

前面几节我们已经看见：

```text
App 私有数据
  -> 直接使用 App-specific storage

共享媒体
  -> MediaStore / Photo Picker

用户文档
  -> SAF
```

这一节专门梳理权限演进。

不是为了背历史，而是为了理解：

```text
为什么老代码升级 targetSdk 后突然不能读写？
为什么一个“选择图片”功能不应该申请整个存储权限？
为什么用户授权了照片，App 仍然只能看到一部分？
```

## 本节定位

本节负责回答：

- `READ_EXTERNAL_STORAGE`、`WRITE_EXTERNAL_STORAGE` 为什么逐渐不再是主角？
- Android 13 的 `READ_MEDIA_*` 权限解决什么问题？
- Android 14 的部分照片授权给 App 带来什么变化？
- `MANAGE_EXTERNAL_STORAGE` 为什么不能当普通方案？
- 存储权限适配应该如何组织在工程里？

## 学习目标

学完本节后，你应该能够：

- 理解存储权限从“大而粗”走向“小而准”的演进方向。
- 能根据目标 Android 版本选择 Photo Picker、媒体权限或 SAF。
- 知道为什么 `MANAGE_EXTERNAL_STORAGE` 是高风险特殊能力。
- 能设计一套版本适配清晰的媒体访问策略。
- 能排查权限通过但读取结果不符合预期的问题。

## 第一部分：早期存储权限的问题

早期外部存储访问常见权限：

```text
READ_EXTERNAL_STORAGE
WRITE_EXTERNAL_STORAGE
```

它们的问题是访问范围太大。

一个只想选择头像的 App，可能会申请整个外部存储读取权限。

这在用户视角很难接受：

```text
我只是换个头像
  -> 为什么你要访问我的所有照片和文件？
```

从系统治理角度看，也不健康：

```text
权限语义太粗
用户难以理解
App 容易过度收集
隐私风险大
清理和归属不清晰
```

所以 Android 存储权限逐步被拆细。

## 第二部分：从“存储权限”转向“媒体权限”

Android 13 引入更细的媒体读取权限：

```text
READ_MEDIA_IMAGES
READ_MEDIA_VIDEO
READ_MEDIA_AUDIO
```

它背后的思路是：

```text
你要图片，就不要顺便拿音频。
你要视频，就不要顺便拿所有文件。
```

这对产品和工程都有影响。

产品要问：

```text
我们真的需要浏览用户整个相册吗？
还是只需要用户选择几张图片？
```

工程要问：

```text
这个功能应该走 Photo Picker？
还是需要媒体权限 + MediaStore 查询？
```

经验规则：

| 需求 | 推荐 |
| --- | --- |
| 选择头像 | Photo Picker |
| 选择作业图片 | Photo Picker |
| 批量备份相册 | 媒体权限 + MediaStore |
| 音乐播放器扫描本地音频 | READ_MEDIA_AUDIO + MediaStore |
| 视频剪辑 App 管理素材库 | READ_MEDIA_VIDEO / IMAGES + MediaStore |

## 第二部分补充：版本适配矩阵

工程里最容易出错的是：只按“有没有权限”判断，而没有把 Android 版本、`targetSdk` 和业务入口一起考虑。

可以先用这张矩阵建立直觉：

| 场景 | 低版本常见思路 | Android 10+ 主线 | Android 13+ 变化 | Android 14+ 变化 |
| --- | --- | --- | --- | --- |
| 保存 App 私有文件 | 直接写私有目录 | App-specific storage | 基本不变 | 基本不变 |
| 保存图片到相册 | 写外部路径 / 扫描 | MediaStore + scoped storage | 基本沿用 MediaStore | 基本沿用 MediaStore |
| 选择单张图片 | 存储权限 + 路径 | Photo Picker / SAF | 更推荐 Photo Picker | 继续推荐 Photo Picker |
| 读取相册集合 | READ_EXTERNAL_STORAGE | MediaStore 查询 | READ_MEDIA_IMAGES / VIDEO | 可能只有部分照片 |
| 读取音频集合 | READ_EXTERNAL_STORAGE | MediaStore 查询 | READ_MEDIA_AUDIO | 基本沿用音频权限 |
| 导入 PDF / zip | 外部路径 | SAF | SAF | SAF |
| 管理所有文件 | 大存储权限 | 特殊场景才考虑 | 高风险能力 | 高风险能力 |

这张表不是让你背版本，而是提醒你：

```text
同一个业务需求
  -> 在不同版本上可能需要不同入口

同一个权限结果
  -> 在不同版本上可能代表不同访问范围
```

因此工程里要避免散落判断：

```kotlin
if (Build.VERSION.SDK_INT >= 33) { ... }
```

更好的方式是把它收敛成策略：

```text
StorageAccessPolicy
  -> 当前功能是否可以用 Photo Picker？
  -> 当前版本需要哪些媒体权限？
  -> 当前授权是全部、部分，还是未授权？
  -> 如果不能访问，应该引导用户选择还是申请权限？
```

## 第三部分：部分照片授权：权限不再等于全部

Android 14 以后，用户可以只授权部分照片和视频给 App。

这会改变一个旧假设：

```text
以前：
拿到媒体权限 -> 可以查询完整媒体集合

现在：
拿到授权 -> 可能只能看到用户选择的一部分
```

App 必须能接受：

```text
查询结果变少
用户后续调整授权范围
已选图片可能不再可访问
相册列表不能默认代表用户全部媒体
```

这对 UI 也有影响。

不要写：

```text
已获得相册权限
```

更准确的是：

```text
已获得部分或全部照片访问权限
```

工程上也要准备：

```text
空列表
权限变化
Uri 失效
重新选择入口
用户撤销授权
```

部分授权会直接影响数据模型。

例如你的相册选择页不能再默认认为：

```text
数据库中保存的 mediaId
  -> 永远可以重新读取
```

更稳的设计是：

```text
媒体引用
  -> 保存 Uri / mediaId / displayName / lastKnownSize / lastCheckedAt
      -> 使用前重新校验可访问性
          -> 失败时展示“需要重新授权或重新选择”
```

如果 App 把用户选择的图片用于头像或课程封面，通常应该复制一份到 App 私有目录：

```text
用户选择头像
  -> 读取 Uri
      -> 压缩或裁剪
          -> 保存到 filesDir
              -> 后续头像展示不再依赖原始相册权限
```

这样即使用户撤销了照片访问，App 也不会突然失去自己的头像缓存。

但如果 App 是相册管理工具，就不能偷偷复制整库。它必须尊重用户授权范围。

## 第四部分：Photo Picker 为什么越来越重要

Photo Picker 是现代媒体选择的推荐入口之一。

它让很多功能不再需要申请媒体库权限：

```text
换头像
选择封面
上传图片
选择几张作业照片
选择视频素材
```

它的工程价值是：

```text
授权范围小
用户理解成本低
隐私压力小
适配复杂度低于完整相册权限
```

但它不是万能的。

不适合：

```text
后台扫描媒体库
完整相册管理
长期媒体库同步
复杂媒体资产管理
```

所以权限策略不是一句“都用 Photo Picker”，而是：

```text
用户主动选少量媒体
  -> Photo Picker

App 需要管理某类媒体集合
  -> 媒体权限 + MediaStore

用户选择任意文档
  -> SAF
```

## 第五部分：MANAGE_EXTERNAL_STORAGE 为什么危险

`MANAGE_EXTERNAL_STORAGE` 常被叫作“所有文件访问权限”。

它不是普通存储方案。

适合的通常是非常特殊的 App 类型：

```text
文件管理器
备份恢复工具
杀毒或安全工具
设备迁移工具
少数需要广泛文件管理能力的应用
```

普通业务 App 不应该把它当成解决 Scoped Storage 的捷径。

因为它的问题很明显：

```text
权限范围极大
审核风险高
用户信任成本高
隐私风险高
和现代 Android 存储方向相反
```

课程 App 通常不需要它。

如果一个需求非要它才能成立，应该先反问：

```text
这个需求是不是设计错了？
能不能改成 SAF 用户选择目录？
能不能改成 MediaStore？
能不能把数据放回 App 专属目录？
```

## 第六部分：存储权限适配不要散落在页面里

存储权限适配很容易写散：

```text
页面 A 判断 Android 13
页面 B 判断 Android 14
工具类 C 还在判断 READ_EXTERNAL_STORAGE
导入功能 D 又自己处理 SAF
```

最后会变成一锅版本分支汤。

更好的结构：

```text
StorageAccessPolicy
  -> canUsePhotoPicker()
  -> requiredMediaPermissions()
  -> shouldUseSafForDocument()
  -> explainPermissionToUser()

StorageGateway
  -> pickImage()
  -> saveImageToMediaStore()
  -> createDocument()
  -> openDocument()
  -> persistUriPermission()
```

业务层不要直接关心：

```text
Android 10
Android 13
Android 14
某个 permission 字符串
某个 Uri flag
```

业务层只表达：

```text
我要选头像
我要导出报告
我要保存海报
我要导入课程包
```

## 第七部分：权限事故排查

### 事故一：申请权限成功，但照片列表不完整

可能原因：

```text
用户只授权了部分照片
App 没处理部分授权状态
查询条件过滤过严
媒体类型权限不匹配
```

### 事故二：Android 13 上读不到图片

可能原因：

```text
仍在只申请 READ_EXTERNAL_STORAGE
没有申请 READ_MEDIA_IMAGES
实际应该使用 Photo Picker
用户拒绝或只授权部分媒体
```

### 事故三：为了导入 PDF 申请媒体权限

问题：

```text
PDF 不是图片、视频、音频。
媒体权限不适合任意文档。
```

更合理：

```text
SAF ACTION_OPEN_DOCUMENT
```

### 事故四：申请所有文件访问权限被拒

可能原因：

```text
业务类型不符合
权限范围过大
没有给出必要性
本来可以用 SAF / MediaStore 解决
```

## 第八部分：本节自测

请回答：

- 为什么选择头像不应该默认申请整个相册权限？
- Android 13 的 `READ_MEDIA_IMAGES` 和 `READ_MEDIA_VIDEO` 解决了什么问题？
- Android 14 部分照片授权会让查询结果发生什么变化？
- 什么场景可以考虑媒体权限，什么场景应该优先 Photo Picker？
- `MANAGE_EXTERNAL_STORAGE` 为什么不适合普通课程 App？
- 存储权限适配为什么应该集中到网关层？

## 本节小结

存储权限演进的方向很清晰：

```text
从大权限
  -> 到媒体类型权限
      -> 到用户选择
          -> 到部分授权
              -> 到最小必要访问
```

第 22.6 节的关键结论是：

```text
权限不是越大越省事，而是越准确越稳。
```
