package com.helloandroid.architecture.common

data class CourseListUiState(
    val isLoading: Boolean = false,
    val lessons: List<CourseLesson> = emptyList(),
    val showCompleted: Boolean = true,
    val message: String = "等待刷新课程列表"
) {
    val visibleLessons: List<CourseLesson>
        get() = if (showCompleted) {
            lessons
        } else {
            lessons.filterNot { it.isCompleted }
        }
}
