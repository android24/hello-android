package com.helloandroid.governance

enum class RiskLevel(val label: String, val weight: Int) {
    P0("P0 阻塞", 0),
    P1("P1 高风险", 8),
    P2("P2 可控风险", 18),
    P3("P3 观察项", 28),
}

enum class GovernanceArea(val label: String) {
    Module("模块边界"),
    Component("组件契约"),
    Plugin("插件化风险"),
    Build("构建效率"),
    Dependency("依赖资源"),
    Release("发布链路"),
    Monitor("监控告警"),
    Debt("技术债"),
    Report("治理报告"),
}

data class GovernanceTask(
    val id: String,
    val title: String,
    val description: String,
)

data class GovernanceRisk(
    val id: String,
    val title: String,
    val level: RiskLevel,
    val area: GovernanceArea,
    val evidence: String,
    val decision: String,
    val action: String,
)

data class GovernanceScenario(
    val id: String,
    val title: String,
    val surface: String,
    val hiddenRisk: String,
    val expectedDecision: String,
    val riskIds: List<String>,
)

data class GovernancePolicy(
    val id: String,
    val title: String,
    val description: String,
    val p1Decision: String,
    val minHealthToGray: Int,
)

data class BuildMetric(
    val task: String,
    val duration: String,
    val hint: String,
)

data class ConfigChange(
    val name: String,
    val value: String,
    val risk: String,
    val safe: Boolean,
)

data class ReleaseSignal(
    val name: String,
    val current: String,
    val baseline: String,
    val status: RiskLevel,
)

data class ReleaseGate(
    val name: String,
    val owner: String,
    val passed: Boolean,
    val evidence: String,
)

data class TechDebtItem(
    val title: String,
    val area: GovernanceArea,
    val impact: String,
    val owner: String,
    val due: String,
)

data class GovernanceLabState(
    val appVersion: String,
    val buildNumber: String,
    val gitCommit: String,
    val mappingId: String,
    val symbolsId: String,
    val remoteConfigVersion: String,
    val selectedPolicyId: String,
    val selectedScenarioId: String,
    val policies: List<GovernancePolicy>,
    val tasks: List<GovernanceTask>,
    val completedTaskIds: Set<String>,
    val scenarios: List<GovernanceScenario>,
    val risks: List<GovernanceRisk>,
    val selectedRiskId: String?,
    val buildMetrics: List<BuildMetric>,
    val configChanges: List<ConfigChange>,
    val releaseSignals: List<ReleaseSignal>,
    val releaseGates: List<ReleaseGate>,
    val techDebts: List<TechDebtItem>,
    val notes: String,
) {
    val selectedScenario: GovernanceScenario
        get() = scenarios.first { it.id == selectedScenarioId }

    val selectedPolicy: GovernancePolicy
        get() = policies.first { it.id == selectedPolicyId }

    val activeRisks: List<GovernanceRisk>
        get() = risks.filter { it.id in selectedScenario.riskIds }

    val selectedRisk: GovernanceRisk?
        get() = selectedRiskId?.let { id -> risks.firstOrNull { it.id == id } }

    val completedCount: Int
        get() = completedTaskIds.size

    val healthScore: Int
        get() {
            val penalty = activeRisks.sumOf { 32 - it.level.weight }
            val progressBonus = completedTaskIds.size * 2
            return (96 - penalty + progressBonus).coerceIn(0, 100)
        }

    val releaseDecision: String
        get() {
            val hasP0 = activeRisks.any { it.level == RiskLevel.P0 }
            val hasP1 = activeRisks.any { it.level == RiskLevel.P1 }
            return when {
                hasP0 -> "暂停发布"
                hasP1 -> selectedPolicy.p1Decision
                healthScore < selectedPolicy.minHealthToGray -> "修复后再继续"
                else -> "可以进入灰度"
            }
        }

    val nextTask: GovernanceTask?
        get() = tasks.firstOrNull { it.id !in completedTaskIds }
}
