package fr.phytok.apps.cachecast.yas

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface YasApiClient {
    @GET("get/{videoId}")
    fun metaById(@Path("videoId") videoId: String) : Call<Search>

    @GET("cache/{videoId}")
    fun trackById(@Path("videoId") videoId: String) : Call<ResponseBody>
}