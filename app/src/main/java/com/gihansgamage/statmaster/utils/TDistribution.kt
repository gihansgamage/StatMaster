package com.gihansgamage.statmaster.utils

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.sin

object TDistribution {

    /**
     * Calculate the probability density function (PDF) for t-distribution
     * Formula: f(t) = Γ((df+1)/2) / (√(df*π) * Γ(df/2)) * (1 + t²/df)^(-(df+1)/2)
     */
    fun pdf(t: Double, df: Double): Double {
        require(df > 0) { "Degrees of freedom must be positive" }

        val n = (df + 1.0) / 2.0
        val d = df / 2.0

        val gammaN = gamma(n)
        val gammaD = gamma(d)

        val coefficient = gammaN / (sqrt(df * PI) * gammaD)
        val factor = (1.0 + t * t / df).pow(-n)

        return coefficient * factor
    }

    /**
     * Calculate the cumulative distribution function (CDF) for t-distribution
     * Uses numerical integration
     */
    fun cdf(t: Double, df: Double): Double {
        require(df > 0) { "Degrees of freedom must be positive" }

        if (t == 0.0) return 0.5
        if (t < 0) return 1.0 - cdf(-t, df)

        // Numerical integration using Simpson's rule
        return integrateSimpson(0.0, t, df) + 0.5
    }

    /**
     * Find the t-value for a given cumulative probability (inverse CDF)
     * Uses Newton-Raphson method
     */
    fun inverseCDF(p: Double, df: Double): Double {
        require(p in 0.0..1.0) { "Probability must be between 0 and 1" }
        require(df > 0) { "Degrees of freedom must be positive" }

        if (p == 0.5) return 0.0
        if (p < 0.5) return -inverseCDF(1.0 - p, df)

        // Initial guess using normal approximation
        var t = NormalDistribution.inverseCDF(p)

        // Newton-Raphson iteration
        for (i in 0 until 50) {
            val cdfValue = cdf(t, df)
            val pdfValue = pdf(t, df)

            if (pdfValue < 1e-10) break

            val delta = (cdfValue - p) / pdfValue
            t -= delta

            if (kotlin.math.abs(delta) < 1e-8) break
        }

        return t
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
        // Coefficients for Lanczos approximation with g=7
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
            return PI / (sin(PI * x) * gamma(1.0 - x))
        }

        var xVal = x - 1.0
        var a = p[0]
        for (i in 1 until p.size) {
            a += p[i] / (xVal + i.toDouble())
        }

        val t = xVal + g + 0.5
        return sqrt(2.0 * PI) * t.pow(xVal + 0.5) * exp(-t) * a
    }

    /**
     * Two-tailed critical value
     */
    fun criticalValue(alpha: Double, df: Double): Double {
        require(alpha in 0.0..1.0) { "Alpha must be between 0 and 1" }
        return inverseCDF(1.0 - alpha / 2.0, df)
    }
}