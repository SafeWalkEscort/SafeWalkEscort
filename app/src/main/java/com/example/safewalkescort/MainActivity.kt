package com.example.safewalkescort

import android.content.res.AssetManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var myMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        myMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(37.0, 127.0)))
        readFromAssets("19_22_pedstrians utf-8.xlsx")
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