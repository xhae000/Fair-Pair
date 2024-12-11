package org.example.fairpair_backend

data class Response(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val blueDuration: Int = 0,
    val redDuration: Int = 0,
    val greenDuration: Int = 0,
    val durationHistory: String = "",
) {

    fun toMap() : Map<String, Any> =  mapOf(
            "lat" to lat,
            "lon" to lon,
            "blueDuration" to blueDuration,
            "redDuration" to redDuration,
            "greenDuration" to greenDuration,
            "durationHistory" to durationHistory
    )
}