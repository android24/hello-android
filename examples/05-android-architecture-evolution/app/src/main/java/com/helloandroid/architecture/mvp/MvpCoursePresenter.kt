package com.helloandroid.architecture.mvp

import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.CourseLesson
import com.helloandroid.architecture.common.CourseListUiState
import com.helloandroid.architecture.common.SampleLessonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MvpCoursePresenter(
    private val source: SampleLessonSource,
    private val scope: CoroutineScope
) : MvpCoursePresenterContract {
    private var view: MvpCourseView? = null
    private var lessons: List<CourseLesson> = emptyList()
    private var showCompleted: Boolean = true

    override fun attach(view: MvpCourseView) {
        this.view = view
        view.render(
            CourseListUiState(
                lessons = lessons,
                showCompleted = showCompleted,
                message = "MVP：View 已绑定 Presenter。"
            )
        )
    }

    override fun detach() {
        view = null
    }

    override fun refresh() {
        view?.render(
            CourseListUiState(
                isLoading = true,
                lessons = lessons,
                showCompleted = showCompleted,
                message = "MVP：Presenter 正在处理刷新事件。"
            )
        )

        scope.launch {
            lessons = source.loadLessons(ArchitectureMode.MVP)
            view?.render(
                CourseListUiState(
                    lessons = lessons,
                    showCompleted = showCompleted,
                    message = "MVP：Presenter 拿到数据后调用 View.render。"
                )
            )
        }
    }

    override fun setShowCompleted(showCompleted: Boolean) {
        this.showCompleted = showCompleted
        view?.render(
            CourseListUiState(
                lessons = lessons,
                showCompleted = showCompleted,
                message = "MVP：View 转发开关事件，Presenter 决定展示状态。"
            )
        )
    }
}
