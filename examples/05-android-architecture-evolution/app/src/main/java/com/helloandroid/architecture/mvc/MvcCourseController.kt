package com.helloandroid.architecture.mvc

import com.helloandroid.architecture.common.CourseListUiState

class MvcCourseController(
    private val model: CourseCatalogModel
) {
    suspend fun refresh(showCompleted: Boolean): CourseListUiState {
        val lessons = model.loadLessons()
        return CourseListUiState(
            lessons = lessons,
            showCompleted = showCompleted,
            message = "MVC：Controller 读取 Model 后直接组装页面状态。"
        )
    }

    fun setShowCompleted(
        currentState: CourseListUiState,
        showCompleted: Boolean
    ): CourseListUiState {
        return currentState.copy(
            showCompleted = showCompleted,
            message = "MVC：页面配置仍然由 Controller 和 View 一起维护。"
        )
    }
}
