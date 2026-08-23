package com.helloandroid.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.math.max

object StorageLabStore {
    private const val TAG = "StorageLab"
    private const val DRAFT_FILE = "course-draft.txt"
    private const val CACHE_FILE = "poster-thumb.cache"
    private const val PRIVATE_REPORT = "private-storage-report.txt"
    private const val HIDDEN_EXPORT = "hidden-export-report.txt"
    private const val CACHE_DRAFT_TRAP = "draft-in-cache-trap.txt"
    private const val FILE_PROVIDER_AUTHORITY = "com.helloandroid.storage.fileprovider"

    private val _state = MutableStateFlow(StorageLabState())
    val state: StateFlow<StorageLabState> = _state

    fun refresh(context: Context) {
        val snapshot = runtimeSnapshot(context)
        _state.update {
            it.copy(
                runtime = snapshot,
                score = it.score.copy(runtimeObserved = true)
            )
        }
        record("Runtime", "REFRESH", "targetSdk=${snapshot.targetSdk}, sdk=${snapshot.sdk}")
    }

    fun chooseDecision(scenario: String) {
        val decision = when (scenario) {
            "draft" -> StorageDecision(
                scenario = "保存学习草稿",
                ownership = "App 私有核心数据。",
                shouldSurviveUninstall = "通常不需要，但不能被清缓存误删。",
                visibleToOtherApps = "否。",
                recommended = "filesDir / Room。",
                evidence = "filesDir 文件、数据库索引、清理策略。"
            )

            "cache" -> StorageDecision(
                scenario = "保存网络缩略图缓存",
                ownership = "App 可再生缓存。",
                shouldSurviveUninstall = "否。",
                visibleToOtherApps = "否。",
                recommended = "cacheDir / externalCacheDir。",
                evidence = "缓存大小、清理日志、核心数据是否仍存在。"
            )

            "poster" -> StorageDecision(
                scenario = "保存课程海报到相册",
                ownership = "用户媒体。",
                shouldSurviveUninstall = "是。",
                visibleToOtherApps = "是，相册和媒体应用应该可见。",
                recommended = "MediaStore Images。",
                evidence = "content Uri、MIME_TYPE、RELATIVE_PATH、IS_PENDING。"
            )

            "avatar" -> StorageDecision(
                scenario = "选择头像图片",
                ownership = "用户主动选择的媒体。",
                shouldSurviveUninstall = "原始媒体由用户决定；头像结果建议复制到 App 私有目录。",
                visibleToOtherApps = "不需要。",
                recommended = "Photo Picker + ContentResolver。",
                evidence = "返回 Uri、读取方式、授权范围。"
            )

            "report" -> StorageDecision(
                scenario = "导出学习报告",
                ownership = "用户文档。",
                shouldSurviveUninstall = "是。",
                visibleToOtherApps = "取决于用户保存位置。",
                recommended = "SAF ACTION_CREATE_DOCUMENT。",
                evidence = "目标 Uri、输出流结果、用户选择位置。"
            )

            "share" -> StorageDecision(
                scenario = "分享 App 私有报告",
                ownership = "App 私有文件临时共享。",
                shouldSurviveUninstall = "否。",
                visibleToOtherApps = "只在分享动作中临时可见。",
                recommended = "FileProvider + FLAG_GRANT_READ_URI_PERMISSION。",
                evidence = "content Uri、MIME_TYPE、grant flag。"
            )

            else -> StorageDecision(
                scenario = "导入课程包",
                ownership = "用户文档，导入后可变成 App 私有数据。",
                shouldSurviveUninstall = "原始文件由用户决定；导入副本由 App 管理。",
                visibleToOtherApps = "不需要。",
                recommended = "SAF open document + 复制到 App 私有目录 + 校验。",
                evidence = "来源 Uri、复制日志、size/hash、Room 索引。"
            )
        }
        _state.update {
            it.copy(
                decision = decision,
                score = it.score.copy(decisionMade = true),
                report = it.report.copy(
                    title = decision.scenario,
                    dataOwnership = decision.ownership,
                    storageEntry = decision.recommended,
                    rootCause = "先判断数据归属，再选择存储入口。",
                    fix = decision.recommended,
                    regression = "用 ${decision.evidence} 验证。"
                )
            )
        }
        record("Decision", "SELECT", decision.scenario)
    }

    fun writePrivateDraft(context: Context) {
        val file = File(context.filesDir, DRAFT_FILE)
        val content = """
            第22章草稿
            dataOwnership=App private
            createdAt=${now()}
            rule=清缓存不能删除草稿
        """.trimIndent()
        file.writeText(content)
        _state.update {
            it.copy(
                privatePanel = PrivateStoragePanel(
                    status = "已写入 App 私有草稿。",
                    path = file.absolutePath,
                    size = "${file.length()} bytes",
                    contentPreview = content.lines().take(3).joinToString(" / ")
                ),
                score = it.score.copy(privateDraftWritten = true),
                report = it.report.copy(
                    title = "保存学习草稿",
                    dataOwnership = "App 私有核心数据。",
                    storageEntry = "filesDir",
                    permissionState = "不需要共享存储权限。",
                    systemIndex = "不进入 MediaStore。",
                    rootCause = "草稿属于核心数据，不应放入 cacheDir。",
                    fix = "保存到 filesDir 或 Room，并和清缓存策略隔离。",
                    regression = "清缓存后重新读取草稿，确认仍然存在。"
                )
            )
        }
        record("Private", "WRITE_DRAFT", "${file.name}, size=${file.length()}")
    }

    fun readPrivateDraft(context: Context) {
        val file = File(context.filesDir, DRAFT_FILE)
        val exists = file.exists()
        val preview = if (exists) file.readText().lines().take(3).joinToString(" / ") else "草稿不存在。"
        _state.update {
            it.copy(
                privatePanel = it.privatePanel.copy(
                    status = if (exists) "读取成功：草稿仍在。" else "读取失败：草稿不存在。",
                    path = file.absolutePath,
                    size = if (exists) "${file.length()} bytes" else "0 bytes",
                    contentPreview = preview
                )
            )
        }
        record("Private", "READ_DRAFT", "exists=$exists")
    }

    fun writeCache(context: Context) {
        val file = File(context.cacheDir, CACHE_FILE)
        val text = buildString {
            repeat(128) { index ->
                append("cache-line-$index: 可再生缩略图缓存，只用于观察清理边界。\n")
            }
        }
        file.writeText(text)
        val size = directorySize(context.cacheDir)
        _state.update {
            it.copy(
                cachePanel = CachePanel(
                    status = "已写入可再生缓存。",
                    path = file.absolutePath,
                    sizeBefore = "$size bytes",
                    sizeAfter = "-",
                    diagnosis = "这份缓存可以被安全清理，清理后草稿应该仍然存在。"
                )
            )
        }
        record("Cache", "WRITE", "${file.name}, cacheSize=$size")
    }

    fun clearCacheSafely(context: Context) {
        val before = directorySize(context.cacheDir)
        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        val after = directorySize(context.cacheDir)
        val draftExists = File(context.filesDir, DRAFT_FILE).exists()
        _state.update {
            it.copy(
                cachePanel = it.cachePanel.copy(
                    status = "已清理 cacheDir。",
                    sizeBefore = "$before bytes",
                    sizeAfter = "$after bytes",
                    diagnosis = "清理完成：草稿存在=$draftExists。缓存可再生，草稿不可误删。"
                ),
                score = it.score.copy(cacheCleaned = true),
                report = it.report.copy(
                    title = "清缓存不误删",
                    dataOwnership = "缓存是可再生数据，草稿是核心数据。",
                    storageEntry = "cacheDir + filesDir",
                    rootCause = "清理逻辑必须按数据等级执行。",
                    fix = "只清理 cacheDir，草稿和数据库不进入清缓存范围。",
                    regression = "清理后读取草稿，并验证缓存大小归零。"
                )
            )
        }
        record("Cache", "CLEAR", "before=$before, after=$after, draftExists=$draftExists")
    }

    fun savePosterToMediaStore(context: Context) {
        val resolver = context.contentResolver
        val displayName = "hello-storage-poster-${System.currentTimeMillis()}.png"
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/HelloAndroid")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val result = runCatching {
            val uri = resolver.insert(collection, values) ?: error("MediaStore insert returned null")
            val bitmap = createPosterBitmap()
            val wrote = resolver.openOutputStream(uri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            } ?: false
            if (!wrote) {
                resolver.delete(uri, null, null)
                error("Bitmap compress failed")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val publish = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, publish, null, null)
            }
            uri
        }

        _state.update {
            if (result.isSuccess) {
                val uri = result.getOrThrow()
                it.copy(
                    mediaPanel = MediaStorePanel(
                        uri = uri.toString(),
                        displayName = displayName,
                        pending = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "0" else "低版本无 IS_PENDING 字段",
                        status = "课程海报已写入 MediaStore。",
                        evidence = "共享媒体已拿到 content Uri。下一步可用 content query 或系统相册观察。"
                    ),
                    score = it.score.copy(mediaSaved = true, mediaEvidenceRead = true),
                    report = it.report.copy(
                        title = "保存课程海报到相册",
                        dataOwnership = "用户媒体。",
                        storageEntry = "MediaStore Images",
                        permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            "Android 10+ 写入自有媒体不需要申请全存储权限。"
                        } else {
                            "Android 9 及以下可能需要写外部存储权限。"
                        },
                        systemIndex = "MIME_TYPE=image/png, RELATIVE_PATH=Pictures/HelloAndroid, IS_PENDING=0。",
                        rootCause = "共享图片需要进入系统媒体索引，而不是只写一个路径。",
                        fix = "MediaStore insert -> openOutputStream -> publish。",
                        regression = "保存后检查 Uri、相册可见性和半成品清理。"
                    )
                )
            } else {
                it.copy(
                    mediaPanel = it.mediaPanel.copy(
                        status = "保存失败：${result.exceptionOrNull()?.message}",
                        evidence = "检查权限、存储空间、MediaStore insert 和输出流。"
                    )
                )
            }
        }
        record("MediaStore", if (result.isSuccess) "SAVE" else "ERROR", result.fold({ it.toString() }, { it.message ?: "unknown" }))
    }

    fun onPhotoPicked(context: Context, uri: Uri?) {
        if (uri == null) {
            _state.update {
                it.copy(pickerPanel = it.pickerPanel.copy(status = "未选择图片。"))
            }
            record("Picker", "CANCEL", "photo picker returned null")
            return
        }
        val readable = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                max(0, input.read(ByteArray(64)))
            } ?: -1
        }.getOrDefault(-1)
        _state.update {
            it.copy(
                pickerPanel = PickerPanel(
                    uri = uri.toString(),
                    status = "已选择图片，并完成一次 ContentResolver 读取尝试。",
                    access = "Photo Picker 授权的是用户选择的媒体，不是整个相册。",
                    diagnosis = "读取结果=$readable bytes。用于头像时，建议裁剪后复制到 App 私有目录。"
                ),
                score = it.score.copy(photoPicked = true),
                report = it.report.copy(
                    title = "选择头像图片",
                    dataOwnership = "用户主动选择的媒体。",
                    storageEntry = "Photo Picker Uri",
                    permissionState = "用户选择形成授权范围。",
                    systemIndex = "通过 ContentResolver 读取 Uri，不依赖真实路径。",
                    rootCause = "Uri 不是永久文件路径。",
                    fix = "头像结果复制到 App 私有目录，原始 Uri 只作为输入。",
                    regression = "撤销照片权限或重新选择后验证头像仍可展示。"
                )
            )
        }
        record("Picker", "PICK", "uri=$uri, firstRead=$readable")
    }

    fun exportReportToUri(context: Context, uri: Uri) {
        val text = diagnosisText(_state.value.report)
        val result = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(text.toByteArray())
            } ?: error("openOutputStream returned null")
        }
        _state.update {
            it.copy(
                documentPanel = DocumentPanel(
                    uri = uri.toString(),
                    status = if (result.isSuccess) "学习报告已导出到用户选择的位置。" else "导出失败：${result.exceptionOrNull()?.message}",
                    diagnosis = if (result.isSuccess) "SAF 导出成功：用户文档由用户选择保存位置。" else "检查 Uri、输出流和目标 Provider 状态。"
                ),
                score = if (result.isSuccess) it.score.copy(safExported = true) else it.score,
                report = it.report.copy(
                    title = "导出学习报告",
                    dataOwnership = "用户文档。",
                    storageEntry = "SAF ACTION_CREATE_DOCUMENT",
                    permissionState = "用户通过系统文档 UI 选择目标 Uri。",
                    systemIndex = "不是 MediaStore 图片索引，而是 DocumentsProvider 输出。",
                    rootCause = "用户报告不应该只藏在 filesDir。",
                    fix = "通过 SAF 创建文档并写入输出流。",
                    regression = "导出后用文件管理器或目标 App 打开。"
                )
            )
        }
        record("SAF", if (result.isSuccess) "EXPORT" else "ERROR", uri.toString())
    }

    fun onPersistableDocumentPicked(context: Context, uri: Uri?) {
        if (uri == null) {
            _state.update {
                it.copy(persistedUriPanel = it.persistedUriPanel.copy(status = "未选择文档。"))
            }
            record("SAF", "PERSIST_CANCEL", "open document returned null")
            return
        }
        val persistResult = runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val readResult = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.read(ByteArray(64))
            } ?: -1
        }
        val permissions = persistedPermissions(context)
        _state.update {
            it.copy(
                persistedUriPanel = PersistedUriPanel(
                    uri = uri.toString(),
                    status = if (persistResult.isSuccess) {
                        "已尝试持久化读取授权。"
                    } else {
                        "持久授权失败：${persistResult.exceptionOrNull()?.message}"
                    },
                    persistedPermissions = permissions.ifBlank { "当前没有持久 Uri 授权记录。" },
                    readResult = readResult.fold(
                        onSuccess = { bytes -> "ContentResolver 读取结果=$bytes bytes" },
                        onFailure = { error -> "读取失败：${error.message}" }
                    ),
                    diagnosis = "观察重点：保存 Uri 字符串不等于拥有文件，长期访问要看 persistedUriPermissions。"
                ),
                score = it.score.copy(persistedUriObserved = true),
                report = it.report.copy(
                    title = "持久 URI 授权观察",
                    dataOwnership = "用户选择的文档。",
                    storageEntry = "SAF ACTION_OPEN_DOCUMENT + takePersistableUriPermission",
                    permissionState = permissions.ifBlank { "未看到持久授权记录。" },
                    systemIndex = "DocumentsProvider Uri，不依赖真实路径。",
                    rootCause = "Uri 是授权入口，不是本地文件所有权。",
                    fix = "长期访问时持久化授权；一次性导入时复制到 App 私有目录。",
                    regression = "重启 App 后刷新持久授权列表，并尝试重新读取。"
                )
            )
        }
        record("SAF", "PERSIST_URI", "uri=$uri, success=${persistResult.isSuccess}")
    }

    fun refreshPersistedUriPermissions(context: Context) {
        val permissions = persistedPermissions(context)
        _state.update {
            it.copy(
                persistedUriPanel = it.persistedUriPanel.copy(
                    persistedPermissions = permissions.ifBlank { "当前没有持久 Uri 授权记录。" },
                    status = "已刷新持久 Uri 授权列表。"
                ),
                score = it.score.copy(persistedUriObserved = true)
            )
        }
        record("SAF", "PERSIST_REFRESH", permissions.ifBlank { "empty" })
    }

    fun createPrivateReport(context: Context) {
        val file = privateReportFile(context)
        file.writeText(diagnosisText(_state.value.report))
        _state.update {
            it.copy(
                sharePanel = it.sharePanel.copy(
                    status = "已在 filesDir 创建私有报告，可用于 FileProvider 分享。",
                    uri = "-",
                    diagnosis = "私有文件不能直接用 file:// 暴露给其他 App。"
                )
            )
        }
        record("Share", "CREATE_PRIVATE_REPORT", "${file.absolutePath}, size=${file.length()}")
    }

    fun sharePrivateReport(context: Context) {
        val file = privateReportFile(context)
        if (!file.exists()) {
            file.writeText(diagnosisText(_state.value.report))
        }
        val result = runCatching {
            val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "第22章存储诊断报告")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(share, "分享存储诊断报告"))
            uri
        }
        _state.update {
            if (result.isSuccess) {
                val uri = result.getOrThrow()
                it.copy(
                    sharePanel = SharePanel(
                        uri = uri.toString(),
                        status = "已通过 FileProvider 发起分享。",
                        diagnosis = "接收方拿到的是 content Uri 和临时读授权，不是 filesDir 真实路径。"
                    ),
                    score = it.score.copy(fileShared = true),
                    report = it.report.copy(
                        title = "分享 App 私有报告",
                        dataOwnership = "App 私有文件临时共享。",
                        storageEntry = "FileProvider",
                        permissionState = "Intent FLAG_GRANT_READ_URI_PERMISSION。",
                        systemIndex = "不进入 MediaStore，只通过 Provider 临时暴露。",
                        rootCause = "跨应用分享不能使用 file:// 暴露私有路径。",
                        fix = "FileProvider content Uri + MIME_TYPE + grant flag。",
                        regression = "用外部 App 打开报告，确认不会暴露数据库或 token。"
                    )
                )
            } else {
                it.copy(
                    sharePanel = it.sharePanel.copy(
                        status = "分享失败：${result.exceptionOrNull()?.message}",
                        diagnosis = "检查 FileProvider authority、paths 配置和目标 App。"
                    )
                )
            }
        }
        record("Share", if (result.isSuccess) "SHARE" else "ERROR", result.fold({ it.toString() }, { it.message ?: "unknown" }))
    }

    fun simulatePrivateExportTrap(context: Context) {
        val file = File(context.filesDir, HIDDEN_EXPORT)
        file.writeText(diagnosisText(_state.value.report))
        _state.update {
            it.copy(
                antiPatternPanel = AntiPatternPanel(
                    activeCase = "反例：把用户报告导出到 filesDir",
                    badSignal = "文件真实存在：${file.absolutePath}",
                    expectedFailure = "用户在文件管理器里找不到；卸载 App 后文件会被清理。",
                    lesson = "用户文档应该用 SAF 或 Downloads，而不是藏在 App 私有目录。"
                ),
                score = it.score.copy(antiPatternObserved = true),
                report = it.report.copy(
                    title = "反例：导出报告找不到",
                    dataOwnership = "用户文档被误判为 App 私有数据。",
                    storageEntry = "错误入口：filesDir",
                    permissionState = "其他 App 和文件管理器不能直接访问 App 私有目录。",
                    rootCause = "数据归属判断错误。",
                    fix = "使用 SAF ACTION_CREATE_DOCUMENT 或 MediaStore Downloads。",
                    regression = "导出后让用户在目标位置打开文件。"
                )
            )
        }
        record("AntiPattern", "PRIVATE_EXPORT", file.absolutePath)
    }

    fun simulatePendingMediaRollback(context: Context) {
        val resolver = context.contentResolver
        val displayName = "pending-rollback-${System.currentTimeMillis()}.png"
        val result = runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                error("IS_PENDING 反例需要 Android 10+。")
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/HelloAndroid")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                values
            ) ?: error("MediaStore insert returned null")
            resolver.delete(uri, null, null)
            uri
        }
        _state.update {
            it.copy(
                antiPatternPanel = AntiPatternPanel(
                    activeCase = "反例：MediaStore 半成品回滚",
                    badSignal = result.fold(
                        onSuccess = { uri -> "插入了 IS_PENDING=1 的半成品记录，并立即删除回滚：$uri" },
                        onFailure = { error -> "模拟失败：${error.message}" }
                    ),
                    expectedFailure = "如果失败分支不回滚，可能留下相册不可见或脏索引记录。",
                    lesson = "MediaStore 写入要有状态机：insert -> write -> publish；失败必须 rollback。"
                ),
                score = it.score.copy(antiPatternObserved = true)
            )
        }
        record("AntiPattern", "PENDING_ROLLBACK", result.fold({ it.toString() }, { it.message ?: "unknown" }))
    }

    fun simulateFileUriTrap(context: Context) {
        val file = privateReportFile(context)
        if (!file.exists()) {
            file.writeText(diagnosisText(_state.value.report))
        }
        val fileUri = Uri.fromFile(file)
        _state.update {
            it.copy(
                antiPatternPanel = AntiPatternPanel(
                    activeCase = "反例：使用 file:// 分享私有报告",
                    badSignal = fileUri.toString(),
                    expectedFailure = "其他 App 没有权限读取你的私有目录，新系统也会限制 file:// 跨应用暴露。",
                    lesson = "跨应用分享应该使用 FileProvider content Uri + 临时读授权。"
                ),
                score = it.score.copy(antiPatternObserved = true),
                report = it.report.copy(
                    title = "反例：分享报告失败",
                    dataOwnership = "App 私有文件临时共享。",
                    storageEntry = "错误入口：file://",
                    permissionState = "没有授予接收方临时读权限。",
                    rootCause = "把路径暴露当成了授权。",
                    fix = "FileProvider.getUriForFile + FLAG_GRANT_READ_URI_PERMISSION。",
                    regression = "用外部 App 打开 content Uri，确认可读且不暴露私有路径。"
                )
            )
        }
        record("AntiPattern", "FILE_URI", fileUri.toString())
    }

    fun simulateCacheDraftTrap(context: Context) {
        val trap = File(context.cacheDir, CACHE_DRAFT_TRAP)
        trap.writeText("这是假草稿，用来演示草稿误放 cacheDir 会被清理。createdAt=${now()}")
        val before = trap.exists()
        trap.delete()
        val after = trap.exists()
        _state.update {
            it.copy(
                antiPatternPanel = AntiPatternPanel(
                    activeCase = "反例：把草稿放进 cacheDir",
                    badSignal = "清理前存在=$before，清理后存在=$after，path=${trap.absolutePath}",
                    expectedFailure = "用户点击清缓存后，误放在 cacheDir 的草稿会消失。",
                    lesson = "缓存必须可再生；草稿、未同步记录和用户生成内容不能放进 cacheDir。"
                ),
                score = it.score.copy(antiPatternObserved = true)
            )
        }
        record("AntiPattern", "CACHE_DRAFT", "before=$before, after=$after")
    }

    fun selectScript(script: StorageIncidentScript) {
        _state.update {
            it.copy(
                activeScript = script,
                score = it.score.copy(scriptCompleted = true),
                decision = it.decision.copy(
                    scenario = script.title,
                    recommended = script.expectedMechanism,
                    evidence = script.firstClue
                ),
                report = StorageDiagnosisReport(
                    title = script.title,
                    dataOwnership = script.hiddenTrap,
                    storageEntry = script.expectedMechanism,
                    rootCause = script.hiddenTrap,
                    fix = script.nextMove,
                    regression = "完成剧本后，用页面证据和 adb 命令验证结论。"
                )
            )
        }
        record("Script", "START", script.title)
    }

    fun markReportReady() {
        _state.update { it.copy(score = it.score.copy(reportReady = true)) }
        record("Report", "READY", "storage diagnosis report marked ready")
    }

    fun clearEvents() {
        _state.update { it.copy(events = emptyList()) }
    }

    fun record(source: String, signal: String, detail: String) {
        Log.d(TAG, "$source $signal: $detail")
        val event = StorageEvent(source, signal, detail, now())
        _state.update { state ->
            state.copy(events = (listOf(event) + state.events).take(80))
        }
    }

    private fun directorySize(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf { directorySize(it) } ?: 0L
    }

    private fun privateReportFile(context: Context): File {
        return File(context.filesDir, PRIVATE_REPORT)
    }

    private fun diagnosisText(report: StorageDiagnosisReport): String {
        return """
            第22章存储诊断报告
            title=${report.title}
            dataOwnership=${report.dataOwnership}
            storageEntry=${report.storageEntry}
            permissionState=${report.permissionState}
            systemIndex=${report.systemIndex}
            rootCause=${report.rootCause}
            fix=${report.fix}
            regression=${report.regression}
            generatedAt=${now()}
        """.trimIndent()
    }

    private fun persistedPermissions(context: Context): String {
        return context.contentResolver.persistedUriPermissions.joinToString(separator = "\n") { permission ->
            "uri=${permission.uri}, read=${permission.isReadPermission}, write=${permission.isWritePermission}"
        }
    }

    private fun createPosterBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(247, 246, 240))

        paint.color = Color.rgb(46, 94, 82)
        canvas.drawRoundRect(56f, 56f, 904f, 484f, 24f, 24f, paint)

        paint.color = Color.rgb(243, 196, 93)
        canvas.drawRoundRect(88f, 88f, 872f, 168f, 18f, 18f, paint)

        paint.color = Color.WHITE
        paint.textSize = 58f
        paint.isFakeBoldText = true
        canvas.drawText("Hello Android Storage Lab", 112f, 142f, paint)

        paint.textSize = 38f
        paint.isFakeBoldText = false
        canvas.drawText("MediaStore -> content Uri -> system index", 112f, 260f, paint)
        canvas.drawText("MIME_TYPE / RELATIVE_PATH / IS_PENDING", 112f, 326f, paint)
        canvas.drawText("Saved at ${now()}", 112f, 400f, paint)

        return bitmap
    }
}
