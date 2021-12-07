package fr.phytok.apps.youcast.yas

import retrofit2.http.GET
import retrofit2.http.Path

interface YasApiClient {
    @GET("get/{videoId}")
    fun metaById(@Path("videoId") videoId: String) : Search

    @GET("cache/{videoId}")
    fun trackById(@Path("videoId") videoId: String) : ByteArray
}