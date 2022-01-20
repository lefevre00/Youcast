package fr.phytok.apps.cachecast.util

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.db.AppDatabase
import fr.phytok.apps.cachecast.db.VideoDao
import fr.phytok.apps.cachecast.yas.YasApiClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Module
@InstallIn(SingletonComponent::class)
object InjectionModule {

    @Provides
    fun provideYasApiClient(): YasApiClient = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER)
        .addConverterFactory(JacksonConverterFactory.create(createMapper()))
        .build().create(YasApiClient::class.java)

    @Provides
    fun provideVideoDao(@ApplicationContext context: Context): VideoDao = AppDatabase.getInstance(context).videoDao()

    @Provides
    fun provideExecutorService(): ExecutorService = Executors.newFixedThreadPool(4)

}