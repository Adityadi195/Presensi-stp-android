package com.example.presensi_android_stp.networking

import com.example.presensi_android_stp.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface PresensiApi {

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("auth/login")
    fun loginRequest(@Body body: String): Call<LoginResponse>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("auth/password/forgot")
    fun lupaPasswordRequest(@Body body: String): Call<LupaPasswordResponse>

    @Multipart
    @Headers( "Accept: application/json")
    @POST("presensi")
    fun presensi(@Header("Authorization") token: String,
                 @PartMap params: HashMap<String, RequestBody>,
                 @Part foto: MultipartBody.Part
    ): Call<PresensiResponse>

    @Headers("Accept: application/json")
    @GET("presensi/riwayat")
    fun getRiwayatPresensi(@Header("Authorization") token: String,
                           @Query("from") fromDate: String,
                           @Query("to") toDate: String
    ): Call<RiwayatResponse>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("auth/logout")
    fun logoutRequest(@Header("Authorization") token: String): Call<LogoutResponse>

    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("auth/password/reset")
    fun ubahPassword(@Header("Authorization") token: String, @Body body: String): Call<UbahPasswordResponse>
}