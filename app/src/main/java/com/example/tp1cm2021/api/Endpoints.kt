package com.example.tp1cm2021.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Endpoints {

    @GET("api/reports")
    fun getReports(): Call<List<Report>>

    //login endpoint
    @FormUrlEncoded
    @POST("api/login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginOutput>
}