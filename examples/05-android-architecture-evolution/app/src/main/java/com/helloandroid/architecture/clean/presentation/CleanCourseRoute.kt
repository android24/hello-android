package com.helloandroid.architecture.clean.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.helloandroid.architecture.clean.data.SampleCourseRepository
import com.helloandroid.architecture.clean.domain.GetCourseLessonsUseCase
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.SampleLessonSource
import com.helloandroid.architecture.ui.CourseArchitecturePanel

@Composable
fun CleanCourseRoute(source: SampleLessonSource) {
    val useCase = remember(source) {
        GetCourseLessonsUseCase(SampleCourseRepository(source))
    }
    val viewModel: CleanCourseViewModel = viewModel(
        factory = CleanCourseViewModelFactory(useCase)
    )
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    CourseArchitecturePanel(
        mode = ArchitectureMode.CLEAN,
        state = state.value,
        boundaryNote = "代码位置：clean/domain、clean/data、clean/presentation。",
        onRefresh = viewModel::refresh,
        onShowCompletedChange = viewModel::setShowCompleted
    )
}
