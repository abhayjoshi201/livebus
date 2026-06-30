package com.example.livebus.data

data class LocationData(
    val busId: String? = "101-A",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val eta: String? = "5 mins",
    val distance: String? = "1.2 km"
)
