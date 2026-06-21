package com.helloandroid.networkroomdatastore.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val level: String,
    val isCompleted: Boolean,
    val updatedAtMillis: Long
)
