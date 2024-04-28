package com.charityright.bd.Retrofit

import android.content.Context
import com.aqualinkbd.agrotraceprinting.Models.LoginBaseResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiRequestInterface {

    @POST("v2/tobacco/login")
    fun loginApi(
        @Field("phone") phone: String,
        @Field("password") password: String,
    ): Call<LoginBaseResponse>



    companion object {

        private val httpClient = OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.MINUTES)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).build()

        operator fun invoke(applicationContext: Context): ApiRequestInterface {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .baseUrl("http://dev.agrotraces.com/api/")
                .build()
                .create(ApiRequestInterface::class.java)
        }
    }
}