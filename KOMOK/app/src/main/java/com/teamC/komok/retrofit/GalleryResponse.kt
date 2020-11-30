package com.teamC.komok.retrofit

import com.google.gson.annotations.SerializedName

data class GalleryResponse(
    val id: Int,
    @SerializedName("nama")
    val name: String,
    @SerializedName("gambar")
    val link: String,
    @SerializedName("tipe")
    val type: Int
//    val title: String,
//    val body: String
)
