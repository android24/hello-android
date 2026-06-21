package com.helloandroid.architecture.mvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.CourseListUiState
import com.helloandroid.architecture.common.SampleLessonSource
import com.helloandroid.architecture.ui.CourseArchitecturePanel

@Composable
fun MvpCourseRoute(source: SampleLessonSource) {
    val scope = rememberCoroutineScope()
    val presenter = remember(source) { MvpCoursePresenter(source, scope) }
    var uiState by remember {
        mutableStateOf(CourseListUiState(message = "MVP：等待 Presenter 绑定 View。"))
    }
    val view = remember {
        object : MvpCourseView {
            override fun render(state: CourseListUiState) {
                uiState = state
            }
        }
    }

    DisposableEffect(presenter, view) {
        presenter.attach(view)
        onDispose { presenter.detach() }
    }

    LaunchedEffect(presenter) {
        presenter.refresh()
    }

    CourseArchitecturePanel(
        mode = ArchitectureMode.MVP,
        state = uiState,
        boundaryNote = "代码位置：mvp/MvpCourseContract、mvp/MvpCoursePresenter、mvp/MvpCourseRoute。",
        onRefresh = presenter::refresh,
        onShowCompletedChange = presenter::setShowCompleted
    )
}
