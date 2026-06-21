package com.helloandroid.networkroomdatastore.ui

data class LessonListUiState(
    val isLoading: Boolean = false,
    val lessons: List<LessonUiModel> = emptyList(),
    val errorMessage: String? = null,
    val showCompleted: Boolean = true,
    val lastSyncText: String = "尚未同步"
)
