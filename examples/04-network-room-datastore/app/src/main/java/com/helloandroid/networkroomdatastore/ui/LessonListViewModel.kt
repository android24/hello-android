package com.helloandroid.networkroomdatastore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.helloandroid.networkroomdatastore.data.repository.LessonRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LessonListViewModel(
    private val repository: LessonRepository
) : ViewModel() {
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LessonListUiState> = combine(
        repository.lessons,
        repository.preferences,
        isLoading,
        errorMessage
    ) { lessons, preferences, loading, error ->
        val visibleLessons = if (preferences.showCompleted) {
            lessons
        } else {
            lessons.filterNot { it.isCompleted }
        }

        LessonListUiState(
            isLoading = loading,
            lessons = visibleLessons,
            errorMessage = error,
            showCompleted = preferences.showCompleted,
            lastSyncText = preferences.lastSyncMillis.toSyncText()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LessonListUiState()
    )

    init {
        refreshLessons()
    }

    fun refreshLessons(shouldFail: Boolean = false) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            runCatching {
                repository.refreshLessons(shouldFail = shouldFail)
            }.onFailure { throwable ->
                errorMessage.value = throwable.message ?: "同步失败，本地缓存仍然可用"
            }

            isLoading.value = false
        }
    }

    fun setShowCompleted(showCompleted: Boolean) {
        viewModelScope.launch {
            repository.setShowCompleted(showCompleted)
        }
    }

    private fun Long?.toSyncText(): String {
        if (this == null) {
            return "尚未同步"
        }

        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return "最近同步：${formatter.format(Date(this))}"
    }
}

class LessonListViewModelFactory(
    private val repository: LessonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LessonListViewModel(repository) as T
    }
}
