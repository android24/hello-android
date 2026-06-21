package com.helloandroid.architecture.mvp

import com.helloandroid.architecture.common.CourseListUiState

interface MvpCourseView {
    fun render(state: CourseListUiState)
}

interface MvpCoursePresenterContract {
    fun attach(view: MvpCourseView)
    fun detach()
    fun refresh()
    fun setShowCompleted(showCompleted: Boolean)
}
