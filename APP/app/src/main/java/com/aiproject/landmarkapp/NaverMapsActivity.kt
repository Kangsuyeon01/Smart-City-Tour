package com.aiproject.landmarkapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.aiproject.landmarkapp.adapter.LandmarkListAdapter
import com.aiproject.landmarkapp.adapter.LandmarkViewPagerAdapter
import com.aiproject.landmarkapp.retrofit.LandmarkDto
import com.aiproject.landmarkapp.retrofit.LandmarkModel
import com.aiproject.landmarkapp.retrofit.LandmarkService
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.math.roundToInt


class NaverMapsActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    private lateinit var mLocationRequest: LocationRequest
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수

    private val mapView: MapView by lazy { findViewById(R.id.mapView) }

    private val viewPager: ViewPager2 by lazy { findViewById(R.id.landmarkViewPager) }
    private val viewPagerAdapter = LandmarkViewPagerAdapter(itemClicked = {
        onLandmarkModelClicked(landmarkModel = it)
    })

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    private val recyclerViewAdapter = LandmarkListAdapter()

    private val currentLocationButton: LocationButtonView by lazy { findViewById(R.id.currentLocationButton) }

    private val bottomSheetTitleTextView: TextView by lazy { findViewById(R.id.bottomSheetTitleTextView) }
    private var lat: Double = 0.0
    private var lng: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_naver_maps)
        // onCreate 연결
        mapView.onCreate(savedInstanceState)

        // 맵 가져오기 -> onMapReady
        mapView.getMapAsync(this)

        mLocationRequest =  LocationRequest.create().apply {
            interval = 2000 // 업데이트 간격 단위(밀리초)
            fastestInterval = 1000 // 가장 빠른 업데이트 간격 단위(밀리초)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 정확성
            maxWaitTime= 2000 // 위치 갱신 요청 최대 대기 시간 (밀리초)
        }
        startLocationUpdates()
        initLandmarkViewPager()
        initLandmarkRecyclerView()
    }

    // 맵 가져오기(from: getMapAsync)
    override fun onMapReady(map: NaverMap) {
        this.naverMap = map

        // 사용자 현재 위치 받아오기
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0
        // 지도 위치 이동
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
        naverMap.moveCamera(cameraUpdate)

        // 현위치 버튼 기능
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false // 뷰 페이져에 가려져 이후 레이아웃에 정의 하였음.

        currentLocationButton.map = naverMap // 이후 정의한 현위치 버튼에 네이버맵 연결

//         -> onRequestPermissionsResult // 위치 권한 요청
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        // 지도 다 로드 이후에 가져오기
        getLandmarkListFromAPI()

    }

    private fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")
        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "startLocationUpdates() 두 위치 권한중 하나라도 없는 경우 ")
            return
        }
        Log.d(TAG, "startLocationUpdates() 위치 권한이 하나라도 존재하는 경우")
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청합니다.
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(TAG, "onLocationResult()")
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged()")
        mLastLocation = location
        lat = mLastLocation.latitude // 갱신 된 위도
        lng = mLastLocation.longitude // 갱신 된 경도
    }

    private fun getLandmarkListFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        retrofit.create(LandmarkService::class.java).also { it ->
            it.getLandmarkList()
                .enqueue(object : Callback<LandmarkDto> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(call: Call<LandmarkDto>, response: Response<LandmarkDto>) {
                        if (response.isSuccessful.not()) {
                            // fail
                            Log.d("Retrofit", "실패1")

                            return
                        }

                        // 성공한 경우 아래 처리
                        response.body()?.let { dto ->
                            updateMarker(dto.items)
                            recyclerViewAdapter.notifyDataSetChanged()
                            viewPagerAdapter.submitList(dto.items.sortedBy { it.dist })
                            recyclerViewAdapter.submitList(dto.items.sortedBy { it.dist })
                        }

                    }

                    override fun onFailure(call: Call<LandmarkDto>, t: Throwable) {
                        // 실패 처리 구현;
                        Log.d("Retrofit", "실패2")
                        Log.d("Retrofit", t.stackTraceToString())
                    }

                })
        }
    }

    private fun initLandmarkViewPager() {
        viewPager.adapter = viewPagerAdapter

        // page 변경시 처리
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]

                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                        .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    private fun initLandmarkRecyclerView() {
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateMarker(landmarks: List<LandmarkModel>) {
        val presentMarker = Marker()


        presentMarker.position = LatLng(lat, lng) // 현재위치 업데이트
        presentMarker.map = naverMap
        presentMarker.icon = MarkerIcons.BLACK
        presentMarker.iconTintColor = Color.GREEN

        landmarks.forEach { landmark ->

            val marker = Marker()
            marker.position = LatLng(landmark.lat, landmark.lng)
            marker.onClickListener = this // 마커 클릭 시 뷰 페이져 연동 되도록 구현
            marker.map = naverMap
            marker.tag = landmark.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
            marker.alpha = 0.8f

            var lanDistance = getDistance(lat, lng, landmark.lat, landmark.lng)

            landmark.dist = lanDistance
            // 1 mile = 1.609344 km

            if (lanDistance > 1000){
                lanDistance /= 1000
                lanDistance = ((lanDistance*100).roundToInt() / 100.0 ).toFloat() // 1m 이상 거리는 km 환산, 소숫점 둘째자리까지 표현
                val mileDist = (((lanDistance / 1.609344)*100).roundToInt() / 100.0).toFloat()
                landmark.distance = lanDistance.toString() + "km " + "("+mileDist.toString() + "mi) from current location"
            }
            else{
                lanDistance = ((lanDistance*100).roundToInt() / 100.0).toFloat() // 소숫점 둘째자리 까지 표현
                val mileDist = ((((lanDistance / 1.609344) * 0.001)*100).roundToInt() / 100.0).toFloat()
                landmark.distance = lanDistance.toString() + "m " + "("+mileDist.toString() + "mi) from current location"
            }

            recyclerViewAdapter.notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
            return

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                // 권한 설정 거부시 위치 추적을 사용하지 않음
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    // 지도 marker 클릭 시
    override fun onClick(overlay: Overlay): Boolean {
        // overlay : 마커
        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }
        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        val selectedModel2 = recyclerViewAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }
        selectedModel2?.let {
            val position = recyclerViewAdapter.currentList.indexOf(it)
            recyclerView.scrollToPosition(position)
        }
        return true
    }

    private fun onLandmarkModelClicked(landmarkModel: LandmarkModel) {
        // 공유 기능; 인텐트에있는 츄져사용할것임
        val intent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "[Seoul Landmark] ${landmarkModel.title} ${landmarkModel.tel} ${landmarkModel.address} 사진 보기(${landmarkModel.image}",
                )
                type = "text/plain"
            }
        startActivity(Intent.createChooser(intent, null))
    }
    // 좌표로 거리구하기
    private fun getDistance( lat1: Double, lng1:Double, lat2:Double, lng2:Double) : Float{
        val myLoc = Location(LocationManager.NETWORK_PROVIDER)
        val targetLoc = Location(LocationManager.NETWORK_PROVIDER)
        myLoc.latitude= lat1
        myLoc.longitude = lng1

        targetLoc.latitude= lat2
        targetLoc.longitude = lng2

        return myLoc.distanceTo(targetLoc)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}