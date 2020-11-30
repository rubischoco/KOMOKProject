package com.teamC.komok.retrofit

import retrofit2.Call
import retrofit2.http.GET

interface ApiServices {
    @GET("api/gallery")
    fun getGallery(): Call<List<GalleryResponse>>
}