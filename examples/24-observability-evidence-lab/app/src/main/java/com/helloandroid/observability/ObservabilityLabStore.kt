package com.helloandroid.observability

class ObservabilityLabStore {
    fun initialState(): ObservabilityLabState {
        val evidence = evidenceItems()
        val scenarios = scenarios()
        return ObservabilityLabState(
            packageName = "com.helloandroid.observability",
            activeTraceId = "trace-not-started",
            selectedScenarioId = scenarios.first().id,
            selectedEvidenceId = scenarios.first().evidenceIds.first(),
            tasks = taskBoard(),
            completedTaskIds = setOf("trigger"),
            scenarios = scenarios,
            evidenceItems = evidence,
            metrics = metrics(),
            quizQuestions = quizQuestions(),
            selectedQuizAnswers = emptyMap(),
            quizSubmitted = false,
            lastAction = "先选择一个事故剧本，再从 logcat 时间点开始串证据。",
        )
    }

    fun selectScenario(current: ObservabilityLabState, scenarioId: String): ObservabilityLabState {
        val scenario = current.scenarios.first { it.id == scenarioId }
        return current.copy(
            selectedScenarioId = scenarioId,
            selectedEvidenceId = scenario.evidenceIds.firstOrNull(),
            completedTaskIds = current.completedTaskIds + "timeline",
            lastAction = "已切换到：${scenario.title}。请先锁定复现时间点。",
        )
    }

    fun selectEvidence(current: ObservabilityLabState, evidenceId: String): ObservabilityLabState {
        val evidence = current.evidenceItems.first { it.id == evidenceId }
        return current.copy(
            selectedEvidenceId = evidenceId,
            completedTaskIds = current.completedTaskIds + taskIdFor(evidence.kind),
            lastAction = "已选中 ${evidence.kind.label} 证据：${evidence.title}。",
        )
    }

    fun completeArea(current: ObservabilityLabState, area: EvidenceArea): ObservabilityLabState {
        return current.copy(
            completedTaskIds = current.completedTaskIds + area.name.lowercase(),
            lastAction = "已完成 ${area.label} 检查。",
        )
    }

    fun recordAction(current: ObservabilityLabState, message: String): ObservabilityLabState {
        return current.copy(
            completedTaskIds = current.completedTaskIds + "trigger",
            lastAction = message,
        )
    }

    fun recordAction(
        current: ObservabilityLabState,
        message: String,
        traceId: String,
    ): ObservabilityLabState {
        return current.copy(
            activeTraceId = traceId,
            completedTaskIds = current.completedTaskIds + "trigger",
            lastAction = message,
        )
    }

    fun selectQuizAnswer(
        current: ObservabilityLabState,
        questionId: String,
        answer: String,
    ): ObservabilityLabState {
        return current.copy(
            selectedQuizAnswers = current.selectedQuizAnswers + (questionId to answer),
            quizSubmitted = false,
            lastAction = "已选择一条证据链判断，提交后会看到解释。",
        )
    }

    fun submitQuiz(current: ObservabilityLabState): ObservabilityLabState {
        return current.copy(
            quizSubmitted = true,
            completedTaskIds = current.completedTaskIds + "quiz",
            lastAction = "证据链答题完成：${current.quizScore}/${current.quizQuestions.size}。",
        )
    }

    fun completeNextTask(current: ObservabilityLabState): ObservabilityLabState {
        val next = current.nextTask ?: return current.copy(lastAction = "证据链任务已经完成，可以复制或分享报告。")
        return current.copy(
            completedTaskIds = current.completedTaskIds + next.id,
            lastAction = "已完成观察点：${next.title}。",
        )
    }

    fun generatedReport(state: ObservabilityLabState): String {
        val evidence = state.activeEvidence.joinToString(separator = "\n\n") {
            """
            - ${it.kind.label}｜${it.title}
              命令：${it.command}
              线索：${it.clue}
              结论：${it.conclusion}
              下一步：${it.nextStep}
            """.trimIndent()
        }

        return """
            包名：${state.packageName}
            traceId：${state.activeTraceId}
            事故剧本：${state.selectedScenario.title}
            表面现象：${state.selectedScenario.symptom}
            第一问题：${state.selectedScenario.firstQuestion}
            根因候选：${state.selectedScenario.rootCandidate}

            诊断可信度：${state.confidenceScore}
            当前等级：${state.diagnosisLevel}
            答题结果：${state.quizResult}

            证据链：
            $evidence

            报告结论：
            - 先用 logcat 固定时间点和关键词。
            - 再用 dumpsys 判断系统当前状态。
            - 用 Perfetto / gfxinfo / simpleperf 解释时间线上谁在运行、谁在等待。
            - 用 bugreport 保存完整现场，必要时补 ANR trace、tombstone 或 DropBox。
            - 分享前必须脱敏包名以外的账号、路径、设备编号和业务数据。
        """.trimIndent()
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

    private fun taskBoard(): List<EvidenceTask> = listOf(
        EvidenceTask("trigger", "制造或选择现场", "触发轻量卡顿、内存增长，或选择一个模拟事故剧本。"),
        EvidenceTask("timeline", "锁定时间点", "用 logcat 找到点击、页面、错误和耗时日志。"),
        EvidenceTask("state", "确认系统状态", "用 dumpsys package / activity / jobscheduler 判断系统看到什么。"),
        EvidenceTask("trace", "阅读时间线", "用 Perfetto 观察主线程、RenderThread、Binder、调度和帧。"),
        EvidenceTask("performance", "补充性能指标", "用 gfxinfo、meminfo、procstats、simpleperf 补齐数字证据。"),
        EvidenceTask("fullscene", "保存完整现场", "用 bugreport、DropBox、ANR trace 或 tombstone 保存事故包。"),
        EvidenceTask("privacy", "脱敏后协作", "检查报告里是否包含账号、路径、设备编号和业务敏感字段。"),
        EvidenceTask("quiz", "完成证据链答题", "选择第一证据、状态证据、时间线证据和根因候选。"),
        EvidenceTask("report", "输出证据链报告", "写出现象、证据、推论、根因、修复和回归。"),
    )

    private fun scenarios(): List<IncidentScenario> = listOf(
        IncidentScenario(
            id = "detail_jank",
            title = "详情页点击后卡顿",
            symptom = "点击课程详情后，页面 2 秒才显示首屏内容。",
            firstQuestion = "是主线程长任务、Binder 等待，还是渲染链路掉帧？",
            rootCandidate = "主线程在点击后执行了同步解析，随后阻塞首帧。",
            evidenceIds = listOf("log_click", "perfetto_main", "gfx_jank", "dumpsys_activity"),
        ),
        IncidentScenario(
            id = "scroll_jank",
            title = "列表滑动掉帧",
            symptom = "课程列表滑动时肉眼能看到停顿。",
            firstQuestion = "慢帧集中在 UI thread、RenderThread，还是 GPU / SurfaceFlinger？",
            rootCandidate = "列表滚动中频繁触发布局和图片解码，帧预算被吃光。",
            evidenceIds = listOf("gfx_jank", "perfetto_frame", "log_scroll", "simpleperf_cpu"),
        ),
        IncidentScenario(
            id = "background_lost",
            title = "后台同步没有执行",
            symptom = "夜间学习进度没有同步，第二天打开仍是旧数据。",
            firstQuestion = "任务没有入队、没有 ready，还是被后台限制推迟？",
            rootCandidate = "JobScheduler 条件未满足，设备进入 idle 后任务被推迟。",
            evidenceIds = listOf("dumpsys_job", "bugreport_job", "log_sync", "procstats_state"),
        ),
        IncidentScenario(
            id = "memory_pressure",
            title = "内存压力后进程被回收",
            symptom = "切到后台一段时间后返回，页面状态丢失。",
            firstQuestion = "是业务状态没保存，还是系统因为内存压力回收进程？",
            rootCandidate = "后台进程 PSS 上升，优先级降低后被 LMKD 回收。",
            evidenceIds = listOf("meminfo_pss", "procstats_state", "bugreport_lmk", "log_process"),
        ),
        IncidentScenario(
            id = "anr_wait",
            title = "偶发 ANR 等待",
            symptom = "用户反馈点击下载后偶尔卡死，系统弹出无响应对话框。",
            firstQuestion = "主线程在执行长任务，还是等待 Binder / 锁 / IO？",
            rootCandidate = "主线程等待远端下载服务返回，Binder wait 超过阈值。",
            evidenceIds = listOf("bugreport_anr", "perfetto_binder", "dumpsys_activity", "log_click"),
        ),
        IncidentScenario(
            id = "native_crash",
            title = "某机型 Native 崩溃",
            symptom = "arm64 设备打开离线课程时闪退。",
            firstQuestion = "崩溃信号、so、backtrace 和符号文件能否对上？",
            rootCandidate = "native 解码库访问非法地址，需要 tombstone 和 symbols 还原。",
            evidenceIds = listOf("bugreport_tombstone", "simpleperf_cpu", "log_native", "meminfo_pss"),
        ),
    )

    private fun evidenceItems(): List<EvidenceItem> = listOf(
        EvidenceItem(
            id = "log_click",
            kind = EvidenceKind.Logcat,
            title = "点击详情页时间点",
            command = "adb logcat -v threadtime | grep Chapter24Lab",
            clue = "10:24:31.122 click course detail；10:24:33.018 first content rendered。",
            conclusion = "logcat 先固定事故窗口，后续证据都围绕这个时间段展开。",
            nextStep = "采集 Perfetto，确认这 1.8 秒主线程在做什么。",
        ),
        EvidenceItem(
            id = "log_scroll",
            kind = EvidenceKind.Logcat,
            title = "滑动开始和掉帧提示",
            command = "adb logcat -v time | grep Choreographer",
            clue = "Skipped 12 frames near course list scroll。",
            conclusion = "已经有掉帧现象，但还不能单靠日志判断根因。",
            nextStep = "用 gfxinfo 量化帧，再用 Perfetto 定位慢帧。",
        ),
        EvidenceItem(
            id = "log_sync",
            kind = EvidenceKind.Logcat,
            title = "后台同步日志",
            command = "adb logcat -v threadtime | grep StudySync",
            clue = "enqueue sync, but no finished event before 02:00。",
            conclusion = "业务确实发起了任务，但没有完成日志。",
            nextStep = "转向 dumpsys jobscheduler 查看系统是否调度。",
        ),
        EvidenceItem(
            id = "log_process",
            kind = EvidenceKind.Logcat,
            title = "进程重建日志",
            command = "adb logcat -b events | grep am_proc_start",
            clue = "返回前出现新的 process start 记录。",
            conclusion = "页面状态丢失可能来自进程重建，而不是普通 Activity recreate。",
            nextStep = "继续看 meminfo、procstats 和 bugreport 中的 LMKD 线索。",
        ),
        EvidenceItem(
            id = "log_native",
            kind = EvidenceKind.Logcat,
            title = "Native crash 摘要",
            command = "adb logcat -b crash | grep observability",
            clue = "Fatal signal 11 in libcourse_decoder.so。",
            conclusion = "Java 日志只能看到崩溃摘要，根因要进入 tombstone。",
            nextStep = "从 bugreport 或 /data/tombstones 中查找对应 tombstone。",
        ),
        EvidenceItem(
            id = "dumpsys_activity",
            kind = EvidenceKind.Dumpsys,
            title = "Activity 和进程状态",
            command = "adb shell dumpsys activity activities",
            clue = "CourseDetailActivity 已 RESUMED，但首帧日志明显滞后。",
            conclusion = "系统认为页面已进入前台，问题更可能在执行或渲染阶段。",
            nextStep = "用 Perfetto 看 Activity resume 后主线程和 RenderThread。",
        ),
        EvidenceItem(
            id = "dumpsys_job",
            kind = EvidenceKind.Dumpsys,
            title = "JobScheduler 状态",
            command = "adb shell dumpsys jobscheduler",
            clue = "job waiting: charging=false, deviceIdle=true。",
            conclusion = "任务存在，但调度条件没有满足。",
            nextStep = "从 bugreport 保存完整后台限制现场。",
        ),
        EvidenceItem(
            id = "perfetto_main",
            kind = EvidenceKind.Perfetto,
            title = "主线程长任务",
            command = "adb shell perfetto -o /data/misc/perfetto-traces/ch24.perfetto-trace -t 10s sched freq idle am wm gfx view binder_driver",
            clue = "main thread 出现 126ms 同步解析 slice。",
            conclusion = "首帧前主线程耗时超过一帧预算，解释了点击后卡顿。",
            nextStep = "回到代码拆分同步解析，补一轮 trace 对比。",
        ),
        EvidenceItem(
            id = "perfetto_frame",
            kind = EvidenceKind.Perfetto,
            title = "FrameTimeline 慢帧",
            command = "使用 quality/perfetto_config.textproto 采集并在 ui.perfetto.dev 打开。",
            clue = "慢帧附近 UI thread 和 RenderThread 都出现连续忙碌。",
            conclusion = "卡顿不只是单帧偶发，而是滚动期间持续超过预算。",
            nextStep = "结合 gfxinfo 和 simpleperf 看 CPU 热点。",
        ),
        EvidenceItem(
            id = "perfetto_binder",
            kind = EvidenceKind.Perfetto,
            title = "Binder wait 时间线",
            command = "adb shell perfetto -t 15s sched binder_driver am wm view",
            clue = "main thread blocked on binder transaction 320ms。",
            conclusion = "ANR 候选方向从“主线程自己忙”转向“等待远端服务”。",
            nextStep = "查看远端服务线程和 ANR trace。",
        ),
        EvidenceItem(
            id = "gfx_jank",
            kind = EvidenceKind.Gfxinfo,
            title = "慢帧数量",
            command = "adb shell dumpsys gfxinfo com.helloandroid.observability framestats",
            clue = "Janky frames: 18.6%，90th percentile: 42ms。",
            conclusion = "掉帧已经可量化，下一步才值得追慢帧根因。",
            nextStep = "用 Perfetto 锁定慢帧附近的线程状态。",
        ),
        EvidenceItem(
            id = "meminfo_pss",
            kind = EvidenceKind.Meminfo,
            title = "PSS 和 native heap",
            command = "adb shell dumpsys meminfo com.helloandroid.observability",
            clue = "PSS 从 96MB 上升到 214MB，native heap 增长明显。",
            conclusion = "内存增长是真实存在的，不只是页面状态错觉。",
            nextStep = "结合 procstats 看后台状态持续时间。",
        ),
        EvidenceItem(
            id = "procstats_state",
            kind = EvidenceKind.Procstats,
            title = "进程状态历史",
            command = "adb shell dumpsys procstats --hours 3",
            clue = "进程长时间停留 cached，随后重新启动。",
            conclusion = "状态丢失和后台进程回收高度相关。",
            nextStep = "从 bugreport 查 LMKD / low memory killer 记录。",
        ),
        EvidenceItem(
            id = "simpleperf_cpu",
            kind = EvidenceKind.Simpleperf,
            title = "CPU 热点函数",
            command = "simpleperf record -p <pid> --duration 10 && simpleperf report",
            clue = "decodeCourseCover 与 parseLessonIndex 占用靠前。",
            conclusion = "CPU 热点指向图片解码和课程索引解析。",
            nextStep = "优化后再次采样，证明热点下降。",
        ),
        EvidenceItem(
            id = "bugreport_job",
            kind = EvidenceKind.Bugreport,
            title = "后台调度完整现场",
            command = "adb bugreport bugreports/",
            clue = "device idle、jobscheduler、alarm 和 app standby 信息在同一报告里。",
            conclusion = "bugreport 能保存跨服务现场，适合事后复盘。",
            nextStep = "记录复现时间点，避免在大报告里迷路。",
        ),
        EvidenceItem(
            id = "bugreport_lmk",
            kind = EvidenceKind.Bugreport,
            title = "LMKD 线索",
            command = "adb bugreport bugreports/ && grep -i lmk bugreport*.txt",
            clue = "lowmemorykiller 记录靠近用户返回前时间点。",
            conclusion = "进程重建和内存压力之间有证据关联。",
            nextStep = "修复状态保存，并降低后台内存占用。",
        ),
        EvidenceItem(
            id = "bugreport_anr",
            kind = EvidenceKind.Bugreport,
            title = "ANR trace",
            command = "adb bugreport bugreports/ && 搜索 traces.txt / ANR in",
            clue = "main thread waiting on BinderProxy.transact。",
            conclusion = "ANR 根因候选是跨进程等待，而不是普通 UI 卡顿。",
            nextStep = "用 Perfetto 对齐主线程和远端 Binder 线程。",
        ),
        EvidenceItem(
            id = "bugreport_tombstone",
            kind = EvidenceKind.Bugreport,
            title = "tombstone 和 symbols",
            command = "adb bugreport bugreports/ && 搜索 tombstone / DEBUG tombstone",
            clue = "signal 11, fault addr 0x0, backtrace 命中 libcourse_decoder.so。",
            conclusion = "Native crash 需要 tombstone 与符号文件共同还原。",
            nextStep = "用对应版本 symbols 还原 native backtrace。",
        ),
    )

    private fun metrics(): List<PerfMetric> = listOf(
        PerfMetric("Janky frames", "18.6%", "<= 8%", "异常"),
        PerfMetric("Frame p90", "42ms", "<= 24ms", "异常"),
        PerfMetric("PSS", "214MB", "<= 160MB", "观察"),
        PerfMetric("Cold start p95", "1820ms", "<= 1800ms", "临界"),
        PerfMetric("CPU top", "decodeCourseCover", "无单点热点", "需采样"),
    )

    private fun quizQuestions(): List<EvidenceQuizQuestion> = listOf(
        EvidenceQuizQuestion(
            id = "first_evidence",
            prompt = "复杂事故排查的第一步应该是什么？",
            options = listOf("先锁定复现时间点", "先截图发群里", "先重启手机", "先猜测根因"),
            answer = "先锁定复现时间点",
            explanation = "时间点是证据链的锚。没有它，logcat、dumpsys、Perfetto 和 bugreport 很难对齐。",
        ),
        EvidenceQuizQuestion(
            id = "state_evidence",
            prompt = "想确认 Job 是否被系统调度，优先看哪类证据？",
            options = listOf("dumpsys jobscheduler", "UI 截图", "版本号文案", "颜色资源"),
            answer = "dumpsys jobscheduler",
            explanation = "dumpsys 能看到系统服务维护的运行时状态，适合确认任务是否存在、是否 ready、是否被限制。",
        ),
        EvidenceQuizQuestion(
            id = "timeline_evidence",
            prompt = "要判断主线程是在运行还是等待，最应该看什么？",
            options = listOf("Perfetto 时间线", "README 标题", "应用图标", "包体积"),
            answer = "Perfetto 时间线",
            explanation = "Perfetto 能把线程运行、调度、Binder、帧和系统服务放到同一条时间线上对齐。",
        ),
        EvidenceQuizQuestion(
            id = "privacy",
            prompt = "bugreport 或 trace 分享前必须做什么？",
            options = listOf("检查并脱敏敏感信息", "直接上传公开网盘", "删掉所有时间点", "只保留截图"),
            answer = "检查并脱敏敏感信息",
            explanation = "完整现场通常包含设备、路径、账号、业务数据或系统状态，分享前必须控制范围。",
        ),
    )
}
