package com.helloandroid.architecture.common

data class CourseLesson(
    val id: Long,
    val title: String,
    val description: String,
    val owner: String,
    val isCompleted: Boolean
)
