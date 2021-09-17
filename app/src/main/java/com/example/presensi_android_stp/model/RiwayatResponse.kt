package com.example.presensi_android_stp.model

import com.google.gson.annotations.SerializedName

data class RiwayatResponse(

    @field:SerializedName("data")
    val histories: List<Riwayat?>? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class Riwayat(

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("user_id")
    val userId: Int? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("detail")
    val detail: List<DetailRiwayat?>? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class DetailRiwayat(

    @field:SerializedName("presensi_id")
    val presensiId: Int? = null,

    @field:SerializedName("lokasi")
    val lokasi: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("foto")
    val foto: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("long")
    val jsonMemberLong: String? = null,

    @field:SerializedName("lat")
    val lat: String? = null
)