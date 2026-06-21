package com.helloandroid.architecture.clean.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.helloandroid.architecture.clean.domain.GetCourseLessonsUseCase
import com.helloandroid.architecture.common.CourseListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CleanCourseViewModel(
    private val getCourseLessonsUseCase: GetCourseLessonsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CourseListUiState(message = "Clean：Presentation 只依赖 UseCase。")
    )
    val uiState: StateFlow<CourseListUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, message = "Clean：UseCase 正在执行业务动作。")
            }

            val lessons = getCourseLessonsUseCase().map { it.toUiModel() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    lessons = lessons,
                    message = "Clean：数据经过 domain -> presentation 后进入 UI。"
                )
            }
        }
    }

    fun setShowCompleted(showCompleted: Boolean) {
        _uiState.update {
            it.copy(
                showCompleted = showCompleted,
                message = "Clean：UI 配置仍由 Presentation 层维护，业务数据来自 UseCase。"
            )
        }
    }
}

class CleanCourseViewModelFactory(
    private val getCourseLessonsUseCase: GetCourseLessonsUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CleanCourseViewModel(getCourseLessonsUseCase) as T
    }
}
