package com.helloandroid.networkroomdatastore.data.repository

import com.helloandroid.networkroomdatastore.data.local.LessonEntity
import com.helloandroid.networkroomdatastore.data.remote.LessonDto
import com.helloandroid.networkroomdatastore.ui.LessonUiModel

fun LessonDto.toEntity(updatedAtMillis: Long): LessonEntity {
    return LessonEntity(
        id = id,
        title = title,
        description = description,
        level = level,
        isCompleted = isCompleted,
        updatedAtMillis = updatedAtMillis
    )
}

fun LessonEntity.toUiModel(): LessonUiModel {
    return LessonUiModel(
        id = id,
        title = title,
        description = description,
        level = level,
        isCompleted = isCompleted
    )
}
