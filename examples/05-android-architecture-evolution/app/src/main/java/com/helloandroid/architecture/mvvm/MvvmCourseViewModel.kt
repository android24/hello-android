package com.helloandroid.architecture.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.helloandroid.architecture.common.ArchitectureMode
import com.helloandroid.architecture.common.CourseListUiState
import com.helloandroid.architecture.common.SampleLessonSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MvvmCourseViewModel(
    private val source: SampleLessonSource
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CourseListUiState(message = "MVVM：ViewModel 准备维护 UI State。")
    )
    val uiState: StateFlow<CourseListUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, message = "MVVM：ViewModel 正在刷新数据。")
            }

            val lessons = source.loadLessons(ArchitectureMode.MVVM)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    lessons = lessons,
                    message = "MVVM：UI 只观察状态，不直接关心数据来源。"
                )
            }
        }
    }

    fun setShowCompleted(showCompleted: Boolean) {
        _uiState.update {
            it.copy(
                showCompleted = showCompleted,
                message = "MVVM：配置变化进入 ViewModel，再产出新的 UI State。"
            )
        }
    }
}

class MvvmCourseViewModelFactory(
    private val source: SampleLessonSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MvvmCourseViewModel(source) as T
    }
}
