package com.aiproject.landmarkapp.retrofit

import com.google.gson.annotations.SerializedName

data class LandmarkDto(
    @SerializedName("landmarks") val items: List<LandmarkModel>
)
