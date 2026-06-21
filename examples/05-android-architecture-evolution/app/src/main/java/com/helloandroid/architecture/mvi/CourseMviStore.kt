package com.helloandroid.architecture.mvi

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

class CourseMviStore(
    private val source: SampleLessonSource
) : ViewModel() {
    private val _state = MutableStateFlow(
        CourseListUiState(message = "MVI：所有变化都从 Intent 进入 Store。")
    )
    val state: StateFlow<CourseListUiState> = _state

    init {
        dispatch(CourseMviIntent.RefreshClicked)
    }

    fun dispatch(intent: CourseMviIntent) {
        when (intent) {
            CourseMviIntent.RefreshClicked -> refresh()
            is CourseMviIntent.ShowCompletedChanged -> {
                _state.update {
                    it.copy(
                        showCompleted = intent.showCompleted,
                        message = "MVI：ShowCompletedChanged Intent 已被 reduce 成新状态。"
                    )
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, message = "MVI：RefreshClicked Intent 开始处理。")
            }

            val lessons = source.loadLessons(ArchitectureMode.MVI)
            _state.update {
                it.copy(
                    isLoading = false,
                    lessons = lessons,
                    message = "MVI：数据返回后，Store 只向外暴露新的 State。"
                )
            }
        }
    }
}

class CourseMviStoreFactory(
    private val source: SampleLessonSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CourseMviStore(source) as T
    }
}
