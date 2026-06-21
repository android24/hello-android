package com.helloandroid.architecture.clean.presentation

import com.helloandroid.architecture.clean.domain.CleanCourseLesson
import com.helloandroid.architecture.common.CourseLesson

fun CleanCourseLesson.toUiModel(): CourseLesson {
    return CourseLesson(
        id = id,
        title = title,
        description = description,
        owner = "Clean",
        isCompleted = isCompleted
    )
}
