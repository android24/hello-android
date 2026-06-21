package com.helloandroid.architecture.clean.domain

class GetCourseLessonsUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): List<CleanCourseLesson> {
        return repository.getLessons()
    }
}
