package com.helloandroid.architecture.mvvm

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.SampleLessonSource
import com.helloandroid.architecture.ui.CourseArchitecturePanel

@Composable
fun MvvmCourseRoute(source: SampleLessonSource) {
    val viewModel: MvvmCourseViewModel = viewModel(
        factory = MvvmCourseViewModelFactory(source)
    )
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    CourseArchitecturePanel(
        mode = ArchitectureMode.MVVM,
        state = state.value,
        boundaryNote = "代码位置：mvvm/MvvmCourseViewModel、mvvm/MvvmCourseRoute。",
        onRefresh = viewModel::refresh,
        onShowCompletedChange = viewModel::setShowCompleted
    )
}
