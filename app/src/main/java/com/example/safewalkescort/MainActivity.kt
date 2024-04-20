package com.example.safewalkescort

import android.content.res.AssetManager
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory.fromAsset
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    // 위치 권한 요청 코드
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 화면 모서리에도 렌더링 허용
        setContentView(R.layout.activity_main) // 레이아웃을 activity_main으로 설정

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment // id가 map인 프래그먼트를 찾기
        mapFragment.getMapAsync(this) // 지도에 관해 비동기 처리

        // 위치 권한 요청
        requestLocationPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap.moveCamera(CameraUpdateFactory.zoomTo(16.0f))
//        myMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(37.0, 127.0)))
        if (isLocationPermissionGranted()) {
            getDeviceLocation()
        }

        // TODO:
        //  스레드로 처리 구현해야함!!!!!!
        //  그렇지 않으면 메인 스레드에서 I/O Block이 걸리게 될 것이며
        //  프로그램이 응답이 없어진다.
        readFromAssets("19_22_pedstrians utf-8.xlsx")
    }

    // 위치 권한 요청
    private fun requestLocationPermission() {
        if (!isLocationPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // 위치 권한이 허용되었는지 확인
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 위치 권한이 허용된 경우 현재 위치 가져오기
                getDeviceLocation()
            } else {
                // 위치 권한이 거부된 경우, 사용자에게 설명하거나 대안을 제시하는 등의 처리 수행
                // 예시: AlertDialog를 통해 권한의 필요성을 설명하고 설정 화면으로 이동할 수 있도록 안내
            }
        }
    }

    // 현재 위치 가져오기
    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    println(currentLatLng)
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                }
            }
    }

    private fun addMarker(googleMap: GoogleMap, title: String, latLng: LatLng) {
        googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(fromAsset("green_1.png"))
        )
    }

    private fun readFromAssets(fileName: String) {
        val assetManager: AssetManager = applicationContext.assets
        val inputStream: InputStream = assetManager.open(fileName)

        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0) // 첫 번째 시트를 가져옴

        // F열과 M열과 N열의 인덱스
        val fColumnIndex = 5  // F열의 인덱스 지점명
        val mColumnIndex = 12 // M열의 인덱스 경도
        val nColumnIndex = 13 // N열의 인덱스 위도

        // 원하는 작업 수행
        for (row in sheet) {
            val fCellValue = row.getCell(fColumnIndex)?.toString() ?: "" // F열의 데이터 읽기
            val mCellValue = row.getCell(mColumnIndex)?.toString() ?: "" // M열의 데이터 읽기
            val nCellValue = row.getCell(nColumnIndex)?.toString() ?: "" // N열의 데이터 읽기

            // String을 Double로 변환, 실패할 경우 null 반환
            val mValue = mCellValue.toDoubleOrNull()
            val nValue = nCellValue.toDoubleOrNull()

            // 변환 실패한 경우 건너뛰기
            if (mValue == null || nValue == null) {
                continue
            }

            // 데이터 출력
            println("F열 데이터: $fCellValue, M열 데이터: $mCellValue, N열 데이터: $nCellValue")
            addMarker(myMap, fCellValue, LatLng(nCellValue.toDouble(), mCellValue.toDouble()))
        }

        workbook.close()
        inputStream.close()
    }
}