package com.gihansgamage.statmaster.utils

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

object FDistribution {

    /**
     * Calculate the probability density function (PDF) for F-distribution
     * Formula: f(x) = √((df1*x)^(df1) * df2^df2 / (df1*x + df2)^(df1+df2)) /
     *                  (x * B(df1/2, df2/2))
     */
    fun pdf(x: Double, df1: Double, df2: Double): Double {
        require(df1 > 0 && df2 > 0) { "Degrees of freedom must be positive" }
        require(x >= 0) { "F-value must be non-negative" }

        if (x == 0.0) {
            if (df1 == 2.0) {
                return 1.0 / beta(df1 / 2.0, df2 / 2.0)
            }
            return 0.0
        }

        val d1 = df1 / 2.0
        val d2 = df2 / 2.0
        val u = d1 * x
        val v = d2

        val coefficient = exp(d1 * ln(u) + d2 * ln(v) - (d1 + d2) * ln(u + v)) / (x * beta(d1, d2))

        return coefficient
    }

    /**
     * Calculate the cumulative distribution function (CDF) for F-distribution
     * Uses numerical integration
     */
    fun cdf(x: Double, df1: Double, df2: Double): Double {
        require(df1 > 0 && df2 > 0) { "Degrees of freedom must be positive" }
        require(x >= 0) { "F-value must be non-negative" }

        if (x == 0.0) return 0.0

        // Numerical integration using Simpson's rule
        return integrateSimpson(0.0, x, df1, df2)
    }

    /**
     * Find the F critical value for a given cumulative probability
     * Uses Newton-Raphson method
     */
    fun inverseCDF(p: Double, df1: Double, df2: Double): Double {
        require(p in 0.0..1.0) { "Probability must be between 0 and 1" }
        require(df1 > 0 && df2 > 0) { "Degrees of freedom must be positive" }

        if (p == 0.0) return 0.0
        if (p == 1.0) return Double.POSITIVE_INFINITY

        // Initial guess using normal approximation
        val z = NormalDistribution.inverseCDF(p)
        var x = (1.0 + 2.0 / df2).pow(0.5) * (1.0 - 2.0 / (9.0 * df2) +
                z * sqrt(2.0 / (9.0 * df2))).pow(-3.0) /
                (1.0 - 2.0 / (9.0 * df1) - z * sqrt(2.0 / (9.0 * df1))).pow(-3.0)
        x = kotlin.math.max(x, 0.001)

        // Newton-Raphson iteration
        for (i in 0..50) {
            val cdfValue = cdf(x, df1, df2)
            val pdfValue = pdf(x, df1, df2)

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
    private fun integrateSimpson(a: Double, b: Double, df1: Double, df2: Double, n: Int = 1000): Double {
        val h = (b - a) / n
        var sum = pdf(a, df1, df2) + pdf(b, df1, df2)

        for (i in 1 until n step 2) {
            sum += 4 * pdf(a + i * h, df1, df2)
        }
        for (i in 2 until n step 2) {
            sum += 2 * pdf(a + i * h, df1, df2)
        }

        return sum * h / 3.0
    }

    /**
     * Beta function
     */
    private fun beta(a: Double, b: Double): Double {
        return gamma(a) * gamma(b) / gamma(a + b)
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
            a += p[i] / (xVal + i)
        }

        val t = xVal + g + 0.5
        return sqrt(2 * PI) * t.pow(xVal + 0.5) * exp(-t) * a
    }
}