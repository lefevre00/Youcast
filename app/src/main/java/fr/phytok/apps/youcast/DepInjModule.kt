package fr.phytok.apps.youcast

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Module
@InstallIn(ActivityComponent::class)
class DepInjModule () {

    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    @Provides
    fun provideExecutor(): Executor = executorService

}