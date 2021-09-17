package com.example.presensi_android_stp.networking

import retrofit2.create

object ApiServices  {
    fun getPresensiApi(): PresensiApi{
        return RetrofitNetworkingClient
            .getClient()
            .create(PresensiApi::class.java)
    }
}