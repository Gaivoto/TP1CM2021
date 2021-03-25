package com.example.tp1cm2021.api

import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*

interface Endpoints {

    //get reports endpoint
    @GET("api/reports")
    fun getReports(
            @Query("curLat") curLat: Float,
            @Query("curLon") curLon: Float,
            @Query("radius") radius: Int?,
            @Query("tipo") tipo: String?
    ): Call<List<Report>>

    //login endpoint
    @FormUrlEncoded
    @POST("api/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginOutput>
}