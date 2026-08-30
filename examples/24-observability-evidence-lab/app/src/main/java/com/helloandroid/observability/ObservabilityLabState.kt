package com.helloandroid.observability

enum class EvidenceKind(val label: String) {
    Logcat("logcat"),
    Dumpsys("dumpsys"),
    Perfetto("Perfetto"),
    Bugreport("bugreport"),
    Gfxinfo("gfxinfo"),
    Meminfo("meminfo"),
    Procstats("procstats"),
    Simpleperf("simpleperf"),
}

enum class EvidenceArea(val label: String) {
    Trigger("现场触发"),
    Timeline("时间点"),
    State("系统状态"),
    Trace("时间线"),
    Performance("性能指标"),
    FullScene("完整现场"),
    Privacy("脱敏协作"),
    Quiz("证据链答题"),
    Report("证据链报告"),
}

data class EvidenceTask(
    val id: String,
    val title: String,
    val description: String,
)

data class IncidentScenario(
    val id: String,
    val title: String,
    val symptom: String,
    val firstQuestion: String,
    val rootCandidate: String,
    val evidenceIds: List<String>,
)

data class EvidenceItem(
    val id: String,
    val kind: EvidenceKind,
    val title: String,
    val command: String,
    val clue: String,
    val conclusion: String,
    val nextStep: String,
)

data class PerfMetric(
    val name: String,
    val value: String,
    val baseline: String,
    val status: String,
)

data class EvidenceQuizQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
)

data class ObservabilityLabState(
    val packageName: String,
    val activeTraceId: String,
    val selectedScenarioId: String,
    val selectedEvidenceId: String?,
    val tasks: List<EvidenceTask>,
    val completedTaskIds: Set<String>,
    val scenarios: List<IncidentScenario>,
    val evidenceItems: List<EvidenceItem>,
    val metrics: List<PerfMetric>,
    val quizQuestions: List<EvidenceQuizQuestion>,
    val selectedQuizAnswers: Map<String, String>,
    val quizSubmitted: Boolean,
    val lastAction: String,
) {
    val selectedScenario: IncidentScenario
        get() = scenarios.first { it.id == selectedScenarioId }

    val activeEvidence: List<EvidenceItem>
        get() = evidenceItems.filter { it.id in selectedScenario.evidenceIds }

    val selectedEvidence: EvidenceItem?
        get() = selectedEvidenceId?.let { id -> evidenceItems.firstOrNull { it.id == id } }

    val completedCount: Int
        get() = completedTaskIds.size

    val nextTask: EvidenceTask?
        get() = tasks.firstOrNull { it.id !in completedTaskIds }

    val quizScore: Int
        get() = quizQuestions.count { selectedQuizAnswers[it.id] == it.answer }

    val quizResult: String
        get() = if (quizSubmitted) {
            "答题 ${quizScore}/${quizQuestions.size}"
        } else {
            "尚未提交"
        }

    val confidenceScore: Int
        get() {
            val evidenceBonus = activeEvidence.count { taskIdFor(it.kind) in completedTaskIds } * 11
            val progressBonus = completedTaskIds.size * 3
            val quizBonus = if (quizSubmitted) quizScore * 4 else 0
            return (24 + evidenceBonus + progressBonus + quizBonus).coerceIn(0, 100)
        }

    val diagnosisLevel: String
        get() = when {
            confidenceScore >= 86 -> "证据链完整"
            confidenceScore >= 68 -> "可以形成初步结论"
            confidenceScore >= 48 -> "证据仍然分散"
            else -> "只有现象，没有诊断"
        }

    private fun taskIdFor(kind: EvidenceKind): String {
        return when (kind) {
            EvidenceKind.Logcat -> "timeline"
            EvidenceKind.Dumpsys -> "state"
            EvidenceKind.Perfetto -> "trace"
            EvidenceKind.Bugreport -> "fullscene"
            EvidenceKind.Gfxinfo,
            EvidenceKind.Meminfo,
            EvidenceKind.Procstats,
            EvidenceKind.Simpleperf -> "performance"
        }
    }
}
