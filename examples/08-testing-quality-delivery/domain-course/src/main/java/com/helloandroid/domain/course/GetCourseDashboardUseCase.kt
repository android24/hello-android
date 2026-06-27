package com.helloandroid.domain.course

import javax.inject.Inject

class GetCourseDashboardUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    operator fun invoke() = repository.observeDashboard()
}
