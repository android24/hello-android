package com.helloandroid.basics

data class CourseInfo(
    val name: String,
    val stage: String,
    val lessonIndex: Int
)

fun buildWelcomeMessage(course: CourseInfo): String {
    return "欢迎来到${course.name}，当前阶段：${course.stage}，第 ${course.lessonIndex} 节"
}
