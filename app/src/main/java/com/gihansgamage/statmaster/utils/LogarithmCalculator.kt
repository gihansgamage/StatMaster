package com.gihansgamage.statmaster.utils

import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2

object LogarithmCalculator {

    /** log base 2 */
    fun log2(x: Double): Double {
        require(x > 0) { "x must be > 0" }
        return log2(x)
    }

    /** log base 10 (common log) */
    fun log10(x: Double): Double {
        require(x > 0) { "x must be > 0" }
        return log10(x)
    }

    /** natural log (base e) */
    fun ln(x: Double): Double {
        require(x > 0) { "x must be > 0" }
        return ln(x)
    }

    /** log of any custom base */
    fun logBase(x: Double, base: Double): Double {
        require(x > 0)    { "x must be > 0" }
        require(base > 0) { "base must be > 0" }
        require(base != 1.0) { "base cannot be 1" }
        return ln(x) / ln(base)
    }

    /** antilog base 10: 10^x */
    fun antilog10(x: Double) = Math.pow(10.0, x)

    /** antilog base 2: 2^x */
    fun antilog2(x: Double) = Math.pow(2.0, x)

    /** antilog natural: e^x */
    fun antilogE(x: Double) = Math.E.pow(x)

    /** antilog custom base: base^x */
    fun antilogBase(x: Double, base: Double): Double {
        require(base > 0)    { "base must be > 0" }
        require(base != 1.0) { "base cannot be 1" }
        return Math.pow(base, x)
    }

    private fun Double.pow(exp: Double) = Math.pow(this, exp)

    // ── Table generators ────────────────────────────────────────────────────

    /**
     * Common log table (base 10): rows x = 1.0–9.9 in 0.1 steps,
     * cols = second decimal 0.00–0.09  → log10(x + col_offset)
     */
    fun generateLog10Table(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val header = mutableListOf("x")
        for (j in 0..9) header.add("+0.0$j")
        table.add(header)

        for (iTen in 10..99) {
            val row = mutableListOf<String>()
            val xBase = iTen / 10.0
            row.add(String.format("%.1f", xBase))
            for (j in 0..9) {
                val x = xBase + j / 100.0
                row.add(String.format("%.4f", log10(x)))
            }
            table.add(row)
        }
        return table
    }

    /**
     * Natural log table (base e): x from 1.0 to 9.9
     */
    fun generateLnTable(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        val header = mutableListOf("x")
        for (j in 0..9) header.add("+0.0$j")
        table.add(header)

        for (iTen in 10..99) {
            val row = mutableListOf<String>()
            val xBase = iTen / 10.0
            row.add(String.format("%.1f", xBase))
            for (j in 0..9) {
                val x = xBase + j / 100.0
                row.add(String.format("%.4f", ln(x)))
            }
            table.add(row)
        }
        return table
    }

    /**
     * Log base 2 table: x from 1 to 64 (powers of 2 highlighted naturally)
     */
    fun generateLog2Table(): List<List<String>> {
        val table = mutableListOf<List<String>>()
        table.add(listOf("x", "log₂(x)", "x", "log₂(x)", "x", "log₂(x)", "x", "log₂(x)"))

        val xValues = (1..64).toList()
        for (row in xValues.chunked(4)) {
            val tableRow = mutableListOf<String>()
            for (x in row) {
                tableRow.add(x.toString())
                tableRow.add(String.format("%.4f", log2(x.toDouble())))
            }
            // pad if last row is short
            while (tableRow.size < 8) tableRow.add("")
            table.add(tableRow)
        }
        return table
    }
}