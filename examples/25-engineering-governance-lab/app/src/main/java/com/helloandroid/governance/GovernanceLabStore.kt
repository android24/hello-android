package com.helloandroid.governance

class GovernanceLabStore {
    fun initialState(): GovernanceLabState {
        val risks = listOf(
            GovernanceRisk(
                id = "module_reverse",
                title = "core:common 反向依赖 feature:profile",
                level = RiskLevel.P0,
                area = GovernanceArea.Module,
                evidence = "依赖图显示 core:common -> feature:profile，基础模块被业务模块污染。",
                decision = "阻塞发布。core 或 domain 反向依赖 feature 属于架构底线问题。",
                action = "把 ProfileSummary 移到 profile-api 或 domain-user，并让 core 只依赖稳定模型。",
            ),
            GovernanceRisk(
                id = "component_contract",
                title = "课程组件直接调用支付实现",
                level = RiskLevel.P1,
                area = GovernanceArea.Component,
                evidence = "feature:course -> feature:payment.impl.PaymentActivity，绕过 PaymentNavigator contract。",
                decision = "只能小流量灰度或先修复。跨组件实现依赖会放大后续支付改造风险。",
                action = "抽出 payment-api，由 app 层装配 PaymentNavigator 实现。",
            ),
            GovernanceRisk(
                id = "plugin_protocol",
                title = "下载插件要求宿主协议 v3，当前宿主仍是 v2",
                level = RiskLevel.P0,
                area = GovernanceArea.Plugin,
                evidence = "plugin manifest: minHostProtocol=3，host protocol=2，启动前兼容检查失败。",
                decision = "阻塞发布。插件能下载不代表能被当前宿主安全加载。",
                action = "升级宿主协议，或下发兼容 v2 的插件版本，并补充插件灰度回滚策略。",
            ),
            GovernanceRisk(
                id = "kapt_slow",
                title = ":feature:course:kaptDebugKotlin 全量重跑",
                level = RiskLevel.P2,
                area = GovernanceArea.Build,
                evidence = "CI 从 16min 上升到 31min，最慢 task 为 kaptDebugKotlin 62.4s。",
                decision = "不直接阻塞发布，但必须记录负责人和期限。",
                action = "评估 KSP 迁移、Hilt 聚合影响、模块 API 变化和缓存命中率。",
            ),
            GovernanceRisk(
                id = "dependency_conflict",
                title = "okio 传递依赖从 2.10.0 被提升到 3.6.0",
                level = RiskLevel.P1,
                area = GovernanceArea.Dependency,
                evidence = "dependencyInsight 显示 network-lib:1.5 依赖 okio:2.10.0，但运行时解析为 3.6.0。",
                decision = "需要验证后才能继续灰度。运行时 classpath 和库编译预期不一致。",
                action = "升级 network-lib 或添加版本约束，并回归下载、缓存和加密链路。",
            ),
            GovernanceRisk(
                id = "resource_prefix",
                title = "支付组件新增 pay_title，与课程组件资源命名接近",
                level = RiskLevel.P2,
                area = GovernanceArea.Dependency,
                evidence = "资源扫描显示 payment 模块新增 pay_title，course 模块已有 course_pay_title，命名语义开始交叉。",
                decision = "不阻塞发布，但需要在资源前缀和 UI 回归里补检查。",
                action = "按组件前缀收敛资源命名，并把新增资源列表加入 PR 检查。",
            ),
            GovernanceRisk(
                id = "permission_added",
                title = "新增 POST_NOTIFICATIONS 权限但缺少拒绝路径说明",
                level = RiskLevel.P1,
                area = GovernanceArea.Dependency,
                evidence = "Manifest diff 显示新增通知权限，但发布说明没有写用户拒绝授权后的降级体验。",
                decision = "只能小流量灰度或补齐权限评审后继续。",
                action = "补权限申请时机、拒绝兜底、商店审核说明和埋点观察项。",
            ),
            GovernanceRisk(
                id = "signing_mismatch",
                title = "release 产物签名证书指纹与构建档案不一致",
                level = RiskLevel.P0,
                area = GovernanceArea.Release,
                evidence = "构建档案记录 SHA-256=A1:B2，但本次产物验签为 9F:7C。",
                decision = "阻塞发布。签名不一致会影响升级、签名权限和供应链可信度。",
                action = "停止分发，核对 signing config、CI secret、Play App Signing 和产物来源。",
            ),
            GovernanceRisk(
                id = "config_leak",
                title = "staging API 域名进入 release 配置",
                level = RiskLevel.P0,
                area = GovernanceArea.Release,
                evidence = "release BuildConfig.API_BASE_URL=https://staging-api.example.com。",
                decision = "阻塞发布。环境配置错误可能导致数据污染和线上功能不可用。",
                action = "修复 flavor 配置，增加 release 配置快照和自动校验。",
            ),
            GovernanceRisk(
                id = "mapping_missing",
                title = "release mapping 未归档",
                level = RiskLevel.P0,
                area = GovernanceArea.Release,
                evidence = "构建档案缺少 map-250010-17，线上 crash 无法还原混淆堆栈。",
                decision = "阻塞发布。健康分再高也不能掩盖符号归档缺失。",
                action = "重新执行 release 构建并归档 mapping、seeds、usage 和 native symbols。",
            ),
            GovernanceRisk(
                id = "crash_free_drop",
                title = "灰度后 crash-free 从 99.93% 降到 99.40%",
                level = RiskLevel.P1,
                area = GovernanceArea.Monitor,
                evidence = "异常集中在 Android 12 + arm64 + 课程下载入口。",
                decision = "降低灰度或关闭 download_new_crypto 开关。",
                action = "关联 mapping 和远程配置版本，确认 DownloadEncryptor.decrypt() 是否为根因。",
            ),
            GovernanceRisk(
                id = "debt_common",
                title = "core:common 承载 6 个业务状态",
                level = RiskLevel.P2,
                area = GovernanceArea.Debt,
                evidence = "CourseDetailState、PaymentResult、HomeBannerConfig 等类型进入公共模块。",
                decision = "可延期但必须排期。公共模块膨胀会让所有业务共享变化风险。",
                action = "建立 core 类型准入规则，并逐步迁移业务状态到对应 feature/domain。",
            ),
        )

        val scenarios = listOf(
            GovernanceScenario(
                id = "release_check",
                title = "2.5.0 发版前体检",
                surface = "release 构建成功，准备灰度 5%。",
                hiddenRisk = "mapping 缺失、依赖冲突和构建耗时回退同时存在。",
                expectedDecision = "暂停发布，先补齐 mapping，再验证依赖冲突。",
                riskIds = listOf("mapping_missing", "dependency_conflict", "kapt_slow"),
            ),
            GovernanceScenario(
                id = "component_mess",
                title = "组件契约失控",
                surface = "课程购买流程能跑通。",
                hiddenRisk = "课程模块直接调用支付实现，支付又依赖个人资料实现。",
                expectedDecision = "抽 contract 后再合并。",
                riskIds = listOf("component_contract", "debt_common"),
            ),
            GovernanceScenario(
                id = "plugin_crash",
                title = "插件协议不兼容",
                surface = "下载插件已下发，但部分设备启动失败。",
                hiddenRisk = "插件要求宿主协议 v3，当前宿主协议仍是 v2。",
                expectedDecision = "阻塞发布或升级宿主协议。",
                riskIds = listOf("plugin_protocol", "crash_free_drop"),
            ),
            GovernanceScenario(
                id = "module_reverse",
                title = "模块依赖倒流",
                surface = "新人把用户资料类型放入 core:common 后编译通过。",
                hiddenRisk = "core 反向依赖 feature，所有业务模块被 profile 变化污染。",
                expectedDecision = "立即阻断，拆出稳定 contract。",
                riskIds = listOf("module_reverse", "debt_common"),
            ),
            GovernanceScenario(
                id = "permission_review",
                title = "权限新增未评审",
                surface = "通知功能开发完成，测试机上提醒正常弹出。",
                hiddenRisk = "新增运行时权限缺少拒绝路径、商店审核说明和灰度监控。",
                expectedDecision = "补齐权限评审后再进入灰度。",
                riskIds = listOf("permission_added", "crash_free_drop"),
            ),
            GovernanceScenario(
                id = "resource_change",
                title = "资源前缀开始混乱",
                surface = "UI 视觉正常，release 包也能安装。",
                hiddenRisk = "资源命名跨组件靠近，后续合并和主题替换更容易误伤。",
                expectedDecision = "不阻塞本次发布，但必须进入技术债排期。",
                riskIds = listOf("resource_prefix", "debt_common"),
            ),
            GovernanceScenario(
                id = "signing_accident",
                title = "签名证书指纹异常",
                surface = "release APK 已生成，准备上传渠道。",
                hiddenRisk = "签名证书与构建档案不一致，产物来源不可确认。",
                expectedDecision = "立即阻塞发布，重新核验产物链路。",
                riskIds = listOf("signing_mismatch", "mapping_missing"),
            ),
            GovernanceScenario(
                id = "config_accident",
                title = "release 配置串环境",
                surface = "打包成功，登录和课程列表在测试环境正常。",
                hiddenRisk = "release 包携带 staging API 域名，真实用户会访问错误环境。",
                expectedDecision = "阻塞发布，修复配置并补自动化检查。",
                riskIds = listOf("config_leak", "permission_added"),
            ),
        )

        val policies = policies()

        return GovernanceLabState(
            appVersion = "2.5.0",
            buildNumber = "20260830.17",
            gitCommit = "a1b2c3d",
            mappingId = "missing",
            symbolsId = "symbols-arm64-250010-17",
            remoteConfigVersion = "rc-20260830-02",
            selectedPolicyId = policies.first().id,
            selectedScenarioId = scenarios.first().id,
            policies = policies,
            tasks = taskBoard(),
            completedTaskIds = setOf("scenario"),
            scenarios = scenarios,
            risks = risks,
            selectedRiskId = scenarios.first().riskIds.first(),
            buildMetrics = buildMetrics(),
            configChanges = configChanges(),
            releaseSignals = releaseSignals(),
            releaseGates = releaseGates(),
            techDebts = techDebts(),
            notes = "先选择一个事故剧本，再按任务板完成治理体检。",
        )
    }

    fun selectPolicy(current: GovernanceLabState, policyId: String): GovernanceLabState {
        val policy = current.policies.first { it.id == policyId }
        return current.copy(
            selectedPolicyId = policyId,
            notes = "已切换治理规则：${policy.title}。请重新观察健康分和发布结论。",
        )
    }

    fun selectScenario(current: GovernanceLabState, scenarioId: String): GovernanceLabState {
        val scenario = current.scenarios.first { it.id == scenarioId }
        return current.copy(
            selectedScenarioId = scenarioId,
            selectedRiskId = scenario.riskIds.firstOrNull(),
            completedTaskIds = current.completedTaskIds + "scenario",
            notes = "已切换到：${scenario.title}。下一步请检查风险证据和发布决策。",
        )
    }

    fun selectRisk(current: GovernanceLabState, riskId: String): GovernanceLabState {
        val risk = current.risks.first { it.id == riskId }
        return current.copy(
            selectedRiskId = riskId,
            completedTaskIds = current.completedTaskIds + risk.area.name.lowercase(),
            notes = "当前选中风险：${risk.title}。",
        )
    }

    fun completeNextTask(current: GovernanceLabState): GovernanceLabState {
        val next = current.nextTask ?: return current.copy(notes = "任务板已经完成，可以输出治理报告。")
        return current.copy(
            completedTaskIds = current.completedTaskIds + next.id,
            notes = "已完成观察点：${next.title}。",
        )
    }

    fun completeArea(current: GovernanceLabState, area: GovernanceArea): GovernanceLabState {
        return current.copy(
            completedTaskIds = current.completedTaskIds + area.name.lowercase(),
            notes = "已完成 ${area.label} 检查。",
        )
    }

    fun generatedReport(state: GovernanceLabState): String {
        val risks = state.activeRisks.joinToString(separator = "\n\n") { risk ->
            """
            - ${risk.level.label}｜${risk.area.label}｜${risk.title}
              证据：${risk.evidence}
              决策：${risk.decision}
              动作：${risk.action}
            """.trimIndent()
        }

        return """
            版本：${state.appVersion}
            构建号：${state.buildNumber}
            Git commit：${state.gitCommit}
            mapping：${state.mappingId}
            symbols：${state.symbolsId}
            remote config：${state.remoteConfigVersion}
            治理规则：${state.selectedPolicy.title}

            发布结论：${state.releaseDecision}
            工程健康分：${state.healthScore}

            当前剧本：${state.selectedScenario.title}
            表面现象：${state.selectedScenario.surface}
            隐藏风险：${state.selectedScenario.hiddenRisk}
            期望决策：${state.selectedScenario.expectedDecision}

            风险清单：
            $risks

            回归计划：
            - 重新生成 release 产物并归档 mapping / symbols。
            - 对组件 contract、插件协议、依赖版本和资源前缀执行检查。
            - 灰度 5% 观察 crash-free、ANR、启动 p95、慢帧率和核心业务成功率。
            - 门禁失败项必须有负责人、截止时间和回滚方案。
            - 修复后用同一张治理报告对比风险等级是否下降。
        """.trimIndent()
    }

    private fun taskBoard(): List<GovernanceTask> = listOf(
        GovernanceTask("scenario", "选择事故剧本", "从发版体检、组件失控、插件不兼容、权限新增、资源变化、签名异常或配置串环境中选择一个。"),
        GovernanceTask("module", "检查模块边界", "识别 core / feature / app 是否存在依赖倒流。"),
        GovernanceTask("component", "检查组件契约", "判断跨组件调用是否经过 api / contract。"),
        GovernanceTask("plugin", "检查插件协议", "确认宿主插件协议、资源隔离、签名和回滚状态。"),
        GovernanceTask("build", "检查构建耗时", "找出最慢 task 和缓存命中风险。"),
        GovernanceTask("dependency", "检查依赖冲突", "阅读 dependencyInsight 输出，判断运行时 classpath 风险。"),
        GovernanceTask("release", "检查发布产物", "确认签名、mapping、symbols、R8 和版本档案。"),
        GovernanceTask("monitor", "检查监控告警", "观察 crash-free、ANR、启动 p95 和业务成功率。"),
        GovernanceTask("debt", "排序技术债", "区分阻塞项、可控风险和观察项。"),
        GovernanceTask("report", "输出治理报告", "写出发布结论、证据、动作和回归计划。"),
    )

    private fun policies(): List<GovernancePolicy> = listOf(
        GovernancePolicy(
            id = "safe",
            title = "保守门禁",
            description = "适合支付、账号、安全和大版本发布。P1 风险也倾向先修复。",
            p1Decision = "先修复再灰度",
            minHealthToGray = 86,
        ),
        GovernancePolicy(
            id = "balanced",
            title = "平衡灰度",
            description = "适合普通业务迭代。P1 风险允许小流量灰度，但必须有监控和回滚。",
            p1Decision = "仅允许小流量灰度",
            minHealthToGray = 75,
        ),
        GovernancePolicy(
            id = "fast",
            title = "快速实验",
            description = "适合内部实验和低风险功能。P1 风险可以灰度，但要求开关可控。",
            p1Decision = "可以 1% 实验灰度",
            minHealthToGray = 68,
        ),
    )

    private fun buildMetrics(): List<BuildMetric> = listOf(
        BuildMetric(":feature:course:kaptDebugKotlin", "62.4s", "注解处理全量重跑，优先评估 KSP 迁移。"),
        BuildMetric(":app:mergeDebugResources", "28.1s", "资源合并偏慢，检查新增资源和前缀规范。"),
        BuildMetric(":app:dexBuilderDebug", "21.7s", "类数量和依赖体积上升，检查传递依赖。"),
        BuildMetric(":core:database:kspDebugKotlin", "12.6s", "仍可接受，继续观察缓存命中。"),
    )

    private fun configChanges(): List<ConfigChange> = listOf(
        ConfigChange(
            name = "新增权限",
            value = "POST_NOTIFICATIONS",
            risk = "需要确认 Android 13+ 授权路径和拒绝后的兜底体验。",
            safe = false,
        ),
        ConfigChange(
            name = "新增 exported 组件",
            value = "DownloadProxyActivity=false",
            risk = "未暴露给外部 App，当前安全。",
            safe = true,
        ),
        ConfigChange(
            name = "资源前缀",
            value = "course_* / payment_*",
            risk = "前缀规范通过，但仍要观察 mergeResources 耗时。",
            safe = true,
        ),
        ConfigChange(
            name = "包体积变化",
            value = "+2.8 MB",
            risk = "超过 2 MB 阈值，需要解释新增 so、图片或传递依赖。",
            safe = false,
        ),
        ConfigChange(
            name = "release 环境配置",
            value = "api.prod.example.com",
            risk = "生产域名正确，但必须和 remote config 版本一起归档。",
            safe = true,
        ),
    )

    private fun releaseSignals(): List<ReleaseSignal> = listOf(
        ReleaseSignal("crash-free", "99.40%", ">= 99.90%", RiskLevel.P1),
        ReleaseSignal("ANR rate", "0.18%", "<= 0.20%", RiskLevel.P3),
        ReleaseSignal("冷启动 p95", "2140ms", "<= 1800ms", RiskLevel.P2),
        ReleaseSignal("慢帧率", "14.8%", "<= 8%", RiskLevel.P2),
        ReleaseSignal("登录成功率", "99.72%", ">= 99.50%", RiskLevel.P3),
        ReleaseSignal("下载成功率", "94.10%", ">= 98.00%", RiskLevel.P1),
    )

    private fun releaseGates(): List<ReleaseGate> = listOf(
        ReleaseGate(
            name = "release mapping 已归档",
            owner = "构建负责人",
            passed = false,
            evidence = "map-250010-17 未进入构建档案。",
        ),
        ReleaseGate(
            name = "组件 contract 无越界调用",
            owner = "架构负责人",
            passed = false,
            evidence = "course 仍直接依赖 payment.impl。",
        ),
        ReleaseGate(
            name = "插件协议兼容当前宿主",
            owner = "动态化负责人",
            passed = false,
            evidence = "download plugin 要求 host protocol v3。",
        ),
        ReleaseGate(
            name = "核心链路灰度监控已配置",
            owner = "稳定性负责人",
            passed = true,
            evidence = "crash-free、ANR、下载成功率和开关版本均已接入。",
        ),
        ReleaseGate(
            name = "回滚路径已演练",
            owner = "发布负责人",
            passed = true,
            evidence = "remote config 可关闭 download_new_crypto。",
        ),
    )

    private fun techDebts(): List<TechDebtItem> = listOf(
        TechDebtItem(
            title = "拆分 core:common 中的业务状态",
            area = GovernanceArea.Module,
            impact = "每次 profile 变化都会牵动公共模块，影响全工程增量构建。",
            owner = "模块治理小组",
            due = "2 个迭代内",
        ),
        TechDebtItem(
            title = "Hilt kapt 迁移到 KSP",
            area = GovernanceArea.Build,
            impact = "CI p95 从 16min 上升到 31min，反馈周期变长。",
            owner = "构建效率小组",
            due = "下个大版本前",
        ),
        TechDebtItem(
            title = "下载插件补齐协议兼容测试",
            area = GovernanceArea.Plugin,
            impact = "插件灰度可能在旧宿主上直接失败，回滚窗口被压缩。",
            owner = "动态化小组",
            due = "本次灰度前",
        ),
    )
}
