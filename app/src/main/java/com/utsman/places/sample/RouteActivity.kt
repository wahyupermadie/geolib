/*
 * Created on 31/1/21 5:51 PM
 * Copyright (c) Muhammad Utsman 2021 All rights reserved.
 */

package com.utsman.places.sample

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.utsman.places.routes.*
import com.utsman.places.routes.data.StackAnimationMode
import com.utsman.places.routes.data.TransportMode
import com.utsman.places.routes.polyline.PlacesPointPolyline
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class RouteActivity : AppCompatActivity() {

    private val placesRoute by lazy {
        createPlacesRoute(getString(R.string.here_maps_api))
    }

    private val btnPoly1 by lazy { findViewById<Button>(R.id.btn_polyline_1) }
    private val btnPoly2 by lazy { findViewById<Button>(R.id.btn_polyline_2) }
    private val btnPoly3 by lazy { findViewById<Button>(R.id.btn_polyline_3) }

    private var poly1HasRender = false
    private var poly2HasRender = false
    private var poly3HasRender = false

    private lateinit var point1: PlacesPointPolyline
    private lateinit var point2: PlacesPointPolyline
    private lateinit var point3: PlacesPointPolyline

    private var markerPoly3: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val mapsFragment =
            supportFragmentManager.findFragmentById(R.id.maps_view) as SupportMapFragment

        lifecycleScope.launch {
            val googleMap = mapsFragment.awaitMap().apply {
                uiSettings.isZoomControlsEnabled = true
                setPadding(0, 0, 0, 200)
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center.toLatLng(), 14f))

            val placesPolyline = googleMap.createPlacesPolylineBuilder()
                .createAnimatePolyline()

            btnPoly1.setOnClickListener {
                lifecycleScope.launch {

                    val first = placesRoute.searchRoute {
                        startLocation = firstPoint1
                        endLocation = firstPoint2
                        transportMode = TransportMode.BIKE
                    }

                    if (!poly1HasRender) {
                        point1 = placesPolyline.startAnimate(first.geometries) {
                            stackAnimationMode = StackAnimationMode.BlockStackAnimation
                        }
                        poly1HasRender = true
                    } else {
                        point1.remove()
                        poly1HasRender = false
                    }
                }
            }

            btnPoly2.setOnClickListener {
                lifecycleScope.launch {
                    val second = placesRoute.searchRoute {
                        startLocation = secondPoint1
                        endLocation = secondPoint2
                        transportMode = TransportMode.BIKE
                    }

                    if (!poly2HasRender) {
                        point2 = placesPolyline.startAnimate(second.geometries) {
                            stackAnimationMode = StackAnimationMode.WaitStackEndAnimation
                            withPrimaryPolyline {
                                width(8f)
                                color(Color.BLUE)
                            }
                            withAccentPolyline {
                                width(8f)
                                color(Color.CYAN)
                            }
                            doOnStartAnimation {
                                toast("start...")
                                googleMap.addMarker {
                                    this.position(it)
                                }
                            }
                            doOnEndAnimation {
                                googleMap.addMarker {
                                    this.position(it)
                                }
                                toast("end...")
                            }
                        }
                        poly2HasRender = true
                    } else {
                        point2.remove()
                        poly2HasRender = false
                    }
                }
            }

            btnPoly3.setOnClickListener {
                lifecycleScope.launch {
                    val third = placesRoute.searchRoute {
                        startLocation = thirdPoint1
                        endLocation = thirdPoint2
                        transportMode = TransportMode.BIKE
                    }

                    if (!poly3HasRender) {
                        markerPoly3 = googleMap.addMarker {
                            position(thirdPoint1.toLatLng())
                        }

                        point3 = placesPolyline.startAnimate(third.geometries) {
                            duration = 10000
                            stackAnimationMode = StackAnimationMode.OffStackAnimation
                            withAccentPolyline {
                                width(8f)
                                color(Color.CYAN)
                            }
                            withPrimaryPolyline {
                                width(8f)
                                color(Color.BLUE)
                            }
                            doOnUpdateAnimation { latLng, _ ->
                                markerPoly3?.position = latLng
                            }
                        }
                        poly3HasRender = true
                    } else {
                        point3.remove()
                        markerPoly3?.remove()
                        markerPoly3 = null
                        poly3HasRender = false
                    }
                }
            }
        }
    }
}

fun logd(message: String) = Log.d("SAMPLE", message)