package com.helloandroid.engineering.di

import com.helloandroid.core.network.NetworkConfig
import com.helloandroid.engineering.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppEnvironmentModule {
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = BuildConfig.BASE_URL,
            environmentName = BuildConfig.ENVIRONMENT_NAME,
            verboseLogEnabled = BuildConfig.ENABLE_VERBOSE_LOG
        )
    }
}
