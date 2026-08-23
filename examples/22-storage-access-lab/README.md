# 第22章示例工程：存储访问观察实验室

这个工程对应课程第 22 章：存储系统、Scoped Storage、MediaStore 与数据访问机制。

它不是普通文件读写 demo，而是一块存储访问观察台。你可以判断数据归属，写入 App 私有草稿，清理缓存，保存课程海报到 MediaStore，用 Photo Picker 选择图片，用 SAF 导出报告，观察持久 URI 授权，用 FileProvider 分享私有文件，触发反例实验，并把一次“文件为什么找不到”的事故写成诊断报告。

## 学习目标

运行本工程后，你应该能回答：

- 为什么存储设计的第一步不是选路径，而是判断数据归属？
- `filesDir` 和 `cacheDir` 为什么不能混用？
- 为什么保存到相册要走 MediaStore？
- `content://` Uri 为什么比真实路径更重要？
- Photo Picker 为什么不等于拿到整本相册权限？
- SAF 导出为什么更适合用户文档？
- 持久 URI 授权为什么不等于拥有真实文件？
- FileProvider 为什么能替代 `file://` 跨应用分享？
- 错误存储设计会留下哪些坏信号？
- 一个存储事故如何从现象走向证据、权限、索引、根因、修复和回归？

## 工程结构

```text
22-storage-access-lab/
  app/
    src/main/AndroidManifest.xml
    src/main/res/xml/file_paths.xml
    src/main/java/com/helloandroid/storage/
      MainActivity.kt
      StorageAccessApplication.kt
      StorageAccessLabScreen.kt
      StorageLabState.kt
      StorageLabStore.kt
      StorageUtils.kt
```

## 实验区域

### 资料室任务板

页面顶部提供任务板，把本章拆成 13 个观察点：

```text
确认运行现场
完成数据归属判断
写入私有草稿
清理可再生缓存
保存共享媒体
阅读媒体证据
选择用户图片
导出用户报告
观察持久 URI
分享私有文件
完成反例实验
完成事故剧本
完成诊断报告
```

不要一上来乱点按钮。先看任务板，再做第一个未完成的观察点。

### 数据归属决策卡

使用第 22 章的五问法：

```text
这份数据属于 App，还是属于用户？
卸载 App 后是否应该保留？
其他 App 是否应该看见？
系统是否需要索引？
用户是否需要亲自选择访问范围？
```

选择不同场景后，页面会给出推荐入口：

```text
学习草稿 -> filesDir / Room
缩略图缓存 -> cacheDir
保存海报 -> MediaStore Images
选择头像 -> Photo Picker
导出报告 -> SAF ACTION_CREATE_DOCUMENT
分享报告 -> FileProvider
```

### App 私有草稿实验区

点击“写入草稿”后，工程会在 `filesDir` 写入 `course-draft.txt`。

观察点：

```text
filesDir
文件大小
内容预览
清缓存后是否仍存在
```

它用来说明：草稿属于 App 私有核心数据，不能被当成缓存清理。

### 缓存清理实验区

点击“写入缓存”后，工程会在 `cacheDir` 写入可再生缓存。

点击“安全清缓存”后，工程只清理 `cacheDir`，并检查草稿是否仍然存在。

它用来说明：

```text
缓存可以被清理
  -> 但核心数据不能混进缓存
```

### MediaStore 海报实验区

点击“保存课程海报”后，工程会生成一张简单课程海报，并写入 MediaStore。

观察点：

```text
content Uri
DISPLAY_NAME
MIME_TYPE
RELATIVE_PATH
IS_PENDING
```

这用来说明：共享图片不是只写一个文件路径，而是要进入系统媒体索引。

### Photo Picker 实验区

点击“选择头像图片”后，系统会打开 Photo Picker。

工程会展示返回的 Uri，并尝试用 `ContentResolver` 读取前几个字节。

它用来说明：

```text
用户选择的图片
  -> 返回 Uri
      -> 通过 ContentResolver 读取
          -> 不要强行转换真实路径
```

### SAF 导出报告实验区

点击“导出学习报告”后，系统会打开文档创建界面。

用户选择保存位置后，工程会把当前诊断报告写入目标 Uri。

它用来说明：导出文件是把用户数据交还给用户，应该让用户选择位置。

### 持久 URI 授权观察区

点击“选择文档并持久授权”后，系统会打开文档选择界面。

工程会尝试调用 `takePersistableUriPermission`，并展示当前 `persistedUriPermissions`。

它用来说明：

```text
保存 Uri 字符串
  -> 不等于拥有文件
      -> 长期访问要看系统是否记录了持久授权
```

如果只是一次性导入，通常更适合复制到 App 私有目录；如果用户希望 App 长期访问外部文档，才需要认真管理持久授权。

### FileProvider 分享实验区

点击“创建私有报告”后，工程会在 `filesDir` 生成一份报告。

点击“分享报告”后，工程会通过 FileProvider 把私有文件映射成 `content://` Uri，并发起系统分享面板。

它用来说明：

```text
App 私有文件
  -> FileProvider content Uri
      -> Intent 临时读授权
          -> 外部 App 可在授权范围内读取
```

### 反例实验区

反例实验区提供 4 个安全反例：

```text
私有导出反例
MediaStore 半成品
file:// 分享反例
缓存草稿反例
```

它们不会要求你真的制造线上事故，而是把错误设计的坏信号展示出来：

```text
用户文档藏进 filesDir
MediaStore 半成品没有发布或回滚
私有文件用 file:// 暴露
草稿误放进 cacheDir
```

### 存储事故剧本

内置 5 个事故剧本：

```text
剧本 A：导出报告找不到
剧本 B：海报相册不可见
剧本 C：头像下次打不开
剧本 D：清缓存丢草稿
剧本 E：分享报告失败
```

剧本模式会给出现象、第一线索、隐藏陷阱和下一步动作。它训练的是“从用户反馈开始判断数据归属和系统边界”的能力。

### 系统证据命令卡

页面会列出推荐命令：

```bash
adb shell run-as com.helloandroid.storage ls files
adb shell run-as com.helloandroid.storage ls cache
adb shell content query --uri content://media/external/images/media
adb shell dumpsys package com.helloandroid.storage
adb shell cmd appops get com.helloandroid.storage
```

这些命令需要你在终端中手动执行。Demo 不替你自动读系统状态，因为第 22 章真正要训练的是：你知道第一证据在哪里。

## 推荐学习路线

1. 先刷新运行现场，记录 `package / targetSdk / sdk / filesDir / cacheDir`。
2. 选择一个事故剧本，只读现象和第一线索，不急着看答案。
3. 在数据归属决策卡里选择对应场景，判断它应该用哪个存储入口。
4. 写入私有草稿，再写入缓存，执行安全清缓存，确认草稿仍存在。
5. 保存课程海报到 MediaStore，记录 Uri、MIME_TYPE、RELATIVE_PATH 和 IS_PENDING。
6. 用 Photo Picker 选择一张图片，观察返回 Uri 和读取结果。
7. 用 SAF 导出学习报告，确认文件出现在用户选择的位置。
8. 选择一个文档并观察持久 URI 授权列表。
9. 创建私有报告并用 FileProvider 分享，观察 `content://` Uri。
10. 触发一个反例实验，解释它为什么会失败。
11. 阅读系统证据命令卡，在终端执行至少一个命令。
12. 最后完成存储诊断报告。

## 通关标准

你不需要把所有按钮都点一遍，但至少应该能做到：

- 能从业务语义判断应该用 filesDir、cacheDir、MediaStore、Photo Picker、SAF 还是 FileProvider。
- 能解释为什么缓存清理不应该影响草稿。
- 能解释为什么 MediaStore 保存图片要观察 `IS_PENDING`。
- 能解释为什么 Photo Picker / SAF 返回 Uri，而不是稳定文件路径。
- 能解释持久 URI 授权和一次性选择的差异。
- 能解释为什么跨应用分享私有文件要用 FileProvider。
- 能根据反例说出错误设计的第一坏信号。
- 能把一次存储问题写成“现象、数据归属、存储入口、权限、索引、根因、修复、回归”。

## 小挑战

请分析下面事故：

```text
用户点击“导出学习报告”后，页面提示成功。
用户在文件管理器里找不到。
开发同学说文件已经写入 /data/user/0/.../files/report.txt。
产品希望用户可以分享到微信。
```

你需要回答：

- 这份报告属于 App 私有数据，还是用户文档？
- 为什么写进 filesDir 后用户找不到？
- 如果用户要保存到自己选择的位置，应该用什么？
- 如果只是从 App 内分享给微信，应该用什么？
- 为什么不应该直接暴露 file:// 路径？
- 需要在诊断报告里记录哪些证据？
