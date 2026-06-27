package com.helloandroid.core.model

data class CourseLesson(
    val id: String,
    val title: String,
    val summary: String,
    val difficulty: LessonDifficulty,
    val isSynced: Boolean
)

enum class LessonDifficulty {
    Beginner,
    Intermediate,
    Advanced
}
