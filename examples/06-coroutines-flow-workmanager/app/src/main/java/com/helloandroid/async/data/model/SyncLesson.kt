package com.helloandroid.async.data.model

data class SyncLesson(
    val id: Long,
    val title: String,
    val description: String,
    val source: String,
    val isSynced: Boolean
)
