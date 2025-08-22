package com.example.globemap

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.style
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.view.animation.LinearInterpolator
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.common.Cancelable
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.OnScaleListener
import com.mapbox.maps.plugin.gestures.gestures
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.globemap.adapters.FeaturesAdapter
import com.example.globemap.models.FeatureItem
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var userInteracting = false
    private var spinEnabled = true
    private lateinit var mapboxMap: MapboxMap
    private lateinit var runningAnimation: Cancelable


    private fun spinGlobe() {
        val zoom = mapboxMap.cameraState.zoom
        if (spinEnabled && !userInteracting && zoom < MAX_SPIN_ZOOM) {
            var distancePerSecond = 360.0 / SECONDS_PER_REVOLUTION
            if (zoom > SLOW_SPIN_ZOOM) {
                // Slow spinning at higher zooms
                val zoomDif = (MAX_SPIN_ZOOM - zoom) / (MAX_SPIN_ZOOM - SLOW_SPIN_ZOOM)
                distancePerSecond *= zoomDif
            }
            val center = mapboxMap.cameraState.center
            val targetCenter = Point.fromLngLat(center.longitude() - distancePerSecond, center.latitude())
            // Smoothly animate the map over one second.
            // When this animation is complete, call it again
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapView = MapView(this)
        setContentView(R.layout.activity_main)

        mapView.scalebar.enabled = false

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
                        0 -> {
                            val intent = Intent(this@MainActivity, StandardView::class.java)
                            startActivity(intent)
                        }
                    }
                }
            })
        }



        mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                userInteracting = true
                runningAnimation.cancel()
            }

            override fun onMove(detector: MoveGestureDetector): Boolean {
                // return false in order to actually handle user movement
                return false
            }

            override fun onMoveEnd(detector: MoveGestureDetector) {
                userInteracting = false
                spinGlobe()
            }
        })

        mapView.gestures.addOnRotateListener(object : OnRotateListener {
            override fun onRotateBegin(detector: RotateGestureDetector) {
                userInteracting = true
                runningAnimation.cancel()
            }

            override fun onRotate(detector: RotateGestureDetector) {
                // no-op
            }

            override fun onRotateEnd(detector: RotateGestureDetector) {
                userInteracting = false
                spinGlobe()
            }
        })

        mapView.gestures.addOnScaleListener(object : OnScaleListener {
            override fun onScaleBegin(detector: StandardScaleGestureDetector) {
                userInteracting = true
                runningAnimation.cancel()
            }

            override fun onScale(detector: StandardScaleGestureDetector) {
                // no-op
            }

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
            )
        }
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }, 2000)
    }


    private companion object {
        private const val ZOOM = 1.00
        private val CENTER = Point.fromLngLat(30.0, 30.0)
        private const val EASE_TO_DURATION = 1_000L
        // At low zooms, complete a revolution every two minutes.
        private const val SECONDS_PER_REVOLUTION = 120
        // Above zoom level 5, do not rotate.
        private const val MAX_SPIN_ZOOM = 5
        // Rotate at intermediate speeds between zoom levels 3 and 5.
        private const val SLOW_SPIN_ZOOM = 3
    }

}