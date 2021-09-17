package com.example.presensi_android_stp.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("data")
	val user: User? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class User(

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("foto")
	val foto: Any? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("level")
	val level: String? = null,

	@field:SerializedName("telepon")
	val telepon: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("email_verified_at")
	val emailVerifiedAt: Any? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("alamat")
	val alamat: String? = null
)

data class Meta(

	@field:SerializedName("token")
	val token: String? = null
)
