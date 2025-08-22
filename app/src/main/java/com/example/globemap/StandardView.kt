package com.example.globemap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.example.globemap.adapters.SuggestionAdapter
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.plugin.LocationPuck2D

class StandardView : AppCompatActivity() {

    var firstLocation = true
    private var layerBtn: ImageButton? = null
    private var currentLocationBtn: ImageButton? = null
    private lateinit var mapView: MapView
    private var lastKnownPoint: Point? = null
    private val LOCATION_PERMISSION_REQUEST = 1001
    private lateinit var searchEditText: EditText
    private lateinit var searchBtn: ImageButton
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var suggestionsRecycler: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_standdard_mapview)

        searchEditText = findViewById(R.id.searchEditText)
        searchBtn = findViewById(R.id.searchButton)
        mapView = findViewById(R.id.mapView)
        layerBtn = findViewById(R.id.layersButton)
        currentLocationBtn = findViewById(R.id.targetButton)
        mapView.scalebar.enabled = false

        suggestionsRecycler = findViewById(R.id.suggestionsRecycler)
        suggestionAdapter = SuggestionAdapter(mutableListOf()) { feature ->
            val point = feature.center()
            if (point != null) {
                mapView.getMapboxMap().flyTo(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(12.0)
                        .build(),
                    mapAnimationOptions { duration(2000) }
                )
            }
            suggestionsRecycler.visibility = View.GONE
        }
        suggestionsRecycler.layoutManager = LinearLayoutManager(this)
        suggestionsRecycler.adapter = suggestionAdapter


        searchBtn.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchLocation(query)
            } else {
                Toast.makeText(this, "Enter location to search", Toast.LENGTH_SHORT).show()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length > 2) { // only after typing 3+ chars
                    fetchSuggestions(query)
                } else {
                    suggestionsRecycler.visibility = View.GONE
                }
            }
        })

        layerBtn?.setOnClickListener {
            val intent = Intent(this@StandardView, EarthMap::class.java)
            startActivityForResult(intent, 2001)
        }

        if (hasLocationPermission()) {
            enableLocationComponent()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun searchLocation(query: String) {
        val geocoding = MapboxGeocoding.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .query(query)
            .limit(1)
            .build()

        geocoding.enqueueCall(object : Callback<com.mapbox.api.geocoding.v5.models.GeocodingResponse> {
            override fun onResponse(
                call: Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                response: Response<com.mapbox.api.geocoding.v5.models.GeocodingResponse>
            ) {
                val results = response.body()?.features()
                if (!results.isNullOrEmpty()) {
                    val feature: CarmenFeature = results[0]
                    val point: Point = feature.center()!!

                    mapView.getMapboxMap().flyTo(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(12.0)
                            .build(),
                        mapAnimationOptions { duration(2000) }
                    )
                } else {
                    Toast.makeText(this@StandardView, "Location not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                t: Throwable
            ) {
                Toast.makeText(this@StandardView, "Search failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchSuggestions(query: String) {
        val geocoding = MapboxGeocoding.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .query(query)
            .autocomplete(true)
            .limit(5)
            .build()

        geocoding.enqueueCall(object : Callback<com.mapbox.api.geocoding.v5.models.GeocodingResponse> {
            override fun onResponse(
                call: Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                response: Response<com.mapbox.api.geocoding.v5.models.GeocodingResponse>
            ) {
                val results = response.body()?.features()
                if (!results.isNullOrEmpty()) {
                    suggestionAdapter.updateData(results)
                    suggestionsRecycler.visibility = View.VISIBLE
                } else {
                    suggestionsRecycler.visibility = View.GONE
                }
            }

            override fun onFailure(
                call: Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                t: Throwable
            ) {
                suggestionsRecycler.visibility = View.GONE
            }
        })
    }



    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableLocationComponent() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            val locationPlugin = mapView.location
            locationPlugin.updateSettings {
                enabled = true
                pulsingEnabled = false
                locationPuck = LocationPuck2D(
                    bearingImage = ImageHolder.from(R.drawable.current_location_icon)
                )
            }

            val gesturesPlugin = mapView.gestures
            gesturesPlugin.updateSettings {
                pinchToZoomEnabled = true
                scrollEnabled = true
                rotateEnabled = true
                pitchEnabled = true
            }

            locationPlugin.addOnIndicatorPositionChangedListener { point ->
                lastKnownPoint = point
                if (firstLocation) {
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(Point.fromLngLat(point.longitude(), point.latitude()))
                            .zoom(12.0)
                            .build()
                    )
                    firstLocation = false
                }
            }
        }

        currentLocationBtn?.setOnClickListener {
            if (lastKnownPoint != null) {
                mapView.getMapboxMap().flyTo(
                    CameraOptions.Builder()
                        .center(lastKnownPoint)
                        .zoom(12.0)
                        .bearing(0.0)
                        .pitch(10.0)
                        .build(),
                    mapAnimationOptions { duration(2000) }
                )
            } else {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationComponent()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && resultCode == RESULT_OK) {
            val selectedType = data?.getIntExtra("selectedMapType", 0) ?: 0
            changeMapStyle(selectedType)
        }
    }
    private fun changeMapStyle(type: Int) {
        val styleUrl = when (type) {
            0 -> com.mapbox.maps.Style.MAPBOX_STREETS
            1 -> com.mapbox.maps.Style.SATELLITE_STREETS
            2 -> com.mapbox.maps.Style.SATELLITE
            3 -> com.mapbox.maps.Style.OUTDOORS
            else -> com.mapbox.maps.Style.MAPBOX_STREETS
        }
        mapView.getMapboxMap().loadStyleUri(styleUrl)
    }
}
