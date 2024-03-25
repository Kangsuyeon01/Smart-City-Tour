package com.aiproject.landmarkapp.retrofit


data class LandmarkModel(
    val id: Int,
    val lat: Double,
    val lng: Double,
    val title: String,
    val address: String,
    val tel: String,
    val homepage: String,
    val image: String,
    var distance : String,
    var dist : Float, // 현재 위치에서 랜드마크까지의 거리
    var diff_dist : Float, // 선택한 마커 주변 랜드마크 거리
    var diff_distance : String
)
