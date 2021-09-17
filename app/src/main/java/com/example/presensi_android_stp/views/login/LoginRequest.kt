package com.example.presensi_android_stp.views.login

import com.google.gson.annotations.SerializedName

data class LoginRequest(

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("perangkat")
	val perangkat: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
