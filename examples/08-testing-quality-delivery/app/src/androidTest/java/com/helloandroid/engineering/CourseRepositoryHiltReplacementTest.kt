package com.helloandroid.engineering

import com.helloandroid.core.model.CourseLesson
import com.helloandroid.core.model.LessonDifficulty
import com.helloandroid.core.network.NetworkConfig
import com.helloandroid.data.course.di.RepositoryModule
import com.helloandroid.domain.course.CourseDashboard
import com.helloandroid.domain.course.CourseRepository
import com.helloandroid.domain.course.RefreshReport
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class CourseRepositoryHiltReplacementTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val fakeRepository: CourseRepository = FakeCourseRepository()

    @Inject
    lateinit var injectedRepository: CourseRepository

    @Test
    fun hiltInjectsFakeRepositoryInTest() {
        hiltRule.inject()

        assertSame(fakeRepository, injectedRepository)
    }

    private class FakeCourseRepository : CourseRepository {
        override fun observeDashboard(): Flow<CourseDashboard> {
            return flowOf(
                CourseDashboard(
                    lessons = listOf(
                        CourseLesson(
                            id = "fake",
                            title = "Hilt 测试替换",
                            summary = "测试环境使用 Fake Repository，不访问真实数据源。",
                            difficulty = LessonDifficulty.Advanced,
                            isSynced = true
                        )
                    ),
                    networkConfig = NetworkConfig(
                        baseUrl = "https://test.local/",
                        environmentName = "test",
                        verboseLogEnabled = true
                    ),
                    refreshCount = 0
                )
            )
        }

        override suspend fun refreshCourses(): RefreshReport {
            return RefreshReport(
                changedCount = 1,
                source = "hilt-fake"
            )
        }
    }
}
