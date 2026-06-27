package com.helloandroid.domain.course

import javax.inject.Inject

class RefreshCoursesUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): RefreshReport {
        return repository.refreshCourses()
    }
}
