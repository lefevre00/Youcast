package fr.phytok.apps.cachecast.util

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.yas.YasApiClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Module
@InstallIn(ActivityComponent::class)
class InjectionModule () {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    @Provides
    fun provideExecutor(): Executor = executorService

    @Provides
    fun provideYasApiClient(): YasApiClient = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER)
        .addConverterFactory(JacksonConverterFactory.create(createMapper()))
        .build().create(YasApiClient::class.java)

}