package com.helloandroid.data.course.remote

class FakeCourseRemoteDataSource(
    private val api: FakeCourseApi
) : CourseRemoteDataSource {
    override suspend fun fetchLessons() = api.requestLessons()
}
