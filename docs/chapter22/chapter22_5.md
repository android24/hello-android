# 22.5 SAF、DocumentsProvider 与 URI 权限：让用户亲自打开文件柜

MediaStore 适合图片、视频、音频这类共享媒体。

但用户的文件世界远不止媒体：

```text
PDF
zip
txt
docx
课程包
备份文件
任意目录
云盘文档
第三方文件管理器里的内容
```

这些内容不应该由 App 随便扫描。

更合适的模式是：

```text
用户自己选择文件或目录
  -> 系统给 App 一个 Uri
      -> App 在授权范围内读写
```

这就是 Storage Access Framework，简称 SAF。

## 本节定位

本节负责回答：

- SAF 解决什么问题？
- `ACTION_OPEN_DOCUMENT`、`ACTION_CREATE_DOCUMENT`、`ACTION_OPEN_DOCUMENT_TREE` 分别适合什么？
- Uri 权限为什么比文件路径更重要？
- 持久化 URI 授权是什么？
- DocumentsProvider 在这条链路里扮演什么角色？

## 学习目标

学完本节后，你应该能够：

- 区分“选择文件”“创建文件”“选择目录”三种 SAF 场景。
- 知道为什么从 SAF 拿到的结果应该当 Uri 使用，而不是强转路径。
- 理解临时 URI 权限和持久化 URI 权限的区别。
- 能设计导入、导出、备份、恢复类功能。
- 能排查“用户明明选过文件，下次打开却没权限”的问题。

## 第一部分：SAF 的本质是用户授权

SAF 的核心不是文件选择器 UI，而是授权模型。

它表达的是：

```text
用户选择了某个文件或目录
  -> 系统把访问这份内容的 Uri 给 App
      -> App 不需要知道真实路径
          -> App 只能访问用户授权的范围
```

这和传统路径思维完全不同。

路径思维：

```text
我知道文件在哪里，所以我要读它。
```

SAF 思维：

```text
用户选择了这个内容，所以系统允许我通过 Uri 访问它。
```

这一步非常关键。

现代 Android 存储里，很多内容没有稳定、可访问、可公开的真实路径。

它可能来自：

```text
本地文件管理器
云盘
下载目录
另一个 App 的 DocumentsProvider
压缩管理器
厂商文件服务
```

Uri 才是统一入口。

从系统调用链看，SAF 通常是这样的：

```text
App 发起 ACTION_OPEN_DOCUMENT / ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT_TREE
  -> DocumentsUI 展示系统文件选择界面
      -> 用户选择某个 provider 下的文档或目录
          -> 系统返回 content Uri
              -> App 通过 ContentResolver 访问 Uri
                  -> 对应 DocumentsProvider 提供真实读写能力
```

这条链路有两个关键点：

```text
第一，用户选择发生在系统 UI 中。
第二，App 拿到的是访问令牌，不是文件所有权。
```

所以 SAF 返回的 Uri 更像一张通行证：

```text
可以读什么
可以写什么
能不能长期保存
范围是单个文件还是整棵目录
都由这张通行证决定
```

## 第二部分：三个常用 Intent

### ACTION_OPEN_DOCUMENT：用户选择已有文件

适合：

```text
导入课程包
选择 PDF
选择备份文件
读取一个用户文档
```

语义：

```text
用户把某个已有文件授权给 App 读取，必要时也可以请求写入。
```

注意：

```text
结果是 Uri，不是路径。
应该通过 ContentResolver.openInputStream(uri) 读取。
如果下次还要访问，需要持久化 URI 权限。
```

### ACTION_CREATE_DOCUMENT：用户选择保存位置并创建文件

适合：

```text
导出学习报告 PDF
导出课程笔记 Markdown
导出备份 zip
```

语义：

```text
用户决定文件名和保存位置，App 负责写入内容。
```

这是导出功能非常健康的方式。

因为它尊重了用户的主权：

```text
文件放哪里
叫什么名字
是否覆盖
由用户决定
```

### ACTION_OPEN_DOCUMENT_TREE：用户选择一个目录

适合：

```text
选择同步目录
选择长期导出目录
选择课程资料根目录
批量导入某个目录内文件
```

语义：

```text
用户授权 App 访问一个目录树。
```

这比选择单个文件权限更大，所以要谨慎。

不要为了省事，一上来就让用户授权整个目录树。

## 第三部分：临时 URI 权限与持久 URI 权限

当用户通过 SAF 选择内容后，App 会拿到 Uri。

这份授权可能是临时的，也可以被持久化。

临时授权：

```text
这次 Activity 结果回来后可以访问
进程重启、设备重启或下次启动不一定还可靠
```

持久授权：

```text
App 调用 takePersistableUriPermission()
系统记录授权关系
以后可以继续访问这个 Uri
```

适合持久授权的场景：

```text
用户选择一个课程资料目录
用户选择一个长期备份文件
用户选择一个导出目录
用户选择一个需要后续继续读取的文档
```

不适合滥用持久授权的场景：

```text
一次性上传头像
一次性导入文件后已经复制到 App 私有目录
一次性预览
```

授权越大，责任越大。

持久授权也不是“永远有效”的魔法。

它只能说明：

```text
系统记录了 App 对某个 Uri 的长期访问关系。
```

但访问仍然可能失败：

```text
用户删除了文件
用户移动了文件
用户撤销了授权
提供方 App 被卸载或禁用
云端文件暂时不可用
Provider 返回错误
```

因此，工程上要把持久授权当成“较稳定的门票”，而不是本地文件。

访问前仍然要处理：

```text
openInputStream 返回 null
SecurityException
FileNotFoundException
Provider 响应慢
读取中断
用户重新选择
```

这也是为什么导入后长期使用的课程包，通常建议复制到 App 自己的可管理目录。

## 第四部分：DocumentsProvider 是谁

SAF 背后可能连接多个 DocumentsProvider。

你可以把 DocumentsProvider 理解成：

```text
一个向系统文件选择器提供文档树的内容服务。
```

它可能来自：

```text
系统下载
本地文件管理器
云盘 App
厂商文件服务
你的 App 自己
```

当用户在系统文件选择器里点击一个文件时，App 拿到的是这个 provider 发出来的 Uri。

所以：

```text
Uri 不是普通路径
Uri 背后可能是一段跨进程访问
读取可能比较慢
可能失败
可能需要流式处理
可能没有文件大小
可能不支持随机访问
```

这也是为什么导入大文件时不能在主线程直接读。

第 19、20、21 章的知识会在这里重逢：

```text
ContentResolver 访问 Uri
  -> 可能跨进程
      -> 可能阻塞
          -> 不能卡主线程
              -> 大文件要有进度、取消、错误恢复
```

如果要做得更深入，还要记住：

```text
ContentResolver 访问 Uri 可能跨进程
跨进程调用可能进入 Binder
Provider 内部可能再访问磁盘、云端或数据库
磁盘或网络慢会放大阻塞
主线程读取就可能变成卡顿或 ANR
```

所以 SAF 不是“文件选择器 + 读文件”这么简单。

它连接了：

```text
Intent
DocumentsUI
ContentResolver
DocumentsProvider
Binder
权限授权
IO 调度
异常恢复
```

这就是为什么本章会把 SAF 放在 Framework 阶段讲，而不是只在入门阶段当作一个选择文件 API。

## 第五部分：导入文件的正确心智

用户选择一个课程包后，不建议长期依赖原始 Uri 直接使用。

更稳妥的流程：

```text
用户通过 ACTION_OPEN_DOCUMENT 选择 zip
  -> App 通过 ContentResolver 打开输入流
      -> 复制到 App 私有导入目录
          -> 校验 hash / size / manifest
              -> 解压或解析
                  -> 写入 Room 索引
                      -> 记录导入来源和结果
```

这样做的好处：

```text
后续使用不依赖外部 provider 是否可用
可以校验完整性
可以支持失败重试
可以清理半成品
可以把用户授权范围降到最小
```

如果只是预览文件，可以不复制。

但如果要长期使用，最好把它变成 App 自己可管理的数据。

## 第六部分：导出文件的正确心智

导出学习报告时，推荐流程：

```text
ACTION_CREATE_DOCUMENT
  -> 用户选择保存位置
      -> App 拿到目标 Uri
          -> openOutputStream(uri)
              -> 写入 PDF / Markdown / zip
                  -> 成功后提示用户
```

不要悄悄把文件塞到一个用户找不到的位置。

导出功能的本质是：

```text
App 把用户的数据交还给用户。
```

所以文件名、位置、格式和失败提示都要清楚。

## 第七部分：FileProvider：把私有文件临时交给别人

SAF 解决的是用户选择文件或目录。

但还有一类常见需求：

```text
App 已经有一个私有文件
  -> 想分享给微信、邮箱、浏览器、系统分享面板或 PDF 阅读器
```

例如：

```text
分享一份学习报告 PDF
分享一张错误截图
分享一份脱敏日志 zip
用外部阅读器打开 App 私有目录里的文档
```

这时不要把私有路径直接暴露出去。

旧式做法可能是：

```text
file:///data/user/0/com.xxx/files/report.pdf
```

问题很明显：

```text
其他 App 没有权限读你的私有目录
暴露真实路径没有必要
新系统会限制 file:// 跨应用暴露
接收方应该拿到的是临时访问权，而不是你的文件系统结构
```

更合理的是使用 FileProvider：

```text
App 私有文件
  -> FileProvider 映射成 content:// Uri
      -> Intent 附带 FLAG_GRANT_READ_URI_PERMISSION
          -> 接收方临时读取
              -> 授权随 Intent 语义结束
```

FileProvider 的关键不是“换一种 Uri 格式”，而是：

```text
用 content Uri 表达受控分享
用临时授权替代暴露路径
用 provider 路径配置限制可分享范围
```

它和 SAF 的区别可以这样记：

| 场景 | 推荐入口 | 授权来源 |
| --- | --- | --- |
| 用户选择一个外部文件给 App | SAF | 用户通过系统选择器授权 App |
| App 把自己的文件分享给外部 App | FileProvider | App 临时授权给接收方 |
| 用户选择保存导出位置 | ACTION_CREATE_DOCUMENT | 用户决定目标位置 |
| App 保存图片到相册 | MediaStore | 系统媒体集合和权限规则 |

FileProvider 也要注意边界：

```text
只暴露需要分享的目录
分享前确认文件是否脱敏
不要把数据库、token、授权文件放进可分享路径
不要长期依赖接收方保存的临时 Uri
```

这部分会在第 23 章安全模型里继续和权限、签名、数据保护连起来。

## 第八部分：SAF 事故排查

### 事故一：下次打开 Uri 没权限

可能原因：

```text
没有调用 takePersistableUriPermission()
Intent 没带持久授权 flag
只适合一次性访问，却被当成长期访问
用户在系统设置里撤销了授权
```

### 事故二：Uri 转路径失败

可能原因：

```text
这个 Uri 来自 DocumentsProvider
它不暴露真实文件路径
App 不应该依赖路径
```

解决方向：

```text
使用 ContentResolver.openInputStream()
需要复制时复制到 App 私有目录
```

### 事故三：导入大文件导致 ANR

可能原因：

```text
在主线程读取 Uri
Provider 响应慢
没有分块读取
没有进度和取消
```

解决方向：

```text
放到 IO dispatcher
分块复制
显示进度
支持取消
处理异常和半成品清理
```

### 事故四：分享私有文件失败

可能原因：

```text
使用了 file:// Uri
FileProvider 路径没有覆盖目标文件
Intent 没有添加临时读权限
接收方不支持对应 MIME_TYPE
分享的文件还没写完或已经被清理
```

解决方向：

```text
使用 FileProvider 生成 content Uri
设置正确 MIME_TYPE
添加 FLAG_GRANT_READ_URI_PERMISSION
只分享脱敏后的临时文件
失败后给用户明确提示
```

## 第九部分：本节自测

请回答：

- SAF 和 MediaStore 的边界是什么？
- `ACTION_OPEN_DOCUMENT` 和 `ACTION_CREATE_DOCUMENT` 的语义差异是什么？
- 为什么 SAF 返回的 Uri 不应该强行转成本地路径？
- 什么场景需要 `takePersistableUriPermission()`？
- 导入课程包后，为什么通常要复制到 App 私有目录？
- 读取 SAF Uri 为什么不能放在主线程？
- FileProvider 和 SAF 的授权方向有什么不同？
- 为什么跨应用分享私有文件不应该使用 `file://`？

## 本节小结

SAF 的核心是：

```text
用户选择
  -> 系统授权
      -> App 通过 Uri 访问
```

第 22.5 节的关键结论是：

```text
当数据属于用户的文件柜时，应该让用户亲自开门，而不是让 App 偷偷翻柜子。
当数据属于 App 自己但需要分享时，应该临时发钥匙，而不是暴露房间地址。
```
