package com.helloandroid.architecture.mvi

sealed interface CourseMviIntent {
    data object RefreshClicked : CourseMviIntent
    data class ShowCompletedChanged(val showCompleted: Boolean) : CourseMviIntent
}
