package com.helloandroid.capstone

import android.os.SystemClock
import android.os.Trace
import android.util.Log

class CapstoneStore {
    fun initialState(): CapstoneState {
        return CapstoneState(
            selectedPhase = CapstonePhase.V1,
            selectedReviewStageId = "foundation",
            selectedChapterId = "chapter-14",
            selectedScenarioId = "detail-jank",
            traceId = "course-open-26-001",
            tasks = tasks(),
            reviewStages = reviewStages(),
            practiceMissions = practiceMissions(),
            recallQuestions = recallQuestions(),
            engineeringSnapshot = engineeringSnapshot(),
            chapters = chapters(),
            frameworkNodes = frameworkNodes(),
            scenarios = scenarios(),
            releaseGates = releaseGates(),
            defenseRubric = defenseRubric(),
            traceEvents = defaultTrace(),
            operationHistory = listOf(
                OperationRecord(
                    timestampMs = 0L,
                    title = "创建工程快照",
                    detail = "appId=${BuildConfig.APPLICATION_ID}, profile=${BuildConfig.BUILD_PROFILE}",
                ),
            ),
            notes = "先选择一个阶段，再跑通课程主路径；终章 Demo 的关键不是功能多，而是每条链路都能被解释和证明。",
        )
    }

    fun selectPhase(current: CapstoneState, phase: CapstonePhase): CapstoneState {
        return current.copy(
            selectedPhase = phase,
            notes = "已切换到 ${phase.label}：${phase.goal}",
        ).withRecord("切换毕业阶段", "${phase.label}：${phase.goal}")
    }

    fun selectReviewStage(current: CapstoneState, stageId: String): CapstoneState {
        val stage = current.reviewStages.first { it.id == stageId }
        return current.copy(
            selectedReviewStageId = stageId,
            notes = "正在回顾 ${stage.title}。先回答关键问题，再动手完成对应证据。",
        ).withRecord("选择回顾阶段", "${stage.title}｜${stage.chapters}")
    }

    fun selectChapter(current: CapstoneState, chapterId: String): CapstoneState {
        val chapter = current.chapters.first { it.id == chapterId }
        return current.copy(
            selectedChapterId = chapterId,
            traceId = "course-open-${chapter.id.takeLast(2)}-001",
            traceEvents = listOf(
                "[trace] click ${chapter.title}",
                "[ui] CourseCard.onClick -> CourseDetailRoute",
                "[domain] ObserveCourseDetailUseCase(${chapter.id})",
                "[data] local cache hit, progress=${chapter.progress}%",
                "[render] content frame scheduled by Choreographer",
            ),
            notes = "已打开 ${chapter.title}。现在可以观察它如何从课程卡片进入 Framework 因果链。",
        ).withRecord("打开课程章节", "${chapter.title}｜traceId=course-open-${chapter.id.takeLast(2)}-001")
    }

    fun selectScenario(current: CapstoneState, scenarioId: String): CapstoneState {
        val scenario = current.scenarios.first { it.id == scenarioId }
        return current.copy(
            selectedScenarioId = scenarioId,
            notes = "已选择事故剧本：${scenario.title}。请根据现象、证据和章节映射判断根因。",
        ).withRecord("选择事故剧本", "${scenario.title}｜${scenario.chapterMapping}")
    }

    fun toggleTask(current: CapstoneState, taskId: String): CapstoneState {
        val updated = current.tasks.map { task ->
            if (task.id == taskId) task.copy(status = task.status.next()) else task
        }
        val task = updated.first { it.id == taskId }
        return current.copy(
            tasks = updated,
            notes = "任务「${task.title}」当前状态：${task.status.label}。",
        ).withRecord("推进终章任务", "${task.title} -> ${task.status.label}")
    }

    fun toggleMission(current: CapstoneState, missionId: String): CapstoneState {
        val updated = current.practiceMissions.map { mission ->
            if (mission.id == missionId) mission.copy(status = mission.status.next()) else mission
        }
        val mission = updated.first { it.id == missionId }
        return current.copy(
            practiceMissions = updated,
            notes = "动手任务「${mission.title}」当前状态：${mission.status.label}。",
        ).withRecord("推进动手实验", "${mission.title} -> ${mission.status.label}")
    }

    fun toggleReleaseGate(current: CapstoneState, title: String): CapstoneState {
        val updated = current.releaseGates.map { gate ->
            if (gate.title == title) gate.copy(passed = !gate.passed) else gate
        }
        val gate = updated.first { it.title == title }
        val action = if (gate.passed) "已补齐证据" else "重新标记为未通过"
        return current.copy(
            releaseGates = updated,
            notes = "发布门禁「${gate.title}」$action。请观察毕业结论是否变化。",
        ).withRecord("切换发布门禁", "${gate.level.label}｜${gate.title} -> ${if (gate.passed) "通过" else "未通过"}")
    }

    fun runTrace(current: CapstoneState): CapstoneState {
        val chapter = current.selectedChapter
        val start = SystemClock.elapsedRealtime()
        Trace.beginSection("CapstoneRunTrace")
        return try {
            Log.i(TAG, "runTrace traceId=${current.traceId}, chapter=${chapter.id}")
            current.copy(
                traceEvents = listOf(
                    "[${current.traceId}] t+0ms BuildConfig profile=${BuildConfig.BUILD_PROFILE}",
                    "[${current.traceId}] t+${elapsed(start)}ms InputDispatcher found target window",
                    "[${current.traceId}] t+${elapsed(start)}ms ViewRootImpl delivered MotionEvent",
                    "[${current.traceId}] t+${elapsed(start)}ms Compose clickable emitted CourseIntent.OpenDetail",
                    "[${current.traceId}] t+${elapsed(start)}ms ViewModel requested ${chapter.title}",
                    "[${current.traceId}] t+${elapsed(start)}ms Repository returned local snapshot",
                    "[${current.traceId}] t+${elapsed(start)}ms Choreographer scheduled detail frame",
                    "[${current.traceId}] t+${elapsed(start)}ms RenderThread submitted buffer to SurfaceFlinger",
                ),
                notes = "已重新生成一次点击链路，并写入 Logcat 与 Trace section。注意观察每个节点的因果：它为什么存在，它能证明什么。",
            ).withRecord("追踪点击链路", "${chapter.title}｜section=CapstoneRunTrace")
        } finally {
            Trace.endSection()
        }
    }

    fun simulateIncident(current: CapstoneState): CapstoneState {
        val scenario = current.selectedScenario
        Trace.beginSection("CapstoneIncident:${scenario.id}")
        return try {
            Log.w(TAG, "simulateIncident id=${scenario.id}, symptom=${scenario.symptom}")
            current.copy(
                tasks = promoteTasksForScenario(current.tasks, scenario.id),
                practiceMissions = promoteMissionsForScenario(current.practiceMissions, scenario.id),
                traceEvents = scenario.evidence.withSessionTime(),
                notes = "事故已注入：${scenario.symptom}。相关任务已自动推进，请先找证据，不要直接猜根因。",
            ).withRecord("注入事故", "${scenario.title}｜section=CapstoneIncident:${scenario.id}")
        } finally {
            Trace.endSection()
        }
    }

    fun markReportGenerated(current: CapstoneState, channel: String): CapstoneState {
        return current.copy(
            notes = "毕业报告已通过$channel生成。现在请对照 Rubric，说明自己能被追问到哪一层。",
        ).withRecord("生成答辩报告", "channel=$channel｜reportReady=true")
    }

    fun generatedReport(state: CapstoneState): String {
        val taskSummary = state.tasks.joinToString("\n") {
            "- ${it.status.label}｜${it.title}｜输出：${it.output}"
        }
        val missionSummary = state.practiceMissions.joinToString("\n") {
            "- ${it.status.label}｜${it.title}｜证据：${it.proof}"
        }
        val abilitySummary = state.dynamicAbilityRadar.joinToString("\n") {
            "- ${it.name}：${it.score} 分｜${it.evidence}"
        }
        val gates = state.releaseGates.joinToString("\n") {
            "- ${it.level.label}｜${it.title}｜${if (it.passed) "通过" else "未通过"}｜${it.evidence}"
        }
        val weakestAbilities = state.dynamicAbilityRadar
            .sortedBy { it.score }
            .take(2)
            .joinToString("\n") {
                "- ${it.name}：${it.score} 分｜优先补强：${it.evidence}"
            }
        val failedGates = state.releaseGates
            .filterNot { it.passed }
            .joinToString("\n") {
                "- ${it.level.label}｜${it.title}｜${it.evidence}"
            }
            .ifBlank { "- 当前没有未通过门禁，可以进入答辩预演。" }
        val defenseAdvice = defenseAdvice(state)
        val operationHistory = state.operationHistory.joinToString("\n") {
            "- t+${it.timestampMs}ms｜${it.title}｜${it.detail}"
        }
        val challengeSummary = state.challengeSteps.joinToString("\n") {
            "- ${if (it.done) "通过" else "待完成"}｜${it.title}｜${it.evidence}"
        }

        return """
            # Hello Android Capstone 毕业报告

            ## 当前结论
            ${state.graduationDecision}

            ## 项目阶段
            ${state.selectedPhase.label}
            ${state.selectedPhase.goal}

            ## 工程快照
            appId：${state.engineeringSnapshot.appId}
            version：${state.engineeringSnapshot.versionName}(${state.engineeringSnapshot.versionCode})
            buildProfile：${state.engineeringSnapshot.buildProfile}
            debug：${state.engineeringSnapshot.debug}
            sessionStartMs：${state.engineeringSnapshot.sessionStartMs}

            ## 当前回顾阶段
            ${state.selectedReviewStage.title}
            章节范围：${state.selectedReviewStage.chapters}
            关键问题：${state.selectedReviewStage.keyQuestion}
            动手目标：${state.selectedReviewStage.handsOnGoal}

            ## 当前课程主路径
            ${state.selectedChapter.title}
            ${state.selectedChapter.note}

            ## 任务板
            $taskSummary

            ## 毕业挑战模式
            完成度：${state.challengeCompletion}%
            $challengeSummary

            ## 动手回顾任务
            $missionSummary

            ## 能力雷达
            $abilitySummary

            ## 事故剧本
            ${state.selectedScenario.title}
            现象：${state.selectedScenario.symptom}
            隐藏根因：${state.selectedScenario.hiddenCause}
            映射章节：${state.selectedScenario.chapterMapping}

            ## 发布门禁
            $gates

            ## 当前最需要补强
            $weakestAbilities

            ## 未通过门禁
            $failedGates

            ## 答辩建议
            $defenseAdvice

            ## 操作记录
            $operationHistory

            ## 答辩 Rubric
            ${state.defenseRubric.joinToString("\n") { "- ${it.level}：${it.standard}｜证据：${it.evidence}" }}

            ## 答辩提醒
            请用 10 到 15 分钟讲清：项目目标、主路径、架构边界、Framework 因果链、一次事故诊断、一次发版治理和后续演进。
        """.trimIndent()
    }

    private fun defenseAdvice(state: CapstoneState): String {
        val lowest = state.dynamicAbilityRadar.minBy { it.score }
        val p0Gates = state.releaseGates.filter { !it.passed && it.level == RiskLevel.P0 }
        val gateAdvice = if (p0Gates.isNotEmpty()) {
            "先处理 P0：${p0Gates.joinToString { it.title }}。答辩时要说明为什么它们阻塞发布，以及修复后如何证明。"
        } else {
            "P0 已清理。答辩时可以把重点放在架构取舍、事故诊断和后续演进。"
        }
        return """
            - 当前最低能力项：${lowest.name}，分数 ${lowest.score}。
            - 当前事故剧本：${state.selectedScenario.title}，建议按“现象 -> 时间线 -> 证据 -> 假设 -> 反证 -> 根因 -> 回归”讲。
            - 当前回顾阶段：${state.selectedReviewStage.title}，请先回答：${state.selectedReviewStage.keyQuestion}
            - $gateAdvice
        """.trimIndent()
    }

    private fun TaskStatus.next(): TaskStatus {
        return when (this) {
            TaskStatus.NotStarted -> TaskStatus.Doing
            TaskStatus.Doing -> TaskStatus.NeedEvidence
            TaskStatus.NeedEvidence -> TaskStatus.Done
            TaskStatus.Done -> TaskStatus.NotStarted
        }
    }

    private fun CapstoneState.withRecord(title: String, detail: String): CapstoneState {
        val record = OperationRecord(
            timestampMs = SystemClock.elapsedRealtime() - engineeringSnapshot.sessionStartMs,
            title = title,
            detail = detail,
        )
        return copy(operationHistory = (listOf(record) + operationHistory).take(MAX_OPERATION_RECORDS))
    }

    private fun engineeringSnapshot(): EngineeringSnapshot {
        return EngineeringSnapshot(
            appId = BuildConfig.APPLICATION_ID,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            buildProfile = BuildConfig.BUILD_PROFILE,
            debug = BuildConfig.DEBUG,
            sessionStartMs = SystemClock.elapsedRealtime(),
        )
    }

    private fun promoteTasksForScenario(tasks: List<CapstoneTask>, scenarioId: String): List<CapstoneTask> {
        val ids = when (scenarioId) {
            "detail-jank" -> setOf("incident", "framework")
            "background-lost" -> setOf("background", "incident")
            "release-blocked", "signing-mismatch" -> setOf("governance", "security")
            "resource-theme", "classloader-missing" -> setOf("framework", "incident")
            "permission-storage" -> setOf("security", "background")
            else -> emptySet()
        }
        return tasks.map { task ->
            if (task.id in ids) {
                task.copy(status = task.status.promoteByIncident())
            } else {
                task
            }
        }
    }

    private fun promoteMissionsForScenario(missions: List<PracticeMission>, scenarioId: String): List<PracticeMission> {
        val ids = when (scenarioId) {
            "detail-jank" -> setOf("mission-framework")
            "background-lost" -> setOf("mission-data")
            "resource-theme" -> setOf("mission-resource")
            "permission-storage" -> setOf("mission-security")
            "release-blocked", "signing-mismatch", "classloader-missing" -> setOf("mission-release")
            else -> emptySet()
        }
        return missions.map { mission ->
            if (mission.id in ids) {
                mission.copy(status = mission.status.promoteByIncident())
            } else {
                mission
            }
        }
    }

    private fun TaskStatus.promoteByIncident(): TaskStatus {
        return when (this) {
            TaskStatus.NotStarted -> TaskStatus.Doing
            TaskStatus.Doing -> TaskStatus.NeedEvidence
            TaskStatus.NeedEvidence -> TaskStatus.NeedEvidence
            TaskStatus.Done -> TaskStatus.Done
        }
    }

    private fun List<String>.withSessionTime(): List<String> {
        val start = SystemClock.elapsedRealtime()
        return mapIndexed { index, event ->
            "[session+${elapsed(start) + index}ms] $event"
        }
    }

    private fun elapsed(startMs: Long): Long {
        return SystemClock.elapsedRealtime() - startMs
    }

    private fun reviewStages(): List<ReviewStage> {
        return listOf(
            ReviewStage(
                id = "foundation",
                title = "应用基础回顾",
                chapters = "第 1 到 3 章",
                keyQuestion = "一个 Android App 如何从能运行，走到能表达页面、状态和导航？",
                handsOnGoal = "在主路径中选一章、改一个状态、观察 UI 如何更新。",
                proof = "能解释 Activity、Compose 状态和导航之间的关系。",
            ),
            ReviewStage(
                id = "data",
                title = "数据与异步回顾",
                chapters = "第 4 到 6 章",
                keyQuestion = "数据如何从网络、本地存储和后台任务流向 UI？",
                handsOnGoal = "模拟离线任务等待、失败和恢复，观察 traceId 如何串起过程。",
                proof = "能说明 Repository、UseCase、协程、Flow、WorkManager 各自负责什么。",
            ),
            ReviewStage(
                id = "architecture",
                title = "架构与工程化回顾",
                chapters = "第 5、7、8、25 章",
                keyQuestion = "为什么大项目不能只靠目录分层，而要靠依赖方向和治理规则？",
                handsOnGoal = "检查任务板里的架构任务，把状态切到需要补证据或已完成。",
                proof = "能画出 feature / domain / data / core 的边界，并指出一次越界风险。",
            ),
            ReviewStage(
                id = "framework",
                title = "Framework 主线回顾",
                chapters = "第 10 到 18 章",
                keyQuestion = "一次点击如何穿过 Input、Activity、Window、渲染、资源和代码加载链路？",
                handsOnGoal = "点击重新追踪链路，并用因果句式解释每个节点。",
                proof = "能把至少一条用户操作追到 Framework 关键类和系统服务。",
            ),
            ReviewStage(
                id = "system",
                title = "系统诊断回顾",
                chapters = "第 19 到 24 章",
                keyQuestion = "进程、后台、存储、安全和观测工具如何共同决定事故诊断质量？",
                handsOnGoal = "选择一个事故剧本，注入证据，再判断根因和修复方向。",
                proof = "能写出现象、时间线、证据、假设、反证、根因和回归。",
            ),
            ReviewStage(
                id = "capstone",
                title = "终章答辩回顾",
                chapters = "第 26 章",
                keyQuestion = "如何证明自己具备资深 Android 工程师的完整交付能力？",
                handsOnGoal = "补齐任务板、能力雷达和发布门禁，生成毕业报告。",
                proof = "能用 10 到 15 分钟讲清目标、主路径、架构、Framework、诊断和治理。",
            ),
        )
    }

    private fun practiceMissions(): List<PracticeMission> {
        return listOf(
            PracticeMission(
                id = "mission-ui",
                title = "改一个课程状态",
                reviewTarget = "第 1 到 3 章",
                action = "切换课程主路径中的章节，观察进度、缓存状态和 traceId 变化。",
                proof = "能说明 UI 状态为什么会变化。",
                status = TaskStatus.Done,
            ),
            PracticeMission(
                id = "mission-data",
                title = "模拟一次后台等待",
                reviewTarget = "第 4、6、21、22 章",
                action = "选择离线同步事故，注入 WorkManager 约束证据。",
                proof = "能区分任务没入队、约束不满足和执行失败。",
                status = TaskStatus.Doing,
            ),
            PracticeMission(
                id = "mission-framework",
                title = "讲一次点击的系统因果",
                reviewTarget = "第 10 到 15 章",
                action = "重新追踪点击链路，用“因为...所以...”解释节点。",
                proof = "能讲清 InputDispatcher、ViewRootImpl、Choreographer 的存在理由。",
                status = TaskStatus.NeedEvidence,
            ),
            PracticeMission(
                id = "mission-resource",
                title = "补一个资源错乱剧本",
                reviewTarget = "第 17 章",
                action = "新增资源主题错乱事故，说明 R、AAPT、Theme 和 Configuration 如何参与。",
                proof = "能说明资源不是按文件名随便读取，而是按编译表和配置匹配。",
                status = TaskStatus.NotStarted,
            ),
            PracticeMission(
                id = "mission-security",
                title = "补一次权限降级体验",
                reviewTarget = "第 22、23 章",
                action = "设计权限拒绝后仍能完成核心路径的降级方案。",
                proof = "能说明权限、AppOps、存储边界和用户体验之间的取舍。",
                status = TaskStatus.NeedEvidence,
            ),
            PracticeMission(
                id = "mission-release",
                title = "修复一个 P0 门禁",
                reviewTarget = "第 20、24、25 章",
                action = "解释 mapping 缺失为什么阻塞发布，并写出修复与回归动作。",
                proof = "能证明线上 crash 可以被还原、定位和复盘。",
                status = TaskStatus.Doing,
            ),
        )
    }

    private fun recallQuestions(): List<RecallQuestion> {
        return listOf(
            RecallQuestion(
                question = "如果用户说详情页偶尔卡住，你先看业务日志、Perfetto 还是代码？为什么？",
                answerHint = "先用 traceId 定时间点，再用 Perfetto 判断主线程、Binder、I/O 和帧。",
            ),
            RecallQuestion(
                question = "为什么 Compose Navigation 不等于可以忘记 ActivityThread？",
                answerHint = "页面栈可以在应用层组织，但 App 启动、生命周期、窗口接入仍依赖系统调度。",
            ),
            RecallQuestion(
                question = "为什么 mapping 缺失时，即使功能测试通过也不能发布？",
                answerHint = "因为线上 crash 无法还原混淆堆栈，事故不可诊断就是 P0 治理缺口。",
            ),
        )
    }

    private fun defenseRubric(): List<DefenseRubric> {
        return listOf(
            DefenseRubric("60 分", "主路径可以运行", "能演示首页、章节、详情和进度变化。"),
            DefenseRubric("70 分", "架构可以解释", "能说明 feature / domain / data / core 的职责和依赖方向。"),
            DefenseRubric("80 分", "Framework 因果链可以讲清", "能把一次点击追到 Input、ViewRootImpl、ActivityThread 和 Choreographer。"),
            DefenseRubric("90 分", "事故诊断和发布门禁形成闭环", "能用证据解释事故，并判断 P0 / P1 是否阻塞发布。"),
            DefenseRubric("95 分以上", "具备持续演进和治理意识", "能提出 CI 门禁、真实 Perfetto、Baseline Profile、DataStore 持久化等后续方案。"),
        )
    }

    private fun tasks(): List<CapstoneTask> {
        return listOf(
            CapstoneTask("main-path", "跑通课程主路径", "第 1 到 4 章", "首页 -> 详情 -> 进度", TaskStatus.Done),
            CapstoneTask("local-state", "接入本地状态", "第 4、6、22 章", "收藏、笔记、进度、离线任务", TaskStatus.Done),
            CapstoneTask("architecture", "讲清模块架构", "第 5、7、25 章", "feature / domain / data / core 边界图", TaskStatus.Doing),
            CapstoneTask("framework", "追踪 Framework 因果链", "第 10 到 15 章", "Input、ActivityThread、Window、Choreographer、RenderThread", TaskStatus.NeedEvidence),
            CapstoneTask("incident", "完成一次事故诊断", "第 19、20、24 章", "现象、证据、假设、根因、回归", TaskStatus.Doing),
            CapstoneTask("background", "观察后台与存储", "第 21、22 章", "WorkManager、约束、URI、私有存储", TaskStatus.NotStarted),
            CapstoneTask("security", "完成安全检查", "第 23 章", "权限、AppOps、签名、组件暴露", TaskStatus.NeedEvidence),
            CapstoneTask("governance", "完成发版治理", "第 25 章", "P0/P1/P2/P3、灰度、回滚、监控", TaskStatus.Doing),
            CapstoneTask("defense", "准备终章答辩", "第 26 章", "10 到 15 分钟答辩大纲", TaskStatus.NotStarted),
        )
    }

    private fun chapters(): List<CourseChapter> {
        return listOf(
            CourseChapter("chapter-14", "第14章 Input 事件分发", "Framework 主线", 82, "用一次点击解释 InputReader、InputDispatcher 和 ViewRootImpl。", "已缓存"),
            CourseChapter("chapter-17", "第17章 资源系统", "Framework 主线", 76, "用主题切换和资源冲突解释 R、AAPT、resources.arsc 和 Resources。", "等待下载"),
            CourseChapter("chapter-20", "第20章 稳定性诊断", "系统稳定性", 88, "用 ANR、Crash、DropBox、bugreport 训练事故证据链。", "已缓存"),
            CourseChapter("chapter-24", "第24章 系统观测工具", "系统工程", 71, "用 Perfetto、dumpsys、gfxinfo、meminfo 把感觉变成证据。", "同步中"),
            CourseChapter("chapter-25", "第25章 大型工程治理", "工程治理", 64, "用模块边界、发布门禁、监控基线和复盘机制收束工程能力。", "未缓存"),
        )
    }

    private fun frameworkNodes(): List<FrameworkNode> {
        return listOf(
            FrameworkNode("InputDispatcher", "找到当前触摸事件应该交给哪个窗口。", "点击日志 + 窗口焦点状态。"),
            FrameworkNode("ViewRootImpl", "把系统输入和 App 侧 View / Compose 树接起来。", "MotionEvent 到达 App 主线程。"),
            FrameworkNode("ActivityThread", "承载 Activity 生命周期和主线程消息调度。", "页面创建、恢复、停止日志。"),
            FrameworkNode("Choreographer", "把 UI 状态变化安排到下一帧。", "doFrame、frame cost、掉帧记录。"),
            FrameworkNode("RenderThread", "把绘制命令提交给渲染管线。", "渲染耗时、Buffer 提交时间。"),
            FrameworkNode("SurfaceFlinger", "把多个 Surface 合成到屏幕。", "Perfetto SurfaceFlinger timeline。"),
        )
    }

    private fun scenarios(): List<IncidentScenario> {
        return listOf(
            IncidentScenario(
                id = "detail-jank",
                title = "课程详情打开卡顿",
                symptom = "点击第 20 章详情后 loading 停留 1.3 秒。",
                hiddenCause = "主线程同步解析大段本地 JSON，导致连续忙碌超过 1 秒。",
                chapterMapping = "第 6、15、20、24、26 章",
                evidence = listOf(
                    "[course-open-20] 10:01:12.120 click course card",
                    "[course-open-20] 10:01:12.146 local json parse start on main",
                    "[course-open-20] 10:01:13.420 local json parse end",
                    "[gfxinfo] longest frame = 96ms",
                    "[perfetto] main thread busy, no Binder wait",
                    "[fix] move parse to Dispatchers.Default, content visible = 240ms",
                ),
            ),
            IncidentScenario(
                id = "background-lost",
                title = "离线同步迟迟不执行",
                symptom = "章节加入离线队列后，任务一直停留在等待中。",
                hiddenCause = "WorkManager 约束要求 unmetered network，但当前设备处于移动网络。",
                chapterMapping = "第 6、21、22、24、26 章",
                evidence = listOf(
                    "[offline-sync-17] enqueue WorkRequest",
                    "[work] state=ENQUEUED, constraint=UNMETERED",
                    "[dumpsys jobscheduler] connectivity constraint not satisfied",
                    "[ui] show waiting for Wi-Fi",
                    "[fix] expose constraint reason and allow user choose mobile network",
                ),
            ),
            IncidentScenario(
                id = "release-blocked",
                title = "release 准备发布但被门禁拦截",
                symptom = "功能测试全部通过，治理工作台仍提示暂缓发布。",
                hiddenCause = "mapping 未归档，新增 exported Activity 缺少保护条件。",
                chapterMapping = "第 16、20、23、24、25、26 章",
                evidence = listOf(
                    "[release] assembleRelease success",
                    "[archive] mapping id = missing",
                    "[manifest-diff] exported Activity added without permission",
                    "[monitor] crash stack cannot be retraced",
                    "[decision] P0 blocks release regardless of health score",
                ),
            ),
            IncidentScenario(
                id = "resource-theme",
                title = "夜间主题资源错乱",
                symptom = "切到夜间模式后，课程详情标题颜色和按钮背景对比度异常。",
                hiddenCause = "feature-course 新增同名 theme attribute，资源合并后覆盖了 core-ui 的默认主题值。",
                chapterMapping = "第 16、17、23、24、26 章",
                evidence = listOf(
                    "[resource] selected config = night, density=440dpi, locale=zh-CN",
                    "[aapt] attr/courseTitleColor resolved from feature-course",
                    "[resources.arsc] entry id stable, value source changed after merge",
                    "[ui] contrast warning: title/button ratio below baseline",
                    "[fix] add module resource prefix and move shared token to core-ui",
                ),
            ),
            IncidentScenario(
                id = "permission-storage",
                title = "权限拒绝后离线导出失败",
                symptom = "用户拒绝照片权限后，导出学习报告入口直接不可用。",
                hiddenCause = "业务把导出能力错误绑定到媒体权限，没有提供 SAF 或系统分享降级路径。",
                chapterMapping = "第 21、22、23、24、26 章",
                evidence = listOf(
                    "[permission] READ_MEDIA_IMAGES denied by user",
                    "[appops] mode=ignored for media read",
                    "[storage] report export does not require direct media read",
                    "[ux] no fallback action visible",
                    "[fix] use ACTION_CREATE_DOCUMENT or ACTION_SEND for report export",
                ),
            ),
            IncidentScenario(
                id = "signing-mismatch",
                title = "签名证书指纹异常",
                symptom = "渠道包可以安装，但从旧版本升级失败。",
                hiddenCause = "release 产物签名证书与历史版本不一致，破坏升级链路和签名权限信任。",
                chapterMapping = "第 16、23、25、26 章",
                evidence = listOf(
                    "[apksigner] current SHA-256 = 9F:7C:11",
                    "[archive] expected SHA-256 = A1:B2:09",
                    "[package] INSTALL_FAILED_UPDATE_INCOMPATIBLE",
                    "[security] signature permission trust cannot be inherited",
                    "[fix] stop distribution and verify CI signing secret / Play App Signing",
                ),
            ),
            IncidentScenario(
                id = "classloader-missing",
                title = "动态模块类加载失败",
                symptom = "打开实验章节时提示插件不可用，日志出现 ClassNotFoundException。",
                hiddenCause = "动态能力依赖的实现类被 R8 移除，宿主 ClassLoader 只能找到接口找不到实现。",
                chapterMapping = "第 18、20、24、25、26 章",
                evidence = listOf(
                    "[route] open feature experiment/plugin-demo",
                    "[classloader] host PathClassLoader cannot find DynamicLessonEntry",
                    "[r8] usage.txt shows DynamicLessonEntry removed",
                    "[crash] ClassNotFoundException at PluginEntryFactory.create",
                    "[fix] add keep rule and move plugin contract to stable api module",
                ),
            ),
        )
    }

    private fun releaseGates(): List<ReleaseGate> {
        return listOf(
            ReleaseGate("release 构建可安装启动", RiskLevel.P3, true, "debug 与 release variant 均能启动。"),
            ReleaseGate("mapping / symbols 已归档", RiskLevel.P0, false, "mappingId=missing，线上 crash 无法还原。"),
            ReleaseGate("签名证书与历史版本一致", RiskLevel.P0, false, "当前产物 SHA-256 与构建档案不一致，升级链路不可证明。"),
            ReleaseGate("新增权限有拒绝路径", RiskLevel.P1, false, "POST_NOTIFICATIONS 拒绝态说明不完整。"),
            ReleaseGate("核心性能基线无退化", RiskLevel.P1, true, "详情打开 P95 仍在 300ms 内。"),
            ReleaseGate("灰度回滚策略明确", RiskLevel.P2, true, "1%、5%、20% 灰度窗口与停止条件已配置。"),
        )
    }

    private fun defaultTrace(): List<String> {
        return listOf(
            "[course-open-26-001] click course card",
            "[course-open-26-001] Compose clickable -> CourseIntent.OpenDetail",
            "[course-open-26-001] ViewModel -> ObserveCourseDetailUseCase",
            "[course-open-26-001] Repository -> local cache hit",
            "[course-open-26-001] UiState updated",
            "[course-open-26-001] Choreographer scheduled next frame",
        )
    }

    private companion object {
        const val TAG = "HelloCapstone"
        const val MAX_OPERATION_RECORDS = 12
    }
}
