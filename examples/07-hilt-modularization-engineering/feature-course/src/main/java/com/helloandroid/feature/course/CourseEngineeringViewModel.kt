package com.helloandroid.feature.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helloandroid.domain.course.GetCourseDashboardUseCase
import com.helloandroid.domain.course.RefreshCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CourseEngineeringViewModel @Inject constructor(
    getCourseDashboard: GetCourseDashboardUseCase,
    private val refreshCourses: RefreshCoursesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CourseEngineeringUiState())
    val uiState: StateFlow<CourseEngineeringUiState> = _uiState

    init {
        getCourseDashboard()
            .onEach { dashboard ->
                _uiState.update {
                    it.copy(
                        dashboard = dashboard,
                        message = "Hilt 已装配 Repository，当前课程数：${dashboard.lessons.size}"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, message = "正在通过 Hilt 注入的数据源刷新课程") }

            runCatching { refreshCourses() }
                .onSuccess { report ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            message = "刷新完成：${report.changedCount} 门课程来自 ${report.source}"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            message = error.message ?: "刷新失败，请检查依赖链路"
                        )
                    }
                }
        }
    }
}
