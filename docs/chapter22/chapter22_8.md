# 22.8 综合实践：存储访问观察实验室

第 22 章最后一节，我们把 App 私有存储、缓存、MediaStore、Photo Picker、SAF、URI 权限、清理、迁移和存储事故排查放进一个综合实践。

目标是：让你不只是会调用读写 API，而是能从一个业务需求出发，判断数据属于谁、应该放在哪里、谁能访问、系统是否索引、出问题时第一证据在哪里。

## 本节剧情钩子

现在你已经知道：

```text
App 私有数据适合放进 App-specific storage
缓存必须可以再生
共享媒体要通过 MediaStore 或 Photo Picker 处理
用户文档要通过 SAF 选择和授权
URI 权限不是文件路径
存储权限正在从大权限走向精确授权
```

综合实践要继续追问：

```text
一个文件没有按预期出现
  -> 是写入失败？
      -> 是写错位置？
          -> 是没有系统索引？
              -> 是权限失效？
                  -> 是清理或迁移策略错了？
                      -> 是业务一开始就判断错了数据归属？
```

本节要把这些问题做成一套存储访问观察流程。

## 本节定位

本节是第 22 章综合实践。

后续可以配套示例工程：

```text
examples/22-storage-access-lab/
```

这个工程可以围绕 App 私有文件、缓存清理、MediaStore 写入、Photo Picker 选择、SAF 导入导出、URI 授权观察、存储迁移剧本和诊断报告做成一个可运行实验室。

它不是为了证明“某个路径一定能写”，而是为了训练一个更重要的能力：

```text
当数据没有按预期出现时
  -> 你能不能解释它为什么这样表现
```

## 学习目标

学完本节后，你应该能够：

- 区分 App 私有数据、缓存、用户媒体和用户文档。
- 使用 MediaStore 保存共享图片，并解释相册可见性的条件。
- 使用 Photo Picker 或 SAF 获取用户选择的内容。
- 理解 URI 权限、持久授权和路径不可用的原因。
- 设计导入、导出、清缓存、迁移和备份恢复策略。
- 写出一份存储事故诊断报告。

## 第一部分：实践工程规划

第 22 章 Demo 可以拆成这些可观察区域：

- `数据归属决策卡`：根据业务语义推荐 App 私有目录、缓存、MediaStore、Photo Picker、SAF 或 ContentProvider。
- `App 私有存储实验区`：写入草稿、读取草稿、删除草稿、观察 filesDir。
- `缓存清理实验区`：写入缩略图缓存、估算大小、清理缓存、确认核心数据不受影响。
- `MediaStore 实验区`：保存一张课程海报，展示 Uri、MIME_TYPE、RELATIVE_PATH、IS_PENDING。
- `Photo Picker 实验区`：选择头像或封面，观察返回 Uri 和读取方式。
- `SAF 导入导出实验区`：导入课程包，导出学习报告，观察 Uri 授权。
- `URI 权限面板`：展示临时授权、持久授权、已保存 Uri、权限失效提示。
- `迁移剧本模式`：模拟旧路径迁移、半成品文件、索引缺失、清缓存误删。
- `存储事故诊断报告`：输出现象、数据归属、存储入口、权限、索引、根因和回归。
- `体验评分机制`：提醒学习者是否完成了“判断、写入、读取、授权、索引、诊断、恢复”。

## 第一部分补充：体验评分怎么设计

Demo 可以把学习过程拆成 11 个观察点：

| 观察点 | 得分条件 |
| --- | --- |
| 运行现场 | 记录 package、targetSdk、Android 版本 |
| 数据归属 | 为一个需求选择正确存储入口 |
| 私有写入 | 写入并读取一份 App 私有草稿 |
| 缓存清理 | 清理缓存但不影响核心数据 |
| MediaStore 写入 | 保存一张共享图片并拿到 content Uri |
| 媒体索引 | 能解释 MIME_TYPE、RELATIVE_PATH、IS_PENDING |
| Photo Picker | 选择一张用户图片并通过 Uri 读取 |
| SAF 导出 | 创建一份用户可见报告 |
| URI 授权 | 说明临时授权和持久授权差异 |
| 事故剧本 | 完成至少一个存储事故判断 |
| 诊断报告 | 写出证据、原因、修复和回归 |

评分不是为了让学习者刷进度，而是为了防止只记住 API，没有形成判断路径。

## 第二部分：存储决策路线

建议按四段完成：

```text
第一段：定性
  -> 这份数据属于 App、缓存、用户媒体、用户文档，还是跨应用共享内容？

第二段：选择入口
  -> filesDir / cacheDir / externalFilesDir / MediaStore / Photo Picker / SAF / ContentProvider

第三段：观察证据
  -> Uri、权限、MediaStore 字段、文件大小、目录位置、授权状态、异常日志

第四段：设计恢复
  -> 清理、迁移、重试、重新选择、重新下载、索引修复、导出说明
```

这四段对应真实工作里的存储设计闭环。

## 第二部分补充：把 Demo 做成“证据驱动”

第 22 章 Demo 最好不要只展示成功结果，而要展示每一步证据。

例如保存海报时，页面不要只显示：

```text
保存成功
```

而应该显示：

```text
业务语义：用户媒体
存储入口：MediaStore.Images
Uri：content://media/...
MIME_TYPE：image/png
RELATIVE_PATH：Pictures/HelloAndroid
IS_PENDING：0
写入状态：PUBLISHED
下一步证据：content query / 系统相册
```

导入课程包时，页面不要只显示：

```text
导入成功
```

而应该显示：

```text
来源 Uri
是否持久授权
是否复制到 App 私有目录
文件大小
校验结果
Room 索引状态
失败时半成品是否清理
```

这样读者会自然形成一个习惯：

```text
每个存储动作
  -> 都要留下可解释的证据
```

## 第二部分补充：存储选择表

实践时可以先填这张表：

| 业务需求 | 第一判断 | 推荐机制 | 关键证据 |
| --- | --- | --- | --- |
| 保存学习草稿 | App 私有核心数据 | filesDir / Room | 文件存在、数据库索引、清缓存不删除 |
| 保存网络缩略图 | 可再生缓存 | cacheDir | 可删除、可重新生成 |
| 保存课程海报到相册 | 用户媒体 | MediaStore Images | content Uri、MIME_TYPE、RELATIVE_PATH、IS_PENDING |
| 选择头像 | 用户主动选择媒体 | Photo Picker | 返回 Uri、ContentResolver 读取 |
| 导入课程包 zip | 用户文档 | SAF open document | Uri、复制结果、校验日志 |
| 导出学习报告 PDF | 用户文档 | SAF create document / Downloads | 用户选择位置、输出流结果 |
| 分享私有文件给微信 | 跨应用临时共享 | FileProvider | content Uri、grant read permission |
| 旧版本外部路径迁移 | 迁移任务 | 用户授权 + 分批复制 | 迁移状态、成功数量、失败原因 |

## 第三部分：事故剧本怎么玩

存储问题最像一场资料室悬案。

Demo 可以提供几个事故剧本：

| 剧本 | 表面现象 | 第一线索 | 隐藏陷阱 |
| --- | --- | --- | --- |
| 导出报告找不到 | 页面提示成功但文件管理器没有 | 文件写进 filesDir | 用户文档被当成 App 私有数据 |
| 海报相册不显示 | 文件存在但相册没有 | 未清 `IS_PENDING` | 媒体写入没有完成索引语义 |
| 头像下次打不开 | 保存了 Uri 字符串 | 没有长期授权或复制 | Uri 不是永久路径 |
| 清缓存丢草稿 | 清理后草稿没了 | 草稿附件在 cacheDir | 数据分级错误 |
| 升级后旧资料丢失 | targetSdk 升级后旧路径不可访问 | 旧版使用裸路径 | 迁移方案缺失 |

玩剧本时先不要看答案，先写下自己的第一判断：

```text
这是文件不存在、路径不可访问、权限失效、索引不可见，还是数据归属判断错误？
第一证据应该看哪里？
下一步要查 Uri、权限、MediaStore，还是清理和迁移日志？
这份数据卸载 App 后是否应该保留？
```

## 第四部分：推荐观察命令与工具

App 私有目录：

```bash
adb shell run-as com.helloandroid.storage ls files
adb shell run-as com.helloandroid.storage ls cache
```

MediaStore 与媒体文件：

```bash
adb shell content query --uri content://media/external/images/media
adb shell content query --uri content://media/external/video/media
adb shell content query --uri content://media/external/audio/media
```

权限与 AppOps：

```bash
adb shell dumpsys package com.helloandroid.storage
adb shell cmd appops get com.helloandroid.storage
```

存储空间：

```bash
adb shell dumpsys diskstats
adb shell df
```

日志：

```bash
adb logcat | grep -i storage
adb logcat | grep -i MediaProvider
```

注意：命令不是为了背，而是为了把页面现象和系统证据对上。

## 第四部分补充：命令观察顺序

排查存储问题时，可以按下面顺序：

```text
先看业务语义
  -> 这份数据属于谁？

再看 App 证据
  -> 文件、Uri、数据库索引、事件日志

再看系统入口
  -> MediaStore、权限、AppOps、DocumentsProvider

再看生命周期
  -> 清缓存、卸载、迁移、备份恢复、用户撤销授权

最后看修复路径
  -> 重新选择、重新导出、重新索引、重新下载、迁移恢复
```

不要一开始就说“Android 版本兼容问题”。

要把兼容问题拆成具体证据。

## 第五部分：存储诊断报告模板

建议报告格式：

```text
问题标题：
用户现象：
业务场景：
数据类型：
数据归属：
Android 版本：
targetSdk：
设备 / 厂商：
存储入口：
文件名：
Uri：
MIME_TYPE：
RELATIVE_PATH：
IS_PENDING：
是否 App 私有：
是否缓存：
是否共享媒体：
是否 SAF：
是否持久 URI 授权：
是否媒体权限：
是否部分照片授权：
是否经过清缓存：
是否经过迁移：
是否参与备份恢复：
第一证据：
系统证据：
根因判断：
修复方案：
回归用例：
监控指标：
```

这份报告要回答的不是：

```text
这个文件在哪个路径？
```

而是：

```text
这份数据为什么在这个系统边界下应该或不应该被访问？
```

## 第六部分：本章通关检查

完成第 22 章后，请确认自己能回答：

- Scoped Storage 解决的核心问题是什么？
- App 私有目录和共享媒体空间有什么区别？
- `filesDir`、`cacheDir`、`externalFilesDir` 分别适合什么？
- 为什么保存图片到相册要走 MediaStore？
- `IS_PENDING` 的作用是什么？
- Photo Picker 和媒体读取权限的边界是什么？
- SAF 为什么返回 Uri，而不是文件路径？
- 什么场景需要持久 URI 授权？
- 为什么清缓存可能造成用户数据事故？
- 存储迁移为什么要支持中断恢复和校验？
- 一个文件找不到，第一证据应该看哪里？

## 第六部分补充：本章最终事故题

请分析下面这个综合事故：

```text
某课程 App 升级到新的 targetSdk 后，用户集中反馈：

1. 导出的学习报告找不到。
2. 保存到相册的课程海报偶尔不可见。
3. 从文件管理器导入课程包后，下次打开 App 提示无权限。
4. 清缓存后，部分未发布草稿丢失。
5. 老用户升级后，旧版下载目录里的课程资料没有自动出现。

日志和系统证据：

- 导出报告实际写入了 filesDir。
- 海报写入 MediaStore 时设置了 IS_PENDING=1，但失败分支没有清理半成品。
- 导入课程包只保存了 Uri 字符串，没有复制也没有持久授权。
- 草稿附件被放在 cacheDir。
- 旧版本使用 /sdcard/CourseApp/ 保存资料，新版本没有迁移引导。
```

你需要回答：

- 哪些问题属于数据归属错误，哪些属于权限或索引错误？
- 导出报告应该如何改造？
- 海报不可见应该如何排查 MediaStore 字段？
- 导入课程包后应不应该长期依赖原始 Uri？
- 清缓存为什么会删掉草稿？如何重新设计目录？
- 旧路径迁移需要哪些步骤和回滚策略？
- 你会如何设计回归用例和监控指标？

底线是：

```text
不能只说“存储权限问题”。
必须拆成数据归属、存储入口、授权状态、系统索引和生命周期。
```

## 第七部分：Demo 开发建议

后续实现 `examples/22-storage-access-lab/` 时，建议按下面优先级推进：

```text
第一优先级：数据归属决策卡
  -> 先让读者看懂“为什么选这个存储入口”

第二优先级：App 私有存储和缓存实验
  -> 写入、读取、清理、保护核心数据

第三优先级：MediaStore 实验
  -> 保存图片、展示 Uri、MIME_TYPE、RELATIVE_PATH、IS_PENDING

第四优先级：Photo Picker / SAF 实验
  -> 用户选择媒体、创建文档、导入文档、观察 Uri 权限

第五优先级：存储事故剧本
  -> 文件找不到、相册不可见、Uri 失效、清缓存误删、迁移失败

第六优先级：诊断报告和评分机制
  -> 把实验结果变成工程判断
```

不要一开始就追求覆盖所有文件类型。

第 22 章 Demo 最重要的是让读者形成判断路径：

```text
数据归属
  -> 存储入口
      -> 权限授权
          -> 系统索引
              -> 生命周期
                  -> 修复方案
```

## 第七部分补充：第一版 Demo MVP 范围

为了让第 22 章 Demo 可以尽快落地，第一版建议先收敛到一个清晰 MVP。

第一版必须具备：

```text
数据归属决策卡
  -> 选择草稿、缓存、海报、头像、报告、课程包等场景

App 私有草稿实验
  -> 写入 filesDir，读取，展示路径和大小

缓存清理实验
  -> 写入 cacheDir，清理缓存，并证明草稿仍在

MediaStore 保存海报实验
  -> 生成一张简单课程海报，写入 MediaStore，展示 Uri / MIME_TYPE / RELATIVE_PATH / IS_PENDING

SAF 导出报告实验
  -> 用 ACTION_CREATE_DOCUMENT 导出一份学习报告

FileProvider 分享实验
  -> 把 App 私有报告映射成 content Uri，并通过系统分享面板发出

事故剧本
  -> 私有导出找不到、相册不可见、Uri 失效、清缓存误删、file:// 分享失败

诊断报告和评分机制
  -> 把每次实验变成证据链
```

第一版可以暂缓：

```text
完整 Photo Picker 适配矩阵
真实旧版本路径迁移
复杂目录树授权
大文件分块导入
加密存储
云端 DocumentsProvider 特殊行为
```

这样取舍的理由是：

```text
先让读者玩懂数据归属、Uri、MediaStore、SAF、FileProvider 和清理边界
再逐步增加版本适配、迁移和大文件复杂度
```

第一版完成后，Demo 的最小通关路径应该是：

```text
选择业务场景
  -> 判断数据归属
      -> 触发一次真实读写或分享
          -> 查看 Uri / 路径 / 权限 / 索引证据
              -> 完成一个事故剧本
                  -> 写出诊断报告
```

## 第八部分：Demo 的原理观察点

第 22 章 Demo 不能只做成：

```text
写文件按钮
选图片按钮
导出文件按钮
```

这样会退回 API 示例。

更好的设计是让每个按钮都对应一个系统判断点。

| 实验 | 观察点 | 想证明什么 |
| --- | --- | --- |
| 写入私有草稿 | filesDir、数据库索引 | App 私有数据不需要共享权限 |
| 清理缓存 | cacheDir、草稿仍存在 | 缓存必须可再生 |
| 保存海报 | MediaStore Uri、MIME_TYPE、RELATIVE_PATH、IS_PENDING | 共享媒体要进入系统索引 |
| 选择头像 | Photo Picker Uri、ContentResolver 读取 | 选择媒体不一定需要全相册权限 |
| 导入课程包 | SAF Uri、复制进度、校验结果 | 用户文档通过授权访问 |
| 导出报告 | ACTION_CREATE_DOCUMENT、输出流结果 | 用户文件应由用户选择保存位置 |
| 分享报告 | FileProvider Uri、grant flag、MIME_TYPE | 私有文件跨应用分享要临时授权 |
| 持久授权 | takePersistableUriPermission、授权列表 | Uri 权限需要明确管理 |
| 迁移剧本 | 旧路径、新位置、迁移状态 | 存储升级要能中断恢复 |
| 事故报告 | 现象、证据、根因、修复 | 存储问题需要证据链 |

每个实验页面都应该显示：

```text
业务期望
数据归属
存储入口
当前 Uri / 文件
权限状态
系统索引
复盘结论
```

还可以额外提供一个“反例开关”，让学习者亲眼看到错误设计会如何失败：

| 反例 | 触发方式 | 学到什么 |
| --- | --- | --- |
| 私有导出 | 把报告写入 filesDir | 用户文件不该只藏在 App 私有目录 |
| 半成品媒体 | 模拟写入失败且不清 pending | MediaStore 写入需要 rollback |
| Uri 当路径 | 尝试把 SAF Uri 转 File | Uri 不是文件路径 |
| file:// 分享 | 用裸文件路径分享私有报告 | 跨应用分享应使用 content Uri |
| 清缓存误删 | 把草稿附件放入 cacheDir | 缓存必须可再生 |
| 无日志迁移 | 模拟迁移中断 | 数据迁移必须可观测、可恢复 |

反例不是为了制造 bug，而是为了让读者明白：

```text
系统规则不是背出来的
而是在错误设计里撞出来、再被证据解释清楚的
```

## 第九部分：Framework / AOSP 阅读入口

如果想把第 22 章继续往 Framework 深处读，可以从这些入口开始：

```text
应用侧访问入口
  -> ContextImpl
  -> ContentResolver

媒体索引
  -> MediaProvider
  -> MediaStore

文档访问
  -> DocumentsUI
  -> DocumentsProvider

权限与 AppOps
  -> PackageManagerService
  -> PermissionManagerService
  -> AppOpsService

安装包与数据目录
  -> Installd
  -> PackageManagerService

跨应用共享
  -> ContentProvider
  -> UriPermissionOwner
  -> FileProvider
```

建议带着具体问题进入源码：

```text
为什么这个 Uri 能访问？
为什么这个媒体记录相册看不到？
为什么卸载 App 后这个目录被清理？
为什么 targetSdk 变化后权限行为不同？
为什么持久 URI 权限重启后仍然存在？
```

源码阅读的目标不是背类名，而是把“用户授权”和“系统边界”对上。

## 本节小结

第 22 章把数据持久化、权限模型、系统索引、用户授权和 Framework 入口连接到了一起。

你不只是知道几个文件 API，而是要能把存储需求翻译成工程决策：

```text
业务语义
  -> 数据归属
      -> 存储入口
          -> 权限授权
              -> 系统索引
                  -> 生命周期
                      -> 诊断复盘
```

真正成熟的存储设计，不是让 App 找到更多路径，而是让每一份数据都待在它应该待的位置。
