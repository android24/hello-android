package com.helloandroid.data.course.di

import com.helloandroid.data.course.remote.CourseRemoteDataSource
import com.helloandroid.data.course.remote.FakeCourseApi
import com.helloandroid.data.course.remote.FakeCourseRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideCourseRemoteDataSource(
        api: FakeCourseApi
    ): CourseRemoteDataSource {
        return FakeCourseRemoteDataSource(api)
    }
}
