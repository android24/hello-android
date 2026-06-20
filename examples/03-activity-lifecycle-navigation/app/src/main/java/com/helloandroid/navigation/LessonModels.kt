package com.helloandroid.navigation

data class NavigationLesson(
    val index: Int,
    val title: String,
    val description: String
)

fun navigationLessons(): List<NavigationLesson> {
    return listOf(
        NavigationLesson(
            index = 1,
            title = "Activity 与应用入口",
            description = "理解 Manifest、入口 Activity 与系统启动规则。"
        ),
        NavigationLesson(
            index = 2,
            title = "生命周期日志",
            description = "通过 Logcat 观察 onCreate、onStart、onResume 等回调。"
        ),
        NavigationLesson(
            index = 3,
            title = "Intent 页面跳转",
            description = "使用显式 Intent 打开详情页，并传递简单参数。"
        ),
        NavigationLesson(
            index = 4,
            title = "返回键与任务栈",
            description = "理解页面打开、返回和 Activity 栈之间的关系。"
        )
    )
}
