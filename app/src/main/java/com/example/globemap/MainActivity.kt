package com.example.globemap

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.adapters.FeaturesAdapter
import com.example.globemap.models.FeatureItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.common.Cancelable
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.OnScaleListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var userInteracting = false
    private var spinEnabled = true
    private lateinit var mapboxMap: MapboxMap
    private var runningAnimation: Cancelable? = null
    private lateinit var mapView: MapView
    private lateinit var btnSearch: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var btnDrawer: ImageButton
    private var markerManager: PointAnnotationManager? = null

    private fun spinGlobe() {
        val zoom = mapboxMap.cameraState.zoom
        if (spinEnabled && !userInteracting && zoom < MAX_SPIN_ZOOM) {
            var distancePerSecond = 360.0 / SECONDS_PER_REVOLUTION
            if (zoom > SLOW_SPIN_ZOOM) {
                val zoomDif = (MAX_SPIN_ZOOM - zoom) / (MAX_SPIN_ZOOM - SLOW_SPIN_ZOOM)
                distancePerSecond *= zoomDif
            }
            val center = mapboxMap.cameraState.center
            val targetCenter = Point.fromLngLat(center.longitude() - distancePerSecond, center.latitude())
            runningAnimation = mapboxMap.easeTo(
                cameraOptions { center(targetCenter) },
                mapAnimationOptions {
                    duration(1_000L)
                    interpolator(LinearInterpolator())
                },
                animatorListener = object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        spinGlobe()
                    }
                }
            )
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = MapView(this)
        mapView.scalebar.enabled = false

        btnSearch = findViewById(R.id.btn_search)
        searchEditText = findViewById(R.id.searchEditText)
        btnDrawer = findViewById(R.id.btn_drawer)

        val featureList = listOf(
            FeatureItem(R.drawable.globe_ic, "Earth Map", resources.getColor(R.color.purple)),
            FeatureItem(R.drawable.arrow_ic, "GPS Navigation", resources.getColor(R.color.cream)),
            FeatureItem(R.drawable.location_share, "Location Sharing", resources.getColor(R.color.skyBlue)),
            FeatureItem(R.drawable.camera_ic, "Live Web Camera", resources.getColor(R.color.green)),
            FeatureItem(R.drawable.traffic_ic, "Traffic Alert", resources.getColor(R.color.pink)),
            FeatureItem(R.drawable.locations_ic, "Famous Places", resources.getColor(R.color.LightBlue)),
            FeatureItem(R.drawable.route_ico, "Route Finder", resources.getColor(R.color.cream2)),
            FeatureItem(R.drawable.area_ic, "Area Calculator", resources.getColor(R.color.yellow))
        )

        val recyclerView = findViewById<RecyclerView>(R.id.features_recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = FeaturesAdapter(featureList).apply {
            setOnItemClickListener(object : FeaturesAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    when (position) {
                        0 -> startActivity(Intent(this@MainActivity, StandardView::class.java))
                    }
                }
            })
        }

        // Gesture listeners
        mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                userInteracting = true
                runningAnimation?.cancel()
            }
            override fun onMove(detector: MoveGestureDetector) = false
            override fun onMoveEnd(detector: MoveGestureDetector) {
                userInteracting = false
                spinGlobe()
            }
        })
        mapView.gestures.addOnRotateListener(object : OnRotateListener {
            override fun onRotateBegin(detector: RotateGestureDetector) {
                userInteracting = true
                runningAnimation?.cancel()
            }
            override fun onRotate(detector: RotateGestureDetector) {}
            override fun onRotateEnd(detector: RotateGestureDetector) {
                userInteracting = false
                spinGlobe()
            }
        })
        mapView.gestures.addOnScaleListener(object : OnScaleListener {
            override fun onScaleBegin(detector: StandardScaleGestureDetector) {
                userInteracting = true
                runningAnimation?.cancel()
            }
            override fun onScale(detector: StandardScaleGestureDetector) {}
            override fun onScaleEnd(detector: StandardScaleGestureDetector) {
                userInteracting = false
                spinGlobe()
            }
        })

        val mapContainer = findViewById<FrameLayout>(R.id.map_container)
        mapContainer.addView(mapView)

        mapboxMap = mapView.mapboxMap

        mapView.mapboxMap.apply {
            setCamera(
                cameraOptions {
                    center(CENTER)
                    zoom(ZOOM)
                    padding(EdgeInsets(0.0, 0.0, 180.0, 0.0))
                }
            )
            spinGlobe()
            loadStyle(
                style(Style.SATELLITE_STREETS) {
                    +atmosphere { }
                    +projection(ProjectionName.GLOBE)
                }
            ) {
                addMarkerOnGlobe(
                    lng = 74.3587,  // Lahore
                    lat = 31.5204,
                    iconRes = R.drawable.globe_icon
                )
            }
        }

        searchEditText.setOnClickListener {
            val query = searchEditText.text.toString().trim()

            if (query.isNotEmpty()) {
                // Case 1: Check if user entered coordinates (lat,lng)
                if (query.contains(",")) {
                    val parts = query.split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].toDoubleOrNull()
                        val lng = parts[1].toDoubleOrNull()
                        if (lat != null && lng != null) {
                            placeMarkerOnGlobe(Point.fromLngLat(lng, lat))
                        } else {
                            Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Enter coordinates as: lat,lng", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Case 2: Place name → use Mapbox Geocoding API
                    val geocoding = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .query(query)
                        .limit(1)
                        .build()

                    geocoding.enqueueCall(object : retrofit2.Callback<com.mapbox.api.geocoding.v5.models.GeocodingResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                            response: retrofit2.Response<com.mapbox.api.geocoding.v5.models.GeocodingResponse>
                        ) {
                            val results = response.body()?.features()
                            if (!results.isNullOrEmpty()) {
                                val feature = results[0]
                                val point = feature.center()
                                if (point != null) {
                                    placeMarkerOnGlobe(point)
                                }
                            } else {
                                Toast.makeText(this@MainActivity, "No results found", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(
                            call: retrofit2.Call<com.mapbox.api.geocoding.v5.models.GeocodingResponse>,
                            t: Throwable
                        ) {
                            t.printStackTrace()
                            Toast.makeText(this@MainActivity, "Search failed: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } else {
                Toast.makeText(this, "Enter a location", Toast.LENGTH_SHORT).show()
            }
        }


        // Search button click → expand EditText
        btnSearch.setOnClickListener {
            if (searchEditText.visibility == View.GONE) {
                btnSearch.visibility = View.GONE
                searchEditText.visibility = View.VISIBLE
                searchEditText.alpha = 0f   // start invisible
                searchEditText.translationX = btnSearch.width.toFloat() // start from button position
                searchEditText.requestFocus()

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                // Animate slide + fade
                searchEditText.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null)
                    .start()

                // Smooth width expansion
                val startWidth = 0
                val endWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    300f,
                    resources.displayMetrics
                ).toInt()

                val anim = ValueAnimator.ofInt(startWidth, endWidth)
                anim.duration = 300
                anim.addUpdateListener { valueAnimator ->
                    val params = searchEditText.layoutParams
                    params.width = valueAnimator.animatedValue as Int
                    searchEditText.layoutParams = params
                }
                anim.start()

                // Show keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
            }
        }


        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }, 2000)
    }

    private fun addMarkerOnGlobe(lng: Double, lat: Double, iconRes: Int) {
        if (markerManager == null) {
            markerManager = mapView.annotations.createPointAnnotationManager()
        } else {
            markerManager?.deleteAll()
        }

        val bmp = BitmapFactory.decodeResource(resources, iconRes)
            ?: BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_mylocation)

        val opts = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(lng, lat))
            .withIconImage(bmp)
            .withIconSize(0.3)

        markerManager?.create(opts)
    }

    private fun collapseSearchBar() {
        val startWidth = searchEditText.width
        val endWidth = 0

        // Animate width shrinking
        val widthAnim = ValueAnimator.ofInt(startWidth, endWidth)
        widthAnim.duration = 250
        widthAnim.addUpdateListener { valueAnimator ->
            val params = searchEditText.layoutParams
            params.width = valueAnimator.animatedValue as Int
            searchEditText.layoutParams = params
        }

        // Animate fade + slide
        searchEditText.animate()
            .translationX(btnSearch.width.toFloat())
            .alpha(0f)
            .setDuration(250)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    searchEditText.visibility = View.GONE
                    btnSearch.visibility = View.VISIBLE
                }
            }).start()

        widthAnim.start()

        // Hide keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    // Detect taps outside EditText to collapse
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (searchEditText.visibility == View.VISIBLE) {
            val rect = Rect()
            searchEditText.getGlobalVisibleRect(rect)
            if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                collapseSearchBar()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun placeMarkerOnGlobe(point: Point) {
        addMarkerOnGlobe(point.longitude(), point.latitude(), R.drawable.globe_icon)

        mapView.getMapboxMap().easeTo(
            cameraOptions {
                center(point)
            },
            mapAnimationOptions { duration(2000) }
        )
    }

    private companion object {
        private const val ZOOM = 1.00
        private val CENTER = Point.fromLngLat(30.0, 30.0)
        private const val SECONDS_PER_REVOLUTION = 120
        private const val MAX_SPIN_ZOOM = 5
        private const val SLOW_SPIN_ZOOM = 3
    }
}
