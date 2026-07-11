package com.helloandroid.launch

data class LaunchLabState(
    val processInfo: LaunchProcessInfo = LaunchProcessInfo(),
    val currentScreen: String = "MainActivity",
    val standardLaunchCount: Int = 0,
    val singleTopLaunchCount: Int = 0,
    val newIntentCount: Int = 0,
    val clearTopCount: Int = 0,
    val duplicateDetailCount: Int = 0,
    val expectedActual: LaunchExpectedActual = LaunchExpectedActual(),
    val stackSnapshot: List<LaunchStackNode> = defaultStackSnapshot,
    val missions: List<LaunchMission> = defaultLaunchMissions,
    val launchModels: List<LaunchModelCard> = defaultLaunchModels,
    val traceEvents: List<LaunchTraceEvent> = emptyList()
)

data class LaunchProcessInfo(
    val pid: Int = 0,
    val threadName: String = "unknown",
    val processAgeMs: Long = 0L
)

data class LaunchMission(
    val title: String,
    val clue: String,
    val action: String
)

data class LaunchExpectedActual(
    val operation: String = "等待第一次启动操作",
    val expected: String = "点击按钮后，先写下你认为系统会创建、复用还是清理页面。",
    val actual: String = "生命周期日志出现后，再把观察结果和预期对照。",
    val verdict: String = "还没有证据。"
)

data class LaunchStackNode(
    val label: String,
    val note: String
)

data class LaunchModelCard(
    val name: String,
    val rule: String,
    val observation: String
)

data class LaunchTraceEvent(
    val title: String,
    val detail: String,
    val timestamp: String
)

val defaultLaunchMissions = listOf(
    LaunchMission(
        title = "任务一：普通启动详情页",
        clue = "观察 MainActivity 暂停、DetailActivity 创建和返回销毁。",
        action = "点击普通启动详情页，再按返回。"
    ),
    LaunchMission(
        title = "任务二：验证 singleTop",
        clue = "当目标 Activity 已在栈顶时，观察 onNewIntent。",
        action = "进入 singleTop 页面后再次启动自己。"
    ),
    LaunchMission(
        title = "任务三：CLEAR_TOP 回首页",
        clue = "观察中间页面是否被清理，MainActivity 是否回到前台。",
        action = "从子页面点击 CLEAR_TOP 回首页。"
    ),
    LaunchMission(
        title = "任务四：连续启动详情页",
        clue = "故意制造多个 standard 实例，观察返回键为什么要一层层退出。",
        action = "在 DetailActivity 里连续点击“再开一个详情页”。"
    ),
    LaunchMission(
        title = "任务五：写启动链路报告",
        clue = "把操作、日志、返回栈推断和源码入口写成短报告。",
        action = "参考 quality/activity-launch-report-template.md。"
    )
)

val defaultStackSnapshot = listOf(
    LaunchStackNode(
        label = "MainActivity",
        note = "任务栈根页面"
    )
)

val defaultLaunchModels = listOf(
    LaunchModelCard(
        name = "standard",
        rule = "默认模式。每次启动通常创建一个新实例。",
        observation = "连续启动详情页时，重点看 DetailActivity.onCreate 出现几次。"
    ),
    LaunchModelCard(
        name = "singleTop",
        rule = "如果目标已经在栈顶，则复用栈顶实例。",
        observation = "再次启动栈顶 SingleTopActivity 时，应重点观察 onNewIntent。"
    ),
    LaunchModelCard(
        name = "FLAG_ACTIVITY_CLEAR_TOP",
        rule = "回到栈内已有目标，并清理目标之上的页面。",
        observation = "从子页面回 MainActivity 时，观察子页面 onDestroy 和 MainActivity onNewIntent。"
    ),
    LaunchModelCard(
        name = "ActivityRecord / Task",
        rule = "系统服务侧维护 Activity 调度记录和任务归属。",
        observation = "App 侧看生命周期，Framework 侧看 ActivityRecord、Task、ActivityStarter。"
    )
)
