package com.gihansgamage.statmaster.utils

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

object ChiSquareDistribution {

    /**
     * Calculate the probability density function (PDF) for chi-square distribution
     * Formula: f(x) = (1 / (2^(df/2) * Γ(df/2))) * x^(df/2-1) * e^(-x/2)
     */
    fun pdf(x: Double, df: Double): Double {
        require(df > 0) { "Degrees of freedom must be positive" }
        require(x >= 0) { "Chi-square value must be non-negative" }

        if (x == 0.0 && df == 2.0) return 0.5
        if (x <= 0.0) return 0.0

        val k = df / 2.0
        val coefficient = 1.0 / (2.0.pow(k) * gamma(k))
        val factor = x.pow(k - 1.0) * exp(-x / 2.0)

        return coefficient * factor
    }

    /**
     * Calculate the cumulative distribution function (CDF) for chi-square distribution
     * Uses numerical integration
     */
    fun cdf(x: Double, df: Double): Double {
        require(df > 0) { "Degrees of freedom must be positive" }
        require(x >= 0) { "Chi-square value must be non-negative" }

        if (x == 0.0) return 0.0

        // Numerical integration using Simpson's rule
        return integrateSimpson(0.0, x, df)
    }

    /**
     * Find the chi-square critical value for a given cumulative probability
     * Uses Newton-Raphson method
     */
    fun inverseCDF(p: Double, df: Double): Double {
        require(p in 0.0..1.0) { "Probability must be between 0 and 1" }
        require(df > 0) { "Degrees of freedom must be positive" }

        if (p == 0.0) return 0.0
        if (p == 1.0) return Double.POSITIVE_INFINITY

        // Initial guess using Wilson-Hilferty approximation
        val z = NormalDistribution.inverseCDF(p)
        var x = df * (1.0 - 2.0 / (9.0 * df) + z * sqrt(2.0 / (9.0 * df))).pow(3.0)
        x = kotlin.math.max(x, 0.001)

        // Newton-Raphson iteration
        for (i in 0 until 50) {
            val cdfValue = cdf(x, df)
            val pdfValue = pdf(x, df)

            if (pdfValue < 1e-10) break

            val delta = (cdfValue - p) / pdfValue
            x -= delta
            x = kotlin.math.max(x, 0.001)

            if (kotlin.math.abs(delta) < 1e-8) break
        }

        return x
    }

    /**
     * Numerical integration using Simpson's rule
     */
    private fun integrateSimpson(a: Double, b: Double, df: Double, n: Int = 1000): Double {
        val h = (b - a) / n
        var sum = pdf(a, df) + pdf(b, df)

        for (i in 1 until n step 2) {
            sum += 4.0 * pdf(a + i * h, df)
        }
        for (i in 2 until n step 2) {
            sum += 2.0 * pdf(a + i * h, df)
        }

        return sum * h / 3.0
    }

    /**
     * Approximation of gamma function using Lanczos approximation
     */
    private fun gamma(x: Double): Double {
        val p = doubleArrayOf(
            0.99999999999980993,
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.13857109526572012,
            9.9843695780195716e-6,
            1.5056327351493116e-7
        )

        val g = 7.0

        if (x < 0.5) {
            return PI / (kotlin.math.sin(PI * x) * gamma(1.0 - x))
        }

        var xVal = x - 1.0
        var a = p[0]
        for (i in 1 until p.size) {
            a += p[i] / (xVal + i.toDouble())
        }

        val t = xVal + g + 0.5
        return sqrt(2.0 * PI) * t.pow(xVal + 0.5) * exp(-t) * a
    }
}