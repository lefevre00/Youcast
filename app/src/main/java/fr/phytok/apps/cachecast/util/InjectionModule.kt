package fr.phytok.apps.cachecast.util

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.components.SingletonComponent
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.yas.YasApiClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object InjectionModule {

    @Provides
    fun provideYasApiClient(): YasApiClient = Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER)
        .addConverterFactory(JacksonConverterFactory.create(createMapper()))
        .build().create(YasApiClient::class.java)

}