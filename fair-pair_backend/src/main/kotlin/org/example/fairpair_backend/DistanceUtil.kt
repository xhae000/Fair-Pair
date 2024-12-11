package org.example.fairpair_backend

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.stereotype.Component

@Component("DistanceUtil")
class   DistanceUtil {

    private val apiKey = "저의 비밀스러운 API 키 입니다."
    private val baseUrl = "https://maps.googleapis.com/maps/api/distancematrix/json"

    fun fetchDistance(originCoordinate: Coordinate, destinationCoordinate: Coordinate): JSONObject? {
        val origin = "${originCoordinate.x},${originCoordinate.y}"
        val destination = "${destinationCoordinate.x},${destinationCoordinate.y}"
        val url = "$baseUrl?origins=$origin&destinations=$destination&units=metric&mode=transit&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let {
                        JSONObject(it)
                    }
                } else {
                    println("Request failed: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error fetching distance: ${e.message}")
            null
        }
    }

    fun getAverageCoordinate(vararg coordinates: Coordinate): Coordinate {
        val avgX = coordinates.map { it.x }.average().toDouble()
        val avgY = coordinates.map { it.y }.average().toDouble()

        return Coordinate(avgX, avgY)
    }

    fun printPrice(response: JSONObject?){
        try {
            val rows = response?.getJSONArray("rows") ?: throw Exception("응답 이상함")
            val elements = rows.getJSONObject(0).getJSONArray("elements")
            val distance = elements.getJSONObject(0).getJSONObject("distance").getString("text")
            val duration = elements.getJSONObject(0).getJSONObject("duration").getString("text")

            println("거리: $distance")
            println("소요시간 (대중교통+도보): $duration")
        } catch (e: Exception) {
            e.printStackTrace()
            println("정의되지 않은 좌표임")
        }
    }

    fun getDuration(response: JSONObject?): Int{
        try {
            val rows = response?.getJSONArray("rows") ?: throw Exception("응답 이상함")
            val elements = rows.getJSONObject(0).getJSONArray("elements")

            val split =  elements.getJSONObject(0).getJSONObject("duration").getString("text")
                .split(" ")

            if(split[1] == "분" || split[1] =="mins")
                return Integer.parseInt(split[0])
            else {
                var result: Int = Integer.parseInt(split[0]) * 60

                if(split.size > 2)
                    result += Integer.parseInt(split[2])

                return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("정의되지 않은 좌표임")
        }

        return Int.MAX_VALUE
    }
}
