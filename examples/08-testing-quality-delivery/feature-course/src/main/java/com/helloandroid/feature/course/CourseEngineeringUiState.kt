package com.helloandroid.feature.course

import com.helloandroid.domain.course.CourseDashboard

data class CourseEngineeringUiState(
    val dashboard: CourseDashboard? = null,
    val isRefreshing: Boolean = false,
    val message: String = "等待 Hilt 完成第一次对象装配"
)
