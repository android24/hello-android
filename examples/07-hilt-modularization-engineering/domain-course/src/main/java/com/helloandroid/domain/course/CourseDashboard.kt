package com.helloandroid.domain.course

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.network.NetworkConfig

data class CourseDashboard(
    val lessons: List<CourseLesson>,
    val networkConfig: NetworkConfig,
    val refreshCount: Int
) {
    val syncedCount: Int
        get() = lessons.count { it.isSynced }
}
