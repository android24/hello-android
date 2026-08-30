package com.helloandroid.security

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Process
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

class SecurityLabStore(
    private val app: Application,
) {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private var encryptedPayload: EncryptedPayload? = null

    fun loadState(): SecurityLabState {
        val identity = collectIdentity()
        val permissions = collectPermissions()
        val cards = buildJudgmentCards(identity, permissions)
        val incidents = buildIncidents()
        val keystore = KeystoreExperiment()
        val signatureExperiment = buildSignatureExperiment(identity)
        return SecurityLabState(
            identity = identity,
            permissions = permissions,
            judgmentCards = cards,
            incidents = incidents,
            scoreItems = buildScoreItems(
                identity = identity,
                permissions = permissions,
                hasKeystoreResult = false,
                hasShareUri = false,
                hasSanitizedLog = false,
                hasAppOpsJudgment = false,
                hasPendingIntentJudgment = false,
                hasPhotoExperiment = false,
                hasSignatureScenario = false,
                hasSignatureComparison = false,
                hasIncident = false,
                hasQuizDone = false,
                hasFreeTextReport = false,
                hasSafExport = false,
            ),
            keystore = keystore,
            signatureExperiment = signatureExperiment,
            diagnosisReport = buildDiagnosisReport(
                identity = identity,
                permissions = permissions,
                keystore = keystore,
                shareUri = "尚未创建",
                safExportUri = "尚未导出",
                appOpsExperiment = AppOpsExperiment(),
                pendingIntentExperiment = PendingIntentExperiment(),
                photoAccessExperiment = PhotoAccessExperiment(),
                signatureExperiment = signatureExperiment,
                selectedIncident = null,
                diagnosisQuiz = DiagnosisQuiz(),
            ),
        )
    }

    fun refreshWith(current: SecurityLabState): SecurityLabState {
        val identity = collectIdentity()
        val permissions = collectPermissions()
        return current.copy(
            identity = identity,
            permissions = permissions,
            judgmentCards = buildJudgmentCards(identity, permissions),
            scoreItems = buildScoreItems(
                identity = identity,
                permissions = permissions,
                hasKeystoreResult = current.keystore.status != "等待实验",
                hasShareUri = current.shareUri.startsWith("content://"),
                hasSanitizedLog = current.sanitizedLog != "点击“生成脱敏日志”后查看安全输出。",
                hasAppOpsJudgment = current.appOpsExperiment.done,
                hasPendingIntentJudgment = current.pendingIntentExperiment.done,
                hasPhotoExperiment = current.photoAccessExperiment.done,
                hasSignatureScenario = current.signatureExperiment.done,
                hasSignatureComparison = current.signatureExperiment.comparisonDone,
                hasIncident = current.selectedIncident != null,
                hasQuizDone = current.diagnosisQuiz.done,
                hasFreeTextReport = current.diagnosisQuiz.freeTextDone,
                hasSafExport = current.safExportUri.startsWith("content://"),
            ),
            signatureExperiment = current.signatureExperiment.copy(currentSha256 = identity.signatureSha256),
            diagnosisReport = buildDiagnosisReport(
                identity = identity,
                permissions = permissions,
                keystore = current.keystore,
                shareUri = current.shareUri,
                safExportUri = current.safExportUri,
                appOpsExperiment = current.appOpsExperiment,
                pendingIntentExperiment = current.pendingIntentExperiment,
                photoAccessExperiment = current.photoAccessExperiment,
                signatureExperiment = current.signatureExperiment.copy(currentSha256 = identity.signatureSha256),
                selectedIncident = current.selectedIncident,
                diagnosisQuiz = current.diagnosisQuiz,
            ),
        )
    }

    fun judgeAppOps(current: SecurityLabState, op: String, mode: String): SecurityLabState {
        val verdict = appOpsVerdict(op, mode)
        val next = current.appOpsExperiment.copy(
            selectedOp = op,
            selectedMode = mode,
            verdict = verdict,
            evidenceCommand = "adb shell cmd appops get ${current.identity.packageName.ifBlank { app.packageName }} $op",
            done = true,
        )
        return refreshWith(current.copy(appOpsExperiment = next))
    }

    fun updateAppOpsOutput(current: SecurityLabState, output: String): SecurityLabState {
        val mode = parseAppOpsMode(output)
        val op = current.appOpsExperiment.selectedOp
        val verdict = if (mode == "unknown") {
            "没有识别到明确 mode。请确认输出中包含 allow、ignore、foreground、deny 或 default。"
        } else {
            appOpsVerdict(op, mode) + " 这是从命令输出解析出的结果。"
        }
        val next = current.appOpsExperiment.copy(
            rawOutput = output,
            parsedMode = mode,
            selectedMode = if (mode == "unknown") current.appOpsExperiment.selectedMode else mode,
            verdict = verdict,
            done = mode != "unknown",
        )
        return refreshWith(current.copy(appOpsExperiment = next))
    }

    fun selectPendingIntentCase(current: SecurityLabState, caseName: String): SecurityLabState {
        val intent = if (caseName == "implicit") {
            Intent("com.helloandroid.security.OPEN_INTERNAL").apply {
                setPackage(app.packageName)
                putExtra("course_id", "security-lab")
            }
        } else {
            Intent(app, SafeInternalActivity::class.java).apply {
                putExtra("course_id", "security-lab")
            }
        }
        val flags = when (caseName) {
            "mutable" -> PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag()
            "implicit" -> PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            else -> PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        }
        PendingIntent.getActivity(app, 2301, intent, flags)

        val next = when (caseName) {
            "mutable" -> PendingIntentExperiment(
                selectedCase = "可变 PendingIntent",
                riskLevel = "高风险",
                verdict = "Mutable 允许接收方补写或替换部分 Intent 字段。除非是 RemoteInput 等明确需要修改的场景，否则应优先使用 FLAG_IMMUTABLE。",
                done = true,
            )
            "implicit" -> PendingIntentExperiment(
                selectedCase = "隐式意图入口",
                riskLevel = "中风险",
                verdict = "隐式 Intent 会扩大匹配面。安全入口应尽量指向明确组件，并校验 action、data、extras 和调用来源。",
                done = true,
            )
            else -> PendingIntentExperiment(
                selectedCase = "显式 + Immutable",
                riskLevel = "推荐",
                verdict = "显式组件限制目的地，FLAG_IMMUTABLE 锁住授权令牌内容。它更像一张只允许指定动作的门票。",
                done = true,
            )
        }
        return refreshWith(current.copy(pendingIntentExperiment = next))
    }

    fun recordPickedPhoto(current: SecurityLabState, uri: String?): SecurityLabState {
        val next = if (uri == null) {
            current.photoAccessExperiment.copy(
                verdict = "没有选择图片。这个结果也有意义：Photo Picker 把访问范围限定在用户主动挑选的内容上。",
            )
        } else {
            current.photoAccessExperiment.copy(
                pickedUri = uri,
                verdict = "已拿到用户选择的 content Uri。它不是文件路径，也不要求读取整个相册；后续访问仍要遵守 Uri grant 的生命周期。",
                done = true,
            )
        }
        return refreshWith(current.copy(photoAccessExperiment = next))
    }

    fun selectSignatureScenario(current: SecurityLabState, scenario: String): SecurityLabState {
        val next = when (scenario) {
            "same" -> current.signatureExperiment.copy(
                selectedScenario = "同一签名升级",
                verdict = "包名相同、versionCode 更高、SigningDetails 与已安装包匹配，PackageManagerService 才会把它当成可信升级。",
                done = true,
            )
            "lineage" -> current.signatureExperiment.copy(
                selectedScenario = "签名轮换 lineage",
                verdict = "如果 APK Signature Scheme v3 的 lineage 能证明新旧证书连续，系统可以接受密钥轮换后的升级身份。",
                done = true,
            )
            else -> current.signatureExperiment.copy(
                selectedScenario = "包名相同但签名不同",
                verdict = "APK 自身签名可能有效，但与已安装包身份不匹配，安装阶段会失败；这不是混淆问题，而是升级身份断裂。",
                done = true,
            )
        }.copy(currentSha256 = current.identity.signatureSha256)
        return refreshWith(current.copy(signatureExperiment = next))
    }

    fun updateSignatureOutputs(
        current: SecurityLabState,
        debugOutput: String? = null,
        releaseOutput: String? = null,
    ): SecurityLabState {
        val debugText = debugOutput ?: current.signatureExperiment.debugCertOutput
        val releaseText = releaseOutput ?: current.signatureExperiment.releaseCertOutput
        val debugSha = parseCertificateSha256(debugText)
        val releaseSha = parseCertificateSha256(releaseText)
        val hasBoth = debugSha != "尚未解析" && releaseSha != "尚未解析"
        val verdict = when {
            !hasBoth -> "还没有拿到两个证书指纹。请分别粘贴 debug 和 release APK 的 apksigner 输出。"
            debugSha == releaseSha -> "两个 APK 的证书 SHA-256 相同：它们具备同一签名身份，覆盖安装还要继续检查 versionCode 和安装来源。"
            else -> "两个 APK 的证书 SHA-256 不同：即使包名相同，系统也不会把它们当成可信升级关系。"
        }
        val next = current.signatureExperiment.copy(
            debugCertOutput = debugText,
            releaseCertOutput = releaseText,
            debugSha256 = debugSha,
            releaseSha256 = releaseSha,
            comparisonVerdict = verdict,
            comparisonDone = hasBoth,
            done = current.signatureExperiment.done,
        )
        return refreshWith(current.copy(signatureExperiment = next))
    }

    fun selectIncident(current: SecurityLabState, incident: IncidentScript): SecurityLabState {
        val quiz = DiagnosisQuiz(
            feedback = "已选择 ${incident.title}。先别急着看答案，试着选出问题类型、第一证据和修复动作。",
        )
        return refreshWith(current.copy(selectedIncident = incident, diagnosisQuiz = quiz))
    }

    fun updateDiagnosisDraft(
        current: SecurityLabState,
        category: String? = null,
        evidence: String? = null,
        fix: String? = null,
    ): SecurityLabState {
        val next = current.diagnosisQuiz.copy(
            selectedCategory = category ?: current.diagnosisQuiz.selectedCategory,
            selectedEvidence = evidence ?: current.diagnosisQuiz.selectedEvidence,
            selectedFix = fix ?: current.diagnosisQuiz.selectedFix,
            done = false,
            feedback = "判断已记录。提交后会和当前事故剧本的标准诊断对照。",
        )
        return refreshWith(current.copy(diagnosisQuiz = next))
    }

    fun updateFreeTextReport(current: SecurityLabState, report: String): SecurityLabState {
        val score = freeTextScore(report)
        val feedback = when (score) {
            4 -> "4/4：自由报告完整，已经包含现象、证据、根因和修复。"
            3 -> "3/4：报告基本完整，再补一层回归或系统模块会更稳。"
            2 -> "2/4：已经有骨架，但证据链还不够扎实。"
            1 -> "1/4：有方向，但还像一句结论，需要补现象、证据和修复。"
            else -> "0/4：先按“现象、第一证据、根因、修复”四句写。"
        }
        val next = current.diagnosisQuiz.copy(
            freeTextReport = report,
            freeTextScore = score,
            freeTextFeedback = feedback,
            freeTextDone = score >= 3,
        )
        return refreshWith(current.copy(diagnosisQuiz = next))
    }

    fun submitDiagnosisQuiz(current: SecurityLabState): SecurityLabState {
        val incident = current.selectedIncident
        if (incident == null) {
            return refreshWith(
                current.copy(
                    diagnosisQuiz = current.diagnosisQuiz.copy(
                        feedback = "请先在安全事故剧本里选择一个案子，再提交诊断。",
                        done = false,
                    )
                )
            )
        }

        val quiz = current.diagnosisQuiz
        val categoryOk = quiz.selectedCategory == incident.expectedCategory
        val evidenceOk = quiz.selectedEvidence == incident.expectedEvidence
        val fixOk = quiz.selectedFix == incident.expectedFix
        val score = listOf(categoryOk, evidenceOk, fixOk).count { it }
        val feedback = when (score) {
            3 -> "3/3：判断链完整。你已经把现象、第一证据和修复动作串起来了。"
            2 -> "2/3：方向基本正确，再检查漏掉的是问题类型、第一证据还是修复动作。"
            1 -> "1/3：抓到了一点线索，但还没有形成完整判断链。回到剧本看“隐藏陷阱”。"
            else -> "0/3：这次像是在凭感觉排查。先找第一证据，再谈修复。"
        }
        val next = quiz.copy(score = score, feedback = feedback, done = score >= 2)
        return refreshWith(current.copy(diagnosisQuiz = next))
    }

    fun encryptToken(current: SecurityLabState): SecurityLabState {
        val alias = current.keystore.alias
        val key = getOrCreateSecretKey(alias)
        val cipher = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
        val cipherText = cipher.doFinal(current.keystore.plainText.toByteArray(Charsets.UTF_8))
        encryptedPayload = EncryptedPayload(cipher.iv, cipherText)
        val nextKeystore = current.keystore.copy(
            cipherTextPreview = cipherText.toHexPreview(),
            decryptedText = "等待解密",
            status = "加密成功：密文已保存，key 仍由 Android Keystore 保护。",
            lastError = "无",
        )
        return refreshWith(current.copy(keystore = nextKeystore))
    }

    fun decryptToken(current: SecurityLabState): SecurityLabState {
        val payload = encryptedPayload
        if (payload == null) {
            val next = current.keystore.copy(
                status = "解密失败：还没有密文。",
                lastError = "missing ciphertext",
            )
            return refreshWith(current.copy(keystore = next))
        }

        return try {
            val key = keyStore.getKey(current.keystore.alias, null) as SecretKey
            val plainText = Cipher.getInstance(AES_GCM).run {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, payload.iv))
                String(doFinal(payload.cipherText), Charsets.UTF_8)
            }
            val next = current.keystore.copy(
                decryptedText = plainText,
                status = "解密成功：alias、key、iv、tag 和密文匹配。",
                lastError = "无",
            )
            refreshWith(current.copy(keystore = next))
        } catch (error: Exception) {
            val next = current.keystore.copy(
                decryptedText = "解密失败",
                status = "解密失败：请根据异常判断是 key missing、key invalidated 还是 tag mismatch。",
                lastError = error.javaClass.simpleName,
            )
            refreshWith(current.copy(keystore = next))
        }
    }

    fun deleteKeyAndTryDecrypt(current: SecurityLabState): SecurityLabState {
        keyStore.deleteEntry(current.keystore.alias)
        val next = current.keystore.copy(
            status = "已删除 alias 对应 key。再次解密会模拟备份恢复后“密文还在，key 不在”的事故。",
            lastError = "key deleted",
        )
        return decryptToken(current.copy(keystore = next))
    }

    fun sanitizeLog(current: SecurityLabState): SecurityLabState {
        val sanitized = current.rawLog
            .replace(Regex("Authorization=Bearer\\s+[^\\s]+"), "Authorization=Bearer <redacted>")
            .replace(Regex("1[3-9]\\d{9}"), "<phone-redacted>")
            .replace(Regex("content://[^\\s]+"), "content://<uri-redacted>")
        return refreshWith(current.copy(sanitizedLog = sanitized))
    }

    fun createShareReport(current: SecurityLabState): Pair<SecurityLabState, Intent> {
        val shareDir = File(app.filesDir, "share").apply { mkdirs() }
        val reportFile = File(shareDir, "security-diagnosis-report.txt")
        reportFile.writeText(
            current.diagnosisReport.ifBlank {
                buildDiagnosisReport(
                    identity = current.identity,
                    permissions = current.permissions,
                    keystore = current.keystore,
                    shareUri = current.shareUri,
                    safExportUri = current.safExportUri,
                    appOpsExperiment = current.appOpsExperiment,
                    pendingIntentExperiment = current.pendingIntentExperiment,
                    photoAccessExperiment = current.photoAccessExperiment,
                    signatureExperiment = current.signatureExperiment,
                    selectedIncident = current.selectedIncident,
                    diagnosisQuiz = current.diagnosisQuiz,
                )
            }
        )
        val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", reportFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val next = refreshWith(current.copy(shareUri = uri.toString()))
        return next to Intent.createChooser(intent, "分享安全诊断报告")
    }

    fun exportReportToSaf(current: SecurityLabState, uriString: String?): SecurityLabState {
        if (uriString == null) {
            return refreshWith(current.copy(safExportUri = "用户取消导出"))
        }
        return try {
            app.contentResolver.openOutputStream(Uri.parse(uriString))?.use { output ->
                val report = current.diagnosisReport.ifBlank {
                    buildDiagnosisReport(
                        identity = current.identity,
                        permissions = current.permissions,
                        keystore = current.keystore,
                        shareUri = current.shareUri,
                        safExportUri = current.safExportUri,
                        appOpsExperiment = current.appOpsExperiment,
                        pendingIntentExperiment = current.pendingIntentExperiment,
                        photoAccessExperiment = current.photoAccessExperiment,
                        signatureExperiment = current.signatureExperiment,
                        selectedIncident = current.selectedIncident,
                        diagnosisQuiz = current.diagnosisQuiz,
                    )
                }
                output.write(report.toByteArray(Charsets.UTF_8))
            }
            refreshWith(current.copy(safExportUri = uriString))
        } catch (error: Exception) {
            refreshWith(current.copy(safExportUri = "导出失败：${error.javaClass.simpleName}"))
        }
    }

    private fun collectIdentity(): IdentitySnapshot {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            app.packageManager.getPackageInfo(app.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            app.packageManager.getPackageInfo(app.packageName, PackageManager.GET_SIGNATURES)
        }
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { app.packageManager.getInstallSourceInfo(app.packageName).installingPackageName }.getOrNull()
        } else {
            @Suppress("DEPRECATION")
            app.packageManager.getInstallerPackageName(app.packageName)
        } ?: "adb / unknown"

        return IdentitySnapshot(
            packageName = app.packageName,
            uid = Process.myUid(),
            pid = Process.myPid(),
            processName = currentProcessName(),
            targetSdk = app.applicationInfo.targetSdkVersion,
            buildProfile = BuildConfig.BUILD_PROFILE,
            dataDir = app.applicationInfo.dataDir,
            filesDir = app.filesDir.absolutePath,
            cacheDir = app.cacheDir.absolutePath,
            installer = installer,
            signatureSha256 = packageInfo.signatureDigest(),
        )
    }

    private fun collectPermissions(): List<PermissionSnapshot> {
        val declared = declaredPermissions()
        val packageName = app.packageName
        return listOf(
            PermissionSnapshot(
                label = "相机",
                permission = Manifest.permission.CAMERA,
                declaredInManifest = Manifest.permission.CAMERA in declared,
                runtimeGranted = hasPermission(Manifest.permission.CAMERA),
                appOpsCommand = "adb shell cmd appops get $packageName CAMERA",
                note = "拍照上传作业：granted 后还要看系统隐私开关、设备相机和调用时机。",
            ),
            PermissionSnapshot(
                label = "通知",
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else "Android 13 以下无需 runtime 通知权限",
                declaredInManifest = Manifest.permission.POST_NOTIFICATIONS in declared,
                runtimeGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasPermission(Manifest.permission.POST_NOTIFICATIONS),
                appOpsCommand = "adb shell cmd appops get $packageName POST_NOTIFICATION",
                note = "学习提醒：还要看 AppOps、通知总开关和 channel。",
            ),
            PermissionSnapshot(
                label = "精确定位",
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                declaredInManifest = Manifest.permission.ACCESS_FINE_LOCATION in declared,
                runtimeGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
                appOpsCommand = "adb shell cmd appops get $packageName FINE_LOCATION",
                note = "位置能力：foreground mode 代表前台可用，后台调用仍可能被拦。",
            ),
            PermissionSnapshot(
                label = "照片媒体",
                permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE,
                declaredInManifest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES in declared
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE in declared
                },
                runtimeGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasPermission(Manifest.permission.READ_MEDIA_IMAGES),
                appOpsCommand = "adb shell cmd appops get $packageName READ_MEDIA_IMAGES",
                note = "照片上传：Android 14 后还要考虑部分照片授权和 Photo Picker。",
            ),
        )
    }

    private fun declaredPermissions(): Set<String> {
        val info = app.packageManager.getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
        return info.requestedPermissions?.toSet().orEmpty()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(app, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildJudgmentCards(identity: IdentitySnapshot, permissions: List<PermissionSnapshot>): List<JudgmentCard> {
        val notification = permissions.first { it.label == "通知" }
        return listOf(
            JudgmentCard(
                title = "身份观察卡",
                module = "Linux uid / PackageManagerService / Binder callingUid",
                input = "packageName=${identity.packageName}, uid=${identity.uid}, process=${identity.processName}",
                result = "App 的安全身份不只是包名，还包括 uid、签名和当前用户空间。",
                evidence = "adb shell dumpsys package ${identity.packageName}\nadb shell ps -A | grep ${identity.packageName}",
                fix = "跨 App 访问不要传私有路径，改用 FileProvider、SAF 或 ContentProvider。",
            ),
            JudgmentCard(
                title = "权限状态卡",
                module = "PackageManagerService / PermissionManagerService / PermissionController",
                input = permissions.joinToString("\n") { "${it.label}: declared=${it.declaredInManifest}, granted=${it.runtimeGranted}" },
                result = "Manifest 声明、runtime 授权、用户设置和 AppOps 是不同层。",
                evidence = "adb shell dumpsys package ${identity.packageName}",
                fix = "按场景请求最小权限，为拒绝、部分授权和设置关闭设计降级路径。",
            ),
            JudgmentCard(
                title = "AppOps 裁决卡",
                module = "AppOpsService + NotificationManagerService / LocationManagerService / MediaProvider",
                input = "op=POST_NOTIFICATION / CAMERA / FINE_LOCATION / READ_MEDIA_IMAGES",
                result = "granted 后仍要看 allow、ignore、foreground、default。",
                evidence = notification.appOpsCommand,
                fix = "把权限问题拆成 permission grant、appops mode、channel / setting、前后台状态。",
            ),
            JudgmentCard(
                title = "签名观察卡",
                module = "PackageInstallerService / PackageManagerService / ApkSignatureVerifier / SigningDetails",
                input = "signatureSha256=${identity.signatureSha256}",
                result = "包名相同不等于同一个 App，签名身份连续才是可信升级。",
                evidence = "apksigner verify --verbose --print-certs app-debug.apk",
                fix = "发布前校验证书指纹、versionCode、variant 和渠道包来源。",
            ),
            JudgmentCard(
                title = "组件边界卡",
                module = "PackageManagerService / ActivityTaskManagerService / UriGrantsManagerService",
                input = "DebugPanelActivity exported=true + signature permission; FileProvider paths=share/",
                result = "组件入口要限制谁能进、带什么参数、触发什么动作、带出什么数据。",
                evidence = "adb shell am start -a com.helloandroid.security.DEBUG_PANEL",
                fix = "内部组件默认 exported=false，需要外部访问时加 signature permission 和参数校验。",
            ),
        )
    }

    private fun buildIncidents(): List<IncidentScript> {
        return listOf(
            IncidentScript(
                title = "剧本 A：学习提醒不弹",
                symptom = "用户说通知权限已开启，但晚上没有收到复习提醒。",
                firstEvidence = "dumpsys package 看 POST_NOTIFICATIONS，cmd appops 看 POST_NOTIFICATION，再看 channel。",
                hiddenTrap = "runtime granted 不是通知展示的终点，AppOps 和 channel 都可能拦截。",
                nextAction = "把诊断拆成 permission、AppOps、channel、后台调度四层。",
                wrongGuess = "直接重装 App 或让用户反复点授权。",
                correctDiagnosis = "先区分 permission granted、AppOps mode、通知 channel、后台调度是否各自通过。",
                expectedCategory = "AppOps / 设置问题",
                expectedEvidence = "通知权限 + AppOps + channel",
                expectedFix = "分层引导和降级",
            ),
            IncidentScript(
                title = "剧本 B：拍照上传失败",
                symptom = "点击拍照上传作业，页面提示相机不可用。",
                firstEvidence = "Manifest CAMERA、runtime grant、AppOps CAMERA、前台状态、设备相机。",
                hiddenTrap = "用户说授权了，不等于系统隐私开关和 AppOps 都允许。",
                nextAction = "展示明确状态，并为拒绝、设备无相机、系统关闭做不同文案。",
                wrongGuess = "只检查 checkSelfPermission()，看到 granted 就认定业务代码有 bug。",
                correctDiagnosis = "继续检查 AppOps、系统隐私开关、调用时机、设备能力和相机返回结果。",
                expectedCategory = "权限和实际放行问题",
                expectedEvidence = "CAMERA 权限 + AppOps + 前台状态",
                expectedFix = "权限拒绝降级",
            ),
            IncidentScript(
                title = "剧本 C：换机后 token 解不开",
                symptom = "恢复数据后密文仍在，但登录态无法恢复。",
                firstEvidence = "key alias 是否存在、异常是否是 key missing / invalidated / AEAD tag mismatch。",
                hiddenTrap = "密文能备份不代表 Keystore key 也能恢复。",
                nextAction = "清理无效密文，重新登录，从服务端重建可信状态。",
                wrongGuess = "把密文再 base64 一次，或者把 token 明文写进 SharedPreferences。",
                correctDiagnosis = "确认 alias、iv、tag、密文和设备密钥状态，再决定清理密文并重新建立登录态。",
                expectedCategory = "Keystore / 数据保护问题",
                expectedEvidence = "alias + 解密异常",
                expectedFix = "清理密文并重新登录",
            ),
            IncidentScript(
                title = "剧本 D：调试页被外部拉起",
                symptom = "安全扫描发现 DebugPanelActivity 可被另一个 App 启动。",
                firstEvidence = "Manifest exported、intent-filter、permission、调用方签名。",
                hiddenTrap = "有 intent-filter 的组件更容易被外部发现。",
                nextAction = "内部组件改 exported=false，或使用 signature permission 并校验参数。",
                wrongGuess = "觉得 Activity 名字带 Debug，线上用户不会知道，所以没关系。",
                correctDiagnosis = "以 Manifest 为准判断边界：exported、intent-filter、permission 和参数校验缺一不可。",
                expectedCategory = "组件边界问题",
                expectedEvidence = "manifest exported + permission",
                expectedFix = "收窄 exported 或加 signature permission",
            ),
            IncidentScript(
                title = "剧本 E：分享报告暴露过宽",
                symptom = "诊断报告分享功能使用 FileProvider，但 paths 暴露了整个 filesDir。",
                firstEvidence = "res/xml/file_paths.xml、生成的 content Uri、分享文件目录。",
                hiddenTrap = "FileProvider 是安全入口，但 paths 配错仍会扩大暴露面。",
                nextAction = "只暴露 share/ 子目录，分享前生成脱敏副本，分享后清理。",
                wrongGuess = "只要是 content:// 就一定安全。",
                correctDiagnosis = "content Uri 只是入口形式，还要检查 provider exported、grant flag、paths 范围和文件内容。",
                expectedCategory = "日志 / 导出泄露问题",
                expectedEvidence = "file_paths + content Uri",
                expectedFix = "收窄 paths 并脱敏",
            ),
            IncidentScript(
                title = "剧本 F：前台定位可用，后台定位失败",
                symptom = "学习打卡页打开时能定位，锁屏后一段时间后台签到失败。",
                firstEvidence = "检查 ACCESS_FINE_LOCATION / ACCESS_BACKGROUND_LOCATION、AppOps FINE_LOCATION mode、前后台状态和后台任务触发时机。",
                hiddenTrap = "前台定位 granted 不代表后台定位也能用，foreground mode 在后台会重新拦截。",
                nextAction = "把定位能力拆成前台触发、后台需求、AppOps mode、后台限制和用户可理解的降级提示。",
                wrongGuess = "只看前台页面定位成功，就认定后台签到失败是网络问题。",
                correctDiagnosis = "后台调用需要重新看后台定位权限、AppOps foreground、前后台状态和后台调度限制。",
                expectedCategory = "AppOps / 设置问题",
                expectedEvidence = "定位权限 + AppOps foreground + 前后台状态",
                expectedFix = "前台触发或后台定位降级",
            ),
        )
    }

    private fun buildScoreItems(
        identity: IdentitySnapshot,
        permissions: List<PermissionSnapshot>,
        hasKeystoreResult: Boolean,
        hasShareUri: Boolean,
        hasSanitizedLog: Boolean,
        hasAppOpsJudgment: Boolean,
        hasPendingIntentJudgment: Boolean,
        hasPhotoExperiment: Boolean,
        hasSignatureScenario: Boolean,
        hasSignatureComparison: Boolean,
        hasIncident: Boolean,
        hasQuizDone: Boolean,
        hasFreeTextReport: Boolean,
        hasSafExport: Boolean,
    ): List<ScoreItem> {
        return listOf(
            ScoreItem("运行身份", identity.uid > 0, "记录 packageName、uid、pid、processName、targetSdk。"),
            ScoreItem("沙箱路径", identity.dataDir.isNotBlank(), "观察 dataDir、filesDir、cacheDir。"),
            ScoreItem("权限声明", permissions.any { it.declaredInManifest }, "读取 Manifest 请求权限。"),
            ScoreItem("runtime 权限", permissions.any { it.runtimeGranted }, "检查至少一个危险权限授权状态。"),
            ScoreItem("AppOps", hasAppOpsJudgment, "选择 op/mode，或粘贴命令输出解析裁决。"),
            ScoreItem("签名", identity.signatureSha256 != "unknown" && hasSignatureScenario, "观察证书 SHA-256，并判断一次升级场景。"),
            ScoreItem("签名实测", hasSignatureComparison, "粘贴 debug / release 证书输出并比较。"),
            ScoreItem("Keystore", hasKeystoreResult, "生成密钥并加密一段 token。"),
            ScoreItem("FileProvider", hasShareUri, "创建 share/ 目录下的 content Uri。"),
            ScoreItem("PendingIntent", hasPendingIntentJudgment, "判断授权令牌是否显式、不可变、可回收。"),
            ScoreItem("Photo Picker", hasPhotoExperiment, "选择一张图片，观察 Uri grant 与整库权限的差异。"),
            ScoreItem("日志脱敏", hasSanitizedLog, "把 token、手机号、Uri 转为安全输出。"),
            ScoreItem("事故剧本", hasIncident, "选择至少一个事故剧本并判断错误直觉与正确诊断。"),
            ScoreItem("诊断答题", hasQuizDone, "提交问题类型、第一证据和修复动作。"),
            ScoreItem("自由报告", hasFreeTextReport, "写出含现象、证据、根因和修复的文本报告。"),
            ScoreItem("诊断报告", hasQuizDone && hasFreeTextReport && (hasAppOpsJudgment || hasKeystoreResult || hasShareUri || hasSafExport), "把身份、授权、证据、修复和回归串起来。"),
        )
    }

    private fun buildSignatureExperiment(identity: IdentitySnapshot): SignatureExperiment {
        return SignatureExperiment(
            currentSha256 = identity.signatureSha256,
            command = "apksigner verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk",
        )
    }

    private fun parseCertificateSha256(output: String): String {
        if (output.isBlank()) return "尚未解析"
        val labeled = Regex("(?i)sha-?256[^0-9A-Fa-f]*([0-9A-Fa-f:]{32,})").find(output)?.groupValues?.getOrNull(1)
        val fallback = Regex("(?i)([0-9A-Fa-f]{2}(?::[0-9A-Fa-f]{2}){15,})").find(output)?.groupValues?.getOrNull(1)
        return (labeled ?: fallback)?.uppercase() ?: "尚未解析"
    }

    private fun freeTextScore(report: String): Int {
        val normalized = report.lowercase()
        val checks = listOf(
            listOf("现象", "用户", "失败", "不弹", "不可用", "打不开").any { it in normalized },
            listOf("证据", "dumpsys", "appops", "manifest", "sha-256", "异常", "log").any { it in normalized },
            listOf("根因", "因为", "原因", "拦截", "不匹配", "暴露", "丢失").any { it in normalized },
            listOf("修复", "降级", "收窄", "重新登录", "脱敏", "immutable", "回归").any { it in normalized },
        )
        return checks.count { it }
    }

    private fun appOpsVerdict(op: String, mode: String): String {
        return when (mode) {
            "allow" -> "$op = allow：权限层通过后，系统服务通常继续执行业务动作。"
            "ignore" -> "$op = ignore：系统服务不会抛出权限异常，但结果可能是静默失败、空列表或通知不展示。"
            "foreground" -> "$op = foreground：前台调用通常可继续，后台任务会被重新拦住。"
            "deny" -> "$op = deny：系统服务会把这次访问当成明确拒绝，调用方要走降级路径。"
            else -> "$op = default：回到系统默认策略，最终结果取决于 permission、targetSdk、用户设置和具体系统服务。"
        }
    }

    private fun parseAppOpsMode(output: String): String {
        val normalized = output.lowercase()
        return when {
            Regex("\\ballow\\b|mode=allow").containsMatchIn(normalized) -> "allow"
            Regex("\\bignore\\b|mode=ignore").containsMatchIn(normalized) -> "ignore"
            Regex("\\bforeground\\b|mode=foreground").containsMatchIn(normalized) -> "foreground"
            Regex("\\bdeny\\b|mode=deny").containsMatchIn(normalized) -> "deny"
            Regex("\\bdefault\\b|mode=default").containsMatchIn(normalized) -> "default"
            else -> "unknown"
        }
    }

    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    }

    private fun mutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    }

    private fun buildDiagnosisReport(
        identity: IdentitySnapshot,
        permissions: List<PermissionSnapshot>,
        keystore: KeystoreExperiment,
        shareUri: String,
        safExportUri: String,
        appOpsExperiment: AppOpsExperiment,
        pendingIntentExperiment: PendingIntentExperiment,
        photoAccessExperiment: PhotoAccessExperiment,
        signatureExperiment: SignatureExperiment,
        selectedIncident: IncidentScript?,
        diagnosisQuiz: DiagnosisQuiz,
    ): String {
        return buildString {
            appendLine("第23章安全诊断报告")
            appendLine()
            appendLine("问题标题：Hello Android 学习中心安全体检")
            appendLine("packageName：${identity.packageName}")
            appendLine("uid：${identity.uid}")
            appendLine("processName：${identity.processName}")
            appendLine("targetSdk：${identity.targetSdk}")
            appendLine("buildProfile：${identity.buildProfile}")
            appendLine("签名证书 SHA-256：${identity.signatureSha256}")
            appendLine("安装来源：${identity.installer}")
            appendLine("签名实验：${signatureExperiment.selectedScenario}")
            appendLine("签名结论：${signatureExperiment.verdict}")
            appendLine("debug 证书 SHA-256：${signatureExperiment.debugSha256}")
            appendLine("release 证书 SHA-256：${signatureExperiment.releaseSha256}")
            appendLine("签名对比结论：${signatureExperiment.comparisonVerdict}")
            appendLine()
            appendLine("权限状态：")
            permissions.forEach {
                appendLine("- ${it.label}：declared=${it.declaredInManifest}, granted=${it.runtimeGranted}")
            }
            appendLine()
            appendLine("AppOps：")
            appendLine("op/mode：${appOpsExperiment.selectedOp} / ${appOpsExperiment.selectedMode}")
            appendLine("解析 mode：${appOpsExperiment.parsedMode}")
            appendLine("结论：${appOpsExperiment.verdict}")
            appendLine("证据：${appOpsExperiment.evidenceCommand}")
            appendLine()
            appendLine("Photo Picker：")
            appendLine("pickedUri：${photoAccessExperiment.pickedUri}")
            appendLine("结论：${photoAccessExperiment.verdict}")
            appendLine()
            appendLine("PendingIntent：")
            appendLine("场景：${pendingIntentExperiment.selectedCase}")
            appendLine("风险：${pendingIntentExperiment.riskLevel}")
            appendLine("结论：${pendingIntentExperiment.verdict}")
            appendLine()
            appendLine("Keystore：")
            appendLine("alias：${keystore.alias}")
            appendLine("status：${keystore.status}")
            appendLine("lastError：${keystore.lastError}")
            appendLine()
            appendLine("组件边界：")
            appendLine("DebugPanelActivity：exported=true + signature permission")
            appendLine("FileProvider：exported=false + grantUriPermissions=true + paths=share/")
            appendLine("shareUri：$shareUri")
            appendLine("safExportUri：$safExportUri")
            appendLine()
            appendLine("事故剧本：")
            appendLine("选中剧本：${selectedIncident?.title ?: "尚未选择"}")
            appendLine("正确诊断：${selectedIncident?.correctDiagnosis ?: "先选择一个事故剧本，再补充根因判断。"}")
            appendLine("答题得分：${diagnosisQuiz.score}/3")
            appendLine("选择的问题类型：${diagnosisQuiz.selectedCategory}")
            appendLine("选择的第一证据：${diagnosisQuiz.selectedEvidence}")
            appendLine("选择的修复动作：${diagnosisQuiz.selectedFix}")
            appendLine("答题反馈：${diagnosisQuiz.feedback}")
            appendLine("自由报告得分：${diagnosisQuiz.freeTextScore}/4")
            appendLine("自由报告反馈：${diagnosisQuiz.freeTextFeedback}")
            appendLine("自由报告：${diagnosisQuiz.freeTextReport.ifBlank { "尚未填写" }}")
            appendLine()
            appendLine("修复原则：")
            appendLine("1. 先确认调用方身份，再判断授权来源。")
            appendLine("2. runtime granted 后继续检查 AppOps、设置页和业务系统服务。")
            appendLine("3. 内部组件默认不暴露，必须暴露时使用 signature permission 和参数校验。")
            appendLine("4. 敏感数据保存密文，日志和导出文件必须脱敏。")
            appendLine("5. 每个修复都要留下可回归证据。")
        }
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    private fun currentProcessName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            app.packageName
        }
    }

    private fun android.content.pm.PackageInfo.signatureDigest(): String {
        val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            this.signatures ?: emptyArray()
        }
        return signatures.firstOrNull()?.toByteArray()?.sha256() ?: "unknown"
    }

    private fun ByteArray.sha256(): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(this)
            .joinToString(":") { "%02X".format(it) }
    }

    private fun ByteArray.toHexPreview(): String {
        return joinToString("") { "%02x".format(it) }.let { value ->
            if (value.length <= 48) value else value.take(48) + "..."
        }
    }

    private data class EncryptedPayload(
        val iv: ByteArray,
        val cipherText: ByteArray,
    )

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_GCM = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
