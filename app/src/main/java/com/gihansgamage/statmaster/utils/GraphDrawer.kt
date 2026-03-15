package com.gihansgamage.statmaster.utils

import com.gihansgamage.statmaster.models.DistributionType
import com.gihansgamage.statmaster.models.GraphData
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

object GraphDrawer {

    /**
     * Generate graph data for the specified distribution
     */
    fun generateGraphData(
        distributionType: DistributionType,
        parameters: Map<String, Double>,
        criticalValue: Double? = null
    ): GraphData {
        val xValues = mutableListOf<Double>()
        val yValues = mutableListOf<Double>()

        when (distributionType) {
            DistributionType.NORMAL -> {
                val mean = parameters["mean"] ?: 0.0
                val stdDev = parameters["stdDev"] ?: 1.0
                val z = parameters["z"]

                val range = 4.0 * stdDev
                val start = mean - range
                val end = mean + range
                val step = (end - start) / 200.0

                var x = start
                while (x <= end) {
                    xValues.add(x)
                    yValues.add(NormalDistribution.pdf(x, mean, stdDev))
                    x += step
                }

                // Add shaded area if critical value provided
                val areaX = mutableListOf<Double>()
                val areaY = mutableListOf<Double>()

                z?.let { zVal ->
                    val areaStart = if (zVal < 0) mean - range else mean
                    val areaEnd = zVal

                    var ax = areaStart
                    while (ax <= areaEnd) {
                        areaX.add(ax)
                        areaY.add(NormalDistribution.pdf(ax, mean, stdDev))
                        ax += step
                    }
                }

                return GraphData(
                    xValues = xValues,
                    yValues = yValues,
                    criticalX = z,
                    areaX = if (areaX.isNotEmpty()) areaX else null,
                    areaY = if (areaY.isNotEmpty()) areaY else null
                )
            }

            DistributionType.T -> {
                val df = parameters["df"] ?: 10.0
                val t = parameters["t"]

                val range = when {
                    df > 30.0 -> 4.0
                    df > 10.0 -> 3.5
                    else -> 3.0
                }

                val start = -range
                val end = range
                val step = (end - start) / 200.0

                var x = start
                while (x <= end) {
                    xValues.add(x)
                    yValues.add(TDistribution.pdf(x, df))
                    x += step
                }

                return GraphData(
                    xValues = xValues,
                    yValues = yValues,
                    criticalX = t
                )
            }

            DistributionType.CHISQUARE -> {
                val df = parameters["df"] ?: 5.0
                val chi = parameters["chi"]

                val mean = df
                val stdDev = sqrt(2.0 * df)
                val range = max(4.0 * stdDev, 10.0)

                val start = 0.0
                val end = mean + range
                val step = (end - start) / 200.0

                var x = start
                while (x <= end) {
                    xValues.add(x)
                    yValues.add(ChiSquareDistribution.pdf(x, df))
                    x += step
                }

                // Add shaded area
                val areaX = mutableListOf<Double>()
                val areaY = mutableListOf<Double>()

                chi?.let { chiVal ->
                    if (chiVal > 0 && chiVal < end) {
                        var ax = chiVal
                        while (ax <= end) {
                            areaX.add(ax)
                            areaY.add(ChiSquareDistribution.pdf(ax, df))
                            ax += step
                        }
                    }
                }

                return GraphData(
                    xValues = xValues,
                    yValues = yValues,
                    criticalX = chi,
                    areaX = if (areaX.isNotEmpty()) areaX else null,
                    areaY = if (areaY.isNotEmpty()) areaY else null
                )
            }

            DistributionType.F -> {
                val df1 = parameters["df1"] ?: 5.0
                val df2 = parameters["df2"] ?: 10.0
                val f = parameters["f"]

                val mean = if (df2 > 2) df2 / (df2 - 2.0) else 5.0
                val end = max(mean * 3.0, 10.0)

                val start = 0.0
                val step = end / 200.0

                var x = start
                while (x <= end) {
                    xValues.add(x)
                    yValues.add(FDistribution.pdf(x, df1, df2))
                    x += step
                }

                return GraphData(
                    xValues = xValues,
                    yValues = yValues,
                    criticalX = f
                )
            }
        }
    }
}