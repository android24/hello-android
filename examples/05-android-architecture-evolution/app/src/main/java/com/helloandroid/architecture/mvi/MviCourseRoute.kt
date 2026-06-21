package com.helloandroid.architecture.mvi

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.SampleLessonSource
import com.helloandroid.architecture.ui.CourseArchitecturePanel

@Composable
fun MviCourseRoute(source: SampleLessonSource) {
    val store: CourseMviStore = viewModel(
        factory = CourseMviStoreFactory(source)
    )
    val state = store.state.collectAsStateWithLifecycle()

    CourseArchitecturePanel(
        mode = ArchitectureMode.MVI,
        state = state.value,
        boundaryNote = "代码位置：mvi/CourseMviIntent、mvi/CourseMviStore、mvi/MviCourseRoute。",
        onRefresh = { store.dispatch(CourseMviIntent.RefreshClicked) },
        onShowCompletedChange = { showCompleted ->
            store.dispatch(CourseMviIntent.ShowCompletedChanged(showCompleted))
        }
    )
}
