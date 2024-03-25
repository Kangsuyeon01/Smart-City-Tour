package com.aiproject.landmarkapp.retrofit

import retrofit2.Call
import retrofit2.http.GET

// for retrofit
interface LandmarkService {
    @GET("/v3/afb7a53c-4a71-4c8b-b02b-93de2b5bf64c") // 생성해둔 mocky 주소
    fun getLandmarkList(): Call<LandmarkDto>
}