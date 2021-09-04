package org.openstreetmap.josm.plugins.hexgrid

import org.openstreetmap.josm.data.coor.LatLon
import org.openstreetmap.josm.data.osm.BBox
import org.openstreetmap.josm.data.osm.Node
import org.openstreetmap.josm.data.osm.Way
import org.openstreetmap.josm.tools.Logging
import org.openstreetmap.josm.tools.Utils
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

object HexGridGenerator {
    fun buildHexGridWays(bBox: BBox, cellSide: Double): Sequence<Way> {
        val bufferedBox = bBox.buffer(cellSide * 1.5)
        val center = bufferedBox.center

        val cellWidth = bufferedBox.width * cellSide * 2 / LatLon(
            center.lat(),
            bufferedBox.topLeftLon
        ).greatCircleDistanceInKilometers(
            LatLon(
                center.lat(),
                bufferedBox.bottomRightLon
            )
        )
        val cellHeight = bufferedBox.height * cellSide * 2 / LatLon(
            bufferedBox.bottomRightLat,
            center.lon()
        ).greatCircleDistanceInKilometers(
            LatLon(
                bufferedBox.topLeftLat,
                center.lon()
            )
        )
        val radius = cellWidth / 2

        val hexWidth = 0.75 * cellWidth
        val hexHeight = sqrt(3.0) / 2 * cellHeight

        val xCount = floor((bufferedBox.width - cellWidth) / (cellWidth - radius / 2)).toInt()
        val xAdjust = (xCount * hexWidth - radius / 2 - bufferedBox.width) / 2 - radius / 2 + hexWidth / 2

        val yCount = floor((bufferedBox.height - hexHeight) / hexHeight).toInt()
        val hasOffsetY = yCount * hexHeight - bufferedBox.height > hexHeight / 2
        val yOffset = if (hasOffsetY) -hexHeight / 4 else 0.0
        val yAdjust = (bufferedBox.height - yCount * hexHeight) / 2 + yOffset

        return (0..xCount).asSequence().flatMap { x ->
            (0..yCount).asSequence().mapNotNull { y ->
                val isOdd = x % 2 == 1
                if (y == 0 && (isOdd || hasOffsetY))
                    null
                else {
                    val oddOffset = if (isOdd) -hexHeight / 2 else 0.0

                    val centerX = x * hexWidth + bufferedBox.topLeftLon - xAdjust
                    val centerY = y * hexHeight + bufferedBox.bottomRightLat + yAdjust + oddOffset
                    createHexagon(LatLon(centerY, centerX), cellWidth / 2, cellHeight / 2).let {
                        it + it.first()
                    }
                }
            }
        }.map { list ->
            Way().apply {
                nodes = list
            }
        }
    }

    private fun createHexagon(center: LatLon, radiusX: Double, radiusY: Double) =
        (0 until 6).map {
            Node(LatLon(center.y + MULTIPLIERS_Y[it] * radiusY, center.x + MULTIPLIERS_X[it] * radiusX))
        }

    private fun LatLon.greatCircleDistanceInKilometers(other: LatLon): Double {
        val sinHalfLat = sin((other.lat() - lat()).toRadians() / 2)
        val sinHalfLon = sin((other.lon() - lon()).toRadians() / 2)
        val d = 2 * EARTH_SPHERE_RADIUS * asin(
            sqrt(
                sinHalfLat * sinHalfLat + cos(lat().toRadians()) * cos(
                    other.lat().toRadians()
                ) * sinHalfLon * sinHalfLon
            )
        )
        // For points opposite to each other on the sphere,
        // rounding errors could make the argument of asin greater than 1
        // (This should almost never happen.)
        // For points opposite to each other on the sphere,
        // rounding errors could make the argument of asin greater than 1
        // (This should almost never happen.)
        return if (d.isNaN()) {
            Logging.error("NaN in greatCircleDistance: {0} {1}", this, other)
            Math.PI * EARTH_SPHERE_RADIUS
        } else d
    }

    private fun BBox.buffer(kilometers: Double): BBox =
        Utils.toDegrees(kilometers / EARTH_SPHERE_RADIUS).let {
            BBox(minLon - it, minLat - it, maxLon + it, maxLat + it)
        }

    private fun Double.toRadians() = Utils.toRadians(this % 360)

    private const val EARTH_SPHERE_RADIUS = 6371.0088
    private val MULTIPLIERS_X = (0 until 6).map { cos((60.0 * it).toRadians()) }
    private val MULTIPLIERS_Y = (0 until 6).map { sin((60.0 * it).toRadians()) }
}
