package com.helloandroid.architecture.mvc

import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

@Composable
fun MvcCourseRoute(source: SampleLessonSource) {
    val controller = remember(source) {
        MvcCourseController(CourseCatalogModel(source))
    }
    val scope = rememberCoroutineScope()
    var state by remember {
        mutableStateOf(
            CourseListUiState(
                isLoading = true,
                message = "MVC：View 持有状态，Controller 负责调度。"
            )
        )
    }

    fun refresh() {
        scope.launch {
            state = state.copy(isLoading = true, message = "MVC：Controller 正在请求 Model。")
            state = controller.refresh(showCompleted = state.showCompleted)
        }
    }

    LaunchedEffect(controller) {
        refresh()
    }

    CourseArchitecturePanel(
        mode = ArchitectureMode.MVC,
        state = state,
        boundaryNote = "代码位置：mvc/CourseCatalogModel、mvc/MvcCourseController、mvc/MvcCourseRoute。",
        onRefresh = ::refresh,
        onShowCompletedChange = { showCompleted ->
            state = controller.setShowCompleted(state, showCompleted)
        }
    )
}
