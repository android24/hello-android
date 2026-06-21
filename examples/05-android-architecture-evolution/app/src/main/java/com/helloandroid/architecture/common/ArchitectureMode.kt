package com.helloandroid.architecture.common

enum class ArchitectureMode(
    val label: String,
    val summary: String
) {
    MVC(
        label = "MVC",
        summary = "Activity / Controller 容易直接调度 Model，适合小 Demo，但页面变复杂时容易变胖。"
    ),
    MVP(
        label = "MVP",
        summary = "Presenter 接管页面逻辑，View 只负责展示和转发事件，适合理解职责拆分。"
    ),
    MVVM(
        label = "MVVM",
        summary = "ViewModel 持有 UI State，Compose 根据状态渲染，是现代 Android 的常见写法。"
    ),
    MVI(
        label = "MVI",
        summary = "用户意图进入 Store，状态单向流出，适合复杂交互和状态追踪。"
    ),
    CLEAN(
        label = "Clean",
        summary = "UseCase 和 Repository 隔离业务边界，适合中大型项目和测试优先的场景。"
    )
}
