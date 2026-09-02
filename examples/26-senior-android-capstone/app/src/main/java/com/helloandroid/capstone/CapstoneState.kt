package com.helloandroid.capstone

enum class CapstonePhase(val label: String, val goal: String) {
    V1("V1 主路径可运行", "课程首页、章节详情、学习进度形成闭环"),
    V2("V2 数据与任务可观察", "收藏、笔记、离线任务和后台同步能被观察"),
    V3("V3 架构与链路可解释", "模块边界、数据流和 Framework 因果链能讲清楚"),
    V4("V4 事故可诊断", "卡顿、后台失败、资源异常和权限拒绝有证据链"),
    V5("V5 发布可治理", "发布门禁、风险分级、监控基线和答辩材料完整"),
}

enum class TaskStatus(val label: String) {
    NotStarted("未开始"),
    Doing("进行中"),
    NeedEvidence("需要补证据"),
    Done("已完成"),
}

enum class RiskLevel(val label: String) {
    P0("P0 阻塞"),
    P1("P1 高风险"),
    P2("P2 可控风险"),
    P3("P3 观察项"),
}

data class CapstoneTask(
    val id: String,
    val title: String,
    val chapterMapping: String,
    val output: String,
    val status: TaskStatus,
)

data class CourseChapter(
    val id: String,
    val title: String,
    val stage: String,
    val progress: Int,
    val note: String,
    val offlineState: String,
)

data class ReviewStage(
    val id: String,
    val title: String,
    val chapters: String,
    val keyQuestion: String,
    val handsOnGoal: String,
    val proof: String,
)

data class PracticeMission(
    val id: String,
    val title: String,
    val reviewTarget: String,
    val action: String,
    val proof: String,
    val status: TaskStatus,
)

data class RecallQuestion(
    val question: String,
    val answerHint: String,
)

data class ChallengeStep(
    val title: String,
    val goal: String,
    val done: Boolean,
    val evidence: String,
)

data class EngineeringSnapshot(
    val appId: String,
    val versionName: String,
    val versionCode: Int,
    val buildProfile: String,
    val debug: Boolean,
    val sessionStartMs: Long,
)

data class AbilityRadar(
    val name: String,
    val score: Int,
    val evidence: String,
)

data class FrameworkNode(
    val name: String,
    val why: String,
    val evidence: String,
)

data class IncidentScenario(
    val id: String,
    val title: String,
    val symptom: String,
    val hiddenCause: String,
    val chapterMapping: String,
    val evidence: List<String>,
)

data class ReleaseGate(
    val title: String,
    val level: RiskLevel,
    val passed: Boolean,
    val evidence: String,
)

data class DefenseRubric(
    val level: String,
    val standard: String,
    val evidence: String,
)

data class OperationRecord(
    val timestampMs: Long,
    val title: String,
    val detail: String,
)

data class CapstoneState(
    val selectedPhase: CapstonePhase,
    val selectedReviewStageId: String,
    val selectedChapterId: String,
    val selectedScenarioId: String,
    val traceId: String,
    val tasks: List<CapstoneTask>,
    val reviewStages: List<ReviewStage>,
    val practiceMissions: List<PracticeMission>,
    val recallQuestions: List<RecallQuestion>,
    val engineeringSnapshot: EngineeringSnapshot,
    val chapters: List<CourseChapter>,
    val frameworkNodes: List<FrameworkNode>,
    val scenarios: List<IncidentScenario>,
    val releaseGates: List<ReleaseGate>,
    val defenseRubric: List<DefenseRubric>,
    val traceEvents: List<String>,
    val operationHistory: List<OperationRecord>,
    val notes: String,
) {
    val selectedReviewStage: ReviewStage
        get() = reviewStages.first { it.id == selectedReviewStageId }

    val selectedChapter: CourseChapter
        get() = chapters.first { it.id == selectedChapterId }

    val selectedScenario: IncidentScenario
        get() = scenarios.first { it.id == selectedScenarioId }

    val doneTaskCount: Int
        get() = tasks.count { it.status == TaskStatus.Done }

    val evidenceDebtCount: Int
        get() = tasks.count { it.status == TaskStatus.NeedEvidence } +
            practiceMissions.count { it.status == TaskStatus.NeedEvidence }

    val completedMissionCount: Int
        get() = practiceMissions.count { it.status == TaskStatus.Done }

    val completionScore: Int
        get() {
            val total = tasks.size + practiceMissions.size
            val done = doneTaskCount + completedMissionCount
            return ((done * 100) / total).coerceIn(0, 100)
        }

    val abilityScore: Int
        get() = dynamicAbilityRadar.map { it.score }.average().toInt()

    val challengeSteps: List<ChallengeStep>
        get() {
            val hasRunTrace = traceEvents.any { it.contains("BuildConfig profile") }
            val hasInjectedIncident = operationHistory.any { it.title == "注入事故" }
            val hasTouchedMission = operationHistory.any { it.title == "推进动手实验" }
            val hasTouchedGate = operationHistory.any { it.title == "切换发布门禁" }
            val hasGeneratedReport = operationHistory.any { it.title == "生成答辩报告" }
            val p0Fixed = releaseGates.none { !it.passed && it.level == RiskLevel.P0 }
            return listOf(
                ChallengeStep(
                    title = "1. 选定回顾路线",
                    goal = "选择课程回顾地图里的一个阶段，说清它训练什么能力。",
                    done = selectedReviewStageId.isNotBlank(),
                    evidence = selectedReviewStage.title,
                ),
                ChallengeStep(
                    title = "2. 跑一次真实点击链路",
                    goal = "点击重新追踪，让页面、Logcat 和 Trace section 出现同一条链路。",
                    done = hasRunTrace,
                    evidence = if (hasRunTrace) "已生成 BuildConfig + traceId 时间线。" else "还没有运行 CapstoneRunTrace。",
                ),
                ChallengeStep(
                    title = "3. 注入一次事故",
                    goal = "选择事故剧本，观察证据时间线和任务状态如何变化。",
                    done = hasInjectedIncident,
                    evidence = if (hasInjectedIncident) selectedScenario.title else "还没有注入事故证据。",
                ),
                ChallengeStep(
                    title = "4. 补一条动手证据",
                    goal = "推进一个跨章节动手实验，把知识点变成可验收输出。",
                    done = hasTouchedMission,
                    evidence = if (hasTouchedMission) "动手实验状态已被修改。" else "还没有手动推进动手实验。",
                ),
                ChallengeStep(
                    title = "5. 修复 P0 门禁",
                    goal = "处理 mapping / symbols 与签名证据，让发布结论不再被 P0 阻塞。",
                    done = p0Fixed && hasTouchedGate,
                    evidence = if (p0Fixed) "P0 门禁已清理。" else "仍有 P0 证据缺口。",
                ),
                ChallengeStep(
                    title = "6. 生成答辩材料",
                    goal = "复制或分享毕业报告，并能解释最低能力项和下一步补强。",
                    done = hasGeneratedReport,
                    evidence = if (hasGeneratedReport) "报告包含工程快照、任务状态、事故、门禁、Rubric 和操作记录。" else "还没有复制或分享毕业报告。",
                ),
            )
        }

    val challengeCompletion: Int
        get() = (challengeSteps.count { it.done } * 100 / challengeSteps.size).coerceIn(0, 100)

    val dynamicAbilityRadar: List<AbilityRadar>
        get() {
            val p0Fixed = releaseGates.none { !it.passed && it.level == RiskLevel.P0 }
            return listOf(
                AbilityRadar(
                    name = "功能交付",
                    score = scoreByStatus("main-path", "local-state", "mission-ui", "mission-data"),
                    evidence = "由主路径、本地状态、课程状态修改和后台等待任务共同计算。",
                ),
                AbilityRadar(
                    name = "架构设计",
                    score = scoreByStatus("architecture"),
                    evidence = "由模块架构任务状态计算，重点看边界是否能被解释和验收。",
                ),
                AbilityRadar(
                    name = scoreNameWithGate("Framework 理解", "mission-framework"),
                    score = scoreByStatus("framework", "mission-framework", bonus = if (traceEvents.size >= 6) 8 else 0),
                    evidence = "由 Framework 任务、点击因果链任务和 traceId 时间线共同计算。",
                ),
                AbilityRadar(
                    name = "诊断能力",
                    score = scoreByStatus("incident", "background", "security", "mission-resource", "mission-security"),
                    evidence = "由事故诊断、后台存储、安全检查、资源错乱和权限降级任务共同计算。",
                ),
                AbilityRadar(
                    name = "工程治理",
                    score = (scoreByStatus("governance", "mission-release") + releaseGateScore()) / 2,
                    evidence = "由发版治理任务、P0 修复任务和发布门禁通过情况共同计算。",
                ),
                AbilityRadar(
                    name = "表达答辩",
                    score = scoreByStatus("defense", bonus = if (p0Fixed) 8 else 0),
                    evidence = "由终章答辩任务和 P0 证据缺口是否清理共同计算。",
                ),
            )
        }

    val hasBlockingGate: Boolean
        get() = releaseGates.any { !it.passed && it.level == RiskLevel.P0 }

    val graduationDecision: String
        get() = when {
            hasBlockingGate -> "暂缓毕业：还有 P0 证据缺口"
            evidenceDebtCount > 0 -> "可以答辩预演：但需要补齐证据"
            completionScore >= 90 && abilityScore >= 85 -> "可以毕业答辩"
            else -> "继续完善毕业项目"
        }
}

private fun CapstoneState.scoreByStatus(vararg ids: String, bonus: Int = 0): Int {
    val statuses = ids.mapNotNull { id ->
        tasks.firstOrNull { it.id == id }?.status
            ?: practiceMissions.firstOrNull { it.id == id }?.status
    }
    if (statuses.isEmpty()) return (50 + bonus).coerceIn(0, 100)
    val average = statuses.map { it.scoreWeight }.average()
    return (45 + average * 50 + bonus).toInt().coerceIn(0, 100)
}

private fun CapstoneState.releaseGateScore(): Int {
    if (releaseGates.isEmpty()) return 50
    val average = releaseGates.map { gate ->
        when {
            gate.passed -> 1.0
            gate.level == RiskLevel.P0 -> 0.1
            gate.level == RiskLevel.P1 -> 0.45
            gate.level == RiskLevel.P2 -> 0.65
            else -> 0.8
        }
    }.average()
    return (40 + average * 60).toInt().coerceIn(0, 100)
}

private fun CapstoneState.scoreNameWithGate(name: String, missionId: String): String {
    val mission = practiceMissions.firstOrNull { it.id == missionId }
    return if (mission?.status == TaskStatus.NeedEvidence) "$name · 需补证据" else name
}

private val TaskStatus.scoreWeight: Double
    get() = when (this) {
        TaskStatus.NotStarted -> 0.0
        TaskStatus.Doing -> 0.45
        TaskStatus.NeedEvidence -> 0.6
        TaskStatus.Done -> 1.0
    }
