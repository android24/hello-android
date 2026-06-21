package com.helloandroid.networkroomdatastore.data.remote

data class LessonDto(
    val id: Long,
    val title: String,
    val description: String,
    val level: String,
    val isCompleted: Boolean
)
