package com.example.presensi_android_stp.views.ubah_password

import com.google.gson.annotations.SerializedName

data class UbahPasswordRequest(

	@field:SerializedName("password_old")
	val passwordOld: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("password_confirmation")
	val passwordConfirmation: String? = null
)
