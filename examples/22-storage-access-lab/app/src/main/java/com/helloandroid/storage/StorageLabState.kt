package com.helloandroid.storage

data class StorageLabState(
    val runtime: RuntimeSnapshot = RuntimeSnapshot(),
    val score: StorageScore = StorageScore(),
    val decision: StorageDecision = StorageDecision(),
    val privatePanel: PrivateStoragePanel = PrivateStoragePanel(),
    val cachePanel: CachePanel = CachePanel(),
    val mediaPanel: MediaStorePanel = MediaStorePanel(),
    val pickerPanel: PickerPanel = PickerPanel(),
    val documentPanel: DocumentPanel = DocumentPanel(),
    val persistedUriPanel: PersistedUriPanel = PersistedUriPanel(),
    val sharePanel: SharePanel = SharePanel(),
    val antiPatternPanel: AntiPatternPanel = AntiPatternPanel(),
    val activeScript: StorageIncidentScript = storageIncidentScripts.first(),
    val scripts: List<StorageIncidentScript> = storageIncidentScripts,
    val report: StorageDiagnosisReport = StorageDiagnosisReport(),
    val events: List<StorageEvent> = emptyList()
)

data class RuntimeSnapshot(
    val packageName: String = "-",
    val targetSdk: String = "-",
    val sdk: String = "-",
    val filesDir: String = "-",
    val cacheDir: String = "-",
    val processName: String = "-"
)

data class StorageScore(
    val runtimeObserved: Boolean = false,
    val decisionMade: Boolean = false,
    val privateDraftWritten: Boolean = false,
    val cacheCleaned: Boolean = false,
    val mediaSaved: Boolean = false,
    val mediaEvidenceRead: Boolean = false,
    val photoPicked: Boolean = false,
    val safExported: Boolean = false,
    val persistedUriObserved: Boolean = false,
    val fileShared: Boolean = false,
    val antiPatternObserved: Boolean = false,
    val scriptCompleted: Boolean = false,
    val reportReady: Boolean = false
)

data class StorageDecision(
    val scenario: String = "等待选择一个存储场景。",
    val ownership: String = "-",
    val shouldSurviveUninstall: String = "-",
    val visibleToOtherApps: String = "-",
    val recommended: String = "先判断数据归属：App 私有、缓存、用户媒体、用户文档，还是跨应用分享。",
    val evidence: String = "-"
)

data class PrivateStoragePanel(
    val fileName: String = "course-draft.txt",
    val status: String = "尚未写入草稿。",
    val path: String = "-",
    val size: String = "-",
    val contentPreview: String = "草稿属于 App 私有核心数据，不能被清缓存误删。"
)

data class CachePanel(
    val fileName: String = "poster-thumb.cache",
    val status: String = "尚未写入缓存。",
    val path: String = "-",
    val sizeBefore: String = "-",
    val sizeAfter: String = "-",
    val diagnosis: String = "缓存必须可再生，清理后不应该影响草稿和学习记录。"
)

data class MediaStorePanel(
    val uri: String = "-",
    val displayName: String = "-",
    val mimeType: String = "image/png",
    val relativePath: String = "Pictures/HelloAndroid",
    val pending: String = "-",
    val status: String = "尚未保存课程海报。",
    val evidence: String = "保存共享图片时要观察 content Uri、MIME_TYPE、RELATIVE_PATH 和 IS_PENDING。"
)

data class PickerPanel(
    val uri: String = "-",
    val status: String = "尚未选择图片。",
    val access: String = "Photo Picker 返回的是用户选择的 Uri，不是整本相册权限。",
    val diagnosis: String = "后续读取应通过 ContentResolver，不要强行转路径。"
)

data class DocumentPanel(
    val uri: String = "-",
    val status: String = "尚未导出学习报告。",
    val mimeType: String = "text/plain",
    val ownership: String = "学习报告属于用户文档，应该由用户选择保存位置。",
    val diagnosis: String = "SAF 导出是把用户数据交还给用户，不是藏进 filesDir。"
)

data class SharePanel(
    val fileName: String = "private-storage-report.txt",
    val uri: String = "-",
    val status: String = "尚未分享私有报告。",
    val authority: String = "com.helloandroid.storage.fileprovider",
    val diagnosis: String = "跨应用分享 App 私有文件时，应使用 FileProvider content Uri 和临时读授权。"
)

data class PersistedUriPanel(
    val uri: String = "-",
    val status: String = "尚未选择需要持久授权的文档。",
    val persistedPermissions: String = "-",
    val readResult: String = "-",
    val diagnosis: String = "持久授权适合后续还要继续访问的用户文档；一次性导入则更适合复制到 App 私有目录。"
)

data class AntiPatternPanel(
    val activeCase: String = "等待选择一个反例。",
    val badSignal: String = "-",
    val expectedFailure: String = "-",
    val lesson: String = "反例不是为了制造线上 bug，而是为了把系统边界撞出来，再用证据解释清楚。"
)

data class StorageIncidentScript(
    val id: String,
    val title: String,
    val symptom: String,
    val firstClue: String,
    val hiddenTrap: String,
    val nextMove: String,
    val expectedMechanism: String
)

data class StorageDiagnosisReport(
    val title: String = "-",
    val dataOwnership: String = "-",
    val storageEntry: String = "-",
    val permissionState: String = "-",
    val systemIndex: String = "-",
    val rootCause: String = "-",
    val fix: String = "-",
    val regression: String = "-"
)

data class StorageEvent(
    val source: String,
    val signal: String,
    val detail: String,
    val timestamp: String
)

val storageIncidentScripts = listOf(
    StorageIncidentScript(
        id = "private-export-hidden",
        title = "剧本 A：导出报告找不到",
        symptom = "页面提示导出成功，但文件管理器里找不到报告。",
        firstClue = "报告被写进 filesDir。",
        hiddenTrap = "用户文档被当成 App 私有数据。",
        nextMove = "使用 SAF 导出报告，让用户选择保存位置。",
        expectedMechanism = "ACTION_CREATE_DOCUMENT / Downloads"
    ),
    StorageIncidentScript(
        id = "poster-not-visible",
        title = "剧本 B：海报相册不可见",
        symptom = "保存课程海报后，文件写入成功，但系统相册没有显示。",
        firstClue = "MediaStore 记录缺少完整发布证据。",
        hiddenTrap = "共享媒体不是只写 bytes，还要进入系统索引。",
        nextMove = "保存课程海报，观察 Uri、MIME_TYPE、RELATIVE_PATH 和 IS_PENDING。",
        expectedMechanism = "MediaStore Images"
    ),
    StorageIncidentScript(
        id = "uri-lost",
        title = "剧本 C：头像下次打不开",
        symptom = "用户选择过头像，下次打开页面时无法读取。",
        firstClue = "只保存了 Uri 字符串，没有复制，也没有考虑授权生命周期。",
        hiddenTrap = "Uri 是授权入口，不是永久文件路径。",
        nextMove = "使用 Photo Picker 选择图片，并思考是否需要复制到 App 私有目录。",
        expectedMechanism = "Photo Picker + ContentResolver"
    ),
    StorageIncidentScript(
        id = "cache-deleted-draft",
        title = "剧本 D：清缓存丢草稿",
        symptom = "用户点击清缓存后，未发布草稿也没了。",
        firstClue = "草稿附件被放在 cacheDir。",
        hiddenTrap = "缓存必须可再生，草稿不是缓存。",
        nextMove = "写入私有草稿和缓存，再执行安全清理，对比结果。",
        expectedMechanism = "filesDir + cacheDir 分级"
    ),
    StorageIncidentScript(
        id = "file-uri-share",
        title = "剧本 E：分享报告失败",
        symptom = "把私有报告分享给外部 App 后，对方打不开。",
        firstClue = "使用了 file:// 或没有授予临时读权限。",
        hiddenTrap = "跨应用分享不是暴露路径，而是临时授权。",
        nextMove = "创建私有报告，用 FileProvider 分享 content Uri。",
        expectedMechanism = "FileProvider + FLAG_GRANT_READ_URI_PERMISSION"
    )
)

const val storageCommandHint = """adb shell run-as com.helloandroid.storage ls files
adb shell run-as com.helloandroid.storage ls cache
adb shell content query --uri content://media/external/images/media
adb shell dumpsys package com.helloandroid.storage
adb shell cmd appops get com.helloandroid.storage
adb shell dumpsys activity providers | grep com.helloandroid.storage"""
