package com.helloandroid.composebasics

data class LessonItem(
    val index: Int,
    val title: String,
    val description: String
)

fun composeLessons(): List<LessonItem> {
    return listOf(
        LessonItem(
            index = 1,
            title = "Jetpack Compose 入门",
            description = "理解声明式 UI、Composable 与 setContent。"
        ),
        LessonItem(
            index = 2,
            title = "Composable、Modifier 与基础布局",
            description = "学习页面拆分、修饰符和 Column / Row / Box。"
        ),
        LessonItem(
            index = 3,
            title = "状态与事件",
            description = "使用 remember 管理简单状态，并响应按钮点击。"
        )
    )
}
