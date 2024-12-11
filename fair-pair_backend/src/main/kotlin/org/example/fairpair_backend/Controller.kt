package org.example.fairpair_backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    private val distanceUtil: DistanceUtil
) {
    private var requestCounter = 1  // 요청마다 증가하는 카운터

    @GetMapping("/getOptimizedDestination")
    fun getOptimizedDestination(
        @RequestParam("p1x", required = true) p1x: Double,
        @RequestParam("p1y", required = true) p1y: Double,
        @RequestParam("p2x", required = true) p2x: Double,
        @RequestParam("p2y", required = true) p2y: Double,
        @RequestParam("p3x", required = true) p3x: Double,
        @RequestParam("p3y", required = true) p3y: Double,
    ): Map<String, Any> {
        println("<$requestCounter 번째 request start>")
        var durationHistory: String = ""

        var optimized: Response = Response(redDuration = 0, blueDuration = 10000000, greenDuration = 500000)

        val coordinates = arrayOf(
            Coordinate(p1x, p1y),
            Coordinate(p2x, p2y),
            Coordinate(p3x, p3y)
        )
        var destination: Coordinate = distanceUtil.getAverageCoordinate(*coordinates) // 초기값은 좌표상 중앙

        var iterationLimit = 20

        var durations: List<Int>? = null

        while (iterationLimit-- > 0) {
            val results = coordinates.map { distanceUtil.fetchDistance(it, destination) }
            durations = results.map { distanceUtil.getDuration(it) }

            // 평균 duration을 목표로 삼고, 각 duration의 차이를 최소화
            val avgDuration: Double = durations.average()
            val maxDuration: Int = durations.maxOrNull() ?: 0

            /**
             * 공평성을 평가하는 척도로, (최대 소요 시간 - 평균 소요 시간) 기록을 담음
             */
            durationHistory += "${maxDuration - avgDuration} "

            println("<==== Current Duration : ${20 - iterationLimit} ====>")
            println("Coordinates : ${coordinates.joinToString { it.toString() }}")
            println("Current Destination : ${destination.toString()}")
            println("duration : ${durations[0]}, ${durations[1]}, ${durations[2]}")
            println("average : $avgDuration")

            val differences = durations.map { Math.abs((it - avgDuration)) }

            val maxDifferenceIndex = differences.indexOf(differences.maxOrNull())
            val targetCoordinate = coordinates[maxDifferenceIndex]

            // 비례 값을 조정하여 목적지 수정
            val adjustmentFactor = (durations[maxDifferenceIndex] - avgDuration) / durations[maxDifferenceIndex] * 0.2

            destination = editDestination(
                destination.x + (targetCoordinate.x - destination.x) * adjustmentFactor,
                destination.y + (targetCoordinate.y - destination.y) * adjustmentFactor
            )

            optimized = getMoreOptimized(
                optimized,
                Response(
                    lat = destination.x,
                    lon = destination.y,
                    blueDuration = durations[0],
                    redDuration = durations[1],
                    greenDuration = durations[2],
                    durationHistory = durationHistory
                )
            )
        }

        println("** Optimized destination: $destination **")
        requestCounter++

        return optimized.toMap()
    }

    private fun editDestination(newX: Double, newY: Double): Coordinate {
        return Coordinate(newX, newY)
    }

    private fun getMoreOptimized(r1: Response, r2: Response) : Response{
        return if(getDifference(r1) > getDifference(r2))
                r2
            else
                r1
    }

    private fun getDifference(r: Response) : Double {
        return maxOf(r.blueDuration, r.redDuration, r.greenDuration) - (r.blueDuration+r.redDuration+r.greenDuration) / 3.0
    }
}


