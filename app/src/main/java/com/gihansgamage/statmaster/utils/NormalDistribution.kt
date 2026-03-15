package com.gihansgamage.statmaster.utils

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sqrt

object NormalDistribution {

    /**
     * Calculate the probability density function (PDF) for normal distribution
     * Formula: f(x) = (1 / (σ√(2π))) * e^(-((x-μ)² / (2σ²)))
     */
    fun pdf(x: Double, mean: Double = 0.0, stdDev: Double = 1.0): Double {
        val exponent = -((x - mean).pow(2.0)) / (2 * stdDev.pow(2.0))
        val coefficient = 1.0 / (stdDev * sqrt(2 * PI))
        return coefficient * exp(exponent)
    }

    /**
     * Calculate the cumulative distribution function (CDF) for standard normal distribution
     * Uses numerical approximation (Abramowitz and Stegun 7.1.26)
     */
    fun cdf(z: Double): Double {
        val sign = if (z < 0) -1 else 1
        val zAbs = kotlin.math.abs(z)

        // Constants for the approximation
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val p = 0.3275911

        val t = 1.0 / (1.0 + p * zAbs)
        val y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-zAbs * zAbs / 2.0)

        return 0.5 * (1.0 + sign * (2 * y - 1))
    }

    /**
     * Find the Z-score for a given cumulative probability (inverse CDF)
     * Uses Beasley-Springer-Moro approximation
     */
    fun inverseCDF(p: Double): Double {
        require(p in 0.0..1.0) { "Probability must be between 0 and 1" }

        // Coefficients for rational approximation
        val a = doubleArrayOf(
            -3.969683028665376e+01,
            2.209460984245205e+02,
            -2.759285104469687e+02,
            1.383577518672690e+02,
            -3.066479806614716e+01,
            2.506628277459239e+00
        )
        val b = doubleArrayOf(
            -5.447609879822406e+01,
            1.615858368580409e+02,
            -1.556989798598866e+02,
            6.680131188771972e+01,
            -1.328068155288572e+01
        )
        val c = doubleArrayOf(
            -7.784894002430293e-03,
            -3.223964580411365e-01,
            -2.400758277161838e+00,
            -2.549732539343734e+00,
            4.374664141464968e+00,
            2.938163982698783e+00
        )
        val d = doubleArrayOf(
            7.784695709041462e-03,
            3.224671290700398e-01,
            2.445134137142996e+00,
            3.754408661907416e+00
        )

        val q = kotlin.math.min(p, 1.0 - p)
        var t: Double
        var u: Double

        if (q > 0.02425) {
            // Rational approximation for central region
            u = q - 0.5
            t = u * u
            u = u * (((((a[0] * t + a[1]) * t + a[2]) * t + a[3]) * t + a[4]) * t + a[5]) /
                    (((((b[0] * t + b[1]) * t + b[2]) * t + b[3]) * t + b[4]) * t + 1.0)
        } else {
            // Rational approximation for tail region
            t = sqrt(-2.0 * kotlin.math.ln(q))
            u = (((((c[0] * t + c[1]) * t + c[2]) * t + c[3]) * t + c[4]) * t + c[5]) /
                    ((((d[0] * t + d[1]) * t + d[2]) * t + d[3]) * t + 1.0)
        }

        // Reflect for negative p
        return if (p < 0.5) -u else u
    }

    /**
     * Calculate probability between two z-scores
     */
    fun probabilityBetween(z1: Double, z2: Double): Double {
        return kotlin.math.abs(cdf(z2) - cdf(z1))
    }

    /**
     * Calculate two-tailed probability (probability of being beyond |z|)
     */
    fun twoTailedProbability(z: Double): Double {
        return 2.0 * (1.0 - cdf(kotlin.math.abs(z)))
    }
}